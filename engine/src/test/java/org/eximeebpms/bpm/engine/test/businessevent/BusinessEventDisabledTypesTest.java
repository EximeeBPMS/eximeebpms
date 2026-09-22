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
 * {@code disabledEventTypes} on the default {@code *} allowlist: everything is published except
 * the entities named in the denylist.
 */
public class BusinessEventDisabledTypesTest extends AbstractBusinessEventIT {

  @Override
  protected BusinessEventConfiguration businessEventConfiguration() {
    return BusinessEventConfiguration.builder()
        .enabled(true)
        .disabledEventTypes(Set.of("variable-instance:*", "activity-instance:*"))
        .build();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void shouldPublishEveryTypeExceptTheDeniedOnes() {
    // given
    RuntimeService runtimeService = engineRule.getRuntimeService();
    TaskService taskService = engineRule.getTaskService();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(),
        Variables.createVariables().putValue("invoiceId", "INV-1"));
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());

    // then
    List<String> publishedTypes = outboxEventTypes(processInstance.getId());

    assertThat(publishedTypes)
        .contains(
            BusinessEventTypes.PROCESS_INSTANCE_START.getBusinessEventName(),
            BusinessEventTypes.PROCESS_INSTANCE_END.getBusinessEventName(),
            BusinessEventTypes.TASK_INSTANCE_CREATE.getBusinessEventName(),
            BusinessEventTypes.TASK_INSTANCE_COMPLETE.getBusinessEventName())
        .doesNotContain(
            BusinessEventTypes.VARIABLE_INSTANCE_CREATE.getBusinessEventName(),
            BusinessEventTypes.VARIABLE_INSTANCE_UPDATE.getBusinessEventName(),
            BusinessEventTypes.VARIABLE_INSTANCE_DELETE.getBusinessEventName(),
            BusinessEventTypes.ACTIVITY_INSTANCE_START.getBusinessEventName(),
            BusinessEventTypes.ACTIVITY_INSTANCE_END.getBusinessEventName());
  }
}
