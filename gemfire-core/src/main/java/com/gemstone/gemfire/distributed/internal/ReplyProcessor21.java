/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

package com.gemstone.gemfire.distributed.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import com.gemstone.gemfire.CancelCriterion;
import com.gemstone.gemfire.CancelException;
import com.gemstone.gemfire.InternalGemFireException;
import com.gemstone.gemfire.cache.EntryExistsException;
import com.gemstone.gemfire.cache.EntryNotFoundException;
import com.gemstone.gemfire.cache.TimeoutException;
import com.gemstone.gemfire.cache.TransactionException;
import com.gemstone.gemfire.distributed.DistributedSystemDisconnectedException;
import com.gemstone.gemfire.distributed.internal.deadlock.MessageDependencyMonitor;
import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;
import com.gemstone.gemfire.i18n.LogWriterI18n;
import com.gemstone.gemfire.internal.Assert;
import com.gemstone.gemfire.internal.DSFIDNotFoundException;
import com.gemstone.gemfire.internal.cache.versions.ConcurrentCacheModificationException;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;
import com.gemstone.gemfire.internal.shared.UnsupportedGFXDVersionException;
import com.gemstone.gemfire.internal.shared.Version;
import com.gemstone.gemfire.internal.util.Breadcrumbs;
import com.gemstone.gemfire.internal.util.concurrent.StoppableCountDownLatch;
import com.gemstone.org.jgroups.util.StringId;

/**
 * This class processes responses to {@link DistributionMessage}s. It
 * handles a the generic case of simply waiting for responses from all
 * members.  It is intended to be subclassed for special cases.
 *
 * <P>
 *
 * Note that, unlike the reply processors in versions previous to
 * GemFire 2.1, this reply processor is kept entirely in JOM.
 *
 * <p>
 *
 * Recommended usage pattern in subclass...<pre>
 *
 * public void process(DistributionMessage msg) {
 *   try {
 *     ...custom code for subclass goes here...
 *   }
 *   finally {
 *     super.process(msg);
 *   }
 * }
 *
 * </pre>The above usage pattern causes the waitForReplies latch to not
 * be released until after the message has been processed. In addition, it is
 * guaranteed to be released even if the custom subclass code throws a
 * runtime exception.
 *
 * <p>
 *
 * TODO: alter process to use template method to enforce above usage pattern.
 *
 * @see MessageWithReply
 *
 * @author David Whitlock
 *
 * @since 2.1
 */
