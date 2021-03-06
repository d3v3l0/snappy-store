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
package com.gemstone.gemfire.management.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.gemstone.gemfire.management.internal.cli.CliAroundInterceptor;
import com.gemstone.gemfire.management.internal.cli.i18n.CliStrings;

/**
 * An annotation to define additional meta-data for commands.
 *
 * @author Abhishek Chaudhari
 *
 * @since 7.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface CliMetaData {

  /** Represents a null value in a parameter to a command **/
  public static final String ANNOTATION_NULL_VALUE = "__NULL__";

  /**
   * Indicates that the command will only run in the gfsh shell and will not
   * need the management service
   **/
  boolean shellOnly() default false;

  /** Indicates that the effect of the command is persisted or the commands affects the persistent configuration */
  boolean isPersisted() default false;
  
  /** In help, topics that are related to this command **/
  String[] relatedTopic() default CliStrings.DEFAULT_TOPIC_GEMFIRE;

  /**
   * The fully qualified name of a class which implements the
   * {@link CliAroundInterceptor} interface in order to provide additional pre-
   * and post-execution functionality for a command.
   */
  String interceptor() default CliMetaData.ANNOTATION_NULL_VALUE;

  /**
   * String used as a separator when multiple values for a command are specified
   */
  String valueSeparator() default CliMetaData.ANNOTATION_NULL_VALUE;


  // TODO - Abhishek - refactor to group this
//  /**
//   * @author Abhishek Chaudhari
//   *
//   * @since 7.5
//   */
//  @Retention(RetentionPolicy.RUNTIME)
//  @Target({ ElementType.PARAMETER })
//  public @interface ParameterMetadata {
//    /**
//     * String used as a separator when multiple values for a command are specified
//     */
//    String valueSeparator() default CliMetaData.ANNOTATION_NULL_VALUE;
//  }

  /**
   * An annotation to define additional meta-data for availability of commands.
   * 
   * @author Abhishek Chaudhari
   *
   * @since 7.5
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.METHOD })
  public @interface AvailabilityMetadata {
    /**
     * String describing the availability condition.
     */
    String availabilityDescription() default CliMetaData.ANNOTATION_NULL_VALUE;
  }

}
