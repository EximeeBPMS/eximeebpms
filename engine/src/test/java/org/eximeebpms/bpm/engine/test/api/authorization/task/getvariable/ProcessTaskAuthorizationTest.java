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
package org.eximeebpms.bpm.engine.test.api.authorization.task.getvariable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Map;

import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.TaskService;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.eximeebpms.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.eximeebpms.bpm.engine.variable.VariableMap;
import org.eximeebpms.bpm.engine.variable.Variables;
import org.eximeebpms.bpm.engine.variable.value.TypedValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runners.Parameterized.Parameter;

/**
 * @author Yana.Vasileva
 *
 */
public abstract class ProcessTaskAuthorizationTest {


  private static final String ONE_TASK_PROCESS = "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String userId = "userId";
  public static final String VARIABLE_NAME = "aVariableName";
  public static final String VARIABLE_VALUE = "aVariableValue";
  protected static final String PROCESS_KEY = "oneTaskProcess";

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule);

  @Parameter
  public AuthorizationScenario scenario;

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected TaskService taskService;
  protected RuntimeService runtimeService;

  protected boolean ensureSpecificVariablePermission;
  protected String deploumentId;


  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    taskService = engineRule.getTaskService();
    runtimeService = engineRule.getRuntimeService();

    authRule.createUserAndGroup("userId", "groupId");
    deploumentId = engineRule.getRepositoryService().createDeployment().addClasspathResource(ONE_TASK_PROCESS).deployWithResult().getId();
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
    engineRule.getRepositoryService().deleteDeployment(deploumentId, true);
  }

  @Test
  public void testGetVariable() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("taskId", taskId)
      .start();

    Object variable = taskService.getVariable(taskId, VARIABLE_NAME);

    // then
    if (authRule.assertScenario(scenario)) {
      assertEquals(VARIABLE_VALUE, variable);
    }
  }

  @Test
  public void testGetVariableLocal() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setVariablesLocal(taskId, getVariables());

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("taskId", taskId)
      .start();

    Object variable = taskService.getVariableLocal(taskId, VARIABLE_NAME);

    // then
    if (authRule.assertScenario(scenario)) {
      assertEquals(VARIABLE_VALUE, variable);
    }
  }

  @Test
  public void testGetVariableTyped() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("taskId", taskId)
      .start();

    TypedValue typedValue = taskService.getVariableTyped(taskId, VARIABLE_NAME);

    // then
    if (authRule.assertScenario(scenario)) {
      assertNotNull(typedValue);
      assertEquals(VARIABLE_VALUE, typedValue.getValue());
    }
  }

  @Test
  public void testGetVariableLocalTyped() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setVariablesLocal(taskId, getVariables());

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("taskId", taskId)
      .start();

    TypedValue typedValue = taskService.getVariableLocalTyped(taskId, VARIABLE_NAME);

    // then
    if (authRule.assertScenario(scenario)) {
      assertNotNull(typedValue);
      assertEquals(VARIABLE_VALUE, typedValue.getValue());
    }
  }

  @Test
  public void testGetVariables() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    Map<String, Object> variables = taskService.getVariables(taskId);

    // then
    if (authRule.assertScenario(scenario)) {
      verifyGetVariables(variables);
    }
  }

  @Test
  public void testGetVariablesLocal() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setVariablesLocal(taskId, getVariables());

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    Map<String, Object> variables = taskService.getVariablesLocal(taskId);

    // then
    if (authRule.assertScenario(scenario)) {
      verifyGetVariables(variables);
    }
  }

  @Test
  public void testGetVariablesTyped() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    VariableMap variables = taskService.getVariablesTyped(taskId);

    // then
    if (authRule.assertScenario(scenario)) {
      verifyGetVariables(variables);
    }
  }

  @Test
  public void testGetVariablesLocalTyped() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setVariablesLocal(taskId, getVariables());

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    VariableMap variables = taskService.getVariablesLocalTyped(taskId);

    // then
    if (authRule.assertScenario(scenario)) {
      verifyGetVariables(variables);
    }
  }

  @Test
  public void testGetVariablesByName() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    Map<String, Object> variables = taskService.getVariables(taskId, Arrays.asList(VARIABLE_NAME));

    // then
    if (authRule.assertScenario(scenario)) {
      verifyGetVariables(variables);
    }
  }

  @Test
  public void testGetVariablesLocalByName() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setVariablesLocal(taskId, getVariables());

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    Map<String, Object> variables = taskService.getVariablesLocal(taskId, Arrays.asList(VARIABLE_NAME));

    // then
    if (authRule.assertScenario(scenario)) {
      verifyGetVariables(variables);
    }
  }

  @Test
  public void testGetVariablesTypedByName() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    VariableMap variables = taskService.getVariablesTyped(taskId, Arrays.asList(VARIABLE_NAME), false);

    // then
    if (authRule.assertScenario(scenario)) {
      verifyGetVariables(variables);
    }
  }

  @Test
  public void testGetVariablesLocalTypedByName() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setVariablesLocal(taskId, getVariables());

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    Map<String, Object> variables = taskService.getVariablesLocalTyped(taskId, Arrays.asList(VARIABLE_NAME), false);

    // then
    if (authRule.assertScenario(scenario)) {
      verifyGetVariables(variables);
    }
  }

  protected void createTask(final String taskId) {
    Task task = taskService.newTask(taskId);
    taskService.saveTask(task);
  }

  protected VariableMap getVariables() {
    return Variables.createVariables().putValue(ProcessTaskAuthorizationTest.VARIABLE_NAME, ProcessTaskAuthorizationTest.VARIABLE_VALUE);
  }

  protected void verifyGetVariables(Map<String, Object> variables) {
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(ProcessTaskAuthorizationTest.VARIABLE_VALUE, variables.get(ProcessTaskAuthorizationTest.VARIABLE_NAME));
  }

}
