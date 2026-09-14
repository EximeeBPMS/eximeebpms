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
package org.eximeebpms.bpm.model.bpmn.instance.eximeebpms;

import java.util.Collection;

import org.eximeebpms.bpm.model.bpmn.instance.BpmnModelElementInstance;

/**
 * The BPMN executionListener camunda extension element
 *
 * @author Sebastian Menski
 */
public interface EximeeBpmsExecutionListener extends BpmnModelElementInstance {

  String getEximeeBpmsEvent();

  void setEximeeBpmsEvent(String camundaEvent);

  String getEximeeBpmsClass();

  void setEximeeBpmsClass(String camundaClass);

  String getEximeeBpmsExpression();

  void setEximeeBpmsExpression(String camundaExpression);

  String getEximeeBpmsDelegateExpression();

  void setEximeeBpmsDelegateExpression(String camundaDelegateExpression);

  Collection<EximeeBpmsField> getEximeeBpmsFields();

  EximeeBpmsScript getEximeeBpmsScript();

  void setEximeeBpmsScript(EximeeBpmsScript camundaScript);

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #getEximeeBpmsEvent()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaEvent() {
    return getEximeeBpmsEvent();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsEvent(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaEvent(String eximeeBpmsEvent) {
    setEximeeBpmsEvent(eximeeBpmsEvent);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsClass()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaClass() {
    return getEximeeBpmsClass();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsClass(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaClass(String eximeeBpmsClass) {
    setEximeeBpmsClass(eximeeBpmsClass);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsDelegateExpression()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaDelegateExpression() {
    return getEximeeBpmsDelegateExpression();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsDelegateExpression(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaDelegateExpression(String eximeeBpmsDelegateExpression) {
    setEximeeBpmsDelegateExpression(eximeeBpmsDelegateExpression);
  }
}
