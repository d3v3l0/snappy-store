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
package com.gemstone.gemfire.management.internal.cli.util;

import com.gemstone.gemfire.GemFireException;

/**
 * The DiskStoreNotFoundException is a GemFireException class indicating that a disk store by name could not be found
 * on a member specified by name!
 * </p>
 * @author jblum
 * @see com.gemstone.gemfire.GemFireException
 * @since 7.0
 */
// TODO this GemFireException should be moved to a more appropriate package!
  @SuppressWarnings("unused")
public class DiskStoreNotFoundException extends GemFireException {

  public DiskStoreNotFoundException() {
  }

  public DiskStoreNotFoundException(final String message) {
    super(message);
  }

  public DiskStoreNotFoundException(final Throwable cause) {
    super(cause);
  }

  public DiskStoreNotFoundException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
