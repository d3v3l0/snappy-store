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
package com.gemstone.gemfire.cache.snapshot;

import java.io.Serializable;

/**
 * Provides a way to configure the behavior of snapshot operations.  The default
 * options are:
 * <dl>
 *  <dt>filter</dt>
 *  <dd>null</dd>
 * </dl>
 * 
 * @param <K> the cache entry key type
 * @param <V> the cache entry value type
 * 
 * @since 7.0
 * @author bakera
 */
public interface SnapshotOptions<K, V> extends Serializable {
  /**
   * Defines the available snapshot file formats.
   * 
   * @since 7.0
   */
  public enum SnapshotFormat {
    /** an optimized binary format specific to GemFire */
    GEMFIRE
  }
  
  /**
   * Sets a filter to apply to snapshot entries.  Entries that are accepted by 
   * the filter will be included in import and export operations.
   * 
   * @param filter the filter to apply, or null to remove the filter
   * @return the snapshot options
   */
  SnapshotOptions<K, V> setFilter(SnapshotFilter<K, V> filter);
  
  /**
   * Returns the filter to be applied to snapshot entries.  Entries that are 
   * accepted by the filter will be included in import and export operations.
   * 
   * @return the filter, or null if the filter is not set
   */
  SnapshotFilter<K, V> getFilter();
}
