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
* The BPMN messageEventDefinition element
*
* @author Sebastian Menski
*
*/
public interface MessageEventDefinition extends EventDefinition {

  Message getMessage();

  void setMessage(Message message);

  Operation getOperation();

  void setOperation(Operation operation);

  /** camunda extensions */

  String getEximeeBpmsClass();

  void setEximeeBpmsClass(String camundaClass);

  String getEximeeBpmsDelegateExpression();

  void setEximeeBpmsDelegateExpression(String camundaExpression);

  String getEximeeBpmsExpression();

  void setEximeeBpmsExpression(String camundaExpression);

  String getEximeeBpmsResultVariable();

  void setEximeeBpmsResultVariable(String camundaResultVariable);

  String getEximeeBpmsTopic();

  void setEximeeBpmsTopic(String camundaTopic);

  String getEximeeBpmsType();

  void setEximeeBpmsType(String camundaType);
  
  String getEximeeBpmsTaskPriority();
  
  void setEximeeBpmsTaskPriority(String taskPriority);

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

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
  default void setCamundaDelegateExpression(String eximeeBpmsExpression) {
    setEximeeBpmsDelegateExpression(eximeeBpmsExpression);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsResultVariable()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaResultVariable() {
    return getEximeeBpmsResultVariable();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsResultVariable(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaResultVariable(String eximeeBpmsResultVariable) {
    setEximeeBpmsResultVariable(eximeeBpmsResultVariable);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsTopic()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaTopic() {
    return getEximeeBpmsTopic();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsTopic(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaTopic(String eximeeBpmsTopic) {
    setEximeeBpmsTopic(eximeeBpmsTopic);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsType()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaType() {
    return getEximeeBpmsType();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsType(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaType(String eximeeBpmsType) {
    setEximeeBpmsType(eximeeBpmsType);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsTaskPriority()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaTaskPriority() {
    return getEximeeBpmsTaskPriority();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsTaskPriority(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaTaskPriority(String taskPriority) {
    setEximeeBpmsTaskPriority(taskPriority);
  }
}
