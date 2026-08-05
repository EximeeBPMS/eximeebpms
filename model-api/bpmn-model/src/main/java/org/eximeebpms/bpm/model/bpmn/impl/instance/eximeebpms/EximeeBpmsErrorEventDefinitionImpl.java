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
package org.eximeebpms.bpm.model.bpmn.impl.instance.eximeebpms;

import org.eximeebpms.bpm.model.bpmn.impl.instance.ErrorEventDefinitionImpl;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsErrorEventDefinition;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.attribute.Attribute;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_EXPRESSION;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ELEMENT_ERROR_EVENT_DEFINITION;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;
import static org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

public class EximeeBpmsErrorEventDefinitionImpl extends ErrorEventDefinitionImpl implements EximeeBpmsErrorEventDefinition {

  protected static Attribute<String> camundaExpressionAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(EximeeBpmsErrorEventDefinition.class, CAMUNDA_ELEMENT_ERROR_EVENT_DEFINITION)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(EximeeBpmsErrorEventDefinitionImpl::new);

    camundaExpressionAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_EXPRESSION)
        .namespace(CAMUNDA_NS)
        .build();

    typeBuilder.build();
  }

  public EximeeBpmsErrorEventDefinitionImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getEximeeBpmsExpression() {
    return camundaExpressionAttribute.getValue(this);
  }

  public void setEximeeBpmsExpression(String camundaExpression) {
    camundaExpressionAttribute.setValue(this, camundaExpression);
  }
}
