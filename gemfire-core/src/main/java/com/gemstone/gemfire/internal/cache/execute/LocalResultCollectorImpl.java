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
package com.gemstone.gemfire.internal.cache.execute;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.gemstone.gemfire.CancelException;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.internal.ReplyProcessor21;
import com.gemstone.gemfire.internal.cache.DataLocationException;
import com.gemstone.gemfire.internal.cache.ForceReattemptException;
import com.gemstone.gemfire.internal.cache.PrimaryBucketException;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;

public final class LocalResultCollectorImpl implements LocalResultCollector {

  private final ResultCollector userRC;

  private CountDownLatch latch = new CountDownLatch(1);
  
  protected volatile boolean endResultRecieved = false;
  
  private volatile boolean resultCollected = false;
  
  protected volatile boolean resultsCleared = false;
  
  private FunctionException functionException = null;
  
  private Function function = null;
  
  private AbstractExecution execution = null;


  public LocalResultCollectorImpl(Function function, ResultCollector rc,
      Execution execution) {
    this.function = function;
    this.userRC = rc;
    this.execution = (AbstractExecution)execution;
  }

  public synchronized void addResult(DistributedMember memberID,
      Object resultOfSingleExecution) {
    if (resultsCleared) {
      return;
    }
    if (!this.endResultRecieved) {
      if (resultOfSingleExecution instanceof Throwable) {
        Throwable t = (Throwable)resultOfSingleExecution;
        if (this.execution.isIgnoreDepartedMembers()) {
          if(t.getCause() != null){
            t = t.getCause();
          }
          this.userRC.addResult(memberID, t);
        } else {
          if (!(t instanceof InternalFunctionException)) {
            if (this.functionException == null) {
              if (resultOfSingleExecution instanceof FunctionException) {
                this.functionException = (FunctionException)resultOfSingleExecution;
                if (t.getCause() != null) {
                  t = t.getCause();
                }
              }
              // wrap retry kind of exceptions in
              // InternalFunctionInvocationTargetException
              else if (resultOfSingleExecution instanceof ForceReattemptException
                  || resultOfSingleExecution instanceof CancelException
                  || resultOfSingleExecution instanceof BucketMovedException
                  || resultOfSingleExecution instanceof PrimaryBucketException
                  || resultOfSingleExecution instanceof DataLocationException) {
                this.functionException =
                    new InternalFunctionInvocationTargetException(t);
              }
              else {
                this.functionException = new FunctionException(t);
              }
            }
            this.functionException.addException(t);
          }
          else {
            this.userRC.addResult(memberID, t.getCause());
          }
        }
      }
      else {
        this.userRC.addResult(memberID, resultOfSingleExecution);
      }
    }
  }

  public void endResults() {
    this.endResultRecieved = true;
    if (this.userRC != null) { // happening during shutdown??
      this.userRC.endResults();
    }
    this.latch.countDown();
  }

  public synchronized void clearResults() {
    this.latch = new CountDownLatch(1);
    this.endResultRecieved = false;
    this.functionException = null;
    if (this.userRC != null) { // happening during shutdown??
      this.userRC.clearResults();
    }
    resultsCleared = true ;
  }

  public Object getResult() throws FunctionException {
    if (this.resultCollected) {
      throw new FunctionException(
          LocalizedStrings.ExecuteFunction_RESULTS_ALREADY_COLLECTED
              .toLocalizedString());
    }
    this.resultCollected = true;
    try {
      this.latch.await();
    }
    catch (InterruptedException e) {
      this.latch.countDown();
      Thread.currentThread().interrupt();
    }
    this.latch = new CountDownLatch(1);
    if (this.functionException != null && !this.execution.isIgnoreDepartedMembers()) {
      if (this.function.isHA()) {
        if (this.functionException instanceof InternalFunctionInvocationTargetException
            || this.functionException.getCause() instanceof
               InternalFunctionInvocationTargetException) {
          clearResults();
          this.execution = this.execution.setIsReExecute();
          ResultCollector newRc = null;
          if (execution.isFnSerializationReqd()) {
            newRc = this.execution.execute(this.function);
          }
          else {
            newRc = this.execution.execute(this.function.getId());
          }
          return newRc.getResult();
        }
      }
      throw this.functionException;
    }
    else {
      Object result = this.userRC.getResult();
      return result;
    }
  }

  public Object getResult(long timeout, TimeUnit unit)
      throws FunctionException, InterruptedException {

    boolean resultRecieved = false;
    if (this.resultCollected) {
      throw new FunctionException(
          LocalizedStrings.ExecuteFunction_RESULTS_ALREADY_COLLECTED
              .toLocalizedString());
    }
    this.resultCollected = true;
    try {
      resultRecieved = this.latch.await(timeout, unit);
    }
    catch (InterruptedException e) {
      this.latch.countDown();
      Thread.currentThread().interrupt();
    }
    if (!resultRecieved) {
      throw new FunctionException(
          LocalizedStrings.ExecuteFunction_RESULTS_NOT_COLLECTED_IN_TIME_PROVIDED
              .toLocalizedString());
    }
    this.latch = new CountDownLatch(1);
    if (this.functionException != null && !this.execution.isIgnoreDepartedMembers()) {
      if (this.function.isHA()) {
        if (this.functionException instanceof InternalFunctionInvocationTargetException
            || this.functionException.getCause() instanceof
               InternalFunctionInvocationTargetException) {
          clearResults();
          this.execution = this.execution.setIsReExecute();
          ResultCollector newRc = null;
          if (execution.isFnSerializationReqd()) {
            newRc = this.execution.execute(this.function);
          }
          else {
            newRc = this.execution.execute(this.function.getId());
          }
          return newRc.getResult(timeout, unit);
        }
      }
      throw this.functionException;
    }
    else {
      Object result = this.userRC.getResult(timeout, unit);
      return result;
    }
  }

  public void setException(Throwable exception) {
    if (exception instanceof FunctionException) {
      this.functionException = (FunctionException)exception;
    }
    else {
      this.functionException = new FunctionException(exception);
    }
  }

  public ReplyProcessor21 getProcessor() {
    if (this.userRC instanceof InternalResultCollector<?, ?>) {
      return ((InternalResultCollector<?, ?>)this.userRC).getProcessor();
    }
    else {
      return null;
    }
  }

  public void setProcessor(ReplyProcessor21 processor) {
    // nothing to be done here since FunctionStreamingResultCollector that
    // wraps this itself implements ReplyProcessor21 and is returned as
    // the ResultCollector so cannot get GCed
    if (this.userRC instanceof InternalResultCollector<?, ?>) {
      ((InternalResultCollector<?, ?>)this.userRC).setProcessor(processor);
    }
  }
}
