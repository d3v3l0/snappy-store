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

package com.gemstone.gemfire.internal.admin;

//import java.util.List;

/**
 * Interface to represent one statistic resource
 *
 * @author Darrel Schneider
 * @author Kirk Lund
 */
public interface StatResource extends GfObject {
  
  public long getResourceID();
  public long getResourceUniqueID();
  public String getSystemName();
  public GemFireVM getGemFireVM();
  public Stat[] getStats();
  public Stat getStatByName(String name);
  public String getName();
  
  /**
   * @return the full description of this statistic resource
   */
  public String getDescription();
}
