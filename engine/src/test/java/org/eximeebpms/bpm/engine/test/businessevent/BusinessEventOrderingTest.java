package org.eximeebpms.bpm.engine.test.businessevent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.eximeebpms.bpm.engine.ManagementService;
import org.eximeebpms.bpm.engine.TaskService;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.eximeebpms.bpm.engine.repository.ProcessDefinition;
import org.eximeebpms.bpm.model.bpmn.Bpmn;
import org.eximeebpms.bpm.engine.runtime.Job;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.eximeebpms.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * Business events are recorded in the outbox in the same relative order as the corresponding
 * history events. The outbox is drained in insertion order, so the order asserted here is the order
 * a publisher sees.
 */
public class BusinessEventOrderingTest extends AbstractBusinessEventIT {

  protected static final String RESOURCE_PREFIX = "org/eximeebpms/bpm/engine/test/businessevent/BusinessEventOrderingTest.";

  protected static final String PROCESS_START = BusinessEventTypes.PROCESS_INSTANCE_START.getBusinessEventName();
  protected static final String VARIABLE_CREATE = BusinessEventTypes.VARIABLE_INSTANCE_CREATE.getBusinessEventName();
  protected static final String FORM_PROPERTY_UPDATE = BusinessEventTypes.FORM_PROPERTY_UPDATE.getBusinessEventName();
  protected static final String JOB_CREATE = BusinessEventTypes.JOB_CREATE.getBusinessEventName();
  protected static final String TASK_CREATE = BusinessEventTypes.TASK_INSTANCE_CREATE.getBusinessEventName();
  protected static final String TASK_UPDATE = BusinessEventTypes.TASK_INSTANCE_UPDATE.getBusinessEventName();
  protected static final String TASK_COMPLETE = BusinessEventTypes.TASK_INSTANCE_COMPLETE.getBusinessEventName();
  protected static final String IDENTITY_LINK_ADD = BusinessEventTypes.IDENTITY_LINK_ADD.getBusinessEventName();
  protected static final String IDENTITY_LINK_DELETE = BusinessEventTypes.IDENTITY_LINK_DELETE.getBusinessEventName();
  protected static final String TASK_DELETE = BusinessEventTypes.TASK_INSTANCE_DELETE.getBusinessEventName();
  protected static final String PROCESS_UPDATE = BusinessEventTypes.PROCESS_INSTANCE_UPDATE.getBusinessEventName();

  // process-instance:start ///////////////////////////////////////////////////

