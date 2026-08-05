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
package org.eximeebpms.bpm.engine.test.bpmn.usertask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;

import org.eximeebpms.bpm.engine.ParseException;
import org.eximeebpms.bpm.engine.RepositoryService;
import org.eximeebpms.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.form.FormDefinition;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.pvm.process.ActivityImpl;
import org.eximeebpms.bpm.engine.impl.task.TaskDefinition;
import org.eximeebpms.bpm.engine.impl.test.TestHelper;
import org.eximeebpms.bpm.engine.repository.ProcessDefinition;
import org.eximeebpms.bpm.engine.test.Deployment;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineTestRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class UserTaskEximeeBpmsFormDefinitionParseTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testRule);

  public RepositoryService repositoryService;
  public ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void setup() {
    repositoryService = engineRule.getRepositoryService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @After
  public void tearDown() {
    for (org.eximeebpms.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  protected ActivityImpl findActivityInDeployedProcessDefinition(String activityId) {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinition);

    ProcessDefinitionEntity cachedProcessDefinition = processEngineConfiguration.getDeploymentCache()
        .getProcessDefinitionCache().get(processDefinition.getId());
    return cachedProcessDefinition.findActivity(activityId);
  }

  @Test
  @Deployment
  public void shouldParseEximeeBpmsFormDefinitionVersionBinding() {
    // given a deployed process with a UserTask containing a Camunda Form definition with version binding
    // then
    TaskDefinition taskDefinition = findUserTaskDefinition("UserTask");
    FormDefinition formDefinition = taskDefinition.getFormDefinition();

    assertThat(taskDefinition.getEximeeBpmsFormDefinitionKey().getExpressionText()).isEqualTo("formId");
    assertThat(formDefinition.getEximeeBpmsFormDefinitionKey().getExpressionText()).isEqualTo("formId");

    assertThat(taskDefinition.getEximeeBpmsFormDefinitionBinding()).isEqualTo("version");
    assertThat(formDefinition.getEximeeBpmsFormDefinitionBinding()).isEqualTo("version");

    assertThat(taskDefinition.getEximeeBpmsFormDefinitionVersion().getExpressionText()).isEqualTo("1");
    assertThat(formDefinition.getEximeeBpmsFormDefinitionVersion().getExpressionText()).isEqualTo("1");
  }

  @Test
  @Deployment
  public void shouldParseEximeeBpmsFormDefinitionLatestBinding() {
    // given a deployed process with a UserTask containing a Camunda Form definition with latest binding
    // then
    TaskDefinition taskDefinition = findUserTaskDefinition("UserTask");
    FormDefinition formDefinition = taskDefinition.getFormDefinition();

    assertThat(taskDefinition.getEximeeBpmsFormDefinitionKey().getExpressionText()).isEqualTo("formId");
    assertThat(formDefinition.getEximeeBpmsFormDefinitionKey().getExpressionText()).isEqualTo("formId");

    assertThat(taskDefinition.getEximeeBpmsFormDefinitionBinding()).isEqualTo("latest");
    assertThat(formDefinition.getEximeeBpmsFormDefinitionBinding()).isEqualTo("latest");
  }

  @Test
  @Deployment
  public void shouldParseEximeeBpmsFormDefinitionDeploymentBinding() {
    // given a deployed process with a UserTask containing a Camunda Form definition with deployment binding
    // then
    TaskDefinition taskDefinition = findUserTaskDefinition("UserTask");
    FormDefinition formDefinition = taskDefinition.getFormDefinition();

    assertThat(taskDefinition.getEximeeBpmsFormDefinitionKey().getExpressionText()).isEqualTo("formId");
    assertThat(formDefinition.getEximeeBpmsFormDefinitionKey().getExpressionText()).isEqualTo("formId");

    assertThat(taskDefinition.getEximeeBpmsFormDefinitionBinding()).isEqualTo("deployment");
    assertThat(formDefinition.getEximeeBpmsFormDefinitionBinding()).isEqualTo("deployment");
  }

  @Test
  @Deployment
  public void shouldParseTwoUserTasksWithEximeeBpmsFormDefinition() {
    // given a deployed process with two UserTask containing a Camunda Form definition with deployment binding
    // then
    TaskDefinition taskDefinition1 = findUserTaskDefinition("UserTask_1");
    FormDefinition formDefinition1 = taskDefinition1.getFormDefinition();

    assertThat(taskDefinition1.getEximeeBpmsFormDefinitionKey().getExpressionText()).isEqualTo("formId_1");
    assertThat(formDefinition1.getEximeeBpmsFormDefinitionKey().getExpressionText()).isEqualTo("formId_1");

    assertThat(taskDefinition1.getEximeeBpmsFormDefinitionBinding()).isEqualTo("deployment");
    assertThat(formDefinition1.getEximeeBpmsFormDefinitionBinding()).isEqualTo("deployment");

    TaskDefinition taskDefinition2 = findUserTaskDefinition("UserTask_2");
    FormDefinition formDefinition2 = taskDefinition2.getFormDefinition();
    assertThat(taskDefinition2.getEximeeBpmsFormDefinitionKey().getExpressionText()).isEqualTo("formId_2");
    assertThat(formDefinition2.getEximeeBpmsFormDefinitionKey().getExpressionText()).isEqualTo("formId_2");

    assertThat(taskDefinition2.getEximeeBpmsFormDefinitionBinding()).isEqualTo("version");
    assertThat(formDefinition2.getEximeeBpmsFormDefinitionBinding()).isEqualTo("version");

    assertThat(taskDefinition2.getEximeeBpmsFormDefinitionVersion().getExpressionText()).isEqualTo("2");
    assertThat(formDefinition2.getEximeeBpmsFormDefinitionVersion().getExpressionText()).isEqualTo("2");
  }

  @Test
  public void shouldNotParseEximeeBpmsFormDefinitionUnsupportedBinding() {
    // given a deployed process with a UserTask containing a Camunda Form definition with unsupported binding
    String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "shouldNotParseEximeeBpmsFormDefinitionUnsupportedBinding");

    // when/then expect parse exception
    assertThatThrownBy(() -> repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy())
      .isInstanceOf(ParseException.class)
      .hasMessageContaining("Invalid element definition: value for formRefBinding attribute has to be one of [deployment, latest, version] but was unsupported");
  }

  @Test
  public void shouldNotParseEximeeBpmsFormDefinitionAndFormKey() {
    // given a deployed process with a UserTask containing a Camunda Form definition and formKey
    String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "shouldNotParseEximeeBpmsFormDefinitionAndFormKey");

    // when/then expect parse exception
    assertThatThrownBy(() -> repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy())
      .isInstanceOf(ParseException.class)
      .hasMessageContaining("Invalid element definition: only one of the attributes formKey and formRef is allowed.");
  }

  private TaskDefinition findUserTaskDefinition(String activityId) {
    ActivityImpl userTask = findActivityInDeployedProcessDefinition(activityId);
    assertThat(userTask).isNotNull();

    TaskDefinition taskDefinition = ((UserTaskActivityBehavior) userTask.getActivityBehavior()).getTaskDecorator()
        .getTaskDefinition();
    return taskDefinition;
  }
}
