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
package org.eximeebpms.bpm.engine.test.assertions.bpmn;

import static org.eximeebpms.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.eximeebpms.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.test.Deployment;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.assertions.helpers.Failure;
import org.eximeebpms.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceAssertHasProcessDefinitionKeyTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasProcessDefinitionKey-1.bpmn", "bpmn/ProcessInstanceAssert-hasProcessDefinitionKey-2.bpmn"
  })
  public void testHasProcessDefinitionKey_Success() {
    // When
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasProcessDefinitionKey-1"
    );
    // Then
    assertThat(processInstance).hasProcessDefinitionKey("ProcessInstanceAssert-hasProcessDefinitionKey-1");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasProcessDefinitionKey-1.bpmn", "bpmn/ProcessInstanceAssert-hasProcessDefinitionKey-2.bpmn"
  })
  public void testHasProcessDefinitionKey_Failure() {
    // Given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasProcessDefinitionKey-2"
    );
    // When
    runtimeService().suspendProcessInstanceById(processInstance.getId());
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).hasProcessDefinitionKey("ProcessInstanceAssert-hasProcessDefinitionKey-1");
      }
    });
  }

}
