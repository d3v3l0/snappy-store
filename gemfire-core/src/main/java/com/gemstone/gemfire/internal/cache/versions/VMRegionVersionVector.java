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
package com.gemstone.gemfire.internal.cache.versions;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;

/**
 * A region version vector for regions without persistent data. This region
 * version vector uses the InternalDistributedMember as the member id.
 * @author dsmith
 *
 */
public class VMRegionVersionVector extends RegionVersionVector<InternalDistributedMember> {

  /** for deserialization */
  public VMRegionVersionVector() {
    super();
  }

  public VMRegionVersionVector(InternalDistributedMember ownerId) {
    super(ownerId);
  }

  public VMRegionVersionVector(
      InternalDistributedMember ownerId,
      ConcurrentHashMap<InternalDistributedMember, RegionVersionHolder<InternalDistributedMember>> vector,
      long version, ConcurrentHashMap<InternalDistributedMember, Long> gcVersions,
      long gcVersion, boolean singleMember,
      RegionVersionHolder<InternalDistributedMember> localExceptions) {
    super(ownerId, vector, version, gcVersions, gcVersion, singleMember,
        localExceptions);
  }

  @Override
  protected RegionVersionVector createCopy(
      InternalDistributedMember ownerId,
      ConcurrentHashMap<InternalDistributedMember, RegionVersionHolder<InternalDistributedMember>> vector,
      long version, ConcurrentHashMap<InternalDistributedMember, Long> gcVersions,
      long gcVersion, boolean singleMember,
      RegionVersionHolder<InternalDistributedMember> localExceptions) {
    return new VMRegionVersionVector(ownerId, vector, version,
        gcVersions, gcVersion, singleMember, localExceptions);
  }

  @Override
  protected void writeMember(InternalDistributedMember member, DataOutput out) throws IOException {
    member.writeEssentialData(out);
    
  }

  @Override
  protected InternalDistributedMember readMember(DataInput in) throws IOException, ClassNotFoundException {
    return InternalDistributedMember.readEssentialData(in);
  }

  @Override
  public int getDSFID() {
    return REGION_VERSION_VECTOR;
  }
  
  /* (non-Javadoc)
   * @see com.gemstone.gemfire.internal.cache.versions.RegionVersionVector#memberDeparted(com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember, boolean)
   */
  @Override
  public void memberDeparted(final InternalDistributedMember id, boolean crashed) {
    super.memberDeparted(id, crashed);
    removeOldMember(id);
  }
  
  /**
   * remove an old member from the vector
   */
  private void removeOldMember(InternalDistributedMember id) {
    super.markDepartedMember(id);
  }
  
}
