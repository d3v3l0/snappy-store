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
package quickstart;

import junit.framework.TestCase;

/**
 * Unit test basic functionality in ProcessWrapper.
 * 
 * @author Kirk Lund
 */
public class ProcessWrapperJUnitTest extends TestCase {

  private static final String OUTPUT_OF_MAIN = "Executing ProcessWrapperJUnitTest main";
  private ProcessWrapper process;
  
  public ProcessWrapperJUnitTest(String name) {
    super(name);
  }

  @Override
  public void setUp() {
  }
  
  @Override
  public void tearDown() {
    if (this.process != null) {
      this.process.destroy();
    }
  }
  
  public void testInvokeWithNullArgs() throws Exception {
    this.process = new ProcessWrapper(getClass());
    this.process.execute();
    this.process.waitFor();
    assertTrue(process.getOutput().contains(OUTPUT_OF_MAIN));
  }

  public static void main(String... args) throws Exception {
    System.out.println(OUTPUT_OF_MAIN);
  }
}
