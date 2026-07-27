package org.eximeebpms.bpm.engine.test.api.form.businessevent;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eximeebpms.bpm.engine.FormService;
import org.eximeebpms.bpm.engine.HistoryService;
import org.eximeebpms.bpm.engine.ProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.TaskService;
import org.eximeebpms.bpm.engine.history.HistoricDetail;
import org.eximeebpms.bpm.engine.history.HistoricFormProperty;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.businessevent.DbBusinessEventHandler;
import org.eximeebpms.bpm.engine.impl.businessevent.form.BusinessFormPropertyEventEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.eximeebpms.bpm.engine.repository.ProcessDefinition;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.engine.test.RequiredHistoryLevel;
import org.eximeebpms.bpm.engine.test.businessevent.AbstractBusinessEventIT;
import org.eximeebpms.bpm.model.bpmn.Bpmn;
import org.eximeebpms.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

public class FormPropertyBusinessEventTest extends AbstractBusinessEventIT {

  private final Gson gson = new GsonBuilder().setDateFormat(DbBusinessEventHandler.ISO_DATE_TIME).create();

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void shouldPublishFormPropertyUpdateBusinessEventMatchingHistory() {
    // given
    FormService formService = engineRule.getFormService();
    HistoryService historyService = engineRule.getHistoryService();

    BpmnModelInstance model = Bpmn.createExecutableProcess("formPropertyProcess")
        .startEvent()
        .userTask("userTask")
        .endEvent()
        .done();

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(model);

    Map<String, Object> properties = new HashMap<>();
    properties.put("myProperty", "myValue");

    // when
    ProcessInstance processInstance = formService.submitStartForm(processDefinition.getId(), properties);

    // then the history event reflects the submitted form property
    HistoricDetail historicDetail = historyService.createHistoricDetailQuery()
        .processInstanceId(processInstance.getId())
        .formFields()
        .singleResult();
    assertThat(historicDetail).isInstanceOf(HistoricFormProperty.class);
    HistoricFormProperty historicFormProperty = (HistoricFormProperty) historicDetail;

    // and a matching business event was published to the outbox
    BusinessEventOutboxEntity outboxEntry = findFormPropertyUpdateRawOutboxEntry(processInstance.getId());
    assertThat(outboxEntry).isNotNull();
    BusinessFormPropertyEventEntity businessEvent = gson.fromJson(outboxEntry.getBusinessEvent(), BusinessFormPropertyEventEntity.class);
    assertThat(businessEvent).isNotNull();
    assertThat(businessEvent.getEventType()).isEqualTo(BusinessEventTypes.FORM_PROPERTY_UPDATE.getEventName());
    assertThat(businessEvent.getBusinessEventType()).isEqualTo(BusinessEventTypes.FORM_PROPERTY_UPDATE.getBusinessEventName());

    // the business event fields must be identical to the history event's fields
    assertThat(businessEvent.getPropertyId()).isEqualTo(historicFormProperty.getPropertyId());
    assertThat(businessEvent.getPropertyValue()).isEqualTo(historicFormProperty.getPropertyValue());
    assertThat(businessEvent.getProcessInstanceId()).isEqualTo(historicFormProperty.getProcessInstanceId());
    assertThat(businessEvent.getExecutionId()).isEqualTo(historicFormProperty.getExecutionId());
    assertThat(businessEvent.getActivityInstanceId()).isEqualTo(historicFormProperty.getActivityInstanceId());
    assertThat(businessEvent.getTaskId()).isEqualTo(historicFormProperty.getTaskId());
    assertThat(businessEvent.getTenantId()).isEqualTo(historicFormProperty.getTenantId());
    assertThat(businessEvent.getProcessDefinitionId()).isEqualTo(historicFormProperty.getProcessDefinitionId());
    assertThat(businessEvent.getProcessDefinitionKey()).isEqualTo(historicFormProperty.getProcessDefinitionKey());
    assertThat(businessEvent.getRootProcessInstanceId()).isEqualTo(historicFormProperty.getRootProcessInstanceId());

    // the outbox row's own taskId column (used by BusinessEventManager.deleteByTaskId for
    // task-deletion cleanup) must be populated too, not just the taskId inside the JSON payload
    assertThat(outboxEntry.getTaskId()).isEqualTo(businessEvent.getTaskId());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void shouldPopulateOutboxTaskIdForTaskFormSubmissionSoTaskCleanupCanFindIt() {
    // given
    RuntimeService runtimeService = engineRule.getRuntimeService();
    TaskService taskService = engineRule.getTaskService();
    FormService formService = engineRule.getFormService();

    BpmnModelInstance model = Bpmn.createExecutableProcess("formPropertyTaskProcess")
        .startEvent()
        .userTask("userTask")
        .endEvent()
        .done();

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    Map<String, Object> properties = new HashMap<>();
    properties.put("myProperty", "myValue");

    // when a form-property update is generated for a task form submission
    formService.submitTaskForm(task.getId(), properties);

    // then the outbox row's own taskId column is populated, not just the JSON payload's taskId
    BusinessEventOutboxEntity outboxEntry = findFormPropertyUpdateRawOutboxEntry(processInstance.getId());
    assertThat(outboxEntry).isNotNull();
    assertThat(outboxEntry.getTaskId()).isEqualTo(task.getId());

    // and BusinessEventManager.deleteByTaskId (invoked by DeleteTaskCmd/TaskManager on task
    // deletion) can find and remove the row by that column, instead of leaving it behind to be
    // dispatched for an already-deleted task
    commandExecutor.execute(ctx -> {
      ctx.getBusinessEventManager().deleteByTaskId(task.getId());
      return null;
    });
    assertThat(findFormPropertyUpdateRawOutboxEntry(processInstance.getId())).isNull();
  }

  @SuppressWarnings("unchecked")
  private BusinessEventOutboxEntity findFormPropertyUpdateRawOutboxEntry(String processInstanceId) {
    List<BusinessEventOutboxEntity> outboxEntries = commandExecutor.execute(ctx ->
        ctx.getDbEntityManager().selectList("selectBusinessEventOutboxByProcInstId", processInstanceId));

    List<BusinessEventOutboxEntity> matching = outboxEntries.stream()
        .filter(entry -> BusinessEventTypes.FORM_PROPERTY_UPDATE.getBusinessEventName().equals(entry.getEventType()))
        .toList();
    assertThat(matching).as("form-property:form-property-update outbox entries for process instance %s", processInstanceId)
        .hasSizeLessThanOrEqualTo(1);
    return matching.isEmpty() ? null : matching.getFirst();
  }
}
