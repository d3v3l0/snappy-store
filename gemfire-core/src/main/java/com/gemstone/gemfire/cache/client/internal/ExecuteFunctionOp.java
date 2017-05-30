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
package com.gemstone.gemfire.cache.client.internal;

import com.gemstone.gemfire.InternalGemFireError;
import com.gemstone.gemfire.cache.client.ServerConnectivityException;
import com.gemstone.gemfire.cache.client.ServerOperationException;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.internal.ServerLocation;
import com.gemstone.gemfire.i18n.LogWriterI18n;
import com.gemstone.gemfire.internal.cache.execute.AbstractExecution;
import com.gemstone.gemfire.internal.cache.execute.FunctionStats;
import com.gemstone.gemfire.internal.cache.execute.InternalFunctionException;
import com.gemstone.gemfire.internal.cache.execute.InternalFunctionInvocationTargetException;
import com.gemstone.gemfire.internal.cache.execute.MemberMappedArgument;
import com.gemstone.gemfire.internal.cache.execute.ServerFunctionExecutor;
import com.gemstone.gemfire.internal.cache.tier.MessageType;
import com.gemstone.gemfire.internal.cache.tier.sockets.ChunkedMessage;
import com.gemstone.gemfire.internal.cache.tier.sockets.Message;
import com.gemstone.gemfire.internal.cache.tier.sockets.Part;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;
import com.gemstone.gemfire.internal.shared.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Executes the function on server (possibly without region/cache).<br> 
 * Also gets the result back from the server
 * @author Suranjan Kumar
 * @since 5.8
 */

public class ExecuteFunctionOp {

  /** index of allMembers in flags[] */
  public static final int ALL_MEMBERS_INDEX = 0;
  /** index of ignoreFailedMembers in flags[] */
  public static final int IGNORE_FAILED_MEMBERS_INDEX = 1;

  private ExecuteFunctionOp() {
    // no instances allowed
  }
  
  static LogWriterI18n  logger = null ;

  /**
   * Does a execute Function on a server using connections from the given pool
   * to communicate with the server.
   * @param pool the pool to use to communicate with the server.
   * @param function of the function to be executed
   * @param args specified arguments to the application function
   */
  public static void execute(final PoolImpl pool, Function function,
      ServerFunctionExecutor executor, Object args,
      MemberMappedArgument memberMappedArg, boolean allServers, byte hasResult,
      ResultCollector rc, boolean isFnSerializationReqd,
      UserAttributes attributes, String[] groups) {
    logger = pool.getLoggerI18n();
    final AbstractOp op = new ExecuteFunctionOpImpl(logger, function, args,
        memberMappedArg, hasResult, rc, isFnSerializationReqd, (byte)0, groups, allServers, executor.isIgnoreDepartedMembers());

    if (allServers && groups.length == 0) {
      if (logger.fineEnabled()) {
        logger
            .fine("ExecuteFunctionOp#execute : Sending Function Execution Message:"
                + op.getMessage() + " to all servers using pool: " + pool);
      }
      List callableTasks = constructAndGetFunctionTasks(pool, function, args,
          memberMappedArg, hasResult, rc, isFnSerializationReqd, attributes);

      SingleHopClientExecutor.submitAll(callableTasks);
    } else {
      boolean reexecuteForServ = false;
      AbstractOp reexecOp = null;
      int retryAttempts = 0;
      boolean reexecute = false;
      int maxRetryAttempts = 0;
      if(function.isHA())
        maxRetryAttempts = pool.getRetryAttempts();
      
      do {
        try {
          if (reexecuteForServ) {
            if (logger.fineEnabled()) {
              logger
                  .fine("ExecuteFunctionOp#execute.reexecuteForServ : Sending Function Execution Message:"
                      + op.getMessage() + " to server using pool: " + pool + " with groups:"
                      + Arrays.toString(groups) + " all members:" + allServers + " ignoreFailedMembers:"+executor.isIgnoreDepartedMembers());
            }
            reexecOp = new ExecuteFunctionOpImpl(logger, function, args,
                memberMappedArg, hasResult, rc, isFnSerializationReqd,
                (byte)1/* isReExecute */, groups, allServers, executor.isIgnoreDepartedMembers());
            pool.execute(reexecOp, 0);
          } else {
            if (logger.fineEnabled()) {
              logger
                  .fine("ExecuteFunctionOp#execute : Sending Function Execution Message:"
                      + op.getMessage() + " to server using pool: " + pool + " with groups:"
                      + Arrays.toString(groups) + " all members:" + allServers + " ignoreFailedMembers:"+executor.isIgnoreDepartedMembers());
            }

            pool.execute(op, 0);
          }
          reexecute = false;
          reexecuteForServ = false;
        } catch (InternalFunctionInvocationTargetException e) {
          if (logger.fineEnabled()) {
            logger
                .fine("ExecuteFunctionOp#execute : Received InternalFunctionInvocationTargetException. The failed node is "
                    + e.getFailedNodeSet());
          }
          reexecute = true;
          rc.clearResults();
        } catch (ServerConnectivityException se) {
          retryAttempts++;

          if (logger.fineEnabled()) {
            logger
                .fine("ExecuteFunctionOp#execute : Received ServerConnectivityException. The exception is "
                    + se
                    + " The retryattempt is : "
                    + retryAttempts
                    + " maxRetryAttempts  " + maxRetryAttempts);
          }
          if (se instanceof ServerOperationException) {
            throw se;
          }
          if ((retryAttempts > maxRetryAttempts && maxRetryAttempts != -1))
            throw se;

          reexecuteForServ = true;
          rc.clearResults();
        }
      } while (reexecuteForServ);

      if (reexecute && function.isHA()) {
        ExecuteFunctionOp.reexecute(pool, function,
            executor, rc, hasResult, isFnSerializationReqd, maxRetryAttempts - 1, groups, allServers);
      }
    }
  }
  
