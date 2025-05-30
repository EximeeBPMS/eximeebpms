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
   * @deprecated use isCamundaAsyncBefore() instead.
   */
  @Deprecated
  boolean isCamundaAsync();

  /**
   * @deprecated use setCamundaAsyncBefore(isCamundaAsyncBefore) instead.
   */
  @Deprecated
  void setCamundaAsync(boolean isCamundaAsync);

  String getCamundaCalledElementBinding();

  void setCamundaCalledElementBinding(String camundaCalledElementBinding);

  String getCamundaCalledElementVersion();

  void setCamundaCalledElementVersion(String camundaCalledElementVersion);

  String getCamundaCalledElementVersionTag();

  void setCamundaCalledElementVersionTag(String camundaCalledElementVersionTag);

  String getCamundaCaseRef();

  void setCamundaCaseRef(String camundaCaseRef);

  String getCamundaCaseBinding();

  void setCamundaCaseBinding(String camundaCaseBinding);

  String getCamundaCaseVersion();

  void setCamundaCaseVersion(String camundaCaseVersion);

  String getCamundaCalledElementTenantId();

  void setCamundaCalledElementTenantId(String tenantId);

  String getCamundaCaseTenantId();

  void setCamundaCaseTenantId(String tenantId);

  String getCamundaVariableMappingClass();

  void setCamundaVariableMappingClass(String camundaClass);

  String getCamundaVariableMappingDelegateExpression();

  void setCamundaVariableMappingDelegateExpression(String camundaExpression);

}
