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
package sql.loader;

import java.sql.SQLException;

import com.pivotal.gemfirexd.callbacks.RowLoader;


public class PortfolioLoader implements RowLoader {

	public Object getRow(String schemaName,
			String tableName, Object[] primarykey) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

  public void init(String initStr) throws SQLException {
    // TODO Auto-generated method stub
    
  }

}
