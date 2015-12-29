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
package com.gemstone.gemfire.management.internal.web.domain;

import java.io.Serializable;
import javax.management.ObjectName;
import javax.management.QueryExp;

/**
 * The QueryParameterSource class encapsulates details in order to perform a query on an JMX MBean server.
 * <p/>
 * @author John Blum
 * @see java.io.Serializable
 * @see javax.management.ObjectName
 * @see javax.management.QueryExp
 * @since 7.5
 */
@SuppressWarnings("unused")
public class QueryParameterSource implements Serializable {

  private static final long serialVersionUID = 34131123582155l;

  private final ObjectName objectName;

  private final QueryExp queryExpression;

  public QueryParameterSource(final ObjectName objectName, final QueryExp queryExpression) {
    this.objectName = objectName;
    this.queryExpression = queryExpression;
  }

  public ObjectName getObjectName() {
    return objectName;
  }

  public QueryExp getQueryExpression() {
    return queryExpression;
  }

  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder("{");
    buffer.append("{ objectName = ").append(String.valueOf(getObjectName()));
    buffer.append(", queryExpression = ").append(String.valueOf(getQueryExpression()));
    buffer.append(" }");
    return buffer.toString();
  }

}
