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

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_NAME;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ELEMENT_OUTPUT_PARAMETER;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.CamundaOutputParameter;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.eximeebpms.bpm.model.xml.type.attribute.Attribute;

/**
 * The BPMN outputParameter camunda extension element
 *
 * @author Sebastian Menski
 */
public class CamundaOutputParameterImpl extends CamundaGenericValueElementImpl implements CamundaOutputParameter {

  protected static Attribute<String> camundaNameAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaOutputParameter.class, CAMUNDA_ELEMENT_OUTPUT_PARAMETER)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaOutputParameter>() {
        public CamundaOutputParameter newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaOutputParameterImpl(instanceContext);
        }
      });

    camundaNameAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_NAME)
      .namespace(CAMUNDA_NS)
      .required()
      .build();

    typeBuilder.build();
  }

  public CamundaOutputParameterImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getCamundaName() {
    return camundaNameAttribute.getValue(this);
  }

  public void setCamundaName(String camundaName) {
    camundaNameAttribute.setValue(this, camundaName);
  }
}
