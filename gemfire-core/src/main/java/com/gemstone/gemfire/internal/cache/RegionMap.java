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

package com.gemstone.gemfire.internal.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gemstone.gemfire.cache.CacheCallback;
import com.gemstone.gemfire.cache.CacheWriterException;
import com.gemstone.gemfire.cache.EntryNotFoundException;
import com.gemstone.gemfire.cache.Operation;
import com.gemstone.gemfire.cache.TimeoutException;
import com.gemstone.gemfire.cache.query.internal.IndexUpdater;
import com.gemstone.gemfire.internal.cache.delta.Delta;
import com.gemstone.gemfire.internal.cache.lru.LRUMapCallbacks;
import com.gemstone.gemfire.internal.cache.tier.sockets.ClientProxyMembershipID;
import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;
import com.gemstone.gemfire.internal.cache.versions.RegionVersionVector;
import com.gemstone.gemfire.internal.cache.versions.VersionHolder;
import com.gemstone.gemfire.internal.cache.versions.VersionSource;
import com.gemstone.gemfire.internal.cache.versions.VersionTag;
import com.gemstone.gemfire.internal.size.SingleObjectSizer;

/**
 * Internal interface used by {@link LocalRegion} to access the map that holds
 * its entries. Note that the value of every entry in this map will
 * implement {@link RegionEntry}.
 *
 * @since 3.5.1
 *
 * @author Darrel Schneider
 *
 */
public interface RegionMap extends LRUMapCallbacks {
  
  /**
   * Parameter object used to facilitate construction of an EntriesMap.
   * Modification of fields after the map is constructed has no effect.
   */
  static class Attributes {
    /** The initial capacity. The implementation
     * performs internal sizing to accommodate this many elements. */
    int initialCapacity = 16;
    
    /** the load factor threshold, used to control resizing. */
    float loadFactor = 0.75f;
    
    /** the estimated number of concurrently
     * updating threads. The implementation performs internal sizing
     * to try to accommodate this many threads. */
    int concurrencyLevel = 16;
    
    /** whether "api" statistics are enabled */
    boolean statisticsEnabled = false;
    
    /** whether LRU stats are required */
//    boolean lru = false;
  }

  public RegionEntryFactory getEntryFactory();

  /**
   * This method should be called before region is initialized
   * to ensure there is no mix of region entries
   */
  public void setEntryFactory(RegionEntryFactory f);

  /**
   * Gets the attributes that this map was created with.
   */
  public Attributes getAttributes();
  
  /**
   * Tells this map what region owns it.
   */
  public void setOwner(Object r);

  public void changeOwner(LocalRegion r, InternalRegionArguments args);

  public int size();
  public boolean isEmpty();

  /**
   * @return number of entries cached in the backing CHM
   */
  public int sizeInVM();

  public Set keySet();
  
  /** Returns a collection of RegionEntry instances.
   */
  public Collection<RegionEntry> regionEntries();

  /**
   * Returns a collection of RegionEntry instances from memory only.
   */
  public Collection<RegionEntry> regionEntriesInVM();

  public boolean containsKey(Object key);

  /**
   * fetches the entry from the backing ConcurrentHashMap
   * @param key
   * @return the RegionEntry from memory or disk
   */
  public RegionEntry getEntry(Object key);
  public RegionEntry putEntryIfAbsent(Object key, RegionEntry re);

  /**
   * fetches the entry from the backing ConcurrentHashMap. In case of
   * HDFS region this <b>DOES NOT</b> fetch the entry from
   * HDFS. <b>The entry returned by this method may have
   * been faulted in from HDFS. Please use {@link #getOperationalEntryInVM(Object)}
   * if you only want operational entry</b>
   * @param key
   * @return the RegionEntry from memory
   */
  public RegionEntry getEntryInVM(Object key);

  /**
   * fetches the entry from the backing ConcurrentHashMap only if the entry
   * is considered to be in operational data i.e. does not have
   * isMarkedForEviction() bit set.
   * @param key
   * @return the RegionEntry in operational data
   */
  public RegionEntry getOperationalEntryInVM(Object key);


