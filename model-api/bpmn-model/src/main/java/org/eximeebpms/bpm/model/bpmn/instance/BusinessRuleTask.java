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

import org.eximeebpms.bpm.model.bpmn.builder.BusinessRuleTaskBuilder;

/**
 * The BPMN businessRuleTask element
 *
 * @author Sebastian Menski
 */
public interface BusinessRuleTask extends Task {

  BusinessRuleTaskBuilder builder();

  String getImplementation();

  void setImplementation(String implementation);

  /** camunda extensions */

  String getEximeeBpmsClass();

  void setEximeeBpmsClass(String camundaClass);

  String getEximeeBpmsDelegateExpression();

  void setEximeeBpmsDelegateExpression(String camundaExpression);

  String getEximeeBpmsExpression();

  void setEximeeBpmsExpression(String camundaExpression);

  String getEximeeBpmsResultVariable();

  void setEximeeBpmsResultVariable(String camundaResultVariable);

  String getEximeeBpmsType();

  void setEximeeBpmsType(String camundaType);

  String getEximeeBpmsTopic();

  void setEximeeBpmsTopic(String camundaTopic);

  String getEximeeBpmsDecisionRef();

  void setEximeeBpmsDecisionRef(String camundaDecisionRef);

  String getEximeeBpmsDecisionRefBinding();

  void setEximeeBpmsDecisionRefBinding(String camundaDecisionRefBinding);

  String getEximeeBpmsDecisionRefVersion();

  void setEximeeBpmsDecisionRefVersion(String camundaDecisionRefVersion);

  String getEximeeBpmsDecisionRefVersionTag();

  void setEximeeBpmsDecisionRefVersionTag(String camundaDecisionRefVersionTag);

  String getEximeeBpmsDecisionRefTenantId();

  void setEximeeBpmsDecisionRefTenantId(String tenantId);

  String getEximeeBpmsMapDecisionResult();

  void setEximeeBpmsMapDecisionResult(String camundaMapDecisionResult);

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
   * @deprecated use {@link #getEximeeBpmsDecisionRef()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaDecisionRef() {
    return getEximeeBpmsDecisionRef();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsDecisionRef(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaDecisionRef(String eximeeBpmsDecisionRef) {
    setEximeeBpmsDecisionRef(eximeeBpmsDecisionRef);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsDecisionRefBinding()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaDecisionRefBinding() {
    return getEximeeBpmsDecisionRefBinding();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsDecisionRefBinding(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaDecisionRefBinding(String eximeeBpmsDecisionRefBinding) {
    setEximeeBpmsDecisionRefBinding(eximeeBpmsDecisionRefBinding);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsDecisionRefVersion()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaDecisionRefVersion() {
    return getEximeeBpmsDecisionRefVersion();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsDecisionRefVersion(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaDecisionRefVersion(String eximeeBpmsDecisionRefVersion) {
    setEximeeBpmsDecisionRefVersion(eximeeBpmsDecisionRefVersion);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsDecisionRefVersionTag()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaDecisionRefVersionTag() {
    return getEximeeBpmsDecisionRefVersionTag();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsDecisionRefVersionTag(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaDecisionRefVersionTag(String eximeeBpmsDecisionRefVersionTag) {
    setEximeeBpmsDecisionRefVersionTag(eximeeBpmsDecisionRefVersionTag);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsDecisionRefTenantId()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaDecisionRefTenantId() {
    return getEximeeBpmsDecisionRefTenantId();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsDecisionRefTenantId(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaDecisionRefTenantId(String tenantId) {
    setEximeeBpmsDecisionRefTenantId(tenantId);
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
