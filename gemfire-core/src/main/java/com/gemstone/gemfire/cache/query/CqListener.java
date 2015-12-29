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

package com.gemstone.gemfire.cache.query;

import com.gemstone.gemfire.cache.CacheCallback;

/**
 * Application plug-in interface for handling continuous query events after 
 * they occur. The listener has two methods, one that is called when there 
 * is an event satisfied by the CQ and the other called when there is an 
 * error during CQ processing. 
 *
 * @author Anil 
 * @since 5.5
 */

public interface CqListener extends CacheCallback {
  
 /**
   * This method is invoked when an event is occurred on the region
   * that satisfied the query condition of this CQ.
   * This event does not contain an error.
   * If CQ is executed using ExecuteWithInitialResults the returned
   * result may already include the changes with respect to this event. 
   * This could arise when updates are happening on the region while
   * CQ registration is in progress. The CQ does not block any region 
   * operation as it could affect the performance of region operation. 
   * Its up to the application to synchronize between the region 
   * operation and CQ registration to avoid duplicate event being 
   * delivered.   
   * 
   * @see com.gemstone.gemfire.cache.query.CqQuery#executeWithInitialResults
   */
  public void onEvent(CqEvent aCqEvent);

  /** 
   * This method is invoked when there is an error during CQ processing.  
   * The error can appear while applying query condition on the event.
   * e.g if the event doesn't has attributes as specified in the CQ query.
   * This event does contain an error. The newValue may or may not be 
   * available, and will be null if not available.
   */
  public void onError(CqEvent aCqEvent);
}
