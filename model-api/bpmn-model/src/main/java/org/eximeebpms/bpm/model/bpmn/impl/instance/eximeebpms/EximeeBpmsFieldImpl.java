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
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsExpression;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsField;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsString;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.attribute.Attribute;
import org.eximeebpms.bpm.model.xml.type.child.ChildElement;
import org.eximeebpms.bpm.model.xml.type.child.SequenceBuilder;

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN field camunda extension element
 *
 * @author Sebastian Menski
 */
public class EximeeBpmsFieldImpl extends BpmnModelElementInstanceImpl implements EximeeBpmsField {

  protected static Attribute<String> camundaNameAttribute;
  protected static Attribute<String> camundaExpressionAttribute;
  protected static Attribute<String> camundaStringValueAttribute;
  protected static ChildElement<EximeeBpmsExpression> camundaExpressionChild;
  protected static ChildElement<EximeeBpmsString> camundaStringChild;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(EximeeBpmsField.class, CAMUNDA_ELEMENT_FIELD)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(EximeeBpmsFieldImpl::new);

    camundaNameAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_NAME)
      .namespace(CAMUNDA_NS)
      .build();

    camundaExpressionAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_EXPRESSION)
      .namespace(CAMUNDA_NS)
      .build();

    camundaStringValueAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_STRING_VALUE)
      .namespace(CAMUNDA_NS)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    camundaExpressionChild = sequenceBuilder.element(EximeeBpmsExpression.class)
      .build();

    camundaStringChild = sequenceBuilder.element(EximeeBpmsString.class)
      .build();

    typeBuilder.build();
  }

  public EximeeBpmsFieldImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getCamundaName() {
    return camundaNameAttribute.getValue(this);
  }

  public void setCamundaName(String camundaName) {
    camundaNameAttribute.setValue(this, camundaName);
  }

  public String getEximeeBpmsExpression() {
    return camundaExpressionAttribute.getValue(this);
  }

  public void setEximeeBpmsExpression(String camundaExpression) {
    camundaExpressionAttribute.setValue(this, camundaExpression);
  }

  public String getEximeeBpmsStringValue() {
    return camundaStringValueAttribute.getValue(this);
  }

  public void setEximeeBpmsStringValue(String camundaStringValue) {
    camundaStringValueAttribute.setValue(this, camundaStringValue);
  }

  public EximeeBpmsString getEximeeBpmsString() {
    return camundaStringChild.getChild(this);
  }

  public void setEximeeBpmsString(EximeeBpmsString camundaString) {
    camundaStringChild.setChild(this, camundaString);
  }

  public EximeeBpmsExpression getEximeeBpmsExpressionChild() {
    return camundaExpressionChild.getChild(this);
  }

  public void setEximeeBpmsExpressionChild(EximeeBpmsExpression camundaExpression) {
    camundaExpressionChild.setChild(this, camundaExpression);
  }
}