  public static void execute(final PoolImpl pool, String functionId,
      ServerFunctionExecutor executor, Object args,
      MemberMappedArgument memberMappedArg, boolean allServers, byte hasResult,
      ResultCollector rc, boolean isFnSerializationReqd, boolean isHA,
      boolean optimizeForWrite, UserAttributes properties, String[] groups) {
    logger = pool.getLoggerI18n();
    final AbstractOp op = new ExecuteFunctionOpImpl(logger, functionId, args,
        memberMappedArg, hasResult, rc, isFnSerializationReqd, isHA,
        optimizeForWrite, (byte)0, groups, allServers, executor.isIgnoreDepartedMembers());
    if (allServers && groups.length == 0) {
      if (logger.fineEnabled()) {
        logger
            .fine("ExecuteFunctionOp#execute : Sending Function Execution Message:"
                + op.getMessage() + " to all servers using pool: " + pool);
      }
      List callableTasks = constructAndGetFunctionTasks(pool, functionId, args,
          memberMappedArg, hasResult, rc, isFnSerializationReqd, isHA,
          optimizeForWrite, properties);

      SingleHopClientExecutor.submitAll(callableTasks);
    } else {
      boolean reexecuteForServ = false;
      AbstractOp reexecOp = null;
      int retryAttempts = 0;
      boolean reexecute = false;
      int maxRetryAttempts = 0;
      if(isHA){
        maxRetryAttempts = pool.getRetryAttempts();
      }
      
      do {
        try {
          if (reexecuteForServ) {
            reexecOp = new ExecuteFunctionOpImpl(logger, functionId, args,
                memberMappedArg, hasResult, rc, isFnSerializationReqd, isHA,
                optimizeForWrite, (byte)1, groups, allServers, executor.isIgnoreDepartedMembers());
            pool.execute(reexecOp, 0);
          } else {
            if (logger.fineEnabled()) {
              logger
                  .fine("ExecuteFunctionOp#execute : Sending Function Execution Message:"
                      + op.getMessage() + " to server using pool: " + pool + " with groups:"
                      + Arrays.toString(groups) + " all members:" + allServers + " ignoreFailedMembers:"+executor.isIgnoreDepartedMembers());
            }
            pool.execute(op, 0);
          }
          reexecute = false;
          reexecuteForServ = false;
        } catch (InternalFunctionInvocationTargetException e) {
          if (logger.fineEnabled()) {
            logger
                .fine("ExecuteFunctionOp#execute : Received InternalFunctionInvocationTargetException. The failed node is "
                    + e.getFailedNodeSet());
          }
          reexecute = true;
          rc.clearResults();
        } catch (ServerConnectivityException se) {
          retryAttempts++;

          if (logger.fineEnabled()) {
            logger
                .fine("ExecuteFunctionOp#execute : Received ServerConnectivityException. The exception is "
                    + se
                    + " The retryattempt is : "
                    + retryAttempts
                    + " maxRetryAttempts  " + maxRetryAttempts);
          }
          if (se instanceof ServerOperationException) {
            throw se;
          }
          if ((retryAttempts > maxRetryAttempts && maxRetryAttempts != -1))
            throw se;

          reexecuteForServ = true;
          rc.clearResults();
        }
      } while (reexecuteForServ);

      if (reexecute && isHA) {
        ExecuteFunctionOp.reexecute(pool, functionId, executor, rc, hasResult,
            isFnSerializationReqd, maxRetryAttempts - 1, args, isHA,
            optimizeForWrite, groups, allServers);
      }
    }
  }
  
