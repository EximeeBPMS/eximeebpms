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

import java.util.List;

/**
 * The BPMN conditionalEventDefinition element
 *
 * @author Sebastian Menski
 */
public interface ConditionalEventDefinition extends EventDefinition {

  Condition getCondition();

  void setCondition(Condition condition);

  String getEximeeBpmsVariableName();

  void setEximeeBpmsVariableName(String variableName);

  String getEximeeBpmsVariableEvents();

  void setEximeeBpmsVariableEvents(String variableEvent);

  List<String> getEximeeBpmsVariableEventsList();

  void setEximeeBpmsVariableEventsList(List<String> variableEventsList);

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #getEximeeBpmsVariableName()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaVariableName() {
    return getEximeeBpmsVariableName();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsVariableName(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaVariableName(String variableName) {
    setEximeeBpmsVariableName(variableName);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsVariableEvents()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaVariableEvents() {
    return getEximeeBpmsVariableEvents();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsVariableEvents(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaVariableEvents(String variableEvent) {
    setEximeeBpmsVariableEvents(variableEvent);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsVariableEventsList()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default List<String> getCamundaVariableEventsList() {
    return getEximeeBpmsVariableEventsList();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsVariableEventsList(List<String>)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaVariableEventsList(List<String> variableEventsList) {
    setEximeeBpmsVariableEventsList(variableEventsList);
  }
}
