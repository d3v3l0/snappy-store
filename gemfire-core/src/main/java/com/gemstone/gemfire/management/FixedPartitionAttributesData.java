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
package com.gemstone.gemfire.management;

import java.beans.ConstructorProperties;

import com.gemstone.gemfire.cache.Region;

/**
 * Composite date type used to distribute the fixed partition attributes for
 * a {@link Region}.
 * 
 * @author rishim
 * @since 7.0
 */
public class FixedPartitionAttributesData {

  /**
   * Name of the Fixed partition
   */
  private String name;

  /**
   * whether this is the primary partition
   */
  private boolean primary;

  /**
   * Number of buckets in the partition
   */
  private int numBucket;

  @ConstructorProperties( { "name", "primary", "numBucket"

  })
  public FixedPartitionAttributesData(String name, boolean primary,
      int numBucket) {
    this.name = name;
    this.primary = primary;
    this.numBucket = numBucket;
  }

  /**
   * Returns the name of the partition.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns whether this member is the primary for the partition.
   * 
   * @return True if this member is the primary, false otherwise.
   */
  public boolean isPrimary() {
    return primary;
  }

  /**
   * Returns the number of buckets allowed for the partition.
   */
  public int getNumBucket() {
    return numBucket;
  }

  @Override
  public boolean equals(Object anObject) {
    if (this == anObject) {
      return true;
    }
    if (anObject instanceof FixedPartitionAttributesData) {
      FixedPartitionAttributesData anotherFprAttrData = (FixedPartitionAttributesData) anObject;
      if (anotherFprAttrData.name.equals(this.name)
          && anotherFprAttrData.primary == this.primary
          && anotherFprAttrData.numBucket == (this.numBucket)) {

        return true;
      }

    }
    return false;
  }

  @Override
  public String toString() {
    return "FixedPartitionAttributesData [name=" + name + ", numBucket="
        + numBucket + ", primary=" + primary + "]";
  }


}