  //   /**
//    * Removes any entry associated with <code>key</code>.
//    * Do nothing if the map has no entry for key.
//    */
//   public void remove(Object key);

//   /**
//    * Removes the entry associated with <code>key</code>
//    * if it is <code>entry</code>.
//    * Otherwise do nothing.
//    */
//   public void remove(Object key, RegionEntry entry);

  /**
   * Clear the region and, if the parameter rvv is not null,
   * return a collection of the IDs of version sources that are
   * still in the map when the operation completes.
   */
  public Set<VersionSource> clear(RegionVersionVector rvv);

  /**
   * Used by disk regions when recovering data from backup.
   * Currently this "put" is done at a very low level to keep it from
   * generating events or pushing updates to others.
   * @return the created RegionEntry or null if entry already existed
   */
  public RegionEntry initRecoveredEntry(Object key, DiskEntry.RecoveredEntry value);
  /**
   * Used by disk regions when recovering data from backup and
   * initRecoveredEntry has already been called for the given key.
   * Currently this "put" is done at a very low level to keep it from
   * generating events or pushing updates to others.
   * @return the updated RegionEntry
   */
  public RegionEntry updateRecoveredEntry(Object key, RegionEntry re,
      DiskEntry.RecoveredEntry value);

  /**
   * Used to modify an existing RegionEntry or create a new one
   * when processing the values obtained during a getInitialImage.
   * @param wasRecovered true if the current entry in the cache was
   *        recovered from disk.
   * @param entryVersion version information from InitialImageOperation or RegisterInterest
   * @param sender the sender of the initial image, if IIO.  Not needed on clients
   * @param forceValue TODO
   */
  public boolean initialImagePut(Object key,
                                 long lastModified,
                                 Object newValue,
                                 boolean wasRecovered, 
                                 boolean deferLRUCallback,
                                 VersionTag entryVersion, InternalDistributedMember sender, boolean forceValue);

  /**
   * Destroy an entry the map.
   * @param event indicates entry to destroy as well as data for a <code>CacheCallback</code>
   * @param inTokenMode true if destroy is occurring during region initialization
   * @param duringRI true if destroy is occurring during register interest
   * @param cacheWrite true if a cacheWriter should be called
   * @param isEviction true if destroy was called in the context of an LRU Eviction
   * @param expectedOldValue if non-null, only destroy if key exists and value is
   *        is equal to expectedOldValue
   * @return true if the entry was destroyed, false otherwise
   * 
   * @see LocalRegion
   * @see AbstractRegionMap
   * @see CacheCallback
   * @see AbstractLRURegionMap
   */
  public boolean destroy(EntryEventImpl event,
                         boolean inTokenMode,
                         boolean duringRI,
                         boolean cacheWrite,
                         boolean isEviction,
                         Object expectedOldValue,
                         boolean removeRecoveredEntry)
    throws CacheWriterException, EntryNotFoundException, TimeoutException;

  /**
   * @param forceNewEntry
   *          used during GII, this forces us to leave an invalid token in the
   *          cache, even if the entry doesn't exist
   * @param forceCallbacks
   *          using for PRs with eviction enabled, this forces invalidate
   *          callbacks and events even if the entry doesn't exist in the cache.
   *          This differs from the forceNewEntry mode in that it doesn't leave
   *          an Invalid token in the cache.
   * @return true if invalidate was done
   */
  public boolean invalidate(EntryEventImpl event,
                            boolean invokeCallbacks,
                            boolean forceNewEntry,
                            boolean forceCallbacks)
    throws EntryNotFoundException;
                      
  public void evictValue(Object key);