  public static void reexecute(ExecutablePool pool, Function function,
      ServerFunctionExecutor serverExecutor, ResultCollector resultCollector,
      byte hasResult, boolean isFnSerializationReqd, int maxRetryAttempts, String[] groups, boolean allMembers) {
    boolean reexecute = true;
    int retryAttempts = 0;
    do {
      reexecute = false;
      AbstractOp reExecuteOp = new ExecuteFunctionOpImpl(logger, function, serverExecutor.getArguments(),
          serverExecutor.getMemberMappedArgument(), hasResult, resultCollector, isFnSerializationReqd, (byte)1, groups, allMembers, serverExecutor.isIgnoreDepartedMembers());
      if (logger.fineEnabled()) {
        logger
            .fine("ExecuteFunction#reexecute : Sending Function Execution Message:"
                + reExecuteOp.getMessage() + " to Server using pool: " + pool + " with groups:"
                + Arrays.toString(groups) + " all members:" + allMembers + " ignoreFailedMembers:"+serverExecutor.isIgnoreDepartedMembers());
      }
      try {
          pool.execute(reExecuteOp,0);
      }
      catch (InternalFunctionInvocationTargetException e) {
        if (logger.fineEnabled()) {
          logger
              .fine("ExecuteFunctionOp#reexecute : Recieved InternalFunctionInvocationTargetException. The failed nodes are "
                  + e.getFailedNodeSet());
        }
        reexecute = true;
        resultCollector.clearResults();
      }
      catch (ServerConnectivityException se) {
        if (logger.fineEnabled()) {
          logger
              .fine("ExecuteFunctionOp#reexecute : Received ServerConnectivity Exception.");
        }
        
        if(se instanceof ServerOperationException){
          throw se;
        }
        retryAttempts++;
        if (retryAttempts > maxRetryAttempts && maxRetryAttempts != -2) 
          throw se;

        reexecute = true;
        resultCollector.clearResults();
      }
    } while (reexecute);
  }
  
  public static void reexecute(ExecutablePool pool, String functionId,
      ServerFunctionExecutor serverExecutor, ResultCollector resultCollector,
      byte hasResult, boolean isFnSerializationReqd, int maxRetryAttempts,
      Object args, boolean isHA, boolean optimizeForWrite, String[] groups, boolean allMembers) {
    boolean reexecute = true;
    int retryAttempts = 0;
    do {
      reexecute = false;
      
      final AbstractOp op = new ExecuteFunctionOpImpl(logger, functionId, args,
          serverExecutor.getMemberMappedArgument(), hasResult, resultCollector, isFnSerializationReqd, isHA, optimizeForWrite, (byte)1, groups, allMembers, serverExecutor.isIgnoreDepartedMembers());
      
      if (logger.fineEnabled()) {
        logger
            .fine("ExecuteFunction#reexecute : Sending Function Execution Message:"
                + op.getMessage() + " to Server using pool: " + pool + " with groups:"
                + Arrays.toString(groups) + " all members:" + allMembers + " ignoreFailedMembers:"+serverExecutor.isIgnoreDepartedMembers());
      }
      try {
          pool.execute(op,0);
      }
      catch (InternalFunctionInvocationTargetException e) {
        if (logger.fineEnabled()) {
          logger
              .fine("ExecuteFunctionOp#reexecute : Recieved InternalFunctionInvocationTargetException. The failed nodes are "
                  + e.getFailedNodeSet());
        }
        reexecute = true;
        resultCollector.clearResults();
      }
      catch (ServerConnectivityException se) {
        if (logger.fineEnabled()) {
          logger
              .fine("ExecuteFunctionOp#reexecute : Received ServerConnectivity Exception.");
        }
        
        if(se instanceof ServerOperationException){
          throw se;
        }
        retryAttempts++;
        if (retryAttempts > maxRetryAttempts && maxRetryAttempts != -2) 
          throw se;

        reexecute = true;
        resultCollector.clearResults();
      }
    } while (reexecute);
  }

