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

}