  /**
   * @param event the event object for this operation, with the exception that
   * the oldValue parameter is not yet filled in. The oldValue will be filled
   * in by this operation.
   *
   * @param lastModified the lastModified time to set with the value; if 0L,
   *        then the lastModified time will be set to now.
   * @param ifNew true if this operation must not overwrite an existing key
   * @param ifOld true if this operation must not create a new entry
   * @param expectedOldValue
   *          only succeed if old value is equal to this value. If null,
   *          then doesn't matter what old value is. If INVALID token,
   *          must be INVALID.
   * @param requireOldValue if old value needs to be returned to caller in event
   *        (e.g. failed putIfAbsent)
   * @param overwriteDestroyed true if okay to overwrite the DESTROYED token:
   *         when this is true has the following effect:
   *           even when ifNew is true will write over DESTROYED token
   *         when overwriteDestroyed is false and ifNew or ifOld is true
   *           then if the put doesn't occur because there is a DESTROYED token
   *           present then the entry flag blockedDestroyed is set.
   * @return null if put was not done; otherwise reference to put entry
   */
  public RegionEntry basicPut(EntryEventImpl event,
                              long lastModified,
                              boolean ifNew,
                              boolean ifOld,
                              Object expectedOldValue,
                              boolean requireOldValue,
                              boolean overwriteDestroyed)
    throws CacheWriterException, TimeoutException;

  /**
   * Write synchronizes the given entry and invokes the runable
   * while holding the lock. Does nothing if the entry does not exist.
   */
  public void writeSyncIfPresent(Object key, Runnable runner);

  /**
   * Remove the entry with the given key if it has been marked as destroyed
   * This is currently used in the cleanup phase of getInitialImage.
   */
  public void removeIfDestroyed(Object key);

  /**
   * @param re the existing RegionEntry of the entry to put
   * @param txState the TXState of the current transaction
   * @param key the key of the entry to destroy
   * @param inTokenMode true if caller has determined we are in destroy token
   *        mode and will keep us in that mode while this call is executing.
   * @param inRI the region is performing registerInterest so we need a token
   * @param localOp true for localDestroy, false otherwise
   * @param eventId filled in if operation performed
   * @param aCallbackArgument callback argument passed by user
   * @param filterRoutingInfo 
   * @param versionTag when not null, it is the tag generated on near-side to be associated with the entry on far-side
   * @param tailKey when not -1, it is the tailKey generated on near-side to be associated with entry on far-side for WAN
   * @param txr the {@link TXRegionState} for the operation
   * @param cbEvent a template EntryEvent for callbacks
   */
  public void txApplyDestroy(RegionEntry re,
                             TXStateInterface txState,
                             Object key,
                             boolean inTokenMode, boolean inRI,
                             boolean localOp,
                             EventID eventId,
                             Object aCallbackArgument,
                             List<EntryEventImpl> pendingCallbacks, FilterRoutingInfo filterRoutingInfo,
                             ClientProxyMembershipID bridgeContext,
                             VersionTag<?> versionTag, long tailKey,
                             TXRegionState txr,
                             EntryEventImpl cbEvent);
  /**
   * @param re the existing RegionEntry of the entry to put
   * @param txState the TXState of the current transaction
   * @param key the key of the entry to invalidate
   * @param newValue the new value of the entry
   * @param didDestroy true if tx destroyed this entry at some point
   * @param localOp true for localInvalidates, false otherwise
   * @param aCallbackArgument callback argument passed by user
   * @param filterRoutingInfo 
   * @param versionTag when not null, it is the tag generated on near-side to be associated with the entry on far-side 
   * @param tailKey when not -1, it is the tailKey generated on near-side to be associated with entry on far-side for WAN
   * @param txr the {@link TXRegionState} for the operation
   * @param cbEvent a template EntryEvent for callbacks
   */
  public void txApplyInvalidate(RegionEntry re,
                                TXStateInterface txState,
                                Object key,
                                Object newValue,
                                boolean didDestroy,
                                boolean localOp,
                                EventID eventId,
                                Object aCallbackArgument,
                                List<EntryEventImpl> pendingCallbacks, FilterRoutingInfo filterRoutingInfo,
                                ClientProxyMembershipID bridgeContext,
                                VersionTag<?> versionTag, long tailKey,
                                TXRegionState txr,
                                EntryEventImpl cbEvent);
  /**
   * @param putOp describes the operation that did the put
   * @param re the existing RegionEntry of the entry to put
   * @param txState the TXState of the current transaction
   * @param key the key of the entry to put
   * @param newValue the new value of the entry
   * @param didDestroy true if tx destroyed this entry at some point
   * @param aCallbackArgument callback argument passed by user
   * @param filterRoutingInfo 
   * @param versionTag when not null, it is the tag generated on near-side to be associated with the entry on far-side
   * @param tailKey when not -1, it is the tailKey generated on near-side to be associated with entry on far-side for WAN
   * @param txr the {@link TXRegionState} for the operation
   * @param cbEvent a template EntryEvent for callbacks
   * @param delta collected changes in case of update operation
   */
  public void txApplyPut(Operation putOp,
                         RegionEntry re,
                         TXStateInterface txState,
                         Object key,
                         Object newValue,
                         boolean didDestroy,
                         EventID eventId,
                         Object aCallbackArgument,
                         List<EntryEventImpl> pendingCallbacks, FilterRoutingInfo filterRoutingInfo,
                         ClientProxyMembershipID bridgeContext,
                         VersionTag<?> versionTag, long tailKey,
                         TXRegionState txr,
                         EntryEventImpl cbEvent,
                         Delta delta);
  /**
   * removes the given key if the enclosing RegionEntry is still in
   * this map
   */
  public void removeEntry(Object key, RegionEntry value, boolean updateStats);