  static List constructAndGetFunctionTasks(final PoolImpl pool,
      final Function function, Object args,
      MemberMappedArgument memberMappedArg, byte hasResult, ResultCollector rc,
      boolean isFnSerializationReqd, UserAttributes attributes) {
    final List<SingleHopOperationCallable> tasks = new ArrayList<SingleHopOperationCallable>();
    ArrayList<ServerLocation> servers = null;
    if (pool.getLocators() == null || pool.getLocators().isEmpty()) { 
      servers = ((ExplicitConnectionSourceImpl)pool.getConnectionSource())
          .getAllServers();
    }
    else {
      servers = ((AutoConnectionSourceImpl)pool.getConnectionSource())
          .findAllServers(); // n/w call on locator
    }

    for (ServerLocation server : servers) {
      final AbstractOp op = new ExecuteFunctionOpImpl(pool.getLoggerI18n(),
          function, args, memberMappedArg, hasResult, rc, isFnSerializationReqd, (byte)0, null/*onGroups does not use single-hop for now*/, false, false);
      SingleHopOperationCallable task = new SingleHopOperationCallable(server, pool, op, attributes);
      tasks.add(task);
    }
    return tasks;
  }
      
  static List constructAndGetFunctionTasks(final PoolImpl pool,
      final String functionId, Object args,
      MemberMappedArgument memberMappedArg, byte hasResult, ResultCollector rc,
      boolean isFnSerializationReqd, boolean isHA, boolean optimizeForWrite, UserAttributes properties) {
    final List<SingleHopOperationCallable> tasks = new ArrayList<SingleHopOperationCallable>();
    ArrayList<ServerLocation> servers = null;
    if (pool.getLocators() == null || pool.getLocators().isEmpty()) { 
      servers = ((ExplicitConnectionSourceImpl)pool.getConnectionSource())
          .getAllServers();
    }
    else {
      servers = ((AutoConnectionSourceImpl)pool.getConnectionSource())
          .findAllServers(); // n/w call on locator
    }

    for (ServerLocation server : servers) {
      final AbstractOp op = new ExecuteFunctionOpImpl(pool.getLoggerI18n(),
          functionId, args, memberMappedArg, hasResult, rc, isFnSerializationReqd, isHA, optimizeForWrite,(byte)0, null/*onGroups does not use single-hop for now*/, false, false);
      SingleHopOperationCallable task = new SingleHopOperationCallable(server, pool, op, properties);
      tasks.add(task);
    }
    return tasks;
  }
  
  static byte[] getByteArrayForFlags(boolean... flags) {
    byte[] retVal = null;
    if (flags.length > 0) {
      retVal = new byte[flags.length];
      for (int i=0; i<flags.length; i++) {
        if (flags[i]) {
          retVal[i] = 1;
        } else {
          retVal[i] = 0;
        }
      }
    }
    return retVal;
  }

  static class ExecuteFunctionOpImpl extends AbstractOp {

    private ResultCollector resultCollector;
    
   //To get the instance of the Function Statistics we need the function name or instance
    private String functionId;

    private Function function;

    private Object args;

    private MemberMappedArgument memberMappedArg;

    private byte hasResult;

    private boolean isFnSerializationReqd;

    private String[] groups;

    /**
     * [0] = allMembers
     * [1] = ignoreFailedMembers
     */
    private byte[] flags;

    /**
     * number of parts in the request message
     */
    private static final int MSG_PARTS = 6;

    /**
     * @throws com.gemstone.gemfire.SerializationException if serialization fails
     */
    public ExecuteFunctionOpImpl(LogWriterI18n lw, Function function,
        Object args, MemberMappedArgument memberMappedArg,
        byte hasResult, ResultCollector rc, boolean isFnSerializationReqd, byte isReexecute, String[] groups, boolean allMembers, boolean ignoreFailedMembers) {
      super(lw, MessageType.EXECUTE_FUNCTION, MSG_PARTS);
      logger = lw;
      byte fnState = AbstractExecution.getFunctionState(function.isHA(),
          function.hasResult(), function.optimizeForWrite());
      if (isReexecute == 1) {
        getMessage().addBytesPart(new byte[] { AbstractExecution.getReexecuteFunctionState(fnState) });
      } else {
        getMessage().addBytesPart(new byte[] { fnState });
      }
      
      if(isFnSerializationReqd){
        getMessage().addStringOrObjPart(function); 
      }
      else{
        getMessage().addStringOrObjPart(function.getId()); 
      }
      getMessage().addObjPart(args);
      getMessage().addObjPart(memberMappedArg);
      getMessage().addObjPart(groups);
      this.flags = getByteArrayForFlags(allMembers, ignoreFailedMembers);
      getMessage().addBytesPart(this.flags);
      resultCollector = rc;
      if(isReexecute == 1) {
        resultCollector.clearResults();
      }
      this.functionId = function.getId();
      this.function = function;
      this.args = args;
      this.memberMappedArg = memberMappedArg;
      this.hasResult = fnState;
      this.isFnSerializationReqd = isFnSerializationReqd;
      this.groups = groups;
    }

