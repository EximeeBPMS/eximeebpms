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

import java.util.Collection;
import org.eximeebpms.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsInputOutput;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsInputParameter;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsOutputParameter;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.child.ChildElementCollection;
import org.eximeebpms.bpm.model.xml.type.child.SequenceBuilder;

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_OUTPUT;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

/**
 * The BPMN inputOutput camunda extension element
 *
 * @author Sebastian Menski
 */
public class EximeeBpmsInputOutputImpl extends BpmnModelElementInstanceImpl implements EximeeBpmsInputOutput {

  protected static ChildElementCollection<EximeeBpmsInputParameter> camundaInputParameterCollection;
  protected static ChildElementCollection<EximeeBpmsOutputParameter> camundaOutputParameterCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(EximeeBpmsInputOutput.class, CAMUNDA_ELEMENT_INPUT_OUTPUT)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(EximeeBpmsInputOutputImpl::new);

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    camundaInputParameterCollection = sequenceBuilder.elementCollection(EximeeBpmsInputParameter.class)
      .build();

    camundaOutputParameterCollection = sequenceBuilder.elementCollection(EximeeBpmsOutputParameter.class)
      .build();

    typeBuilder.build();
  }

  public EximeeBpmsInputOutputImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Collection<EximeeBpmsInputParameter> getEximeeBpmsInputParameters() {
    return camundaInputParameterCollection.get(this);
  }

  public Collection<EximeeBpmsOutputParameter> getEximeeBpmsOutputParameters() {
    return camundaOutputParameterCollection.get(this);
  }
}
