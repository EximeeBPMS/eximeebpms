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
import static org.eximeebpms.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.eximeebpms.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.eximeebpms.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskQuery;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.test.Deployment;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.assertions.helpers.Failure;
import org.eximeebpms.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceAssertIsStartedTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-isStarted.bpmn"
  })
  public void testIsStarted_AndActive_Success() {
    // When
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isStarted"
    );
    // Then
    assertThat(processInstance).isStarted();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-isStarted.bpmn"
  })
  public void testIsStarted_AndEnded_Success() {
    // Given
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isStarted"
    );
    // When
    complete(taskQuery().singleResult());
    // Then
    assertThat(processInstance).isStarted();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-isStarted.bpmn"
  })
  public void testIsStarted_Failure() {
    // When
    final ProcessInstance processInstance = mock(ProcessInstance.class);
    // And
    when(processInstance.getId()).thenReturn("someNonExistingId");
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).isStarted();
      }
    });
  }

}
