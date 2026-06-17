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
package org.eximeebpms.bpm.engine.test.api.history;

import static org.eximeebpms.bpm.engine.history.UserOperationLogEntry.CATEGORY_OPERATOR;
import static org.eximeebpms.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eximeebpms.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.eximeebpms.bpm.engine.BadUserRequestException;
import org.eximeebpms.bpm.engine.ExternalTaskService;
import org.eximeebpms.bpm.engine.FormService;
import org.eximeebpms.bpm.engine.HistoryService;
import org.eximeebpms.bpm.engine.IdentityService;
import org.eximeebpms.bpm.engine.ProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.TaskService;
import org.eximeebpms.bpm.engine.externaltask.LockedExternalTask;
import org.eximeebpms.bpm.engine.history.HistoricDecisionInputInstance;
import org.eximeebpms.bpm.engine.history.HistoricDecisionInstance;
import org.eximeebpms.bpm.engine.history.HistoricDecisionOutputInstance;
import org.eximeebpms.bpm.engine.history.HistoricExternalTaskLog;
import org.eximeebpms.bpm.engine.history.HistoricJobLog;
import org.eximeebpms.bpm.engine.history.UserOperationLogEntry;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.eximeebpms.bpm.engine.impl.interceptor.Command;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.task.Attachment;
import org.eximeebpms.bpm.engine.task.IdentityLinkType;
import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.engine.test.Deployment;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.RequiredHistoryLevel;
import org.eximeebpms.bpm.engine.test.dmn.businessruletask.TestPojo;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineTestRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.eximeebpms.bpm.engine.test.util.ResetDmnConfigUtil;
import org.eximeebpms.bpm.engine.variable.VariableMap;
import org.eximeebpms.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Svetlana Dorokhova
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class BulkHistoryDeleteTest {

  protected static final String ONE_TASK_PROCESS = "oneTaskProcess";

  public static final int PROCESS_INSTANCE_COUNT = 5;

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  private HistoryService historyService;
  private TaskService taskService;
  private RuntimeService runtimeService;
  private FormService formService;
  private ExternalTaskService externalTaskService;
  private IdentityService identityService;

  public static final String USER_ID = "demo";

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    formService = engineRule.getFormService();
    externalTaskService = engineRule.getExternalTaskService();
    identityService = engineRule.getIdentityService();
    identityService.setAuthenticatedUserId(USER_ID);
  }

  @Before
  public void enableDmnFeelLegacyBehavior() {
    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        engineRule.getProcessEngineConfiguration()
        .getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(true)
        .init();
  }

  @After
  public void disableDmnFeelLegacyBehavior() {

    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        engineRule.getProcessEngineConfiguration()
            .getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(false)
        .init();
  }

  @After
  public void tearDown() throws Exception {
    identityService.clearAuthentication();
  }

  @Test
  @Deployment(resources = { "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoryTaskIdentityLink() {
    //given
    final List<String> ids = prepareHistoricProcesses();
    List<Task> taskList = taskService.createTaskQuery().list();
    taskService.addUserIdentityLink(taskList.get(0).getId(), "someUser", IdentityLinkType.ASSIGNEE);

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, historyService.createHistoricIdentityLinkLogQuery().count());
  }

  @Test
  @Deployment(resources = { "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoryActivityInstances() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupTaskAttachmentWithContent() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    List<Task> taskList = taskService.createTaskQuery().list();

    String taskWithAttachmentId = taskList.get(0).getId();
    createTaskAttachmentWithContent(taskWithAttachmentId);
    //remember contentId
    final String contentId = findAttachmentContentId(taskService.getTaskAttachments(taskWithAttachmentId));

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, taskService.getTaskAttachments(taskWithAttachmentId).size());
    //check that attachment content was removed
    verifyByteArraysWereRemoved(contentId);
  }

  private String findAttachmentContentId(List<Attachment> attachments) {
    assertEquals(1, attachments.size());
    return ((AttachmentEntity) attachments.get(0)).getContentId();
  }

  @Test
  @Deployment(resources = { "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupProcessInstanceAttachmentWithContent() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    String processInstanceWithAttachmentId = ids.get(0);
    createProcessInstanceAttachmentWithContent(processInstanceWithAttachmentId);
    //remember contentId
    final String contentId = findAttachmentContentId(taskService.getProcessInstanceAttachments(processInstanceWithAttachmentId));

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, taskService.getProcessInstanceAttachments(processInstanceWithAttachmentId).size());
    //check that attachment content was removed
    verifyByteArraysWereRemoved(contentId);
  }

  private void createProcessInstanceAttachmentWithContent(String processInstanceId) {
    taskService
        .createAttachment("web page", null, processInstanceId, "weatherforcast", "temperatures and more", new ByteArrayInputStream("someContent".getBytes()));

    List<Attachment> taskAttachments = taskService.getProcessInstanceAttachments(processInstanceId);
    assertEquals(1, taskAttachments.size());
    assertNotNull(taskService.getAttachmentContent(taskAttachments.get(0).getId()));
  }

  private void createTaskAttachmentWithContent(String taskId) {
    taskService.createAttachment("web page", taskId, null, "weatherforcast", "temperatures and more", new ByteArrayInputStream("someContent".getBytes()));

    List<Attachment> taskAttachments = taskService.getTaskAttachments(taskId);
    assertEquals(1, taskAttachments.size());
    assertNotNull(taskService.getAttachmentContent(taskAttachments.get(0).getId()));
  }

  @Test
  @Deployment(resources = { "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupTaskComment() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    List<Task> taskList = taskService.createTaskQuery().list();

    String taskWithCommentId = taskList.get(2).getId();
    taskService.createComment(taskWithCommentId, null, "Some comment");

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, taskService.getTaskComments(taskWithCommentId).size());
  }

  @Test
  @Deployment(resources = { "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupProcessInstanceComment() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    String processInstanceWithCommentId = ids.get(0);
    taskService.createComment(null, processInstanceWithCommentId, "Some comment");

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, taskService.getProcessInstanceComments(processInstanceWithCommentId).size());
  }

  @Test
  @Deployment(resources = {
      "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoricVariableInstancesAndHistoricDetails() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    List<Task> taskList = taskService.createTaskQuery().list();

    taskService.setVariables(taskList.get(0).getId(), getVariables());

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, historyService.createHistoricDetailQuery().count());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoryTaskForm() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    List<Task> taskList = taskService.createTaskQuery().list();

    formService.submitTaskForm(taskList.get(0).getId(), getVariables());

    for (ProcessInstance processInstance : runtimeService.createProcessInstanceQuery().list()) {
      runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), null);
    }

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, historyService.createHistoricDetailQuery().count());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
  }

  @Test
  @Deployment(resources = "org/eximeebpms/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  public void testCleanupHistoricExternalTaskLog() {
    //given
    final List<String> ids = prepareHistoricProcesses("oneExternalTaskProcess");

    String workerId = "aWrokerId";
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(1, workerId).topic("externalTaskTopic", 10000L).execute();

    externalTaskService.handleFailure(tasks.get(0).getId(), workerId, "errorMessage", "exceptionStackTrace", 5, 3000L);

    //remember errorDetailsByteArrayId
    final String errorDetailsByteArrayId = findErrorDetailsByteArrayId("errorMessage");

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, historyService.createHistoricExternalTaskLogQuery().count());
    //check that ByteArray was removed
    verifyByteArraysWereRemoved(errorDetailsByteArrayId);
  }

  private String findErrorDetailsByteArrayId(String errorMessage) {
    final List<HistoricExternalTaskLog> historicExternalTaskLogs = historyService.createHistoricExternalTaskLogQuery().errorMessage(errorMessage).list();
    assertEquals(1, historicExternalTaskLogs.size());

    return ((HistoricExternalTaskLogEntity) historicExternalTaskLogs.get(0)).getErrorDetailsByteArrayId();
  }

  @Test
  @Deployment(resources = {
      "org/eximeebpms/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn" })
  public void testCleanupHistoricIncidents() {
    //given
    List<String> ids = prepareHistoricProcesses("failingProcess");

    testRule.executeAvailableJobs();

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("failingProcess").count());
    assertEquals(0, historyService.createHistoricIncidentQuery().count());

  }

  @Test
  @Deployment(resources = {
      "org/eximeebpms/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn" })
  public void testCleanupHistoricJobLogs() {
    //given
    List<String> ids = prepareHistoricProcesses("failingProcess", null, 1);

    testRule.executeAvailableJobs();

    runtimeService.deleteProcessInstances(ids, null, true, true);

    List<String> byteArrayIds = findExceptionByteArrayIds();

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("failingProcess").count());
    assertEquals(0, historyService.createHistoricJobLogQuery().count());

    verifyByteArraysWereRemoved(byteArrayIds.toArray(new String[] {}));
  }

  private List<String> findExceptionByteArrayIds() {
    List<String> exceptionByteArrayIds = new ArrayList<String>();
    List<HistoricJobLog> historicJobLogs = historyService.createHistoricJobLogQuery().list();
    for (HistoricJobLog historicJobLog : historicJobLogs) {
      HistoricJobLogEventEntity historicJobLogEventEntity = (HistoricJobLogEventEntity) historicJobLog;
      if (historicJobLogEventEntity.getExceptionByteArrayId() != null) {
        exceptionByteArrayIds.add(historicJobLogEventEntity.getExceptionByteArrayId());
      }
    }
    return exceptionByteArrayIds;
  }

  @Test
  @Deployment(resources = {"org/eximeebpms/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/eximeebpms/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml" })
  public void testCleanupHistoryDecisionData() {
    //given
    List<String> ids = prepareHistoricProcesses("testProcess", Variables.createVariables().putValue("pojo", new TestPojo("okay", 13.37)));

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //remember input and output ids
    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().includeInputs().includeOutputs().list();
    final List<String> inputIds = new ArrayList<String>();
    final List<String> inputByteArrayIds = new ArrayList<String>();
    collectHistoricDecisionInputIds(historicDecisionInstances, inputIds, inputByteArrayIds);

    final List<String> outputIds = new ArrayList<String>();
    final List<String> outputByteArrayIds = new ArrayList<String>();
    collectHistoricDecisionOutputIds(historicDecisionInstances, outputIds, outputByteArrayIds);

    //when
    historyService.deleteHistoricDecisionInstancesBulk(extractIds(historicDecisionInstances));

    //then
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());

    //check that decision inputs and outputs were removed
    assertDataDeleted(inputIds, inputByteArrayIds, outputIds, outputByteArrayIds);


    List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
      .operationType(OPERATION_TYPE_DELETE_HISTORY)
      .property("nrOfInstances")
      .list();

    assertEquals(1, userOperationLogEntries.size());

    UserOperationLogEntry entry = userOperationLogEntries.get(0);
    assertEquals(String.valueOf(historicDecisionInstances.size()), entry.getNewValue());
    assertEquals(CATEGORY_OPERATOR, entry.getCategory());
  }

  @Test
  @Deployment(resources = {"org/eximeebpms/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
  "org/eximeebpms/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml" })
  public void testCleanupFakeHistoryDecisionData() {
    //given
    List<String> ids = Arrays.asList("aFake");

    //when
    historyService.deleteHistoricDecisionInstancesBulk(ids);

    //then expect no exception
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());
  }

  void assertDataDeleted(final List<String> inputIds, final List<String> inputByteArrayIds, final List<String> outputIds,
    final List<String> outputByteArrayIds) {
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        for (String inputId : inputIds) {
          assertNull(commandContext.getDbEntityManager().selectById(HistoricDecisionInputInstanceEntity.class, inputId));
        }
        for (String inputByteArrayId : inputByteArrayIds) {
          assertNull(commandContext.getDbEntityManager().selectById(ByteArrayEntity.class, inputByteArrayId));
        }
        for (String outputId : outputIds) {
          assertNull(commandContext.getDbEntityManager().selectById(HistoricDecisionOutputInstanceEntity.class, outputId));
        }
        for (String outputByteArrayId : outputByteArrayIds) {
          assertNull(commandContext.getDbEntityManager().selectById(ByteArrayEntity.class, outputByteArrayId));
        }
        return null;
      }
    });
  }

  @Test
  @Deployment(resources = {"org/eximeebpms/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml" })
  public void testCleanupHistoryStandaloneDecisionData() {
    //given
    for (int i = 0; i < 5; i++) {
      engineRule.getDecisionService().evaluateDecisionByKey("testDecision").variables(Variables.createVariables().putValue("pojo", new TestPojo("okay", 13.37))).evaluate();
    }

    //remember input and output ids
    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().includeInputs().includeOutputs().list();
    final List<String> inputIds = new ArrayList<String>();
    final List<String> inputByteArrayIds = new ArrayList<String>();
    collectHistoricDecisionInputIds(historicDecisionInstances, inputIds, inputByteArrayIds);

    final List<String> outputIds = new ArrayList<String>();
    final List<String> outputByteArrayIds = new ArrayList<String>();
    collectHistoricDecisionOutputIds(historicDecisionInstances, outputIds, outputByteArrayIds);

    List<String> decisionInstanceIds = extractIds(historicDecisionInstances);

    //when
    historyService.deleteHistoricDecisionInstancesBulk(decisionInstanceIds);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("testProcess").count());
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());

    //check that decision inputs and outputs were removed
    assertDataDeleted(inputIds, inputByteArrayIds, outputIds, outputByteArrayIds);

  }

  private List<String> extractIds(List<HistoricDecisionInstance> historicDecisionInstances) {
    List<String> decisionInstanceIds = new ArrayList<String>();
    for (HistoricDecisionInstance historicDecisionInstance: historicDecisionInstances) {
      decisionInstanceIds.add(historicDecisionInstance.getId());
    }
    return decisionInstanceIds;
  }

  @Test
  @Deployment(resources = { "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoryEmptyProcessIdsException() {
    //given
    final List<String> ids = prepareHistoricProcesses();
    runtimeService.deleteProcessInstances(ids, null, true, true);

    try {
      historyService.deleteHistoricProcessInstancesBulk(null);
      fail("Empty process instance ids exception was expected");
    } catch (BadUserRequestException ex) {
    }

    try {
      historyService.deleteHistoricProcessInstancesBulk(new ArrayList<String>());
      fail("Empty process instance ids exception was expected");
    } catch (BadUserRequestException ex) {
    }

  }

  @Test
  @Deployment(resources = { "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoryProcessesNotFinishedException() {
    //given
    final List<String> ids = prepareHistoricProcesses();
    runtimeService.deleteProcessInstances(ids.subList(1, ids.size()), null, true, true);

    try {
      historyService.deleteHistoricProcessInstancesBulk(ids);
      fail("Not all processes are finished exception was expected");
    } catch (BadUserRequestException ex) {
    }

  }

  private void collectHistoricDecisionInputIds(List<HistoricDecisionInstance> historicDecisionInstances, List<String> historicDecisionInputIds, List<String> inputByteArrayIds) {
    for (HistoricDecisionInstance historicDecisionInstance : historicDecisionInstances) {
      for (HistoricDecisionInputInstance inputInstanceEntity : historicDecisionInstance.getInputs()) {
        historicDecisionInputIds.add(inputInstanceEntity.getId());
        final String byteArrayValueId = ((HistoricDecisionInputInstanceEntity) inputInstanceEntity).getByteArrayValueId();
        if (byteArrayValueId != null) {
          inputByteArrayIds.add(byteArrayValueId);
        }
      }
    }
    assertEquals(PROCESS_INSTANCE_COUNT, historicDecisionInputIds.size());
  }

  private void collectHistoricDecisionOutputIds(List<HistoricDecisionInstance> historicDecisionInstances, List<String> historicDecisionOutputIds, List<String> outputByteArrayId) {
    for (HistoricDecisionInstance historicDecisionInstance : historicDecisionInstances) {
      for (HistoricDecisionOutputInstance outputInstanceEntity : historicDecisionInstance.getOutputs()) {
        historicDecisionOutputIds.add(outputInstanceEntity.getId());
        final String byteArrayValueId = ((HistoricDecisionOutputInstanceEntity) outputInstanceEntity).getByteArrayValueId();
        if (byteArrayValueId != null) {
          outputByteArrayId.add(byteArrayValueId);
        }
      }
    }
    assertEquals(PROCESS_INSTANCE_COUNT, historicDecisionOutputIds.size());
  }

  private List<String> prepareHistoricProcesses() {
    return prepareHistoricProcesses(ONE_TASK_PROCESS);
  }

  private List<String> prepareHistoricProcesses(String businessKey) {
    return prepareHistoricProcesses(businessKey, null);
  }

  private List<String> prepareHistoricProcesses(String businessKey, VariableMap variables) {
    return prepareHistoricProcesses(businessKey, variables, PROCESS_INSTANCE_COUNT);
  }

  private List<String> prepareHistoricProcesses(String businessKey, VariableMap variables, Integer processInstanceCount) {
    List<String> processInstanceIds = new ArrayList<String>();

    for (int i = 0; i < processInstanceCount; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(businessKey, variables);
      processInstanceIds.add(processInstance.getId());
    }

    return processInstanceIds;
  }

  private void verifyByteArraysWereRemoved(final String... errorDetailsByteArrayIds) {
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        for (String errorDetailsByteArrayId : errorDetailsByteArrayIds) {
          assertNull(commandContext.getDbEntityManager().selectOne("selectByteArray", errorDetailsByteArrayId));
        }
        return null;
      }
    });
  }

  private VariableMap getVariables() {
    return Variables.createVariables()
        .putValue("aVariableName", "aVariableValue")
        .putValue("pojoVariableName", new TestPojo("someValue", 111.));
  }

}
