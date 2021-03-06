/* Copyright 2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentracing.contrib.specialagent;

import java.io.Serializable;

/**
 * The class used for RPC between the AgentRunner's main process, and its
 * spawned child process.
 *
 * @author Seva Safris
 */
class TestResult implements Serializable {
  private static final long serialVersionUID = -5513350610214154919L;

  private final String methodName;
  private final Throwable targetException;

  /**
   * Creates a new {@code TestResult} for the specified method and exception.
   *
   * @param methodName The name of the method.
   * @param targetException The target exception.
   */
  TestResult(final String methodName, final Throwable targetException) {
    this.methodName = methodName;
    this.targetException = targetException;
  }

  /**
   * @return The name of the method represented by this {@code TestResult}.
   */
  String getMethodName() {
    return this.methodName;
  }

  /**
   * @return The target exception represented by this {@code TestResult}.
   */
  Throwable getTargetException() {
    return this.targetException;
  }

  @Override
  public String toString() {
    return "{\"methodName\": \"" + methodName + "\", \"targetException\": " + (targetException == null ? "null" : targetException.getClass().getName()) + "}";
  }
}