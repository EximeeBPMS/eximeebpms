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

import java.util.Arrays;
import java.util.Collection;

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

/**
 * @author Sebastian Menski
 */
public class StartEventTest extends BpmnModelElementInstanceTest {

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(CatchEvent.class, false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return null;
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("isInterrupting", false, false, true),
      /** camunda extensions */
      new AttributeAssumption(CAMUNDA_NS, "async", false, false, false),
      new AttributeAssumption(CAMUNDA_NS, "formHandlerClass"),
      new AttributeAssumption(CAMUNDA_NS, "formKey"),
      new AttributeAssumption(CAMUNDA_NS, "formRef"),
      new AttributeAssumption(CAMUNDA_NS, "formRefBinding"),
      new AttributeAssumption(CAMUNDA_NS, "formRefVersion"),
      new AttributeAssumption(CAMUNDA_NS, "initiator")
    );
  }
}