    public ExecuteFunctionOpImpl(LogWriterI18n lw, String functionId,
        Object args2, MemberMappedArgument memberMappedArg,
        byte hasResult, ResultCollector rc, boolean isFnSerializationReqd,
        boolean isHA, boolean optimizeForWrite, byte isReexecute, String[] groups, boolean allMembers, boolean ignoreFailedMembers) {
      super(lw, MessageType.EXECUTE_FUNCTION, MSG_PARTS);
      logger = lw;
      byte fnState = AbstractExecution.getFunctionState(isHA,
          hasResult == (byte)1 ? true : false, optimizeForWrite);
      
      if (isReexecute == 1) {
        getMessage().addBytesPart(new byte[] { AbstractExecution.getReexecuteFunctionState(fnState) });
      } else {
        getMessage().addBytesPart(new byte[] { fnState });
      }
      
      getMessage().addStringOrObjPart(functionId);
      getMessage().addObjPart(args2);
      getMessage().addObjPart(memberMappedArg);
      getMessage().addObjPart(groups);
      this.flags = getByteArrayForFlags(allMembers, ignoreFailedMembers);
      getMessage().addBytesPart(this.flags);
      resultCollector = rc;
      if(isReexecute == 1) {
        resultCollector.clearResults();
      } 
      this.functionId = functionId;
      this.args = args2;
      this.memberMappedArg = memberMappedArg;
      this.hasResult = fnState;
      this.isFnSerializationReqd = isFnSerializationReqd;
      this.groups = groups;
    }

    public ExecuteFunctionOpImpl(ExecuteFunctionOpImpl op, byte isReexecute) {
      super(op.getLogger(), MessageType.EXECUTE_FUNCTION, MSG_PARTS);
      logger = op.getLogger();
      this.resultCollector = op.resultCollector;
      this.function = op.function;
      this.functionId = op.functionId;
      this.hasResult = op.hasResult;
      this.args = op.args;
      this.memberMappedArg = op.memberMappedArg;
      this.isFnSerializationReqd = op.isFnSerializationReqd;
      this.groups = op.groups;
      this.flags = op.flags;
      
      if (isReexecute == 1) {
        getMessage().addBytesPart(new byte[] { AbstractExecution.getReexecuteFunctionState(this.hasResult) });
      } else {
        getMessage().addBytesPart(new byte[] { this.hasResult });
      }
      
      if(this.isFnSerializationReqd){
        getMessage().addStringOrObjPart(function); 
      }
      else{
        getMessage().addStringOrObjPart(function.getId()); 
      }
      getMessage().addObjPart(this.args);
      getMessage().addObjPart(this.memberMappedArg);
      getMessage().addObjPart(this.groups);
      getMessage().addBytesPart(this.flags);
      if(isReexecute == 1) {
        resultCollector.clearResults();
      }
    }

    /**
     * ignoreFaileMember flag is at index 1
     */
    private boolean getIgnoreFailedMembers() {
      boolean ignoreFailedMembers = false;
      if (this.flags != null && this.flags.length > 1) {
        if (this.flags[IGNORE_FAILED_MEMBERS_INDEX] == 1) {
          ignoreFailedMembers = true;
        }
      }
      return ignoreFailedMembers;
    }

