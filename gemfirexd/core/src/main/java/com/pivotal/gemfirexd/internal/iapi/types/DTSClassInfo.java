/*

   Derby - Class com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package com.pivotal.gemfirexd.internal.iapi.types;

import com.pivotal.gemfirexd.internal.iapi.services.io.FormatableInstanceGetter;

public class DTSClassInfo extends FormatableInstanceGetter {

        public Object getNewInstance() {
        	
        	// Does not handle StoredFormatIds.SQL_DECIMAL_ID as different
        	// implementations are required for different VMs.

        	//The format id for DVDs are handled first.  
        	Object returnObject = DataValueFactoryImpl.getNullDVDWithUCS_BASICcollation(fmtId);
        	return returnObject;


        }
}
