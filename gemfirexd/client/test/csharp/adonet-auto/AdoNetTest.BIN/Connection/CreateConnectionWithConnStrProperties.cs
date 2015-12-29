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
 
using System;
using System.Data.Common;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using Pivotal.Data.GemFireXD;

namespace AdoNetTest.BIN.Connection
{
    class CreateConnectionWithConnStrProperties : GFXDTest
    {
        public CreateConnectionWithConnStrProperties(ManualResetEvent resetEvent)
            : base(resetEvent)
        {
        }

        public override void Run(Object context)
        {            
            GFXDClientConnection conn;
            String connStr = String.Format("{0}",
                Configuration.GFXDConfigManager.GetGFXDServerConnectionString());

            try
            {
                conn = new GFXDClientConnection(connStr);
                conn.Open();

                if (conn.State != System.Data.ConnectionState.Open)
                {
                    Fail(String.Format("Invalid connection state. "
                        + "Expected [{0}]; Actual [{1}]",
                        System.Data.ConnectionState.Open, conn.State));
                }
            }
            catch (DbException e)
            {
                Fail(e);
            }
            finally
            {
                
                base.Run(context);
            }
        }
    }
}