  /**
   * Removes the given key if the enclosing RegionEntry is still in this map for
   * the given EntryEvent and updating the given {@link IndexUpdater} of the
   * region ({@link #getIndexUpdater()}) for the event.
   */
  public void removeEntry(Object key, RegionEntry re, boolean updateStat,
      EntryEventImpl event, LocalRegion owner, IndexUpdater indexUpdater);

  public void copyRecoveredEntries(RegionMap rm, boolean entriesIncompatible);

  public IndexUpdater getIndexUpdater();

  public void setIndexUpdater(IndexUpdater indexManager);

  /**
   * This returns memory overhead excluding individual entry sizes. Just additional
   * memory consumed by this data structure. 
   * 
   * @param sizer The sizer object that is to be used for estimating objects.
   * @return memory overhead by this region without region entry size.
   */
  public long estimateMemoryOverhead(SingleObjectSizer sizer);

  /**
   * Removes an entry that was previously destroyed and made into a tombstone.
   * 
   * @param re the entry that was destroyed
   * @param destroyedVersion the version that was destroyed
   * @param isEviction true if the tombstone is being evicted by LRU
   * @param isScheduledTombstone TODO
   * @return true if the tombstone entry was removed from the entry map
   */
  public boolean removeTombstone(RegionEntry re, VersionHolder destroyedVersion, boolean isEviction, boolean isScheduledTombstone);
  
  /**
   * Checks to see if the given version is still the version in the map

   * @param re the entry that was destroyed
   * @param destroyedVersion the version that was destroyed
   * @return true of the tombstone is no longer needed (entry was resurrected or evicted)
   */
  public boolean isTombstoneNotNeeded(RegionEntry re, int destroyedVersion);
  
  /**
   * a tombstone has been unscheduled - update LRU stats if necessary
   */
  public void unscheduleTombstone(RegionEntry re);

  public void updateEntryVersion(EntryEventImpl event);
  
  /** Not thread-safe and returns internal map. Use only for tests. */
  public Map<?, ?> getTestSuspectMap();

  public void close();

  public void removeIfDelta(Object next);
  
  /**
   * Return true if the region entry mapped to the given key
   * is a ListOfDeltas, which can be created by a concurrent update
   * during GII.
   */
  public boolean isListOfDeltas(Object key);
}