public class ReplyProcessor21
    implements MembershipListener {

  public final static boolean THROW_EXCEPTION_ON_TIMEOUT = Boolean.getBoolean("ack-threshold-exception");

  public static boolean WAIT_ON_EXCEPTION = Boolean
      .getBoolean("rp21.wait-on-exception");

  /** the ratio by which ack-severe-alert-threshold is lowered when
   *  waiting for a BucketRegion operation */
  public final static double PR_SEVERE_ALERT_RATIO;

  /** All live reply processors in this VM */
  protected final static ProcessorKeeper21 keeper = new ProcessorKeeper21();

  ////////////////////  Instance Methods  ////////////////////

  /** The members that haven't replied yet
   *
   *  @guarded.By {@link #members}
   */
  protected final InternalDistributedMember[] members;

  /**
   * Set to true in preWait, set to false in postWait. Used to avoid removing
   * membership listener in Runnable in postWait if we've called waitForReplies
   * again.
   */
  protected volatile boolean startedWaiting = false;

  /**
   * Set to true if this reply processor was added as membership listener else
   * false.
   */
  protected volatile boolean listenerAdded;

  /** An <code>Exception</code> that occurred when processing a
   * reply
   * <p>
   * Since this is set by the executor and read by the initiating thread,
   * this is a volatile.
   *
   * @see ReplyMessage#getException */
  protected volatile ReplyException exception;

  /** Have we heard back from everyone? */
  private volatile boolean done;

  protected volatile boolean keeperCleanedUp;

  /** Have we been aborted due to shutdown? */
  protected volatile boolean shutdown;

  /** Semaphore used for wait/notify */
  private final StoppableCountDownLatch latch;

  /** The id of this processor */
  protected int processorId;

  /** Used to get the ack wait threshold (which might change at
   * runtime), the logger, etc. */
  protected final InternalDistributedSystem system;

  /** the distribution manager - if null, get the manager from the system */
  protected final DM dmgr;

  /** Start time for replyWait stat, in nanos */
  protected long statStart;

  /** Start time for ack-wait-threshold, in millis */
  protected long initTime;

  /** whether this reply processor should perform severe-alert processing
   *  for the message being ack'd */
  protected boolean severeAlertEnabled;

  /** whether the severe-alert timeout has been reset.  This can happen
   *  if a member we're waiting for is waiting on a suspect member, for instance.
   */
  protected volatile boolean severeAlertTimerReset;

  /** whether this reply processor should shorten severe-alert processing
   *  due to another vm waiting on this one.  This is a thread-local so that
   *  lower level comm layers can tell that the interval should be shortened
   */
  public final static ThreadLocal SevereAlertShorten = new ThreadLocal() {
    @Override
    protected Object initialValue() {
      return Boolean.FALSE;
    }
  };

  /**
   * whether the next replyProcessor for the current thread should
   * perform severe-alert processing
   */
  private static ThreadLocal ForceSevereAlertProcessing = new ThreadLocal() {
    @Override
    protected Object initialValue() {
      return Boolean.FALSE;
    }
  };

  private static final AtomicInteger hashGen = new AtomicInteger(0);

  // local atomically allocated unique hashCode instead of going to
  // Object.hashCode() which has a perf impact
  private final int hash;

  //////////////////////  Static Methods  /////////////////////

  static {
    String str = System.getProperty("gemfire.ack-severe-alert-reduction-ratio", ".80");
    double ratio;
    try {
      ratio = Double.parseDouble(str);
    }
    catch (NumberFormatException e) {
      System.err.println("Unable to parse gemfire.ack-severe-alert-reduction-ratio setting of \"" + str + "\"");
      ratio = 0.80;
    }
    PR_SEVERE_ALERT_RATIO = ratio;
  }
  
  // @todo davidw If the processor isn't there, should it return a
  // "no-op" processor that just ignores the message being processed?
  /**
   * Returns the <code>ReplyProcessor</code> with the given
   * id, or <code>null</code> if it no longer exists.
   *
   * @param processorId
   *        The id of the processor to get
   *
   * @see #getProcessorId
   */
  public static ReplyProcessor21 getProcessor(int processorId) {
    return (ReplyProcessor21)keeper.retrieve(processorId);
  }

  ///////////////////////  Constructors  //////////////////////

  /**
   * Creates a new <code>ReplyProcessor</code> that wants replies from
   * a single member of a distributed system.
   *
   * @param system the DistributedSystem connection
   * @param member the member this processor wants a reply from
   */
  public ReplyProcessor21(InternalDistributedSystem system,
                          InternalDistributedMember member) {
    this(system.getDistributionManager(), system, member, null, true);
  }

  /**
   * Creates a new <code>ReplyProcessor</code> that wants replies from
   * a single member of a distributed system.
   *
   * @param system the DistributedSystem connection
   * @param member the member this processor wants a reply from
   * @param cancelCriterion optional CancelCriterion to use; will use the
   *  DistributionManager if null
   */
  public ReplyProcessor21(InternalDistributedSystem system,
                          InternalDistributedMember member,
                          CancelCriterion cancelCriterion) {
    this(system.getDistributionManager(), system, member,
        cancelCriterion, true);
  }

  /**
   * Creates a new <code>ReplyProcessor</code> that wants replies from
   * a single member of a distributed system.
   *
   * @param dm the DistributionManager to use for messaging and membership
   * @param member the member this processor wants a reply from
   */
  public ReplyProcessor21(DM dm,
                          InternalDistributedMember member) {
    this(dm, dm.getSystem(), member, null, true);
  }

  /**
   * Creates a new <code>ReplyProcessor</code> that wants replies from
   * some number of members of a distributed system. Call this method
   * with {@link DistributionManager#getDistributionManagerIds} if
   * you want replies from all DMs including the one hosted in this
   * VM.
   *
   * @param dm the DistributionManager to use for messaging and membership
   * @param initMembers the Set of members this processor wants replies from
   */
  public ReplyProcessor21(DM dm,
                          Collection initMembers) {
    this(dm, dm.getSystem(), initMembers, null);
  }

  /**
   * Creates a new <code>ReplyProcessor</code> that wants replies from
   * some number of members of a distributed system. Call this method
   * with {@link DistributionManager#getDistributionManagerIds} if
   * you want replies from all DMs including the one hosted in this
   * VM.
   *
   * @param system the DistributedSystem connection
   * @param initMembers the Set of members this processor wants replies from
   */
  public ReplyProcessor21(InternalDistributedSystem system,
                          Collection initMembers) {
    this(system.getDistributionManager(), system, initMembers, null);
  }

  /**
   * Creates a new <code>ReplyProcessor</code> that wants replies from
   * some number of members of a distributed system. Call this method
   * with {@link DistributionManager#getDistributionManagerIds} if
   * you want replies from all DMs including the one hosted in this
   * VM.
   *
   * @param system the DistributedSystem connection
   * @param initMembers the Set of members this processor wants replies from
   * @param cancelCriterion optional CancelCriterion to use; will use the
   * DistributedSystem's DistributionManager if null
   */
  public ReplyProcessor21(InternalDistributedSystem system,
                          Collection initMembers,
                          CancelCriterion cancelCriterion) {
    this(system.getDistributionManager(), system, initMembers, cancelCriterion);
  }

  /**
   * Construct new ReplyProcessor21.
   *
   * @param dm the DistributionManager to use for messaging and membership
   * @param system the DistributedSystem connection
   * @param initMembers the collection of members this processor wants replies from
   * @param cancelCriterion optional CancelCriterion to use; will use the dm
   * if null
   */
  private ReplyProcessor21(DM dm,
                           InternalDistributedSystem system,
                           Collection initMembers,
                           CancelCriterion cancelCriterion) {

   this(dm, system, initMembers, cancelCriterion, true);
  }

  /**
   * Construct new ReplyProcessor21.
   *
   * @param dm the DistributionManager to use for messaging and membership
   * @param system the DistributedSystem connection
   * @param initMembers the collection of members this processor wants replies from
   * @param cancelCriterion optional CancelCriterion to use; will use the dm
   * if null
   */
  protected ReplyProcessor21(DM dm,
                           InternalDistributedSystem system,
                           Collection initMembers,
                           CancelCriterion cancelCriterion, boolean register) {
    if (!allowReplyFromSender()) {
      Assert.assertTrue(initMembers != null, "null initMembers");
      Assert.assertTrue(system != null, "null system");
      if (dm != null) {
        Assert.assertTrue(!initMembers.contains(dm.getId()),
            "dm present in initMembers but reply from sender is not allowed");
      }
    }
    this.hash = hashGen.incrementAndGet();
    this.system = system;
    this.dmgr = dm;
    if (cancelCriterion == null) {
      cancelCriterion = dm.getCancelCriterion();
    }
    this.latch = new StoppableCountDownLatch(cancelCriterion, 1);
    int sz = initMembers.size();
    if (sz > 0) {
      this.members = new InternalDistributedMember[sz];
      int i=0;
      for (Iterator it = initMembers.iterator(); it.hasNext(); i++) {
        this.members[i] = (InternalDistributedMember)it.next();
      }
    }
    else {
      this.members = DistributionMessage.ZERO_RECIPIENTS;
    }
    init(register);
  }

  /**
   * Construct new ReplyProcessor21.
   *
   * @param dm the DistributionManager to use for messaging and membership
   * @param system the DistributedSystem connection
   * @param initMember a single member this processor wants reply from
   * @param cancelCriterion optional CancelCriterion to use; will use the dm
   * if null
   */
  protected ReplyProcessor21(DM dm,
                           InternalDistributedSystem system,
                           InternalDistributedMember initMember,
                           CancelCriterion cancelCriterion, boolean register) {
    if (!allowReplyFromSender()) {
      Assert.assertTrue(initMember != null, "null initMember");
      Assert.assertTrue(system != null, "null system");
      if (dm != null) {
        Assert.assertTrue(!initMember.equals(dm.getId()),
            "dm present in initMember but reply from sender is not allowed");
      }
    }
    this.hash = hashGen.incrementAndGet();
    this.system = system;
    this.dmgr = dm;
    if (cancelCriterion == null) {
      cancelCriterion = dm.getCancelCriterion();
    }
    this.latch = new StoppableCountDownLatch(cancelCriterion, 1);
    this.members = new InternalDistributedMember[1];
    this.members[0] = initMember;
    init(register);
  }

  private void init(boolean register) {
    this.done = false;
    this.shutdown = false;
    this.exception = null;
    if(register) {
      register();
    }
    this.keeperCleanedUp = false;
    this.initTime = System.currentTimeMillis();
  }

  public int register() {
    if (numMembers() != 0) {
      this.processorId = keeper.put(this);
    }
    return this.processorId;
  }

  /////////////////////  Instance Methods  /////////////////////

  /** get the distribution manager for this processor.  If the distributed system
   *  has a distribution manager, it is used.  Otherwise, we expect a distribution
   *  manager has been set with setDistributionManager and we'll use that
   */
  protected DM getDistributionManager() {
    try {
      DM result = this.system.getDistributionManager();
      if (result == null) {
        result = this.dmgr;
        Assert.assertTrue(result != null, "null DistributionManager");
      }
      return result;
    } catch (IllegalStateException ex) {
      // fix for bug 35000
      this.system.getCancelCriterion().checkCancelInProgress(null);
      throw new DistributedSystemDisconnectedException(ex.getMessage());
    }
  }


  /**
   * Override and return true if processor should wait for reply from sender.
   * <p>
   * NOTE: the communication layer does not reliably support sending a message to oneself,
   * so other means must be used to execute the message in this VM.  Typically
   * you would set the sender of the message and then invoke its process()
   * method in another thread.  If you have questions, talk to Bruce Schuchardt.
   */
  protected boolean allowReplyFromSender() {
    return false;
  }

  /**
   * The first time a reply is received that contains an exception,
   * the ReplyProcessor will save that exception to be passed
   * along to the waiting client thread.  By default, any exception
   * encountered after that will be logged.  Subclasses can reimplement
   * this method to disable logging of multiple exceptions.
   */
  protected boolean logMultipleExceptions() {
    return true;
  }


  /**
   * Makes note of a reply from a member.  If all members have
   * replied, the waiting thread is signaled.  This method can be
   * overridden to provide customized functionality, however the
   * overriden method should always invoke
   * <code>super.process()</code>.
   */
  public void process(DistributionMessage msg) {
    process(msg, true);
  }

  protected void process(DistributionMessage msg, boolean warn) {
    final LogWriterI18n log = this.system.getLogWriter().convertToLogWriterI18n();
    if (log.fineEnabled()) {
      log.fine(this.toString() + " got process(" + msg + ") from " +
               msg.getSender());
    }
    if (msg instanceof ReplyMessage) {
      ReplyMessage replyMsg = (ReplyMessage)msg;
      replyMsg.processRemoteTXChanges();
      ReplyException ex = replyMsg.getException();
      if (ex != null) {
        if (ex.getCause() instanceof DSFIDNotFoundException) {
          processException(msg, (DSFIDNotFoundException)ex.getCause(), ex);
        }
        else {
          processException(msg, ex);
        }
      }
    }

    final InternalDistributedMember sender = msg.getSender();
    if (!removeMember(sender, false) && warn) {
      // if the member hasn't left the system, something is wrong
      final DM dm = getDistributionManager(); // fix for bug 33253
      Set ids = getDistributionManagerIds();
      if (ids == null || ids.contains(sender)) {
        Vector viewMembers = dm.getViewMembers();
        if (system.getConfig().getMcastPort() == 0  // could be using multicast & will get responses from everyone
             && (viewMembers == null || viewMembers.contains(sender))) {
          log.warning(
            LocalizedStrings.ReplyProcessor21_RECEIVED_REPLY_FROM_MEMBER_0_BUT_WAS_NOT_EXPECTING_ONE_MORE_THAN_ONE_REPLY_MAY_HAVE_BEEN_RECEIVED_THE_REPLY_THAT_WAS_NOT_EXPECTED_IS_1,
            new Object[] {sender, msg});
        }
      }
    }
    checkIfDone();
  }


  protected synchronized void processException(DistributionMessage msg,
                                               ReplyException ex) {
    processException(ex);
  }

  protected synchronized void processException(ReplyException ex) {
    final LogWriterI18n log = this.system.getLogWriterI18n();
    if (this.exception == null) {  // only keep first exception
      this.exception = ex;
    }
    else {
      if (logMultipleExceptions()) {

        // don't log TransactionExceptions since they are likely to be same
        // from all nodes with new distributed TX model; also skip
        // EntryExistsException since those can come from multiple VMs for
        // replicated tables; GFE merge now also skips ConcurrentCacheModificationException
        if (!(ex.getCause() instanceof TransactionException
            && this.exception.getCause() instanceof TransactionException)
            && !(ex.getCause() instanceof ConcurrentCacheModificationException)
            && !(ex.getCause() instanceof EntryExistsException
                && this.exception.getCause() instanceof EntryExistsException)
            && !(ex.getCause() instanceof EntryNotFoundException
                && this.exception.getCause() instanceof EntryNotFoundException)) {
          log.severe(LocalizedStrings
              .ReplyProcessor21_EXCEPTION_RECEIVED_IN_REPLYMESSAGE_ONLY_ONE_EXCEPTION_IS_PASSED_BACK_TO_CALLER_THIS_EXCEPTION_IS_LOGGED_ONLY,
              ex);
        }
      }
      // overwrite current exception if it is a CancelException since that
      // is only to mark the node as departed and not throw to end user
      if (this.exception.getCause() instanceof CancelException) {
        this.exception = ex;
      }
    }
  }

  /**
   * Handle a {@link DSFIDNotFoundException} indicating a message type is not
   * implemented on another server (for example due to different product
   * version).
   */
  protected synchronized void processException(DistributionMessage msg,
      DSFIDNotFoundException ex, ReplyException replyEx) {
    final LogWriterI18n log = this.system.getLogWriterI18n();
    final short versionOrdinal = ex.getProductVersionOrdinal();
    String versionStr = null;
    try {
      Version version = Version.fromOrdinal(versionOrdinal, false);
      versionStr = version.toString();
    } catch (UnsupportedGFXDVersionException e) {
    }
    if (versionStr == null) {
      versionStr = "Ordinal=" + versionOrdinal;
    }
    log.severe(LocalizedStrings.ReplyProcessor21_UNKNOWN_DSFID_ERROR,
        new Object[] { ex.getUnknownDSFID(), msg.getSender(), versionStr }, ex);
    processException(msg, replyEx);
  }

  public void memberJoined(InternalDistributedMember id) {
    // Nothing to do - member wasn't sent the operation, anyway.
  }

  public void quorumLost(Set<InternalDistributedMember> failures, List<InternalDistributedMember> remaining) {
  }

  public void memberSuspect(InternalDistributedMember id,
      InternalDistributedMember whoSuspected) {
    if (isSevereAlertProcessingEnabled()) {
      // if we're waiting for the member that initiated suspicion, we don't
      // want to be hasty about kicking it out of the distributed system
      synchronized (this.members) {
        int cells = this.members.length;
        for (int i=0; i<cells; i++) {
          InternalDistributedMember e = this.members[i];
          if (e != null && e.equals(whoSuspected)) {
            this.severeAlertTimerReset = true;
          }
        }
      } // synchronized
    }
  }

  public void memberDeparted(final InternalDistributedMember id, final boolean crashed) {
    removeMember(id, true);
    checkIfDone();
  }

  /**
   * Wait for all expected acks to be returned or an exception to come
   * in.  This method will return whether a) we have received replies
   * from all desired members or b) the necessary conditions have been
   * met so that we don't need to wait anymore.
   * @throws InternalGemFireException if ack-threshold was exceeded and system
   *         property "ack-threshold-exception" is set to true
   * @throws InterruptedException TODO-javadocs
   * @see #canStopWaiting()
   */
  public final void waitForReplies()
    throws InterruptedException, ReplyException {

    boolean result = waitForReplies(0);
    Assert.assertTrue(result, "failed but no exception thrown");
  }

  /**
   * A wait for replies that can be overridden by child classes.
   */
  public boolean waitForProcessedReplies() throws InterruptedException,
      ReplyException {
    return waitForReplies(0L);
  }

  /**
   * Registers this processor as a membership listener and
   * returns a set of the current members.
   * @return a Set of the current members
   * @since 5.7
   */
  protected Set<?> addListenerAndGetMembers() {
    return getDistributionManager()
        .addMembershipListenerAndGetDistributionManagerIds(this);
  }

  /**
   * Unregisters this processor as a membership listener
   * @since 5.7
   */
  protected void removeListener() {
    try {
      getDistributionManager().removeMembershipListener(this);
    } catch (DistributedSystemDisconnectedException e) {
      // ignore
    }
  }

  /**
   * Returns the set of members that this processor should care about.
   * @return a Set of the current members
   * @since 5.7
   */
  protected Set getDistributionManagerIds() {
    return getDistributionManager().getDistributionManagerIds();
  }

  protected void preWait() {
    startedWaiting = true;
    DM mgr = getDistributionManager();
    statStart = mgr.getStats().startReplyWait();
    // take lock on "this" first because memberDeparted will take a lock on
    // "this" leading to lock inversion against other paths that take in reverse
    // order e.g. toString() calls from any of the "this" synced methods
    // [sumedh] do we need two locks in this class, or only this should do?
    synchronized (this) {
      final InternalDistributedMember[] mbrs = this.members;
      synchronized (mbrs) {
        // no need to register with MembershipListener for the case of self
        // execution only (#47198)
        if (mbrs.length > 0
            && !(mbrs.length == 1 && mbrs[0] != null && mbrs[0]
                .equals(this.system.getDistributedMember()))) {
          Set<?> activeMembers = addListenerAndGetMembers();
          this.listenerAdded = true;
          processActiveMembers(activeMembers);
        }
      }
    }
  }

  /**
   * perform initial membership processing while under synchronization
   * of this.members
   * @param activeMembers the DM's current membership set
   */
  protected void processActiveMembers(Set activeMembers) {
    for (int i = 0; i < this.members.length; i++) {
      if (this.members[i] != null) {
        if (!activeMembers.contains(this.members[i])) {
          memberDeparted(this.members[i], false);
        }
      }
    }
  }

  protected void postWait() {
    startedWaiting = false;
    if (this.listenerAdded) {
      removeListener();
      this.listenerAdded = false;
    }
    final DM mgr = getDistributionManager();
    mgr.getStats().endReplyWait(this.statStart, this.initTime);

    checkCancellationInPostWait(mgr);
  }

  protected void checkCancellationInPostWait(DM mgr) {
    // Make sure that a cancellation check occurs.
    // TODO there may be a more elegant place to put this...
    mgr.getCancelCriterion().checkCancelInProgress(null);
  }

  // start waiting for replies without explicitly waiting for all of them using
  // waitForReplies* methods; useful for streaming of results in function
  // execution and GemFireXD
  public final boolean startWait() {
    if (!this.startedWaiting) {
      if (this.processorId == 0 && !isDirectReplyProcessor()) {
        register();
      }
      if (stillWaiting()) {
        preWait();
        return true;
      }
    }
    return false;
  }

  protected boolean isDirectReplyProcessor() {
    return false;
  }

  // end waiting for replies without explicitly invoking waitForReplies*
  // methods; useful for streaming of results in function execution and
  // GemFireXD
  public final void endWait() {
    try {
      if (this.startedWaiting || this.listenerAdded) {
        postWait();
      }
    } finally {
      cleanup();
    }
  }

  /**
   * Wait a given number of milliseconds for the expected acks to be
   * received.  If <code>msecs</code> milliseconds pass before all
   * acknowlegdements are received, <code>false</code> is returned.
   *
   * @param msecs the number of milliseconds to wait for replies
   * @throws InterruptedException if interrupted while waiting on latch
   * @throws ReplyException an exception passed back in reply
   * @throws InternalGemFireException if ack-threshold was exceeded and system
   * property "ack-threshold-exception" is set to true
   * @throws IllegalStateException if the processor is not registered to receive messages
   *
   * @return Whether or not we received all of the replies in the
   *         given amount of time.
   */
  public final boolean waitForReplies(long msecs) throws InterruptedException,
      ReplyException {
    return waitForReplies(msecs, getLatch(), true);
  }

  public final boolean waitForReplies(long msecs,
      StoppableCountDownLatch latch, boolean doCleanUp)
      throws InterruptedException, ReplyException {
    if (this.keeperCleanedUp) {
      if (!doCleanUp) {
        return false;
      }
      throw new IllegalStateException(LocalizedStrings
          .ReplyProcessor21_THIS_REPLY_PROCESSOR_HAS_ALREADY_BEEN_REMOVED_FROM_THE_PROCESSOR_KEEPER
              .toLocalizedString());
    }
    boolean result = true;
    boolean interrupted = Thread.interrupted();
    MessageDependencyMonitor.waitingForReply(this);
    try {
      // do the interrupted check inside the try so that cleanup is called
      if (interrupted) throw new InterruptedException();
      if (stillWaiting()) {
        preWait();
        try {
          result = basicWait(msecs, latch);
        }
        catch (InterruptedException e) {
          interrupted = true;
        }
        finally {
          if (doCleanUp) {
            postWait();
          }
        }
      }
      if (this.exception != null) {
        throw this.exception;
      }
    }
    finally {
      if (doCleanUp) {
        try {
          cleanup();
        } finally {
          if (interrupted) throw new InterruptedException();
        }
      }
      MessageDependencyMonitor.doneWaiting(this);
    }
    return result;
  }

  /**
   * basicWait occurs after preWait and before postWait. Attempts to acquire
   * the latch are made.
   * @param msecs the number of milliseconds to wait for replies
   * @return whether or not we received all of the replies in the given amount
   * of time
   */
  protected boolean basicWait(long msecs, StoppableCountDownLatch latch)
  throws InterruptedException, ReplyException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }

    if (stillWaiting()) {
      long timeout = getAckWaitThreshold() * 1000L;
      long timeSoFar = System.currentTimeMillis() - this.initTime;
      long severeAlertTimeout = getAckSevereAlertThresholdMS();
      if (timeout <= 0) {
        timeout = Long.MAX_VALUE;
      }
      if (msecs == 0) {
        boolean timedOut = false;
        if (timeout <= timeSoFar+1) {
          timedOut = !latch.await(10);
        }
        if (timedOut || !latch.await(timeout-timeSoFar-1)) {
          this.dmgr.getCancelCriterion().checkCancelInProgress(null);

          // only start SUSPECT processing if severe alerts are enabled
          timeout(isSevereAlertProcessingEnabled() && (severeAlertTimeout > 0), false);

          // If ack-severe-alert-threshold has been set, we now
          // wait for that period of time and then force the non-responding
          // members from the system.  Then we wait indefinitely
          if (isSevereAlertProcessingEnabled() && severeAlertTimeout > 0) {
            boolean timedout;
            do {
              this.severeAlertTimerReset = false;  // retry if this gets set by suspect processing (splitbrain requirement)
              timedout = !latch.await(severeAlertTimeout);
            } while (timedout && this.severeAlertTimerReset);
            if (timedout) {
              this.dmgr.getCancelCriterion().checkCancelInProgress(null);
              timeout(false, true);
              // for consistency, we must now wait for a membership view
              // that ejects the removed members
              latch.await();
            }
          }
          else {
            latch.await();
          }
          // Give an info message since timeout gave a warning.
          this.system.getLogWriter().convertToLogWriterI18n()
              .info(LocalizedStrings.ReplyProcessor21_WAIT_FOR_REPLIES_COMPLETED);
        }
      }
      else {
        if (msecs > timeout) {
          if (!latch.await(timeout)) {
            timeout(false, false);
            // after timeout alert, wait remaining time
            if (!latch.await(msecs-timeout)) {
              this.system.getLogWriterI18n().info(LocalizedStrings.ReplyProcessor21_WAIT_FOR_REPLIES_TIMING_OUT_AFTER_0_SEC, Long.valueOf(msecs / 1000));
              return false;
            }
            // Give an info message since timeout gave a warning.
            this.system.getLogWriterI18n().info(LocalizedStrings.ReplyProcessor21_WAIT_FOR_REPLIES_COMPLETED);
          }
        }
        else {
          if (!latch.await(msecs)) {
            return false;
          }
        }
      }
    }
    Assert.assertTrue(latch != this.latch || !stillWaiting(), this);
    if (stopBecauseOfExceptions()) {
      throw this.exception;
    }
    return true;
  }

  /**
   * Wait a given number of milliseconds for the expected acks to be
   * received.  If <code>msecs</code> milliseconds pass before all
   * acknowlegdements are received, <code>false</code> is returned.
   * <p>
   * Thread interruptions will be ignored while waiting. If interruption
   * occurred while in this method, the current thread's interrupt flag will
   * be true, but InterruptedException will not be thrown.
   *
   * @param p_msecs the number of milliseconds to wait for replies, zero will be
   * interpreted as Long.MAX_VALUE
   *
   * @throws ReplyException an exception passed back in reply
   *
   * @throws InternalGemFireException if ack-threshold was exceeded and system
   * property "ack-threshold-exception" is set to true
   * @throws IllegalStateException if the processor is not registered to receive replies
   */
  public final boolean waitForRepliesUninterruptibly(long p_msecs)
      throws ReplyException {
	  return waitForRepliesUninterruptibly(p_msecs, getLatch(), true);
  }
  
  public final boolean waitForRepliesUninterruptibly(long p_msecs, 
		  StoppableCountDownLatch latch, boolean doCleanUp)
      throws ReplyException {
    if (this.keeperCleanedUp) {
      throw new IllegalStateException(LocalizedStrings.ReplyProcessor21_THIS_REPLY_PROCESSOR_HAS_ALREADY_BEEN_REMOVED_FROM_THE_PROCESSOR_KEEPER.toLocalizedString());
    }
    long msecs = p_msecs; // don't overwrite parameter
    boolean result = true;
    MessageDependencyMonitor.waitingForReply(this);
    try {
      if (stillWaiting()) {
        preWait();
        try {
          while (true) {
            // cancellation check
            this.dmgr.getCancelCriterion().checkCancelInProgress(null);

            long startWaitTime = System.currentTimeMillis();
            boolean interrupted = Thread.interrupted();
            try {
              result = basicWait(msecs, latch);
              break;
            }
            catch (InterruptedException e) {
              interrupted = true; // keep looping
              this.dmgr.getCancelCriterion().checkCancelInProgress(e);
              if (msecs > 0) {
                final long interruptTime = System.currentTimeMillis();
                msecs -= interruptTime - startWaitTime;
                if (msecs <= 0) {
                  msecs = 1;
                  result = false;
                  break;
                }
              }
            }
            finally {
              if (interrupted) {
                Thread.currentThread().interrupt();
              }
            }
          } // while
        }
        finally {
          if (doCleanUp) {
            postWait();
          }
        }
      } // stillWaiting
      if (this.exception != null) {
        throw this.exception;
      }
    } finally {
      if (doCleanUp) {
        cleanup();
      }
      MessageDependencyMonitor.doneWaiting(this);
    }
    return result;
  }

  /**
   * Used to cleanup resources allocated by the processor
   * after we are done using it.
   * @since 5.1
   */
  public void cleanup() {
    final int id = getProcessorId();
    if (id != 0 && !this.keeperCleanedUp) {
      this.keeperCleanedUp = true;
      keeper.remove(id);
    }
  }

  /**
   * Wait for the expected acks to be received.
   * <p>
   * Thread interruptions will be ignored while waiting. If interruption
   * occurred while in this method, the current thread's interrupt flag will
   * be true, but InterruptedException will not be thrown.
   *
   * @throws ReplyException an exception passed back in reply
   *
   * @throws InternalGemFireException if ack-threshold was exceeded and system
   * property "ack-threshold-exception" is set to true
   */
  public final void waitForRepliesUninterruptibly()
  throws ReplyException {
    waitForRepliesUninterruptibly(0);
  }

  /**
   * Returns the id of this reply processor.  This id is
   * often sent in messages, so that reply messages know which
   * processor to notify.
   *
   * @see #getProcessor
   */
  public final int getProcessorId() {
    return processorId;
  }

  /**
   * Returns whether or not this reply processor can stop waiting for
   * replies.  This method can be overridden to allow the waiter to be
   * notified before all responses have been received.  This is useful
   * when we are waiting for a value from any member, or if we can
   * stop waiting if a remote exception occurred.  By default, this
   * method returns <code>false</code>.
   */
  protected boolean canStopWaiting() {
    return false;
  }

  /**
   * We're still waiting if there is any member still left
   * in the set and an exception
   * hasn't been returned from anyone yet.
   *
   * @return true if we are still waiting for a response
   */
  protected boolean stillWaiting() {
    if (shutdown) {
      // Create the exception here, so that the call stack reflects the
      // failed computation.  If you set the exception in onShutdown,
      // the resulting stack is not of interest.
      ReplyException re = new ReplyException(new DistributedSystemDisconnectedException(LocalizedStrings.ReplyProcessor21_ABORTED_DUE_TO_SHUTDOWN.toLocalizedString()));
      this.exception = re;
      return false;
    }

    // Optional override by subclass
    if (canStopWaiting()) {
      return false;
    }

    // If an error has occurred, we're done.
    if (stopBecauseOfExceptions()) {
      return false;
    }

    // All else is good, keep waiting if we have members to wait on.
    return numMembers() > 0;
  }

  /**
   * Control of reply processor waiting behavior in the face of exceptions.
   * @since 5.7
   * @return true to stop waiting when exceptions are present
   */
  protected boolean stopBecauseOfExceptions() {
    return !WAIT_ON_EXCEPTION && exception != null;
  }

  /**
   * If this processor is not waiting for any more replies, then the
   * waiting thread will be notified.
   */
  protected void checkIfDone() {
    boolean finished = !stillWaiting();
    if (finished) {
      finished();
    }
  }

  /** do processing required when finished */
  protected final void finished() {
    boolean isDone = false;
    synchronized (this) {
      if (!this.done) { // make sure only called once
        this.done = true;
        isDone = true;
      }
    } // synchronized

    // ensure that postFinish/countDown is invoked only once
    if (isDone) {
      postFinish();
      // don't release the latch before postFinish else we may end up
      // waking up the waiter too soon
      // getSync().release(); // notifies threads in waitForReplies
      getLatch().countDown();
    }
  }

  /**
   * Override to execute custom code after {@link #finished}. This will be
   * invoked only once for each ReplyProcessor21.
   */
  protected void postFinish() {
  }

  protected final String shortName() {
    String base = this.getClass().getName();
    int dot = base.lastIndexOf('.');
    if (dot == -1) {
      return base + '@' + Integer.toHexString(System.identityHashCode(this));
    }
    return base.substring(dot + 1) + '@'
        + Integer.toHexString(System.identityHashCode(this));
  }

  @Override
  public final int hashCode() {
    return this.hash;
  }

  @Override
  public String toString() {
    return "<" + shortName() + " " + this.getProcessorId() +
      " waiting for " + numMembers() + " replies" +
      (exception == null ? "" : (" exception: " + exception)) +
      " from " + membersToString() + ">";
  }

  /**
   *
   * @param m the member to be removed
   * @param departed true if it is removed due to membership
   * @return true if it was in our list of members
   */
  protected boolean removeMember(InternalDistributedMember m, boolean departed) {
    boolean removed = false;
    synchronized (this.members) {
      int cells = this.members.length;
      for (int i=0; i<cells; i++) {
        InternalDistributedMember e = this.members[i];
        if (e != null && e.equals(m)) {
          this.members[i] = null;
          // we may be expecting more than one response from a member.  so,
          // unless the member left, we only scrub the first occurrence of
          // the member id from the responder list
          if (!departed) {
            return true;
          }
          removed = true;
        }
      }
    } // synchronized
    return removed;
  }

  public final int numMembers() {
    int sz = 0;
    synchronized (this.members) {
      int cells = this.members.length;
      for (int i=0; i<cells; i++) {
        if (this.members[i] != null) {
          sz++;
        }
      }
    } // synchronized
    return sz;
  }

  protected final boolean waitingOnMember(InternalDistributedMember id) {
    synchronized (this.members) {
      int cells = this.members.length;
      for (int i=0; i<cells; i++) {
        if (id.equals(this.members[i])) {
          return true;
        }
      }
      return false;
    } // synchronized
  }


  /**
   * Return the time in sec to wait before sending an alert while
   * waiting for ack replies.  Note that the ack wait threshold may
   * change at runtime, so we have to consult the system every time.
   */
  protected int getAckWaitThreshold() {
    return this.system.getConfig().getAckWaitThreshold();
  }

  /**
   * Return the time in sec to wait before removing unresponsive members
   * from the distributed system.  This period starts after the ack-wait-threshold
   * period has elapsed
   */
  protected int getSevereAlertThreshold() {
    return this.system.getConfig().getAckSevereAlertThreshold();
  }

  /**
   * process a wait-timeout.  Usually suspectThem would be used in the first
   * timeout, followed by a subsequent use of disconnectThem
   * @param suspectThem whether to ask the membership manager to suspect the unresponsive members
   * @param severeAlert whether to ask the membership manager to disconnect the unresponseive members
   */
  private void timeout(boolean suspectThem, boolean severeAlert) {
    Set activeMembers = getDistributionManagerIds();

    // an alert that will show up in the console
    LogWriterI18n logger = this.system.getLogWriter().convertToLogWriterI18n();
    long timeout = getAckWaitThreshold();
    final Object[] msgArgs = new Object[] {Long.valueOf(timeout + (severeAlert? getSevereAlertThreshold() : 0)), this, getDistributionManager().getId(), activeMembers};
    final StringId msg = LocalizedStrings.ReplyProcessor21_0_SEC_HAVE_ELAPSED_WHILE_WAITING_FOR_REPLIES_1_ON_2_WHOSE_CURRENT_MEMBERSHIP_LIST_IS_3;
    if (severeAlert) {
      logger.severe(msg, msgArgs);
    }
    else {
      logger.warning(msg, msgArgs);
    }
    msgArgs[3] = "(omitted)";
    Breadcrumbs.setProblem(msg, msgArgs);

    // Increment the stat
    getDistributionManager().getStats().incReplyTimeouts();

    final Set suspectMembers;
    if (suspectThem || severeAlert) {
      suspectMembers = new HashSet();
    }
    else {
      suspectMembers = null;
    }

    synchronized (this.members) {
      for (int i = 0; i < this.members.length; i++) {
        if (this.members[i] != null) {
          if (!activeMembers.contains(this.members[i])) {
            logger.warning(
              LocalizedStrings.ReplyProcessor21_VIEW_NO_LONGER_HAS_0_AS_AN_ACTIVE_MEMBER_SO_WE_WILL_NO_LONGER_WAIT_FOR_IT,
              this.members[i]);
            memberDeparted(this.members[i], false);
          }
          else {
            if (suspectMembers != null) {
              suspectMembers.add(this.members[i]);
            }
          }
        }
      }
    }

    if (THROW_EXCEPTION_ON_TIMEOUT) {
      // init the cause to be a TimeoutException so catchers can determine cause
      TimeoutException cause = new TimeoutException(LocalizedStrings.TIMED_OUT_WAITING_FOR_ACKS.toLocalizedString());
      throw new InternalGemFireException(LocalizedStrings.ReplyProcessor21_0_SEC_HAVE_ELAPSED_WHILE_WAITING_FOR_REPLIES_1_ON_2_WHOSE_CURRENT_MEMBERSHIP_LIST_IS_3
          .toLocalizedString(msgArgs), cause);
    }
    else if (suspectThem) {
      if (suspectMembers != null && suspectMembers.size() > 0) {
        getDistributionManager().getMembershipManager()
          .suspectMembers(suspectMembers, "Failed to respond within ack-wait-threshold");
      }
    }
  }

  public String membersToString() {
    final StringBuilder sb = new StringBuilder("[");
    boolean first = true;
    synchronized (this.members) {
      for (int i = 0; i < this.members.length; i++) {
        InternalDistributedMember member = this.members[i];
        if (member != null) {
          if (first) {
            first = false;
          } else {
            sb.append(", ");
          }
          sb.append(member);
        }
      }
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * You must be synchronize on the result of this function in order
   * to examine its contents.
   *
   * @return the members that have not replied
   */
  protected InternalDistributedMember[] getMembers() {
    return this.members;
  }

  protected final StoppableCountDownLatch getLatch() {
    return this.latch;
  }

  /**
   * Enables severe alert processing in this reply processor, if
   * it has also been enabled in the distribution config.  Severe
   * alerts are issued if acks are not received within ack-wait-threshold
   * plus ack-severe-alert-threshold seconds.
   */
  public void enableSevereAlertProcessing() {
    this.severeAlertEnabled = true;
  }

  /**
   * Set a shorter ack-severe-alert-threshold than normal
   * @param flag whether to shorten the time or not
   */
  public static void setShortSevereAlertProcessing(boolean flag) {
    SevereAlertShorten.set(Boolean.valueOf(flag));
  }

  public static boolean getShortSevereAlertProcessing() {
    return ((Boolean)SevereAlertShorten.get()).booleanValue();
  }

  /**
   * Force reply-waits in the current thread to perform severe-alert processing
   */
  public static void forceSevereAlertProcessing() {
    ForceSevereAlertProcessing.set(Boolean.TRUE);
  }

  /**
   * Reset the forcing of severe-alert processing for the current thread
   */
  public static void unforceSevereAlertProcessing() {
    ForceSevereAlertProcessing.set(Boolean.FALSE);
  }

  /**
   * Returns true if forceSevereAlertProcessing has been used to force
   * the next reply-wait in the current thread to perform severe-alert
   * processing.
   */
  public static boolean isSevereAlertProcessingForced() {
    return ((Boolean)ForceSevereAlertProcessing.get()).booleanValue();
  }


  /**
   * Get the ack-severe-alert-threshold, in milliseconds,
   * with shortening applied
   */
  public long getAckSevereAlertThresholdMS() {
    long disconnectTimeout = getSevereAlertThreshold() * 1000L;
    if (disconnectTimeout > 0 && ((Boolean)SevereAlertShorten.get()).booleanValue()) {
      disconnectTimeout = (long)(disconnectTimeout * PR_SEVERE_ALERT_RATIO);
    }
    return disconnectTimeout;
  }

  public boolean isSevereAlertProcessingEnabled() {
    return this.severeAlertEnabled || isSevereAlertProcessingForced();
  }


  private final static ThreadLocal messageId = new ThreadLocal();

  private final static Integer VOID_RPID = Integer.valueOf(0);
  /**
   * Used by messages to store the id for the current message into a thread local.
   * This allows the comms layer to still send replies even when it can't deserialize
   * a message.
   */
  public static void setMessageRPId(int id) {
    messageId.set(Integer.valueOf(id));
  }
  public static void initMessageRPId() {
    messageId.set(VOID_RPID);
  }
  public static void clearMessageRPId() {
    messageId.set(VOID_RPID);
    // messageId.remove(); change to use remove when we drop 1.4 support
  }
  /**
   * Returns the reply processor id for the message currently being read.
   * Returns 0 if no id exists.
   */
  public static int getMessageRPId() {
    int result = 0;
    Object v = messageId.get();
    if (v != null) {
      result = ((Integer)v).intValue();
    }
    return result;
  }

  /**
   * To fix the hang of 42951 make sure the guys
   * waiting on this processor are told to quit
   * waiting and tell them why.
   * @param ex the reason the reply processor is being canceled
   */
  public void cancel(InternalDistributedMember sender, RuntimeException ex) {
    processException(new ReplyException("Unexpected exception while processing reply message", ex));
    removeMember(sender, false);
    checkIfDone();
  }
}
