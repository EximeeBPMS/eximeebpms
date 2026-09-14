/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.eximeebpms.bpm.model.bpmn.instance;

import org.eximeebpms.bpm.model.bpmn.builder.StartEventBuilder;

/**
 * The BPMN startEvent element
 *
 * @author Sebastian Menski
 *
 */
public interface StartEvent extends CatchEvent {

  StartEventBuilder builder();

  boolean isInterrupting();

  void setInterrupting(boolean isInterrupting);

  /** camunda extensions */

  /**
   * @deprecated use isEximeeBpmsAsyncBefore() instead.
   */
  @Deprecated
  boolean isEximeeBpmsAsync();

  /**
   * @deprecated use setEximeeBpmsAsyncBefore(isCamundaAsyncBefore) instead.
   */
  @Deprecated
  void setEximeeBpmsAsync(boolean isCamundaAsync);

  String getEximeeBpmsFormHandlerClass();

  void setEximeeBpmsFormHandlerClass(String camundaFormHandlerClass);

  String getEximeeBpmsFormKey();

  void setEximeeBpmsFormKey(String camundaFormKey);

  String getEximeeBpmsFormRef();

  void setEximeeBpmsFormRef(String camundaFormRef);

  String getEximeeBpmsFormRefBinding();

  void setEximeeBpmsFormRefBinding(String camundaFormRefBinding);

  String getEximeeBpmsFormRefVersion();

  void setEximeeBpmsFormRefVersion(String camundaFormRefVersion);

  String getEximeeBpmsInitiator();

  void setEximeeBpmsInitiator(String camundaInitiator);

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #isEximeeBpmsAsync()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default boolean isCamundaAsync() {
    return isEximeeBpmsAsync();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsAsync(boolean)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaAsync(boolean isEximeeBpmsAsync) {
    setEximeeBpmsAsync(isEximeeBpmsAsync);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsFormHandlerClass()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaFormHandlerClass() {
    return getEximeeBpmsFormHandlerClass();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsFormHandlerClass(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaFormHandlerClass(String eximeeBpmsFormHandlerClass) {
    setEximeeBpmsFormHandlerClass(eximeeBpmsFormHandlerClass);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsFormKey()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaFormKey() {
    return getEximeeBpmsFormKey();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsFormKey(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaFormKey(String eximeeBpmsFormKey) {
    setEximeeBpmsFormKey(eximeeBpmsFormKey);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsInitiator()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaInitiator() {
    return getEximeeBpmsInitiator();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsInitiator(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaInitiator(String eximeeBpmsInitiator) {
    setEximeeBpmsInitiator(eximeeBpmsInitiator);
  }
}
