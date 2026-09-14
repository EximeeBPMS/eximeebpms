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

/**
 * The BPMN errorEventDefinition element
 *
 * @author Sebastian Menski
 */
public interface ErrorEventDefinition extends EventDefinition {

  Error getError();

  void setError(Error error);

  void setEximeeBpmsErrorCodeVariable(String camundaErrorCodeVariable);
  
  String getEximeeBpmsErrorCodeVariable();

  void setEximeeBpmsErrorMessageVariable(String camundaErrorCauseVariable);
  
  String getEximeeBpmsErrorMessageVariable();

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #setEximeeBpmsErrorCodeVariable(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaErrorCodeVariable(String eximeeBpmsErrorCodeVariable) {
    setEximeeBpmsErrorCodeVariable(eximeeBpmsErrorCodeVariable);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsErrorCodeVariable()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaErrorCodeVariable() {
    return getEximeeBpmsErrorCodeVariable();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsErrorMessageVariable(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaErrorMessageVariable(String eximeeBpmsErrorCauseVariable) {
    setEximeeBpmsErrorMessageVariable(eximeeBpmsErrorCauseVariable);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsErrorMessageVariable()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaErrorMessageVariable() {
    return getEximeeBpmsErrorMessageVariable();
  }
}
