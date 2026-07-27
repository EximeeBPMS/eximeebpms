package org.eximeebpms.bpm.engine.test.api.runtime.businessevent;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import org.eximeebpms.bpm.engine.HistoryService;
import org.eximeebpms.bpm.engine.ProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.TaskService;
import org.eximeebpms.bpm.engine.history.HistoricActivityInstance;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.businessevent.DbBusinessEventHandler;
import org.eximeebpms.bpm.engine.impl.businessevent.activity.BusinessActivityInstanceEventEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.eximeebpms.bpm.engine.repository.ProcessDefinition;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.engine.test.RequiredHistoryLevel;
import org.eximeebpms.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.eximeebpms.bpm.engine.test.businessevent.AbstractBusinessEventIT;
import org.eximeebpms.bpm.model.bpmn.Bpmn;
import org.eximeebpms.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

public class ActivityInstanceBusinessEventTest extends AbstractBusinessEventIT {

  private final Gson gson = new GsonBuilder().setDateFormat(DbBusinessEventHandler.ISO_DATE_TIME).create();

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void shouldPublishActivityInstanceStartAndEndBusinessEventsMatchingHistory() {
    // given
    RuntimeService runtimeService = engineRule.getRuntimeService();
    TaskService taskService = engineRule.getTaskService();
    HistoryService historyService = engineRule.getHistoryService();

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());

    // then the history reflects the completed activity instance
    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
        .processInstanceId(processInstance.getId())
        .activityId("userTask")
        .singleResult();
    assertThat(historicActivityInstance).isNotNull();

    // and matching start/end business events were published to the outbox
    BusinessActivityInstanceEventEntity startEvent = findActivityInstanceEvent(
        processInstance.getId(), "userTask", BusinessEventTypes.ACTIVITY_INSTANCE_START);
    BusinessActivityInstanceEventEntity endEvent = findActivityInstanceEvent(
        processInstance.getId(), "userTask", BusinessEventTypes.ACTIVITY_INSTANCE_END);

    assertThat(startEvent).isNotNull();
    assertThat(endEvent).isNotNull();

    // the business event fields must be identical to the history event's fields
    assertThat(startEvent.getActivityInstanceId()).isEqualTo(historicActivityInstance.getId());
    assertThat(startEvent.getActivityId()).isEqualTo(historicActivityInstance.getActivityId());
    assertThat(startEvent.getActivityName()).isEqualTo(historicActivityInstance.getActivityName());
    assertThat(startEvent.getActivityType()).isEqualTo(historicActivityInstance.getActivityType());
    assertThat(startEvent.getParentActivityInstanceId()).isEqualTo(historicActivityInstance.getParentActivityInstanceId());
    assertThat(startEvent.getProcessInstanceId()).isEqualTo(historicActivityInstance.getProcessInstanceId());
    assertThat(startEvent.getExecutionId()).isEqualTo(historicActivityInstance.getExecutionId());
    assertThat(startEvent.getProcessDefinitionId()).isEqualTo(historicActivityInstance.getProcessDefinitionId());
    assertThat(startEvent.getProcessDefinitionKey()).isEqualTo(historicActivityInstance.getProcessDefinitionKey());
    assertThat(startEvent.getTenantId()).isEqualTo(historicActivityInstance.getTenantId());
    assertThat(startEvent.getRootProcessInstanceId()).isEqualTo(historicActivityInstance.getRootProcessInstanceId());
    // not compared for exact equality: the history and business event listeners each stamp their
    // own start time independently, so the two values can differ by a millisecond or two
    assertThat(startEvent.getStartTime()).isNotNull();

    assertThat(endEvent.getActivityInstanceId()).isEqualTo(historicActivityInstance.getId());
    // the start time is backfilled from the same persisted historic row, so it must match exactly
    assertThat(endEvent.getStartTime()).isEqualTo(historicActivityInstance.getStartTime());
    // end time and duration are stamped independently by the business event listener, just like
    // the start time above, so they are only asserted to be present and consistent with each other
    assertThat(endEvent.getEndTime()).isNotNull();
    assertThat(endEvent.getDurationInMillis()).isEqualTo(endEvent.getEndTime().getTime() - endEvent.getStartTime().getTime());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void shouldPublishActivityInstanceUpdateBusinessEventMatchingHistoryOnCalledProcessInstanceLinking() {
    // given
    RuntimeService runtimeService = engineRule.getRuntimeService();
    HistoryService historyService = engineRule.getHistoryService();

    ProcessDefinition calledProcessDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
        .startEvent()
        .callActivity("callActivity")
          .calledElement(calledProcessDefinition.getKey())
        .endEvent()
        .done();
    testRule.deployAndGetDefinition(callingProcess);

    // when
    ProcessInstance callingProcessInstance = runtimeService.startProcessInstanceByKey("callingProcess");

    // then the history reflects the called process instance link
    HistoricActivityInstance historicCallActivityInstance = historyService.createHistoricActivityInstanceQuery()
        .processInstanceId(callingProcessInstance.getId())
        .activityId("callActivity")
        .singleResult();
    assertThat(historicCallActivityInstance).isNotNull();
    assertThat(historicCallActivityInstance.getCalledProcessInstanceId()).isNotNull();

    // and a matching update business event was published to the outbox
    BusinessActivityInstanceEventEntity updateEvent = findActivityInstanceEvent(
        callingProcessInstance.getId(), "callActivity", BusinessEventTypes.ACTIVITY_INSTANCE_UPDATE);
    assertThat(updateEvent).isNotNull();

    // the business event fields must be identical to the history event's fields
    assertThat(updateEvent.getActivityInstanceId()).isEqualTo(historicCallActivityInstance.getId());
    assertThat(updateEvent.getActivityId()).isEqualTo(historicCallActivityInstance.getActivityId());
    assertThat(updateEvent.getParentActivityInstanceId()).isEqualTo(historicCallActivityInstance.getParentActivityInstanceId());
    assertThat(updateEvent.getProcessInstanceId()).isEqualTo(historicCallActivityInstance.getProcessInstanceId());
    assertThat(updateEvent.getExecutionId()).isEqualTo(historicCallActivityInstance.getExecutionId());
    assertThat(updateEvent.getCalledProcessInstanceId()).isEqualTo(historicCallActivityInstance.getCalledProcessInstanceId());
  }

  @SuppressWarnings("unchecked")
  private BusinessActivityInstanceEventEntity findActivityInstanceEvent(String processInstanceId, String activityId, BusinessEventTypes eventType) {
    final List<BusinessEventOutboxEntity> outboxEntries = commandExecutor.execute(ctx ->
        ctx.getDbEntityManager().selectList("selectBusinessEventOutboxByProcInstId", processInstanceId));

    List<BusinessActivityInstanceEventEntity> matching = outboxEntries.stream()
        .filter(entry -> eventType.getBusinessEventName().equals(entry.getEventType()))
        .map(entry -> gson.fromJson(entry.getBusinessEvent(), BusinessActivityInstanceEventEntity.class))
        .filter(event -> activityId.equals(event.getActivityId()))
        .toList();
    assertThat(matching).as("%s outbox entries for activity %s of process instance %s",
        eventType.getBusinessEventName(), activityId, processInstanceId).hasSizeLessThanOrEqualTo(1);
    return matching.isEmpty() ? null : matching.getFirst();
  }
}
