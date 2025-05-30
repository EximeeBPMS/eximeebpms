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

import org.eximeebpms.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.CamundaIn;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.attribute.Attribute;

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN in camunda extension element
 *
 * @author Sebastian Menski
 */
public class CamundaInImpl extends BpmnModelElementInstanceImpl implements CamundaIn {

  protected static Attribute<String> camundaSourceAttribute;
  protected static Attribute<String> camundaSourceExpressionAttribute;
  protected static Attribute<String> camundaVariablesAttribute;
  protected static Attribute<String> camundaTargetAttribute;
  protected static Attribute<String> camundaBusinessKeyAttribute;
  protected static Attribute<Boolean> camundaLocalAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaIn.class, CAMUNDA_ELEMENT_IN)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaIn>() {
        public CamundaIn newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaInImpl(instanceContext);
        }
      });

    camundaSourceAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_SOURCE)
      .namespace(CAMUNDA_NS)
      .build();

    camundaSourceExpressionAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_SOURCE_EXPRESSION)
      .namespace(CAMUNDA_NS)
      .build();

    camundaVariablesAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_VARIABLES)
      .namespace(CAMUNDA_NS)
      .build();

    camundaTargetAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_TARGET)
      .namespace(CAMUNDA_NS)
      .build();

    camundaBusinessKeyAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_BUSINESS_KEY)
      .namespace(CAMUNDA_NS)
      .build();

    camundaLocalAttribute = typeBuilder.booleanAttribute(CAMUNDA_ATTRIBUTE_LOCAL)
      .namespace(CAMUNDA_NS)
      .build();

    typeBuilder.build();
  }

  public CamundaInImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getCamundaSource() {
    return camundaSourceAttribute.getValue(this);
  }

  public void setCamundaSource(String camundaSource) {
    camundaSourceAttribute.setValue(this, camundaSource);
  }

  public String getCamundaSourceExpression() {
    return camundaSourceExpressionAttribute.getValue(this);
  }

  public void setCamundaSourceExpression(String camundaSourceExpression) {
    camundaSourceExpressionAttribute.setValue(this, camundaSourceExpression);
  }

  public String getCamundaVariables() {
    return camundaVariablesAttribute.getValue(this);
  }

  public void setCamundaVariables(String camundaVariables) {
    camundaVariablesAttribute.setValue(this, camundaVariables);
  }

  public String getCamundaTarget() {
    return camundaTargetAttribute.getValue(this);
  }

  public void setCamundaTarget(String camundaTarget) {
    camundaTargetAttribute.setValue(this, camundaTarget);
  }

  public String getCamundaBusinessKey() {
    return camundaBusinessKeyAttribute.getValue(this);
  }

  public void setCamundaBusinessKey(String camundaBusinessKey) {
    camundaBusinessKeyAttribute.setValue(this, camundaBusinessKey);
  }

  public boolean getCamundaLocal() {
    return camundaLocalAttribute.getValue(this);
  }

  public void setCamundaLocal(boolean camundaLocal) {
    camundaLocalAttribute.setValue(this, camundaLocal);
  }

}
