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
package org.eximeebpms.bpm.model.bpmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.BUSINESS_RULE_TASK;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.CALL_ACTIVITY_ID;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.END_EVENT_ID;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.PROCESS_ID;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.SCRIPT_TASK_ID;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.SEND_TASK_ID;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.SEQUENCE_FLOW_ID;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.SERVICE_TASK_ID;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.START_EVENT_ID;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_CLASS_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_CLASS_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_DELEGATE_EXPRESSION_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_DELEGATE_EXPRESSION_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_DUE_DATE_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_DUE_DATE_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_EXECUTION_EVENT_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_EXECUTION_EVENT_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_EXPRESSION_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_EXPRESSION_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_FLOW_NODE_JOB_PRIORITY;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_GROUPS_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_GROUPS_LIST_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_GROUPS_LIST_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_GROUPS_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_HISTORY_TIME_TO_LIVE;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_PRIORITY_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_PRIORITY_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_PROCESS_JOB_PRIORITY;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_PROCESS_TASK_PRIORITY;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_SERVICE_TASK_PRIORITY;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_STRING_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_STRING_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_TASK_EVENT_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_TASK_EVENT_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_TYPE_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_TYPE_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_USERS_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_USERS_LIST_API;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_USERS_LIST_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.TEST_USERS_XML;
import static org.eximeebpms.bpm.model.bpmn.BpmnTestConstants.USER_TASK_ID;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.ACTIVITI_NS;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_ERROR_CODE_VARIABLE;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_ERROR_MESSAGE_VARIABLE;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eximeebpms.bpm.model.bpmn.instance.BaseElement;
import org.eximeebpms.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.eximeebpms.bpm.model.bpmn.instance.BusinessRuleTask;
import org.eximeebpms.bpm.model.bpmn.instance.CallActivity;
import org.eximeebpms.bpm.model.bpmn.instance.EndEvent;
import org.eximeebpms.bpm.model.bpmn.instance.Error;
import org.eximeebpms.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.eximeebpms.bpm.model.bpmn.instance.Expression;
import org.eximeebpms.bpm.model.bpmn.instance.MessageEventDefinition;
import org.eximeebpms.bpm.model.bpmn.instance.ParallelGateway;
import org.eximeebpms.bpm.model.bpmn.instance.Process;
import org.eximeebpms.bpm.model.bpmn.instance.ScriptTask;
import org.eximeebpms.bpm.model.bpmn.instance.SendTask;
import org.eximeebpms.bpm.model.bpmn.instance.SequenceFlow;
import org.eximeebpms.bpm.model.bpmn.instance.ServiceTask;
import org.eximeebpms.bpm.model.bpmn.instance.StartEvent;
import org.eximeebpms.bpm.model.bpmn.instance.TimerEventDefinition;
import org.eximeebpms.bpm.model.bpmn.instance.UserTask;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsConnector;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsConnectorId;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsConstraint;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsEntry;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsExecutionListener;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsFailedJobRetryTimeCycle;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsField;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsFormData;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsFormField;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsFormProperty;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsIn;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsInputOutput;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsInputParameter;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsList;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsMap;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsOut;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsOutputParameter;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsPotentialStarter;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsProperties;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsProperty;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsScript;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsTaskListener;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Sebastian Menski
 * @author Ronny Bräunlich
 */
@RunWith(Parameterized.class)
public class EximeeBpmsExtensionsTest {

  private Process process;
  private StartEvent startEvent;
  private SequenceFlow sequenceFlow;
  private UserTask userTask;
  private ServiceTask serviceTask;
  private SendTask sendTask;
  private ScriptTask scriptTask;
  private CallActivity callActivity;
  private BusinessRuleTask businessRuleTask;
  private EndEvent endEvent;
  private MessageEventDefinition messageEventDefinition;
  private ParallelGateway parallelGateway;
  private String namespace;
  private BpmnModelInstance originalModelInstance;
  private BpmnModelInstance modelInstance;
  private Error error;

  @Parameters(name="Namespace: {0}")
  public static Collection<Object[]> parameters(){
    return Arrays.asList(new Object[][]{
        {CAMUNDA_NS, Bpmn.readModelFromStream(EximeeBpmsExtensionsTest.class.getResourceAsStream("EximeeBpmsExtensionsTest.xml"))},
        //for compatability reasons we gotta check the old namespace, too
        {ACTIVITI_NS, Bpmn.readModelFromStream(EximeeBpmsExtensionsTest.class.getResourceAsStream("CamundaExtensionsCompatabilityTest.xml"))}
    });
  }

  public EximeeBpmsExtensionsTest(String namespace, BpmnModelInstance modelInstance) {
    this.namespace = namespace;
    this.originalModelInstance = modelInstance;
  }

  @Before
  public void setUp(){
    modelInstance = originalModelInstance.clone();
    process = modelInstance.getModelElementById(PROCESS_ID);
    startEvent = modelInstance.getModelElementById(START_EVENT_ID);
    sequenceFlow = modelInstance.getModelElementById(SEQUENCE_FLOW_ID);
    userTask = modelInstance.getModelElementById(USER_TASK_ID);
    serviceTask = modelInstance.getModelElementById(SERVICE_TASK_ID);
    sendTask = modelInstance.getModelElementById(SEND_TASK_ID);
    scriptTask = modelInstance.getModelElementById(SCRIPT_TASK_ID);
    callActivity = modelInstance.getModelElementById(CALL_ACTIVITY_ID);
    businessRuleTask = modelInstance.getModelElementById(BUSINESS_RULE_TASK);
    endEvent = modelInstance.getModelElementById(END_EVENT_ID);
    messageEventDefinition = (MessageEventDefinition) endEvent.getEventDefinitions().iterator().next();
    parallelGateway = modelInstance.getModelElementById("parallelGateway");
    error = modelInstance.getModelElementById("error");
  }