  @Test
  public void shouldEmitProcessStartBeforeStartVariables() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    // when
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId(),
        Variables.createVariables().putValue("first", "1").putValue("second", "2"));

    // then
    List<String> types = outboxEventTypes(processInstance.getId());
    assertStartsWithSingleProcessStart(types);
    assertThat(types).filteredOn(VARIABLE_CREATE::equals).hasSize(2);
    // the start event still carries the initial activity, now that it is emitted before PROCESS_START
    assertThat(outboxPayloads(processInstance.getId()).get(0)).contains("\"startActivityId\":\"startEvent\"");
  }

  @Test
  public void shouldEmitProcessStartBeforeVariablesSetByProcessStartListener() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(RESOURCE_PREFIX + "processStartListener.bpmn20.xml");

    // when
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());

    // then
    List<String> types = outboxEventTypes(processInstance.getId());
    assertStartsWithSingleProcessStart(types);
    assertThat(types).contains(VARIABLE_CREATE);
  }

  @Test
  public void shouldEmitProcessStartBeforeStartFormProperties() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    // when
    ProcessInstance processInstance = engineRule.getFormService().submitStartForm(processDefinition.getId(),
        Variables.createVariables().putValue("formField", "value"));

    // then
    List<String> types = outboxEventTypes(processInstance.getId());
    assertStartsWithSingleProcessStart(types);
    assertThat(types).contains(FORM_PROPERTY_UPDATE);
  }

  @Test
  public void shouldEmitProcessStartBeforeProcessScopeTimerJob() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(RESOURCE_PREFIX + "processScopeTimer.bpmn20.xml");

    // when
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());

    // then
    List<String> types = outboxEventTypes(processInstance.getId());
    assertStartsWithSingleProcessStart(types);
    assertThat(types).contains(JOB_CREATE);
  }

  @Test
  public void shouldEmitProcessStartInStartingTransactionForAsyncStartEvent() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(RESOURCE_PREFIX + "asyncStartEvent.bpmn20.xml");

    // when the instance is started, but its async start job has not run yet
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId(),
        Variables.createVariables().putValue("first", "1"));

    // then
    List<String> typesBeforeJob = outboxEventTypes(processInstance.getId());
    assertStartsWithSingleProcessStart(typesBeforeJob);
    assertThat(typesBeforeJob).contains(VARIABLE_CREATE);

    // and executing the async start job does not emit it a second time
    // (only this instance's job: the outbox cleanup job is always available)
    ManagementService managementService = engineRule.getManagementService();
    Job asyncStartJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    managementService.executeJob(asyncStartJob.getId());
    assertThat(outboxEventTypes(processInstance.getId())).filteredOn(PROCESS_START::equals).hasSize(1);
  }

  @Test
  public void shouldEmitProcessStartOnceWhenStartingAtActivity() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    // when
    ProcessInstance processInstance = engineRule.getRuntimeService()
        .createProcessInstanceById(processDefinition.getId())
        .startBeforeActivity("userTask")
        .setVariable("first", "1")
        .execute();

    // then
    List<String> types = outboxEventTypes(processInstance.getId());
    assertStartsWithSingleProcessStart(types);
    assertThat(types).contains(VARIABLE_CREATE);
  }

  @Test
  public void shouldEmitSubProcessStartWithItsOwnProcessInstanceId() {
    // given
    testRule.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition parentDefinition = testRule.deployAndGetDefinition(Bpmn.createExecutableProcess("parent")
        .startEvent()
        .callActivity("callActivity").calledElement(ProcessModels.PROCESS_KEY)
        .endEvent()
        .done());

    // when
    ProcessInstance parent = engineRule.getRuntimeService().startProcessInstanceById(parentDefinition.getId());
    ProcessInstance subProcess = engineRule.getRuntimeService().createProcessInstanceQuery()
        .superProcessInstanceId(parent.getId())
        .singleResult();

    // then the sub-process start, now emitted before PROCESS_START, identifies the sub-process itself;
    // only the root id (used as the envelope's processInstanceId and the Kafka key) points at the parent
    List<String> subProcessTypes = outboxEventTypes(subProcess.getId());
    assertStartsWithSingleProcessStart(subProcessTypes);
    assertThat(outboxPayloads(subProcess.getId()).get(0)).contains(
        "\"processInstanceId\":\"" + subProcess.getId() + "\"",
        "\"rootProcessInstanceId\":\"" + parent.getId() + "\"",
        "\"superProcessInstanceId\":\"" + parent.getId() + "\"");
    assertThat(outboxPayloads(parent.getId()).get(0)).contains(
        "\"processInstanceId\":\"" + parent.getId() + "\"",
        "\"rootProcessInstanceId\":\"" + parent.getId() + "\"");
  }

  // task-instance:complete / delete //////////////////////////////////////////

  @Test
  public void shouldEmitTaskCompleteAfterVariablesSetByCompleteListener() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(RESOURCE_PREFIX + "taskEndListeners.bpmn20.xml");
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    Task task = engineRule.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // when
    engineRule.getTaskService().complete(task.getId());

    // then
    List<String> types = outboxEventTypes(processInstance.getId());
    assertThat(types).filteredOn(TASK_COMPLETE::equals).hasSize(1);
    assertThat(types).filteredOn(TASK_DELETE::equals).isEmpty();
    assertThat(types.lastIndexOf(VARIABLE_CREATE)).as("variable set by the complete listener in %s", types)
        .isLessThan(types.indexOf(TASK_COMPLETE));
  }

  @Test
  public void shouldEmitTaskDeleteAfterVariablesSetByDeleteListener() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(RESOURCE_PREFIX + "taskEndListeners.bpmn20.xml");
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());

    // when
    engineRule.getRuntimeService().deleteProcessInstance(processInstance.getId(), "cancelled");

    // then
    List<String> types = outboxEventTypes(processInstance.getId());
    assertThat(types).filteredOn(TASK_DELETE::equals).hasSize(1);
    assertThat(types).filteredOn(TASK_COMPLETE::equals).isEmpty();
    assertThat(types.lastIndexOf(VARIABLE_CREATE)).as("variable set by the delete listener in %s", types)
        .isLessThan(types.indexOf(TASK_DELETE));
  }

  @Test
  public void shouldEmitTaskLifecycleForStandaloneTask() {
    // given
    TaskService taskService = engineRule.getTaskService();
    Task task = taskService.newTask();

    try {
      // when
      taskService.saveTask(task);
      taskService.setAssignee(task.getId(), "someone");
      taskService.complete(task.getId());

      // then (an assignment fires both the task update and assignment events, each a task-instance:update)
      List<String> types = allOutboxEventTypes();
      List<String> taskTypes = types.stream().filter(type -> type.startsWith("bpms:task-instance")).toList();
      assertThat(taskTypes).containsSubsequence(TASK_CREATE, TASK_UPDATE, TASK_COMPLETE);
      assertThat(taskTypes.get(0)).isEqualTo(TASK_CREATE);
      assertThat(taskTypes.get(taskTypes.size() - 1)).isEqualTo(TASK_COMPLETE);
      assertThat(types.indexOf(IDENTITY_LINK_ADD)).as("assignee link in %s", types)
          .isGreaterThan(types.indexOf(TASK_CREATE))
          .isLessThan(types.indexOf(TASK_UPDATE));
    } finally {
      engineRule.getHistoryService().deleteHistoricTaskInstance(task.getId());
    }
  }

  // identity-link add / delete ////////////////////////////////////////////////

  @Test
  public void shouldEmitAssigneeAndOwnerIdentityLinkEvents() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    Task task = engineRule.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // when
    engineRule.getTaskService().setAssignee(task.getId(), "first");
    engineRule.getTaskService().setAssignee(task.getId(), "second");
    engineRule.getTaskService().setOwner(task.getId(), "owner");

    // then add(first), delete(first), add(second), add(owner) - as history records them
    List<String> identityLinkTypes = allOutboxEventTypes().stream()
        .filter(type -> type.equals(IDENTITY_LINK_ADD) || type.equals(IDENTITY_LINK_DELETE))
        .toList();
    assertThat(identityLinkTypes).containsExactly(IDENTITY_LINK_ADD, IDENTITY_LINK_DELETE, IDENTITY_LINK_ADD, IDENTITY_LINK_ADD);
    assertThat(allOutboxPayloads()).anySatisfy(payload -> assertThat(payload).contains("\"type\":\"assignee\"", "\"userId\":\"second\""));
    assertThat(allOutboxPayloads()).anySatisfy(payload -> assertThat(payload).contains("\"type\":\"owner\"", "\"userId\":\"owner\""));
  }

  // process-instance-update //////////////////////////////////////////////////

  @Test
  public void shouldEmitProcessUpdateOnSuspendAndActivate() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());

    // when
    engineRule.getRuntimeService().suspendProcessInstanceById(processInstance.getId());
    engineRule.getRuntimeService().activateProcessInstanceById(processInstance.getId());

    // then
    assertThat(processUpdatePayloads(processInstance.getId()))
        .hasSize(2)
        .satisfiesExactly(
            suspended -> assertThat(suspended).contains("\"state\":\"SUSPENDED\""),
            activated -> assertThat(activated).contains("\"state\":\"ACTIVE\""));
  }

  @Test
  public void shouldEmitProcessUpdateOnSuspendingProcessDefinitionWithInstances() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());

    // when
    engineRule.getRepositoryService().suspendProcessDefinitionById(processDefinition.getId(), true, null);

    // then
    assertThat(processUpdatePayloads(processInstance.getId()))
        .singleElement()
        .satisfies(suspended -> assertThat(suspended).contains("\"state\":\"SUSPENDED\""));
  }

  @Test
  public void shouldEmitProcessUpdateOnBusinessKeyChange() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(RESOURCE_PREFIX + "businessKeyUpdate.bpmn20.xml");

    // when
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId(), "initialKey");

    // then
    assertThat(processUpdatePayloads(processInstance.getId()))
        .singleElement()
        .satisfies(updated -> assertThat(updated).contains("\"businessKey\":\"updatedKey\""));
  }

  @Test
  public void shouldEmitProcessUpdateOnProcessDefinitionVersionChange() {
    // given
    testRule.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition secondVersion = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(secondVersion.getId());

    // when
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(processInstance.getId(), secondVersion.getVersion() - 1));

    // then
    assertThat(outboxEventTypes(processInstance.getId())).filteredOn(PROCESS_UPDATE::equals).hasSize(1);
  }

  @Test
  public void shouldEmitProcessUpdateForSubProcessWhenDeletingParentWithoutSubProcesses() {
    // given
    testRule.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition parentDefinition = testRule.deployAndGetDefinition(Bpmn.createExecutableProcess("parent")
        .startEvent()
        .callActivity("callActivity").calledElement(ProcessModels.PROCESS_KEY)
        .endEvent()
        .done());
    ProcessInstance parent = engineRule.getRuntimeService().startProcessInstanceById(parentDefinition.getId());
    ProcessInstance subProcess = engineRule.getRuntimeService().createProcessInstanceQuery()
        .superProcessInstanceId(parent.getId())
        .singleResult();

    // when
    engineRule.getRuntimeService().deleteProcessInstance(parent.getId(), "cancelled", false, false, false, true);

    // then
    assertThat(outboxEventTypes(subProcess.getId())).filteredOn(PROCESS_UPDATE::equals).hasSize(1);
  }

  // helpers //////////////////////////////////////////////////////////////////

  /** Every outbox row, in insertion order, whether or not it has a process instance. */
  protected List<BusinessEventOutboxEntity> allOutboxEntries() {
    return engineRule.getProcessEngine().getBusinessEventService().createBusinessEventOutboxQuery().list()
        .stream()
        .map(BusinessEventOutboxEntity.class::cast)
        .sorted(Comparator.comparing(BusinessEventOutboxEntity::getIdAsLong))
        .toList();
  }

  protected List<String> allOutboxEventTypes() {
    return allOutboxEntries().stream().map(BusinessEventOutboxEntity::getEventType).toList();
  }

  protected List<String> allOutboxPayloads() {
    return allOutboxEntries().stream().map(BusinessEventOutboxEntity::getBusinessEvent).toList();
  }

  protected List<String> processUpdatePayloads(String processInstanceId) {
    List<String> types = outboxEventTypes(processInstanceId);
    List<String> payloads = outboxPayloads(processInstanceId);
    return IntStream.range(0, types.size())
        .filter(i -> PROCESS_UPDATE.equals(types.get(i)))
        .mapToObj(payloads::get)
        .toList();
  }

  protected void assertStartsWithSingleProcessStart(List<String> types) {
    assertThat(types).isNotEmpty();
    assertThat(types.get(0)).as("first event of %s", types).isEqualTo(PROCESS_START);
    assertThat(types).filteredOn(PROCESS_START::equals).hasSize(1);
  }
}
