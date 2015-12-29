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
package com.gemstone.gemfire.management.internal.cli.result;

import com.gemstone.gemfire.management.cli.Result.Status;
import com.gemstone.gemfire.management.internal.cli.json.GfJsonObject;

/**
 * 
 * @author Abhishek Chaudhari
 * 
 * @since 7.0
 */
public interface ResultData {
  String RESULT_HEADER  = "header";
  String RESULT_CONTENT = "content";
  String RESULT_FOOTER  = "footer";
  

//  String TYPE_CATALOGED = "catalog";
  String TYPE_COMPOSITE = "composite";
  String TYPE_ERROR     = "error";
  String TYPE_INFO      = "info";
  String TYPE_OBJECT    = "object";
//  String TYPE_SECTION   = "composite-section";
  String TYPE_TABULAR   = "table";
  
  String getHeader();
  
  String getFooter();
  
  GfJsonObject getGfJsonObject();
  
  String getType();
  
  public Status getStatus();
  
  public void setStatus(final Status status);
}