  @Test
  public void testAssignee() {
    assertThat(userTask.getEximeeBpmsAssignee()).isEqualTo(TEST_STRING_XML);
    userTask.setEximeeBpmsAssignee(TEST_STRING_API);
    assertThat(userTask.getEximeeBpmsAssignee()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testAsyncBefore() {
    assertThat(startEvent.isEximeeBpmsAsyncBefore()).isTrue();
    assertThat(endEvent.isEximeeBpmsAsyncBefore()).isTrue();
    assertThat(userTask.isEximeeBpmsAsyncBefore()).isTrue();
    assertThat(parallelGateway.isEximeeBpmsAsyncBefore()).isTrue();

    startEvent.setEximeeBpmsAsyncBefore(false);
    endEvent.setEximeeBpmsAsyncBefore(false);
    userTask.setEximeeBpmsAsyncBefore(false);
    parallelGateway.setEximeeBpmsAsyncBefore(false);

    assertThat(startEvent.isEximeeBpmsAsyncBefore()).isFalse();
    assertThat(endEvent.isEximeeBpmsAsyncBefore()).isFalse();
    assertThat(userTask.isEximeeBpmsAsyncBefore()).isFalse();
    assertThat(parallelGateway.isEximeeBpmsAsyncBefore()).isFalse();
  }

  @Test
  public void testAsyncAfter() {
    assertThat(startEvent.isEximeeBpmsAsyncAfter()).isTrue();
    assertThat(endEvent.isEximeeBpmsAsyncAfter()).isTrue();
    assertThat(userTask.isEximeeBpmsAsyncAfter()).isTrue();
    assertThat(parallelGateway.isEximeeBpmsAsyncAfter()).isTrue();

    startEvent.setEximeeBpmsAsyncAfter(false);
    endEvent.setEximeeBpmsAsyncAfter(false);
    userTask.setEximeeBpmsAsyncAfter(false);
    parallelGateway.setEximeeBpmsAsyncAfter(false);

    assertThat(startEvent.isEximeeBpmsAsyncAfter()).isFalse();
    assertThat(endEvent.isEximeeBpmsAsyncAfter()).isFalse();
    assertThat(userTask.isEximeeBpmsAsyncAfter()).isFalse();
    assertThat(parallelGateway.isEximeeBpmsAsyncAfter()).isFalse();
  }

  @Test
  public void testFlowNodeJobPriority() {
    assertThat(startEvent.getEximeeBpmsJobPriority()).isEqualTo(TEST_FLOW_NODE_JOB_PRIORITY);
    assertThat(endEvent.getEximeeBpmsJobPriority()).isEqualTo(TEST_FLOW_NODE_JOB_PRIORITY);
    assertThat(userTask.getEximeeBpmsJobPriority()).isEqualTo(TEST_FLOW_NODE_JOB_PRIORITY);
    assertThat(parallelGateway.getEximeeBpmsJobPriority()).isEqualTo(TEST_FLOW_NODE_JOB_PRIORITY);
  }

  @Test
  public void testProcessJobPriority() {
    assertThat(process.getEximeeBpmsJobPriority()).isEqualTo(TEST_PROCESS_JOB_PRIORITY);
  }

  @Test
  public void testProcessTaskPriority() {
    assertThat(process.getEximeeBpmsTaskPriority()).isEqualTo(TEST_PROCESS_TASK_PRIORITY);
  }

  @Test
  public void testHistoryTimeToLive() {
    assertThat(process.getEximeeBpmsHistoryTimeToLiveString()).isEqualTo(String.valueOf(TEST_HISTORY_TIME_TO_LIVE));
  }

  @Test
  public void testIsStartableInTasklist() {
    assertThat(process.isEximeeBpmsStartableInTasklist()).isFalse();
  }

  @Test
  public void testVersionTag() {
    assertThat(process.getEximeeBpmsVersionTag()).isEqualTo("v1.0.0");
  }

  @Test
  public void testServiceTaskPriority() {
    assertThat(serviceTask.getEximeeBpmsTaskPriority()).isEqualTo(TEST_SERVICE_TASK_PRIORITY);
  }

  @Test
  public void testCalledElementBinding() {
    assertThat(callActivity.getEximeeBpmsCalledElementBinding()).isEqualTo(TEST_STRING_XML);
    callActivity.setEximeeBpmsCalledElementBinding(TEST_STRING_API);
    assertThat(callActivity.getEximeeBpmsCalledElementBinding()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testCalledElementVersion() {
    assertThat(callActivity.getEximeeBpmsCalledElementVersion()).isEqualTo(TEST_STRING_XML);
    callActivity.setEximeeBpmsCalledElementVersion(TEST_STRING_API);
    assertThat(callActivity.getEximeeBpmsCalledElementVersion()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testCalledElementVersionTag() {
    assertThat(callActivity.getEximeeBpmsCalledElementVersionTag()).isEqualTo(TEST_STRING_XML);
    callActivity.setEximeeBpmsCalledElementVersionTag(TEST_STRING_API);
    assertThat(callActivity.getEximeeBpmsCalledElementVersionTag()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testCalledElementTenantId() {
    assertThat(callActivity.getEximeeBpmsCalledElementTenantId()).isEqualTo(TEST_STRING_XML);
    callActivity.setEximeeBpmsCalledElementTenantId(TEST_STRING_API);
    assertThat(callActivity.getEximeeBpmsCalledElementTenantId()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testCaseRef() {
    assertThat(callActivity.getEximeeBpmsCaseRef()).isEqualTo(TEST_STRING_XML);
    callActivity.setEximeeBpmsCaseRef(TEST_STRING_API);
    assertThat(callActivity.getEximeeBpmsCaseRef()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testCaseBinding() {
    assertThat(callActivity.getEximeeBpmsCaseBinding()).isEqualTo(TEST_STRING_XML);
    callActivity.setEximeeBpmsCaseBinding(TEST_STRING_API);
    assertThat(callActivity.getEximeeBpmsCaseBinding()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testCaseVersion() {
    assertThat(callActivity.getEximeeBpmsCaseVersion()).isEqualTo(TEST_STRING_XML);
    callActivity.setEximeeBpmsCaseVersion(TEST_STRING_API);
    assertThat(callActivity.getEximeeBpmsCaseVersion()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testCaseTenantId() {
    assertThat(callActivity.getEximeeBpmsCaseTenantId()).isEqualTo(TEST_STRING_XML);
    callActivity.setEximeeBpmsCaseTenantId(TEST_STRING_API);
    assertThat(callActivity.getEximeeBpmsCaseTenantId()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testDecisionRef() {
    assertThat(businessRuleTask.getEximeeBpmsDecisionRef()).isEqualTo(TEST_STRING_XML);
    businessRuleTask.setEximeeBpmsDecisionRef(TEST_STRING_API);
    assertThat(businessRuleTask.getEximeeBpmsDecisionRef()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testDecisionRefBinding() {
    assertThat(businessRuleTask.getEximeeBpmsDecisionRefBinding()).isEqualTo(TEST_STRING_XML);
    businessRuleTask.setEximeeBpmsDecisionRefBinding(TEST_STRING_API);
    assertThat(businessRuleTask.getEximeeBpmsDecisionRefBinding()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testDecisionRefVersion() {
    assertThat(businessRuleTask.getEximeeBpmsDecisionRefVersion()).isEqualTo(TEST_STRING_XML);
    businessRuleTask.setEximeeBpmsDecisionRefVersion(TEST_STRING_API);
    assertThat(businessRuleTask.getEximeeBpmsDecisionRefVersion()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testDecisionRefVersionTag() {
    assertThat(businessRuleTask.getEximeeBpmsDecisionRefVersionTag()).isEqualTo(TEST_STRING_XML);
    businessRuleTask.setEximeeBpmsDecisionRefVersionTag(TEST_STRING_API);
    assertThat(businessRuleTask.getEximeeBpmsDecisionRefVersionTag()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testDecisionRefTenantId() {
    assertThat(businessRuleTask.getEximeeBpmsDecisionRefTenantId()).isEqualTo(TEST_STRING_XML);
    businessRuleTask.setEximeeBpmsDecisionRefTenantId(TEST_STRING_API);
    assertThat(businessRuleTask.getEximeeBpmsDecisionRefTenantId()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testMapDecisionResult() {
    assertThat(businessRuleTask.getEximeeBpmsMapDecisionResult()).isEqualTo(TEST_STRING_XML);
    businessRuleTask.setEximeeBpmsMapDecisionResult(TEST_STRING_API);
    assertThat(businessRuleTask.getEximeeBpmsMapDecisionResult()).isEqualTo(TEST_STRING_API);
  }


  @Test
  public void testTaskPriority() {
    assertThat(businessRuleTask.getEximeeBpmsTaskPriority()).isEqualTo(TEST_STRING_XML);
    businessRuleTask.setEximeeBpmsTaskPriority(TEST_SERVICE_TASK_PRIORITY);
    assertThat(businessRuleTask.getEximeeBpmsTaskPriority()).isEqualTo(TEST_SERVICE_TASK_PRIORITY);
  }

  @Test
  public void testCandidateGroups() {
    assertThat(userTask.getEximeeBpmsCandidateGroups()).isEqualTo(TEST_GROUPS_XML);
    assertThat(userTask.getEximeeBpmsCandidateGroupsList()).containsAll(TEST_GROUPS_LIST_XML);
    userTask.setEximeeBpmsCandidateGroups(TEST_GROUPS_API);
    assertThat(userTask.getEximeeBpmsCandidateGroups()).isEqualTo(TEST_GROUPS_API);
    assertThat(userTask.getEximeeBpmsCandidateGroupsList()).containsAll(TEST_GROUPS_LIST_API);
    userTask.setEximeeBpmsCandidateGroupsList(TEST_GROUPS_LIST_XML);
    assertThat(userTask.getEximeeBpmsCandidateGroups()).isEqualTo(TEST_GROUPS_XML);
    assertThat(userTask.getEximeeBpmsCandidateGroupsList()).containsAll(TEST_GROUPS_LIST_XML);
  }

  @Test
  public void testCandidateStarterGroups() {
    assertThat(process.getEximeeBpmsCandidateStarterGroups()).isEqualTo(TEST_GROUPS_XML);
    assertThat(process.getEximeeBpmsCandidateStarterGroupsList()).containsAll(TEST_GROUPS_LIST_XML);
    process.setEximeeBpmsCandidateStarterGroups(TEST_GROUPS_API);
    assertThat(process.getEximeeBpmsCandidateStarterGroups()).isEqualTo(TEST_GROUPS_API);
    assertThat(process.getEximeeBpmsCandidateStarterGroupsList()).containsAll(TEST_GROUPS_LIST_API);
    process.setEximeeBpmsCandidateStarterGroupsList(TEST_GROUPS_LIST_XML);
    assertThat(process.getEximeeBpmsCandidateStarterGroups()).isEqualTo(TEST_GROUPS_XML);
    assertThat(process.getEximeeBpmsCandidateStarterGroupsList()).containsAll(TEST_GROUPS_LIST_XML);
  }

  @Test
  public void testCandidateStarterUsers() {
    assertThat(process.getEximeeBpmsCandidateStarterUsers()).isEqualTo(TEST_USERS_XML);
    assertThat(process.getEximeeBpmsCandidateStarterUsersList()).containsAll(TEST_USERS_LIST_XML);
    process.setEximeeBpmsCandidateStarterUsers(TEST_USERS_API);
    assertThat(process.getEximeeBpmsCandidateStarterUsers()).isEqualTo(TEST_USERS_API);
    assertThat(process.getEximeeBpmsCandidateStarterUsersList()).containsAll(TEST_USERS_LIST_API);
    process.setEximeeBpmsCandidateStarterUsersList(TEST_USERS_LIST_XML);
    assertThat(process.getEximeeBpmsCandidateStarterUsers()).isEqualTo(TEST_USERS_XML);
    assertThat(process.getEximeeBpmsCandidateStarterUsersList()).containsAll(TEST_USERS_LIST_XML);
  }

  @Test
  public void testCandidateUsers() {
    assertThat(userTask.getEximeeBpmsCandidateUsers()).isEqualTo(TEST_USERS_XML);
    assertThat(userTask.getEximeeBpmsCandidateUsersList()).containsAll(TEST_USERS_LIST_XML);
    userTask.setEximeeBpmsCandidateUsers(TEST_USERS_API);
    assertThat(userTask.getEximeeBpmsCandidateUsers()).isEqualTo(TEST_USERS_API);
    assertThat(userTask.getEximeeBpmsCandidateUsersList()).containsAll(TEST_USERS_LIST_API);
    userTask.setEximeeBpmsCandidateUsersList(TEST_USERS_LIST_XML);
    assertThat(userTask.getEximeeBpmsCandidateUsers()).isEqualTo(TEST_USERS_XML);
    assertThat(userTask.getEximeeBpmsCandidateUsersList()).containsAll(TEST_USERS_LIST_XML);
  }

  @Test
  public void testClass() {
    assertThat(serviceTask.getEximeeBpmsClass()).isEqualTo(TEST_CLASS_XML);
    assertThat(messageEventDefinition.getEximeeBpmsClass()).isEqualTo(TEST_CLASS_XML);

    serviceTask.setEximeeBpmsClass(TEST_CLASS_API);
    messageEventDefinition.setEximeeBpmsClass(TEST_CLASS_API);

    assertThat(serviceTask.getEximeeBpmsClass()).isEqualTo(TEST_CLASS_API);
    assertThat(messageEventDefinition.getEximeeBpmsClass()).isEqualTo(TEST_CLASS_API);
  }

  @Test
  public void testDelegateExpression() {
    assertThat(serviceTask.getEximeeBpmsDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_XML);
    assertThat(messageEventDefinition.getEximeeBpmsDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_XML);

    serviceTask.setEximeeBpmsDelegateExpression(TEST_DELEGATE_EXPRESSION_API);
    messageEventDefinition.setEximeeBpmsDelegateExpression(TEST_DELEGATE_EXPRESSION_API);

    assertThat(serviceTask.getEximeeBpmsDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_API);
    assertThat(messageEventDefinition.getEximeeBpmsDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_API);
  }

  @Test
  public void testDueDate() {
    assertThat(userTask.getEximeeBpmsDueDate()).isEqualTo(TEST_DUE_DATE_XML);
    userTask.setEximeeBpmsDueDate(TEST_DUE_DATE_API);
    assertThat(userTask.getEximeeBpmsDueDate()).isEqualTo(TEST_DUE_DATE_API);
  }

  @Test
  public void testErrorCodeVariable(){
    ErrorEventDefinition errorEventDefinition = startEvent.getChildElementsByType(ErrorEventDefinition.class).iterator().next();
    assertThat(errorEventDefinition.getAttributeValueNs(namespace, CAMUNDA_ATTRIBUTE_ERROR_CODE_VARIABLE)).isEqualTo("errorVariable");
  }

  @Test
  public void testErrorMessageVariable(){
    ErrorEventDefinition errorEventDefinition = startEvent.getChildElementsByType(ErrorEventDefinition.class).iterator().next();
    assertThat(errorEventDefinition.getAttributeValueNs(namespace, CAMUNDA_ATTRIBUTE_ERROR_MESSAGE_VARIABLE)).isEqualTo("errorMessageVariable");
  }

  @Test
  public void testErrorMessage() {
    assertThat(error.getEximeeBpmsErrorMessage()).isEqualTo(TEST_STRING_XML);
    error.setEximeeBpmsErrorMessage(TEST_STRING_API);
    assertThat(error.getEximeeBpmsErrorMessage()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testExclusive() {
    assertThat(startEvent.isEximeeBpmsExclusive()).isTrue();
    assertThat(userTask.isEximeeBpmsExclusive()).isFalse();
    userTask.setEximeeBpmsExclusive(true);
    assertThat(userTask.isEximeeBpmsExclusive()).isTrue();
    assertThat(parallelGateway.isEximeeBpmsExclusive()).isTrue();
    parallelGateway.setEximeeBpmsExclusive(false);
    assertThat(parallelGateway.isEximeeBpmsExclusive()).isFalse();

    assertThat(callActivity.isEximeeBpmsExclusive()).isFalse();
    callActivity.setEximeeBpmsExclusive(true);
    assertThat(callActivity.isEximeeBpmsExclusive()).isTrue();
  }

  @Test
  public void testExpression() {
    assertThat(serviceTask.getEximeeBpmsExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(messageEventDefinition.getEximeeBpmsExpression()).isEqualTo(TEST_EXPRESSION_XML);
    serviceTask.setEximeeBpmsExpression(TEST_EXPRESSION_API);
    messageEventDefinition.setEximeeBpmsExpression(TEST_EXPRESSION_API);
    assertThat(serviceTask.getEximeeBpmsExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(messageEventDefinition.getEximeeBpmsExpression()).isEqualTo(TEST_EXPRESSION_API);
  }

  @Test
  public void testFormHandlerClass() {
    assertThat(startEvent.getEximeeBpmsFormHandlerClass()).isEqualTo(TEST_CLASS_XML);
    assertThat(userTask.getEximeeBpmsFormHandlerClass()).isEqualTo(TEST_CLASS_XML);
    startEvent.setEximeeBpmsFormHandlerClass(TEST_CLASS_API);
    userTask.setEximeeBpmsFormHandlerClass(TEST_CLASS_API);
    assertThat(startEvent.getEximeeBpmsFormHandlerClass()).isEqualTo(TEST_CLASS_API);
    assertThat(userTask.getEximeeBpmsFormHandlerClass()).isEqualTo(TEST_CLASS_API);
  }

  @Test
  public void testFormKey() {
    assertThat(startEvent.getEximeeBpmsFormKey()).isEqualTo(TEST_STRING_XML);
    assertThat(userTask.getEximeeBpmsFormKey()).isEqualTo(TEST_STRING_XML);
    startEvent.setEximeeBpmsFormKey(TEST_STRING_API);
    userTask.setEximeeBpmsFormKey(TEST_STRING_API);
    assertThat(startEvent.getEximeeBpmsFormKey()).isEqualTo(TEST_STRING_API);
    assertThat(userTask.getEximeeBpmsFormKey()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testInitiator() {
    assertThat(startEvent.getEximeeBpmsInitiator()).isEqualTo(TEST_STRING_XML);
    startEvent.setEximeeBpmsInitiator(TEST_STRING_API);
    assertThat(startEvent.getEximeeBpmsInitiator()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testPriority() {
    assertThat(userTask.getEximeeBpmsPriority()).isEqualTo(TEST_PRIORITY_XML);
    userTask.setEximeeBpmsPriority(TEST_PRIORITY_API);
    assertThat(userTask.getEximeeBpmsPriority()).isEqualTo(TEST_PRIORITY_API);
  }

  @Test
  public void testResultVariable() {
    assertThat(serviceTask.getEximeeBpmsResultVariable()).isEqualTo(TEST_STRING_XML);
    assertThat(messageEventDefinition.getEximeeBpmsResultVariable()).isEqualTo(TEST_STRING_XML);
    serviceTask.setEximeeBpmsResultVariable(TEST_STRING_API);
    messageEventDefinition.setEximeeBpmsResultVariable(TEST_STRING_API);
    assertThat(serviceTask.getEximeeBpmsResultVariable()).isEqualTo(TEST_STRING_API);
    assertThat(messageEventDefinition.getEximeeBpmsResultVariable()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testType() {
    assertThat(serviceTask.getEximeeBpmsType()).isEqualTo(TEST_TYPE_XML);
    assertThat(messageEventDefinition.getEximeeBpmsType()).isEqualTo(TEST_STRING_XML);
    serviceTask.setEximeeBpmsType(TEST_TYPE_API);
    messageEventDefinition.setEximeeBpmsType(TEST_STRING_API);
    assertThat(serviceTask.getEximeeBpmsType()).isEqualTo(TEST_TYPE_API);
    assertThat(messageEventDefinition.getEximeeBpmsType()).isEqualTo(TEST_STRING_API);

  }

  @Test
  public void testTopic() {
    assertThat(serviceTask.getEximeeBpmsTopic()).isEqualTo(TEST_STRING_XML);
    assertThat(messageEventDefinition.getEximeeBpmsTopic()).isEqualTo(TEST_STRING_XML);
    serviceTask.setEximeeBpmsTopic(TEST_TYPE_API);
    messageEventDefinition.setEximeeBpmsTopic(TEST_STRING_API);
    assertThat(serviceTask.getEximeeBpmsTopic()).isEqualTo(TEST_TYPE_API);
    assertThat(messageEventDefinition.getEximeeBpmsTopic()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testVariableMappingClass() {
    assertThat(callActivity.getEximeeBpmsVariableMappingClass()).isEqualTo(TEST_CLASS_XML);
    callActivity.setEximeeBpmsVariableMappingClass(TEST_CLASS_API);
    assertThat(callActivity.getEximeeBpmsVariableMappingClass()).isEqualTo(TEST_CLASS_API);
  }

  @Test
  public void testVariableMappingDelegateExpression() {
    assertThat(callActivity.getEximeeBpmsVariableMappingDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_XML);
    callActivity.setEximeeBpmsVariableMappingDelegateExpression(TEST_DELEGATE_EXPRESSION_API);
    assertThat(callActivity.getEximeeBpmsVariableMappingDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_API);
  }

  @Test
  public void testExecutionListenerExtension() {
    EximeeBpmsExecutionListener processListener = process.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsExecutionListener.class).singleResult();
    EximeeBpmsExecutionListener startEventListener = startEvent.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsExecutionListener.class).singleResult();
    EximeeBpmsExecutionListener serviceTaskListener = serviceTask.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsExecutionListener.class).singleResult();
    assertThat(processListener.getEximeeBpmsClass()).isEqualTo(TEST_CLASS_XML);
    assertThat(processListener.getEximeeBpmsEvent()).isEqualTo(TEST_EXECUTION_EVENT_XML);
    assertThat(startEventListener.getEximeeBpmsExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(startEventListener.getEximeeBpmsEvent()).isEqualTo(TEST_EXECUTION_EVENT_XML);
    assertThat(serviceTaskListener.getEximeeBpmsDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_XML);
    assertThat(serviceTaskListener.getEximeeBpmsEvent()).isEqualTo(TEST_EXECUTION_EVENT_XML);
    processListener.setEximeeBpmsClass(TEST_CLASS_API);
    processListener.setEximeeBpmsEvent(TEST_EXECUTION_EVENT_API);
    startEventListener.setEximeeBpmsExpression(TEST_EXPRESSION_API);
    startEventListener.setEximeeBpmsEvent(TEST_EXECUTION_EVENT_API);
    serviceTaskListener.setEximeeBpmsDelegateExpression(TEST_DELEGATE_EXPRESSION_API);
    serviceTaskListener.setEximeeBpmsEvent(TEST_EXECUTION_EVENT_API);
    assertThat(processListener.getEximeeBpmsClass()).isEqualTo(TEST_CLASS_API);
    assertThat(processListener.getEximeeBpmsEvent()).isEqualTo(TEST_EXECUTION_EVENT_API);
    assertThat(startEventListener.getEximeeBpmsExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(startEventListener.getEximeeBpmsEvent()).isEqualTo(TEST_EXECUTION_EVENT_API);
    assertThat(serviceTaskListener.getEximeeBpmsDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_API);
    assertThat(serviceTaskListener.getEximeeBpmsEvent()).isEqualTo(TEST_EXECUTION_EVENT_API);
  }

  @Test
  public void testEximeeBpmsScriptExecutionListener() {
    EximeeBpmsExecutionListener sequenceFlowListener = sequenceFlow.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsExecutionListener.class).singleResult();

    EximeeBpmsScript script = sequenceFlowListener.getEximeeBpmsScript();
    assertThat(script.getEximeeBpmsScriptFormat()).isEqualTo("groovy");
    assertThat(script.getEximeeBpmsResource()).isNull();
    assertThat(script.getTextContent()).isEqualTo("println 'Hello World'");

    EximeeBpmsScript newScript = modelInstance.newInstance(EximeeBpmsScript.class);
    newScript.setEximeeBpmsScriptFormat("groovy");
    newScript.setEximeeBpmsResource("test.groovy");
    sequenceFlowListener.setEximeeBpmsScript(newScript);

    script = sequenceFlowListener.getEximeeBpmsScript();
    assertThat(script.getEximeeBpmsScriptFormat()).isEqualTo("groovy");
    assertThat(script.getEximeeBpmsResource()).isEqualTo("test.groovy");
    assertThat(script.getTextContent()).isEmpty();
  }

  @Test
  public void testFailedJobRetryTimeCycleExtension() {
    EximeeBpmsFailedJobRetryTimeCycle timeCycle = sendTask.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsFailedJobRetryTimeCycle.class).singleResult();
    assertThat(timeCycle.getTextContent()).isEqualTo(TEST_STRING_XML);
    timeCycle.setTextContent(TEST_STRING_API);
    assertThat(timeCycle.getTextContent()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testFieldExtension() {
    EximeeBpmsField field = sendTask.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsField.class).singleResult();
    assertThat(field.getEximeeBpmsName()).isEqualTo(TEST_STRING_XML);
    assertThat(field.getEximeeBpmsExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(field.getEximeeBpmsStringValue()).isEqualTo(TEST_STRING_XML);
    assertThat(field.getEximeeBpmsExpressionChild().getTextContent()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(field.getEximeeBpmsString().getTextContent()).isEqualTo(TEST_STRING_XML);
    field.setEximeeBpmsName(TEST_STRING_API);
    field.setEximeeBpmsExpression(TEST_EXPRESSION_API);
    field.setEximeeBpmsStringValue(TEST_STRING_API);
    field.getEximeeBpmsExpressionChild().setTextContent(TEST_EXPRESSION_API);
    field.getEximeeBpmsString().setTextContent(TEST_STRING_API);
    assertThat(field.getEximeeBpmsName()).isEqualTo(TEST_STRING_API);
    assertThat(field.getEximeeBpmsExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(field.getEximeeBpmsStringValue()).isEqualTo(TEST_STRING_API);
    assertThat(field.getEximeeBpmsExpressionChild().getTextContent()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(field.getEximeeBpmsString().getTextContent()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testFormData() {
    EximeeBpmsFormData formData = userTask.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsFormData.class).singleResult();
    EximeeBpmsFormField formField = formData.getEximeeBpmsFormFields().iterator().next();
    assertThat(formField.getEximeeBpmsId()).isEqualTo(TEST_STRING_XML);
    assertThat(formField.getEximeeBpmsLabel()).isEqualTo(TEST_STRING_XML);
    assertThat(formField.getEximeeBpmsType()).isEqualTo(TEST_STRING_XML);
    assertThat(formField.getEximeeBpmsDatePattern()).isEqualTo(TEST_STRING_XML);
    assertThat(formField.getEximeeBpmsDefaultValue()).isEqualTo(TEST_STRING_XML);
    formField.setEximeeBpmsId(TEST_STRING_API);
    formField.setEximeeBpmsLabel(TEST_STRING_API);
    formField.setEximeeBpmsType(TEST_STRING_API);
    formField.setEximeeBpmsDatePattern(TEST_STRING_API);
    formField.setEximeeBpmsDefaultValue(TEST_STRING_API);
    assertThat(formField.getEximeeBpmsId()).isEqualTo(TEST_STRING_API);
    assertThat(formField.getEximeeBpmsLabel()).isEqualTo(TEST_STRING_API);
    assertThat(formField.getEximeeBpmsType()).isEqualTo(TEST_STRING_API);
    assertThat(formField.getEximeeBpmsDatePattern()).isEqualTo(TEST_STRING_API);
    assertThat(formField.getEximeeBpmsDefaultValue()).isEqualTo(TEST_STRING_API);

    EximeeBpmsProperty property = formField.getEximeeBpmsProperties().getEximeeBpmsProperties().iterator().next();
    assertThat(property.getEximeeBpmsId()).isEqualTo(TEST_STRING_XML);
    assertThat(property.getEximeeBpmsValue()).isEqualTo(TEST_STRING_XML);
    property.setEximeeBpmsId(TEST_STRING_API);
    property.setEximeeBpmsValue(TEST_STRING_API);
    assertThat(property.getEximeeBpmsId()).isEqualTo(TEST_STRING_API);
    assertThat(property.getEximeeBpmsValue()).isEqualTo(TEST_STRING_API);

    EximeeBpmsConstraint constraint = formField.getEximeeBpmsValidation().getEximeeBpmsConstraints().iterator().next();
    assertThat(constraint.getEximeeBpmsName()).isEqualTo(TEST_STRING_XML);
    assertThat(constraint.getEximeeBpmsConfig()).isEqualTo(TEST_STRING_XML);
    constraint.setEximeeBpmsName(TEST_STRING_API);
    constraint.setEximeeBpmsConfig(TEST_STRING_API);
    assertThat(constraint.getEximeeBpmsName()).isEqualTo(TEST_STRING_API);
    assertThat(constraint.getEximeeBpmsConfig()).isEqualTo(TEST_STRING_API);

    EximeeBpmsValue value = formField.getEximeeBpmsValues().iterator().next();
    assertThat(value.getEximeeBpmsId()).isEqualTo(TEST_STRING_XML);
    assertThat(value.getEximeeBpmsName()).isEqualTo(TEST_STRING_XML);
    value.setEximeeBpmsId(TEST_STRING_API);
    value.setEximeeBpmsName(TEST_STRING_API);
    assertThat(value.getEximeeBpmsId()).isEqualTo(TEST_STRING_API);
    assertThat(value.getEximeeBpmsName()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testFormProperty() {
    EximeeBpmsFormProperty formProperty = startEvent.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsFormProperty.class).singleResult();
    assertThat(formProperty.getEximeeBpmsId()).isEqualTo(TEST_STRING_XML);
    assertThat(formProperty.getEximeeBpmsName()).isEqualTo(TEST_STRING_XML);
    assertThat(formProperty.getEximeeBpmsType()).isEqualTo(TEST_STRING_XML);
    assertThat(formProperty.isEximeeBpmsRequired()).isFalse();
    assertThat(formProperty.isEximeeBpmsReadable()).isTrue();
    assertThat(formProperty.isEximeeBpmsWriteable()).isTrue();
    assertThat(formProperty.getEximeeBpmsVariable()).isEqualTo(TEST_STRING_XML);
    assertThat(formProperty.getEximeeBpmsExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(formProperty.getEximeeBpmsDatePattern()).isEqualTo(TEST_STRING_XML);
    assertThat(formProperty.getEximeeBpmsDefault()).isEqualTo(TEST_STRING_XML);
    formProperty.setEximeeBpmsId(TEST_STRING_API);
    formProperty.setEximeeBpmsName(TEST_STRING_API);
    formProperty.setEximeeBpmsType(TEST_STRING_API);
    formProperty.setEximeeBpmsRequired(true);
    formProperty.setEximeeBpmsReadable(false);
    formProperty.setEximeeBpmsWriteable(false);
    formProperty.setEximeeBpmsVariable(TEST_STRING_API);
    formProperty.setEximeeBpmsExpression(TEST_EXPRESSION_API);
    formProperty.setEximeeBpmsDatePattern(TEST_STRING_API);
    formProperty.setEximeeBpmsDefault(TEST_STRING_API);
    assertThat(formProperty.getEximeeBpmsId()).isEqualTo(TEST_STRING_API);
    assertThat(formProperty.getEximeeBpmsName()).isEqualTo(TEST_STRING_API);
    assertThat(formProperty.getEximeeBpmsType()).isEqualTo(TEST_STRING_API);
    assertThat(formProperty.isEximeeBpmsRequired()).isTrue();
    assertThat(formProperty.isEximeeBpmsReadable()).isFalse();
    assertThat(formProperty.isEximeeBpmsWriteable()).isFalse();
    assertThat(formProperty.getEximeeBpmsVariable()).isEqualTo(TEST_STRING_API);
    assertThat(formProperty.getEximeeBpmsExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(formProperty.getEximeeBpmsDatePattern()).isEqualTo(TEST_STRING_API);
    assertThat(formProperty.getEximeeBpmsDefault()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testInExtension() {
    EximeeBpmsIn in = callActivity.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsIn.class).singleResult();
    assertThat(in.getEximeeBpmsSource()).isEqualTo(TEST_STRING_XML);
    assertThat(in.getEximeeBpmsSourceExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(in.getEximeeBpmsVariables()).isEqualTo(TEST_STRING_XML);
    assertThat(in.getEximeeBpmsTarget()).isEqualTo(TEST_STRING_XML);
    assertThat(in.getEximeeBpmsBusinessKey()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(in.getEximeeBpmsLocal()).isTrue();
    in.setEximeeBpmsSource(TEST_STRING_API);
    in.setEximeeBpmsSourceExpression(TEST_EXPRESSION_API);
    in.setEximeeBpmsVariables(TEST_STRING_API);
    in.setEximeeBpmsTarget(TEST_STRING_API);
    in.setEximeeBpmsBusinessKey(TEST_EXPRESSION_API);
    in.setEximeeBpmsLocal(false);
    assertThat(in.getEximeeBpmsSource()).isEqualTo(TEST_STRING_API);
    assertThat(in.getEximeeBpmsSourceExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(in.getEximeeBpmsVariables()).isEqualTo(TEST_STRING_API);
    assertThat(in.getEximeeBpmsTarget()).isEqualTo(TEST_STRING_API);
    assertThat(in.getEximeeBpmsBusinessKey()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(in.getEximeeBpmsLocal()).isFalse();
  }

  @Test
  public void testOutExtension() {
    EximeeBpmsOut out = callActivity.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsOut.class).singleResult();
    assertThat(out.getEximeeBpmsSource()).isEqualTo(TEST_STRING_XML);
    assertThat(out.getEximeeBpmsSourceExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(out.getEximeeBpmsVariables()).isEqualTo(TEST_STRING_XML);
    assertThat(out.getEximeeBpmsTarget()).isEqualTo(TEST_STRING_XML);
    assertThat(out.getEximeeBpmsLocal()).isTrue();
    out.setEximeeBpmsSource(TEST_STRING_API);
    out.setEximeeBpmsSourceExpression(TEST_EXPRESSION_API);
    out.setEximeeBpmsVariables(TEST_STRING_API);
    out.setEximeeBpmsTarget(TEST_STRING_API);
    out.setEximeeBpmsLocal(false);
    assertThat(out.getEximeeBpmsSource()).isEqualTo(TEST_STRING_API);
    assertThat(out.getEximeeBpmsSourceExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(out.getEximeeBpmsVariables()).isEqualTo(TEST_STRING_API);
    assertThat(out.getEximeeBpmsTarget()).isEqualTo(TEST_STRING_API);
    assertThat(out.getEximeeBpmsLocal()).isFalse();
  }

  @Test
  public void testPotentialStarter() {
    EximeeBpmsPotentialStarter potentialStarter = startEvent.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsPotentialStarter.class).singleResult();
    Expression expression = potentialStarter.getResourceAssignmentExpression().getExpression();
    assertThat(expression.getTextContent()).isEqualTo(TEST_GROUPS_XML);
    expression.setTextContent(TEST_GROUPS_API);
    assertThat(expression.getTextContent()).isEqualTo(TEST_GROUPS_API);
  }

  @Test
  public void testTaskListener() {
    EximeeBpmsTaskListener taskListener = userTask.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsTaskListener.class).list().get(0);
    assertThat(taskListener.getEximeeBpmsEvent()).isEqualTo(TEST_TASK_EVENT_XML);
    assertThat(taskListener.getEximeeBpmsClass()).isEqualTo(TEST_CLASS_XML);
    assertThat(taskListener.getEximeeBpmsExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(taskListener.getEximeeBpmsDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_XML);
    taskListener.setEximeeBpmsEvent(TEST_TASK_EVENT_API);
    taskListener.setEximeeBpmsClass(TEST_CLASS_API);
    taskListener.setEximeeBpmsExpression(TEST_EXPRESSION_API);
    taskListener.setEximeeBpmsDelegateExpression(TEST_DELEGATE_EXPRESSION_API);
    assertThat(taskListener.getEximeeBpmsEvent()).isEqualTo(TEST_TASK_EVENT_API);
    assertThat(taskListener.getEximeeBpmsClass()).isEqualTo(TEST_CLASS_API);
    assertThat(taskListener.getEximeeBpmsExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(taskListener.getEximeeBpmsDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_API);

    EximeeBpmsField field = taskListener.getEximeeBpmsFields().iterator().next();
    assertThat(field.getEximeeBpmsName()).isEqualTo(TEST_STRING_XML);
    assertThat(field.getEximeeBpmsString().getTextContent()).isEqualTo(TEST_STRING_XML);

    Collection<TimerEventDefinition> timeouts = taskListener.getTimeouts();
    assertThat(timeouts.size()).isEqualTo(1);

    TimerEventDefinition timeout = timeouts.iterator().next();
    assertThat(timeout.getTimeCycle()).isNull();
    assertThat(timeout.getTimeDate()).isNull();
    assertThat(timeout.getTimeDuration()).isNotNull();
    assertThat(timeout.getTimeDuration().getRawTextContent()).isEqualTo("PT1H");
  }

  @Test
  public void testEximeeBpmsScriptTaskListener() {
    EximeeBpmsTaskListener taskListener = userTask.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsTaskListener.class).list().get(1);

    EximeeBpmsScript script = taskListener.getEximeeBpmsScript();
    assertThat(script.getEximeeBpmsScriptFormat()).isEqualTo("groovy");
    assertThat(script.getEximeeBpmsResource()).isEqualTo("test.groovy");
    assertThat(script.getTextContent()).isEmpty();

    EximeeBpmsScript newScript = modelInstance.newInstance(EximeeBpmsScript.class);
    newScript.setEximeeBpmsScriptFormat("groovy");
    newScript.setTextContent("println 'Hello World'");
    taskListener.setEximeeBpmsScript(newScript);

    script = taskListener.getEximeeBpmsScript();
    assertThat(script.getEximeeBpmsScriptFormat()).isEqualTo("groovy");
    assertThat(script.getEximeeBpmsResource()).isNull();
    assertThat(script.getTextContent()).isEqualTo("println 'Hello World'");
  }

  @Test
  public void testCamundaModelerProperties() {
    EximeeBpmsProperties camundaProperties = endEvent.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsProperties.class).singleResult();
    assertThat(camundaProperties).isNotNull();
    assertThat(camundaProperties.getEximeeBpmsProperties()).hasSize(2);

    for (EximeeBpmsProperty camundaProperty : camundaProperties.getEximeeBpmsProperties()) {
      assertThat(camundaProperty.getEximeeBpmsId()).isNull();
      assertThat(camundaProperty.getEximeeBpmsName()).startsWith("name");
      assertThat(camundaProperty.getEximeeBpmsValue()).startsWith("value");
    }
  }

  @Test
  public void testGetNonExistingCamundaCandidateUsers() {
    userTask.removeAttributeNs(namespace, "candidateUsers");
    assertThat(userTask.getEximeeBpmsCandidateUsers()).isNull();
    assertThat(userTask.getEximeeBpmsCandidateUsersList()).isEmpty();
  }

  @Test
  public void testSetNullCamundaCandidateUsers() {
    assertThat(userTask.getEximeeBpmsCandidateUsers()).isNotEmpty();
    assertThat(userTask.getEximeeBpmsCandidateUsersList()).isNotEmpty();
    userTask.setEximeeBpmsCandidateUsers(null);
    assertThat(userTask.getEximeeBpmsCandidateUsers()).isNull();
    assertThat(userTask.getEximeeBpmsCandidateUsersList()).isEmpty();
  }

  @Test
  public void testEmptyCamundaCandidateUsers() {
    assertThat(userTask.getEximeeBpmsCandidateUsers()).isNotEmpty();
    assertThat(userTask.getEximeeBpmsCandidateUsersList()).isNotEmpty();
    userTask.setEximeeBpmsCandidateUsers("");
    assertThat(userTask.getEximeeBpmsCandidateUsers()).isNull();
    assertThat(userTask.getEximeeBpmsCandidateUsersList()).isEmpty();
  }

  @Test
  public void testSetNullCamundaCandidateUsersList() {
    assertThat(userTask.getEximeeBpmsCandidateUsers()).isNotEmpty();
    assertThat(userTask.getEximeeBpmsCandidateUsersList()).isNotEmpty();
    userTask.setEximeeBpmsCandidateUsersList(null);
    assertThat(userTask.getEximeeBpmsCandidateUsers()).isNull();
    assertThat(userTask.getEximeeBpmsCandidateUsersList()).isEmpty();
  }

  @Test
  public void testEmptyCamundaCandidateUsersList() {
    assertThat(userTask.getEximeeBpmsCandidateUsers()).isNotEmpty();
    assertThat(userTask.getEximeeBpmsCandidateUsersList()).isNotEmpty();
    userTask.setEximeeBpmsCandidateUsersList(Collections.<String>emptyList());
    assertThat(userTask.getEximeeBpmsCandidateUsers()).isNull();
    assertThat(userTask.getEximeeBpmsCandidateUsersList()).isEmpty();
  }

  @Test
  public void testScriptResource() {
    assertThat(scriptTask.getScriptFormat()).isEqualTo("groovy");
    assertThat(scriptTask.getEximeeBpmsResource()).isEqualTo("test.groovy");
  }

  @Test
  public void testEximeeBpmsConnector() {
    EximeeBpmsConnector camundaConnector = serviceTask.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsConnector.class).singleResult();
    assertThat(camundaConnector).isNotNull();

    EximeeBpmsConnectorId camundaConnectorId = camundaConnector.getEximeeBpmsConnectorId();
    assertThat(camundaConnectorId).isNotNull();
    assertThat(camundaConnectorId.getTextContent()).isEqualTo("soap-http-connector");

    EximeeBpmsInputOutput camundaInputOutput = camundaConnector.getEximeeBpmsInputOutput();

    Collection<EximeeBpmsInputParameter> inputParameters = camundaInputOutput.getEximeeBpmsInputParameters();
    assertThat(inputParameters).hasSize(1);

    EximeeBpmsInputParameter inputParameter = inputParameters.iterator().next();
    assertThat(inputParameter.getEximeeBpmsName()).isEqualTo("endpointUrl");
    assertThat(inputParameter.getTextContent()).isEqualTo("http://example.com/webservice");

    Collection<EximeeBpmsOutputParameter> outputParameters = camundaInputOutput.getEximeeBpmsOutputParameters();
    assertThat(outputParameters).hasSize(1);

    EximeeBpmsOutputParameter outputParameter = outputParameters.iterator().next();
    assertThat(outputParameter.getEximeeBpmsName()).isEqualTo("result");
    assertThat(outputParameter.getTextContent()).isEqualTo("output");
  }

  @Test
  public void testEximeeBpmsInputOutput() {
    EximeeBpmsInputOutput camundaInputOutput = serviceTask.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsInputOutput.class).singleResult();
    assertThat(camundaInputOutput).isNotNull();
    assertThat(camundaInputOutput.getEximeeBpmsInputParameters()).hasSize(6);
    assertThat(camundaInputOutput.getEximeeBpmsOutputParameters()).hasSize(1);
  }

  @Test
  public void testEximeeBpmsInputParameter() {
    // find existing
    EximeeBpmsInputParameter inputParameter = findInputParameterByName(serviceTask, "shouldBeConstant");

    // modify existing
    inputParameter.setEximeeBpmsName("hello");
    inputParameter.setTextContent("world");
    inputParameter = findInputParameterByName(serviceTask, "hello");
    assertThat(inputParameter.getTextContent()).isEqualTo("world");

    // add new one
    inputParameter = modelInstance.newInstance(EximeeBpmsInputParameter.class);
    inputParameter.setEximeeBpmsName("abc");
    inputParameter.setTextContent("def");
    serviceTask.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsInputOutput.class).singleResult()
      .addChildElement(inputParameter);

    // search for new one
    inputParameter = findInputParameterByName(serviceTask, "abc");
    assertThat(inputParameter.getEximeeBpmsName()).isEqualTo("abc");
    assertThat(inputParameter.getTextContent()).isEqualTo("def");
  }

  @Test
  public void testCamundaNullInputParameter() {
    EximeeBpmsInputParameter inputParameter = findInputParameterByName(serviceTask, "shouldBeNull");
    assertThat(inputParameter.getEximeeBpmsName()).isEqualTo("shouldBeNull");
    assertThat(inputParameter.getTextContent()).isEmpty();
  }

  @Test
  public void testCamundaConstantInputParameter() {
    EximeeBpmsInputParameter inputParameter = findInputParameterByName(serviceTask, "shouldBeConstant");
    assertThat(inputParameter.getEximeeBpmsName()).isEqualTo("shouldBeConstant");
    assertThat(inputParameter.getTextContent()).isEqualTo("foo");
  }

  @Test
  public void testEximeeBpmsExpressionInputParameter() {
    EximeeBpmsInputParameter inputParameter = findInputParameterByName(serviceTask, "shouldBeExpression");
    assertThat(inputParameter.getEximeeBpmsName()).isEqualTo("shouldBeExpression");
    assertThat(inputParameter.getTextContent()).isEqualTo("${1 + 1}");
  }

  @Test
  public void testEximeeBpmsListInputParameter() {
    EximeeBpmsInputParameter inputParameter = findInputParameterByName(serviceTask, "shouldBeList");
    assertThat(inputParameter.getEximeeBpmsName()).isEqualTo("shouldBeList");
    assertThat(inputParameter.getTextContent()).isNotEmpty();
    assertThat(inputParameter.getUniqueChildElementByNameNs(CAMUNDA_NS, "list")).isNotNull();

    EximeeBpmsList list = inputParameter.getValue();
    assertThat(list.getValues()).hasSize(3);
    for (BpmnModelElementInstance values : list.getValues()) {
      assertThat(values.getTextContent()).isIn("a", "b", "c");
    }

    list = modelInstance.newInstance(EximeeBpmsList.class);
    for (int i = 0; i < 4; i++) {
      EximeeBpmsValue value = modelInstance.newInstance(EximeeBpmsValue.class);
      value.setTextContent("test");
      list.getValues().add(value);
    }
    Collection<EximeeBpmsValue> testValues = Arrays.asList(modelInstance.newInstance(EximeeBpmsValue.class), modelInstance.newInstance(EximeeBpmsValue.class));
    list.getValues().addAll(testValues);
    inputParameter.setValue(list);

    list = inputParameter.getValue();
    assertThat(list.getValues()).hasSize(6);
    list.getValues().removeAll(testValues);
    ArrayList<BpmnModelElementInstance> camundaValues = new ArrayList<BpmnModelElementInstance>(list.getValues());
    assertThat(camundaValues).hasSize(4);
    for (BpmnModelElementInstance value : camundaValues) {
      assertThat(value.getTextContent()).isEqualTo("test");
    }

    list.getValues().remove(camundaValues.get(1));
    assertThat(list.getValues()).hasSize(3);

    list.getValues().removeAll(Arrays.asList(camundaValues.get(0), camundaValues.get(3)));
    assertThat(list.getValues()).hasSize(1);

    list.getValues().clear();
    assertThat(list.getValues()).isEmpty();

    // test standard list interactions
    Collection<BpmnModelElementInstance> elements = list.getValues();

    EximeeBpmsValue value = modelInstance.newInstance(EximeeBpmsValue.class);
    elements.add(value);

    List<EximeeBpmsValue> newValues = new ArrayList<EximeeBpmsValue>();
    newValues.add(modelInstance.newInstance(EximeeBpmsValue.class));
    newValues.add(modelInstance.newInstance(EximeeBpmsValue.class));
    elements.addAll(newValues);
    assertThat(elements).hasSize(3);

    assertThat(elements).doesNotContain(modelInstance.newInstance(EximeeBpmsValue.class));
    assertThat(elements.containsAll(Arrays.asList(modelInstance.newInstance(EximeeBpmsValue.class)))).isFalse();

    assertThat(elements.remove(modelInstance.newInstance(EximeeBpmsValue.class))).isFalse();
    assertThat(elements).hasSize(3);

    assertThat(elements.remove(value)).isTrue();
    assertThat(elements).hasSize(2);

    assertThat(elements.removeAll(newValues)).isTrue();
    assertThat(elements).isEmpty();

    elements.add(modelInstance.newInstance(EximeeBpmsValue.class));
    elements.clear();
    assertThat(elements).isEmpty();

    inputParameter.removeValue();
    assertThat((Object) inputParameter.getValue()).isNull();

  }

  @Test
  public void testEximeeBpmsMapInputParameter() {
    EximeeBpmsInputParameter inputParameter = findInputParameterByName(serviceTask, "shouldBeMap");
    assertThat(inputParameter.getEximeeBpmsName()).isEqualTo("shouldBeMap");
    assertThat(inputParameter.getTextContent()).isNotEmpty();
    assertThat(inputParameter.getUniqueChildElementByNameNs(CAMUNDA_NS, "map")).isNotNull();

    EximeeBpmsMap map = inputParameter.getValue();
    assertThat(map.getEximeeBpmsEntries()).hasSize(2);
    for (EximeeBpmsEntry entry : map.getEximeeBpmsEntries()) {
      if (entry.getEximeeBpmsKey().equals("foo")) {
        assertThat(entry.getTextContent()).isEqualTo("bar");
      }
      else {
        assertThat(entry.getEximeeBpmsKey()).isEqualTo("hello");
        assertThat(entry.getTextContent()).isEqualTo("world");
      }
    }

    map = modelInstance.newInstance(EximeeBpmsMap.class);
    EximeeBpmsEntry entry = modelInstance.newInstance(EximeeBpmsEntry.class);
    entry.setEximeeBpmsKey("test");
    entry.setTextContent("value");
    map.getEximeeBpmsEntries().add(entry);

    inputParameter.setValue(map);
    map = inputParameter.getValue();
    assertThat(map.getEximeeBpmsEntries()).hasSize(1);
    entry = map.getEximeeBpmsEntries().iterator().next();
    assertThat(entry.getEximeeBpmsKey()).isEqualTo("test");
    assertThat(entry.getTextContent()).isEqualTo("value");

    Collection<EximeeBpmsEntry> entries = map.getEximeeBpmsEntries();
    entries.add(modelInstance.newInstance(EximeeBpmsEntry.class));
    assertThat(entries).hasSize(2);

    inputParameter.removeValue();
    assertThat((Object) inputParameter.getValue()).isNull();
  }

  @Test
  public void testEximeeBpmsScriptInputParameter() {
    EximeeBpmsInputParameter inputParameter = findInputParameterByName(serviceTask, "shouldBeScript");
    assertThat(inputParameter.getEximeeBpmsName()).isEqualTo("shouldBeScript");
    assertThat(inputParameter.getTextContent()).isNotEmpty();
    assertThat(inputParameter.getUniqueChildElementByNameNs(CAMUNDA_NS, "script")).isNotNull();
    assertThat(inputParameter.getUniqueChildElementByType(EximeeBpmsScript.class)).isNotNull();

    EximeeBpmsScript script = inputParameter.getValue();
    assertThat(script.getEximeeBpmsScriptFormat()).isEqualTo("groovy");
    assertThat(script.getEximeeBpmsResource()).isNull();
    assertThat(script.getTextContent()).isEqualTo("1 + 1");

    script = modelInstance.newInstance(EximeeBpmsScript.class);
    script.setEximeeBpmsScriptFormat("python");
    script.setEximeeBpmsResource("script.py");

    inputParameter.setValue(script);

    script = inputParameter.getValue();
    assertThat(script.getEximeeBpmsScriptFormat()).isEqualTo("python");
    assertThat(script.getEximeeBpmsResource()).isEqualTo("script.py");
    assertThat(script.getTextContent()).isEmpty();

    inputParameter.removeValue();
    assertThat((Object) inputParameter.getValue()).isNull();
  }

  @Test
  public void testCamundaNestedOutputParameter() {
    EximeeBpmsOutputParameter camundaOutputParameter = serviceTask.getExtensionElements().getElementsQuery().filterByType(EximeeBpmsInputOutput.class).singleResult().getEximeeBpmsOutputParameters().iterator().next();

    assertThat(camundaOutputParameter).isNotNull();
    assertThat(camundaOutputParameter.getEximeeBpmsName()).isEqualTo("nested");
    EximeeBpmsList list = camundaOutputParameter.getValue();
    assertThat(list).isNotNull();
    assertThat(list.getValues()).hasSize(2);
    Iterator<BpmnModelElementInstance> iterator = list.getValues().iterator();

    // nested list
    EximeeBpmsList nestedList = (EximeeBpmsList) iterator.next().getUniqueChildElementByType(EximeeBpmsList.class);
    assertThat(nestedList).isNotNull();
    assertThat(nestedList.getValues()).hasSize(2);
    for (BpmnModelElementInstance value : nestedList.getValues()) {
      assertThat(value.getTextContent()).isEqualTo("list");
    }

    // nested map
    EximeeBpmsMap nestedMap = (EximeeBpmsMap) iterator.next().getUniqueChildElementByType(EximeeBpmsMap.class);
    assertThat(nestedMap).isNotNull();
    assertThat(nestedMap.getEximeeBpmsEntries()).hasSize(2);
    Iterator<EximeeBpmsEntry> mapIterator = nestedMap.getEximeeBpmsEntries().iterator();

    // nested list in nested map
    EximeeBpmsEntry nestedListEntry = mapIterator.next();
    assertThat(nestedListEntry).isNotNull();
    assertThat(nestedListEntry.getEximeeBpmsKey()).isEqualTo("list");
    EximeeBpmsList nestedNestedList = nestedListEntry.getValue();
    for (BpmnModelElementInstance value : nestedNestedList.getValues()) {
      assertThat(value.getTextContent()).isEqualTo("map");
    }

    // nested map in nested map
    EximeeBpmsEntry nestedMapEntry = mapIterator.next();
    assertThat(nestedMapEntry).isNotNull();
    assertThat(nestedMapEntry.getEximeeBpmsKey()).isEqualTo("map");
    EximeeBpmsMap nestedNestedMap = nestedMapEntry.getValue();
    EximeeBpmsEntry entry = nestedNestedMap.getEximeeBpmsEntries().iterator().next();
    assertThat(entry.getEximeeBpmsKey()).isEqualTo("so");
    assertThat(entry.getTextContent()).isEqualTo("nested");
  }

  protected EximeeBpmsInputParameter findInputParameterByName(BaseElement baseElement, String name) {
    Collection<EximeeBpmsInputParameter> camundaInputParameters = baseElement.getExtensionElements().getElementsQuery()
      .filterByType(EximeeBpmsInputOutput.class).singleResult().getEximeeBpmsInputParameters();
    for (EximeeBpmsInputParameter camundaInputParameter : camundaInputParameters) {
      if (camundaInputParameter.getEximeeBpmsName().equals(name)) {
        return camundaInputParameter;
      }
    }
    throw new BpmnModelException("Unable to find camunda:inputParameter with name '" + name + "' for element with id '" + baseElement.getId() + "'");
  }

  @After
  public void validateModel() {
    Bpmn.validateModel(modelInstance);
  }
}