    @Override  
    protected Object processResponse(Message msg) throws Exception {      
      ChunkedMessage executeFunctionResponseMsg = (ChunkedMessage)msg;
      try {
        // Read the header which describes the type of message following
        executeFunctionResponseMsg.readHeader();
        switch (executeFunctionResponseMsg.getMessageType()) {
          case MessageType.EXECUTE_FUNCTION_RESULT:
            if (logger.fineEnabled()) {
              logger
                  .fine("ExecuteFunctionOpImpl#processResponse: received message of type EXECUTE_FUNCTION_RESULT.");
            }
            // Read the chunk
            do{
              executeFunctionResponseMsg.receiveChunk();
              Object resultResponse = executeFunctionResponseMsg.getPart(0)
                  .getObject();
              Object result;
              if (resultResponse instanceof ArrayList) {
                result = ((ArrayList)resultResponse).get(0);
              }
              else {
                result = resultResponse;
              }
              if (result instanceof FunctionException) {
                //String s = "While performing a remote " + getOpName();
                FunctionException ex = ((FunctionException)result);
                if (ex instanceof InternalFunctionException || getIgnoreFailedMembers()) {
                  Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                  DistributedMember memberID = (DistributedMember)((ArrayList)resultResponse)
                      .get(1);
                  this.resultCollector.addResult(memberID, cause);
                  FunctionStats.getFunctionStats(this.functionId, null)
                      .incResultsReceived();
                  continue;
                }
                else {
                  throw ex;
                }
              }else if (result instanceof Throwable) {
                String s = "While performing a remote " + getOpName();
                throw new ServerOperationException(s, (Throwable)result);
                // Get the exception toString part.
                // This was added for c++ thin client and not used in java
                //Part exceptionToStringPart = msg.getPart(1);
              }
              else {
                DistributedMember memberID = (DistributedMember)((ArrayList)resultResponse)
                    .get(1);
                synchronized (resultCollector) {
                  resultCollector.addResult(memberID, result);                    
                }
                FunctionStats.getFunctionStats(this.functionId, null)
                    .incResultsReceived();
              }
            }while(!executeFunctionResponseMsg.isLastChunk());
            if (logger.fineEnabled()) {
              logger
                  .fine("ExecuteFunctionOpImpl#processResponse: received all the results from server successfully.");
            }
            return null;          
          case MessageType.EXCEPTION:
            if (logger.fineEnabled()) {
              logger
                  .fine("ExecuteFunctionOpImpl#processResponse: received message of type EXCEPTION");
            }
            // Read the chunk
            executeFunctionResponseMsg.receiveChunk();
            Part part0 = executeFunctionResponseMsg.getPart(0);
            Object obj = part0.getObject();
            if (obj instanceof FunctionException) {
              FunctionException ex = ((FunctionException)obj);
              throw ex;
            }
            else {
              String s = ": While performing a remote execute Function" + ((Throwable)obj).getMessage();
              throw new ServerOperationException(s, (Throwable)obj);              
            }
          case MessageType.EXECUTE_FUNCTION_ERROR:
            if (logger.fineEnabled()) {
              logger
                  .fine("ExecuteFunctionOpImpl#processResponse: received message of type EXECUTE_FUNCTION_ERROR");
            }
            // Read the chunk
            executeFunctionResponseMsg.receiveChunk();
            String errorMessage = executeFunctionResponseMsg.getPart(0)
                .getString();
            throw new ServerOperationException(errorMessage);
          default:
            throw new InternalGemFireError(
                LocalizedStrings.Op_UNKNOWN_MESSAGE_TYPE_0
                  .toLocalizedString(
                     Integer.valueOf(executeFunctionResponseMsg.getMessageType())));
        }
      }
      finally {
        executeFunctionResponseMsg.clear();
      }      
    }

    @Override  
    protected boolean isErrorResponse(int msgType) {
      return msgType == MessageType.EXECUTE_FUNCTION_ERROR;
    }

    @Override  
    protected long startAttempt(ConnectionStats stats) {
      return stats.startExecuteFunction();
    }
    
    protected String getOpName() {
      return "executeFunction";
    }

    @Override  
    protected void endSendAttempt(ConnectionStats stats, long start) {
      stats.endExecuteFunctionSend(start, hasFailed());
    }

    @Override  
    protected void endAttempt(ConnectionStats stats, long start) {
      stats.endExecuteFunction(start, hasTimedOut(), hasFailed());
    }

    @Override  
    protected Message createResponseMessage() {
      return new ChunkedMessage(1, Version.CURRENT_GFE);
    }
  }
  
  public static final int MAX_FE_THREADS = Integer.getInteger(
      "DistributionManager.MAX_FE_THREADS",
      Math.max(Runtime.getRuntime().availableProcessors() * 4, 16)).intValue();
}
