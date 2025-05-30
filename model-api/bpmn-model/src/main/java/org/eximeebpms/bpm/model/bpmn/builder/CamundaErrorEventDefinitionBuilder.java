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
package org.eximeebpms.bpm.model.bpmn.builder;

import org.eximeebpms.bpm.model.bpmn.BpmnModelInstance;
import org.eximeebpms.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.eximeebpms.bpm.model.bpmn.instance.ExtensionElements;
import org.eximeebpms.bpm.model.bpmn.instance.ServiceTask;

public class CamundaErrorEventDefinitionBuilder extends AbstractErrorEventDefinitionBuilder<CamundaErrorEventDefinitionBuilder> {

  public CamundaErrorEventDefinitionBuilder(BpmnModelInstance modelInstance, ErrorEventDefinition element) {
    super(modelInstance, element, CamundaErrorEventDefinitionBuilder.class);
  }

  public CamundaErrorEventDefinitionBuilder expression(String expression) {
    element.setAttributeValue("expression", expression);
    return myself;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public AbstractServiceTaskBuilder errorEventDefinitionDone() {
    return ((ServiceTask)((ExtensionElements) element.getParentElement()).getParentElement()).builder();
  }
}
