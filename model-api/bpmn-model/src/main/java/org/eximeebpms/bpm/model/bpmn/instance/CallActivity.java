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

import org.eximeebpms.bpm.model.bpmn.builder.CallActivityBuilder;

/**
 * The BPMN callActivity element
 *
 * @author Sebastian Menski
 */
public interface CallActivity extends Activity {

  CallActivityBuilder builder();

  String getCalledElement();

  void setCalledElement(String calledElement);

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

  String getEximeeBpmsCalledElementBinding();

  void setEximeeBpmsCalledElementBinding(String camundaCalledElementBinding);

  String getEximeeBpmsCalledElementVersion();

  void setEximeeBpmsCalledElementVersion(String camundaCalledElementVersion);

  String getEximeeBpmsCalledElementVersionTag();

  void setEximeeBpmsCalledElementVersionTag(String camundaCalledElementVersionTag);

  String getEximeeBpmsCaseRef();

  void setEximeeBpmsCaseRef(String camundaCaseRef);

  String getEximeeBpmsCaseBinding();

  void setEximeeBpmsCaseBinding(String camundaCaseBinding);

  String getEximeeBpmsCaseVersion();

  void setEximeeBpmsCaseVersion(String camundaCaseVersion);

  String getEximeeBpmsCalledElementTenantId();

  void setEximeeBpmsCalledElementTenantId(String tenantId);

  String getEximeeBpmsCaseTenantId();

  void setEximeeBpmsCaseTenantId(String tenantId);

  String getEximeeBpmsVariableMappingClass();

  void setEximeeBpmsVariableMappingClass(String camundaClass);

  String getEximeeBpmsVariableMappingDelegateExpression();

  void setEximeeBpmsVariableMappingDelegateExpression(String camundaExpression);

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
   * @deprecated use {@link #getEximeeBpmsCalledElementBinding()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCalledElementBinding() {
    return getEximeeBpmsCalledElementBinding();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCalledElementBinding(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCalledElementBinding(String eximeeBpmsCalledElementBinding) {
    setEximeeBpmsCalledElementBinding(eximeeBpmsCalledElementBinding);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCalledElementVersion()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCalledElementVersion() {
    return getEximeeBpmsCalledElementVersion();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCalledElementVersion(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCalledElementVersion(String eximeeBpmsCalledElementVersion) {
    setEximeeBpmsCalledElementVersion(eximeeBpmsCalledElementVersion);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCalledElementVersionTag()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCalledElementVersionTag() {
    return getEximeeBpmsCalledElementVersionTag();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCalledElementVersionTag(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCalledElementVersionTag(String eximeeBpmsCalledElementVersionTag) {
    setEximeeBpmsCalledElementVersionTag(eximeeBpmsCalledElementVersionTag);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCaseRef()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCaseRef() {
    return getEximeeBpmsCaseRef();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCaseRef(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCaseRef(String eximeeBpmsCaseRef) {
    setEximeeBpmsCaseRef(eximeeBpmsCaseRef);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCaseBinding()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCaseBinding() {
    return getEximeeBpmsCaseBinding();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCaseBinding(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCaseBinding(String eximeeBpmsCaseBinding) {
    setEximeeBpmsCaseBinding(eximeeBpmsCaseBinding);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCaseVersion()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCaseVersion() {
    return getEximeeBpmsCaseVersion();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCaseVersion(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCaseVersion(String eximeeBpmsCaseVersion) {
    setEximeeBpmsCaseVersion(eximeeBpmsCaseVersion);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCalledElementTenantId()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCalledElementTenantId() {
    return getEximeeBpmsCalledElementTenantId();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCalledElementTenantId(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCalledElementTenantId(String tenantId) {
    setEximeeBpmsCalledElementTenantId(tenantId);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCaseTenantId()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCaseTenantId() {
    return getEximeeBpmsCaseTenantId();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCaseTenantId(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCaseTenantId(String tenantId) {
    setEximeeBpmsCaseTenantId(tenantId);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsVariableMappingClass()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaVariableMappingClass() {
    return getEximeeBpmsVariableMappingClass();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsVariableMappingClass(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaVariableMappingClass(String eximeeBpmsClass) {
    setEximeeBpmsVariableMappingClass(eximeeBpmsClass);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsVariableMappingDelegateExpression()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaVariableMappingDelegateExpression() {
    return getEximeeBpmsVariableMappingDelegateExpression();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsVariableMappingDelegateExpression(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaVariableMappingDelegateExpression(String eximeeBpmsExpression) {
    setEximeeBpmsVariableMappingDelegateExpression(eximeeBpmsExpression);
  }
}
