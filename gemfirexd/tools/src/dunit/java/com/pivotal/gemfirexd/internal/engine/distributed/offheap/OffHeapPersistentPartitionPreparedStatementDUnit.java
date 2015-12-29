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
package com.pivotal.gemfirexd.internal.engine.distributed.offheap;

import com.pivotal.gemfirexd.internal.engine.distributed.PersistentPartitionPreparedStatementDUnit;

@SuppressWarnings("serial")
public class OffHeapPersistentPartitionPreparedStatementDUnit extends PersistentPartitionPreparedStatementDUnit{
 
  public OffHeapPersistentPartitionPreparedStatementDUnit(String name) {
    super(name);
  }
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.configureDefaultOffHeap(true);  
  }

  @Override
  public String getSuffix() throws Exception {
    String superSuffix = super.getSuffix();
    String suffix =  superSuffix + " offheap ";
    return suffix;
  }
}
