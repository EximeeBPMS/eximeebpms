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
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.CamundaValue;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.attribute.Attribute;

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN value camunda extension element
 *
 * @author Sebastian Menski
 */
public class CamundaValueImpl extends BpmnModelElementInstanceImpl implements CamundaValue {

  protected static Attribute<String> camundaIdAttribute;
  protected static Attribute<String> camundaNameAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaValue.class, CAMUNDA_ELEMENT_VALUE)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaValue>() {
        public CamundaValue newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaValueImpl(instanceContext);
        }
      });

    camundaIdAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_ID)
      .namespace(CAMUNDA_NS)
      .build();

    camundaNameAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_NAME)
      .namespace(CAMUNDA_NS)
      .build();

    typeBuilder.build();
  }

  public CamundaValueImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getCamundaId() {
    return camundaIdAttribute.getValue(this);
  }

  public void setCamundaId(String camundaId) {
    camundaIdAttribute.setValue(this, camundaId);
  }

  public String getCamundaName() {
    return camundaNameAttribute.getValue(this);
  }

  public void setCamundaName(String camundaName) {
    camundaNameAttribute.setValue(this, camundaName);
  }
}
