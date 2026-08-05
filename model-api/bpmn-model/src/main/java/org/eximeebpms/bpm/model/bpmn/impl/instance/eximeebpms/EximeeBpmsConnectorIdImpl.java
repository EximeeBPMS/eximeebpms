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

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ELEMENT_CONNECTOR_ID;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import org.eximeebpms.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsConnectorId;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;

/**
 * The BPMN connectorId camunda extension element
 *
 * @author Sebastian Menski
 */
public class EximeeBpmsConnectorIdImpl extends BpmnModelElementInstanceImpl implements EximeeBpmsConnectorId {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(EximeeBpmsConnectorId.class, CAMUNDA_ELEMENT_CONNECTOR_ID)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(EximeeBpmsConnectorIdImpl::new);

    typeBuilder.build();
  }

  public EximeeBpmsConnectorIdImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

}
