package org.eximeebpms.bpm.engine.test.businessevent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.eximeebpms.bpm.engine.ProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.TaskService;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventConfiguration;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.repository.ProcessDefinition;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.engine.test.RequiredHistoryLevel;
import org.eximeebpms.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.eximeebpms.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * A narrow {@code enabledEventTypes} allowlist: only the named types reach the outbox, and the
 * denylist still narrows the allowlist further.
 */
public class BusinessEventEnabledTypesTest extends AbstractBusinessEventIT {

  @Override
  protected BusinessEventConfiguration businessEventConfiguration() {
    return BusinessEventConfiguration.builder()
        .enabled(true)
        .enabledEventTypes(Set.of("process-instance:*", "task-instance:*"))
        .disabledEventTypes(Set.of("task-instance:update"))
        .build();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void shouldPublishOnlyAllowedTypes() {
    // given
    RuntimeService runtimeService = engineRule.getRuntimeService();
    TaskService taskService = engineRule.getTaskService();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(),
        Variables.createVariables().putValue("invoiceId", "INV-1"));
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setAssignee(task.getId(), "someone");
    taskService.complete(task.getId());

    // then
    List<String> publishedTypes = outboxEventTypes(processInstance.getId());

    assertThat(publishedTypes)
        .isNotEmpty()
        .contains(
            BusinessEventTypes.PROCESS_INSTANCE_START.getBusinessEventName(),
            BusinessEventTypes.TASK_INSTANCE_CREATE.getBusinessEventName(),
            BusinessEventTypes.TASK_INSTANCE_COMPLETE.getBusinessEventName())
        // the assignment would otherwise produce a task-instance:update event
        .doesNotContain(
            BusinessEventTypes.TASK_INSTANCE_UPDATE.getBusinessEventName(),
            BusinessEventTypes.VARIABLE_INSTANCE_CREATE.getBusinessEventName(),
            BusinessEventTypes.ACTIVITY_INSTANCE_START.getBusinessEventName(),
            BusinessEventTypes.IDENTITY_LINK_ADD.getBusinessEventName())
        // nothing outside the two allowed entities reached the outbox at all
        .allMatch(type ->
            type.startsWith("bpms:process-instance") || type.startsWith("bpms:task-instance"));
  }
}
