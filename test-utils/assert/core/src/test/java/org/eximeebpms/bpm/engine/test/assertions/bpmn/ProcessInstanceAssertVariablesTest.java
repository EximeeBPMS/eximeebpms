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
import static org.eximeebpms.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.eximeebpms.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;

import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.test.Deployment;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.assertions.helpers.Failure;
import org.eximeebpms.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceAssertVariablesTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-variables.bpmn"
  })
  public void testVariables_One_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-variables", withVariables("aVariable", "aValue")
    );
    // Then
    assertThat(processInstance).variables().containsEntry("aVariable", "aValue");
    // When
    complete(task(processInstance));
    // Then
    assertThat(processInstance).variables().containsEntry("aVariable", "aValue");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-variables.bpmn"
  })
  public void testVariables_One_Changed_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-variables", withVariables("aVariable", "aValue")
    );
    // Then
    assertThat(processInstance).variables().containsEntry("aVariable", "aValue");
    // When
    complete(task(processInstance), withVariables("aVariable", "anotherValue"));
    // Then
    assertThat(processInstance).variables().containsEntry("aVariable", "anotherValue");
    // And
    assertThat(processInstance).variables().doesNotContainEntry("aVariable", "aValue");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-variables.bpmn"
  })
  public void testVariables_One_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-variables", withVariables("aVariable", "aValue")
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKey("anotherVariable");
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKeys("aVariable", "anotherVariable");
      }
    });
    // When
    complete(task(processInstance));
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKey("anotherVariable");
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKeys("aVariable", "anotherVariable");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-variables.bpmn"
  })
  public void testVariables_Two_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-variables", withVariables("firstVariable", "firstValue", "secondVariable", "secondValue")
    );
    // Then
    assertThat(processInstance).variables();
    // And
    assertThat(processInstance).variables().containsKey("firstVariable");
    // And
    assertThat(processInstance).variables().containsKey("secondVariable");
    // And
    assertThat(processInstance).variables().containsKeys("firstVariable", "secondVariable");
    // When
    complete(task(processInstance));
    // Then
    assertThat(processInstance).variables();
    // And
    assertThat(processInstance).variables().containsKey("firstVariable");
    // And
    assertThat(processInstance).variables().containsKey("secondVariable");
    // And
    assertThat(processInstance).variables().containsKeys("firstVariable", "secondVariable");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-variables.bpmn"
  })
  public void testVariables_Two_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-variables", withVariables("firstVariable", "firstValue", "secondVariable", "secondValue")
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKey("anotherVariable");
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKeys("firstVariable", "anotherVariable");
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKeys("secondVariable", "anotherVariable");
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKeys("firstVariable", "secondVariable", "anotherVariable");
      }
    });
    // When
    complete(task(processInstance));
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKey("anotherVariable");
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKeys("firstVariable", "anotherVariable");
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKeys("secondVariable", "anotherVariable");
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKeys("firstVariable", "secondVariable", "anotherVariable");
      }
    });
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-variables.bpmn"
  })
  public void testVariables_None_Success() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-variables"
    );
    // Then
    assertThat(processInstance).variables().isEmpty();
    // When
    complete(task(processInstance));
    // Then
    assertThat(processInstance).variables().isEmpty();
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-variables.bpmn"
  })
  public void testVariables_None_Failure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-variables"
    );
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().isNotEmpty();
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKey("aVariable");
      }
    });
    // When
    complete(task(processInstance));
    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().isNotEmpty();
      }
    });
    // And
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).variables().containsKey("aVariable");
      }
    });
  }

}
