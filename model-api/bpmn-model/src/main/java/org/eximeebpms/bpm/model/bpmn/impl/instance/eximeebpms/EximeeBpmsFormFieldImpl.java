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
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsFormField;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsProperties;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsValidation;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsValue;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.attribute.Attribute;
import org.eximeebpms.bpm.model.xml.type.child.ChildElement;
import org.eximeebpms.bpm.model.xml.type.child.ChildElementCollection;
import org.eximeebpms.bpm.model.xml.type.child.SequenceBuilder;

import java.util.Collection;

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN formField camunda extension element
 *
 * @author Sebastian Menski
 */
public class EximeeBpmsFormFieldImpl extends BpmnModelElementInstanceImpl implements EximeeBpmsFormField {

  protected static Attribute<String> camundaIdAttribute;
  protected static Attribute<String> camundaLabelAttribute;
  protected static Attribute<String> camundaTypeAttribute;
  protected static Attribute<String> camundaDatePatternAttribute;
  protected static Attribute<String> camundaDefaultValueAttribute;
  protected static ChildElement<EximeeBpmsProperties> camundaPropertiesChild;
  protected static ChildElement<EximeeBpmsValidation> camundaValidationChild;
  protected static ChildElementCollection<EximeeBpmsValue> camundaValueCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(EximeeBpmsFormField.class, CAMUNDA_ELEMENT_FORM_FIELD)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(EximeeBpmsFormFieldImpl::new);

    camundaIdAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_ID)
      .namespace(CAMUNDA_NS)
      .build();

    camundaLabelAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_LABEL)
      .namespace(CAMUNDA_NS)
      .build();

    camundaTypeAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_TYPE)
      .namespace(CAMUNDA_NS)
      .build();

    camundaDatePatternAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_DATE_PATTERN)
      .namespace(CAMUNDA_NS)
      .build();

    camundaDefaultValueAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_DEFAULT_VALUE)
      .namespace(CAMUNDA_NS)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    camundaPropertiesChild = sequenceBuilder.element(EximeeBpmsProperties.class)
      .build();

    camundaValidationChild = sequenceBuilder.element(EximeeBpmsValidation.class)
      .build();

    camundaValueCollection = sequenceBuilder.elementCollection(EximeeBpmsValue.class)
      .build();

    typeBuilder.build();
  }

  public EximeeBpmsFormFieldImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getCamundaId() {
    return camundaIdAttribute.getValue(this);
  }

  public void setCamundaId(String camundaId) {
    camundaIdAttribute.setValue(this, camundaId);
  }

  public String getCamundaLabel() {
    return camundaLabelAttribute.getValue(this);
  }

  public void setCamundaLabel(String camundaLabel) {
    camundaLabelAttribute.setValue(this, camundaLabel);
  }

  public String getCamundaType() {
    return camundaTypeAttribute.getValue(this);
  }

  public void setCamundaType(String camundaType) {
    camundaTypeAttribute.setValue(this, camundaType);
  }

  public String getCamundaDatePattern() {
    return camundaDatePatternAttribute.getValue(this);
  }

  public void setCamundaDatePattern(String camundaDatePattern) {
    camundaDatePatternAttribute.setValue(this, camundaDatePattern);
  }

  public String getCamundaDefaultValue() {
    return camundaDefaultValueAttribute.getValue(this);
  }

  public void setCamundaDefaultValue(String camundaDefaultValue) {
    camundaDefaultValueAttribute.setValue(this, camundaDefaultValue);
  }

  public EximeeBpmsProperties getEximeeBpmsProperties() {
    return camundaPropertiesChild.getChild(this);
  }

  public void setEximeeBpmsProperties(EximeeBpmsProperties camundaProperties) {
    camundaPropertiesChild.setChild(this, camundaProperties);
  }

  public EximeeBpmsValidation getEximeeBpmsValidation() {
    return camundaValidationChild.getChild(this);
  }

  public void setEximeeBpmsValidation(EximeeBpmsValidation camundaValidation) {
    camundaValidationChild.setChild(this, camundaValidation);
  }

  public Collection<EximeeBpmsValue> getEximeeBpmsValues() {
    return camundaValueCollection.get(this);
  }
}
