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

package com.gemstone.gemfire.distributed.internal.locks;

import java.util.*;
import com.gemstone.gemfire.distributed.internal.membership.*;

/**
 * Collection of distributed locks to be processed as a batch. 
 *
 * @author Kirk Lund
 */
public interface DLockBatch {
  
  /** Returns the originator (owner) of this batch of locks */
  public InternalDistributedMember getOwner();
  
  /** Returns the identity object of the batch; like a hash map key */
  public DLockBatchId getBatchId();
  
  /** Returns the list of TXRegionLockRequest instances */
  public List getReqs();
  
  /** 
   * Specifies the lock grantor id that granted this lock batch.
   * @param lockGrantorId the lock grantor id that granted this lock batch
   */
  public void grantedBy(LockGrantorId lockGrantorId);
  
}

