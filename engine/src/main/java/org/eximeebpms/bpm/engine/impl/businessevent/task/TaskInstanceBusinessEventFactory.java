package org.eximeebpms.bpm.engine.impl.businessevent.task;

import java.util.Date;
import java.util.Optional;
import org.eximeebpms.bpm.engine.delegate.DelegateTask;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.TaskEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;

public class TaskInstanceBusinessEventFactory extends BusinessEventFactorySupport {

  public BusinessEvent createCreateEvent(final DelegateTask task) {
    if (!(task instanceof TaskEntity taskEntity)) {
      return null;
    }

    final BusinessTaskInstanceEventEntity event = createTaskInstanceEvent(taskEntity, BusinessEventTypes.TASK_INSTANCE_CREATE);
    event.setStartTime(ClockUtil.getCurrentTime());
    return event;
  }

  public BusinessEvent createUpdateEvent(final DelegateTask task) {
    if (!(task instanceof TaskEntity taskEntity)) {
      return null;
    }

    final BusinessTaskInstanceEventEntity event = createTaskInstanceEvent(taskEntity, BusinessEventTypes.TASK_INSTANCE_UPDATE);
    event.setStartTime(resolveHistoricTaskStartTime(taskEntity.getId()));
    return event;
  }

  public BusinessEvent createCompleteEvent(final DelegateTask task) {
    if (!(task instanceof TaskEntity taskEntity)) {
      return null;
    }

    final BusinessTaskInstanceEventEntity event = createTaskInstanceEvent(taskEntity, BusinessEventTypes.TASK_INSTANCE_COMPLETE);
    initEndData(event, taskEntity);
    return event;
  }

  public BusinessEvent createDeleteEvent(final DelegateTask task) {
    if (!(task instanceof TaskEntity taskEntity)) {
      return null;
    }

    final BusinessTaskInstanceEventEntity event = createTaskInstanceEvent(taskEntity, BusinessEventTypes.TASK_INSTANCE_DELETE);
    initEndData(event, taskEntity);
    return event;
  }

  private void initEndData(final BusinessTaskInstanceEventEntity event, final TaskEntity taskEntity) {
    final Date endTime = ClockUtil.getCurrentTime();
    final Date startTime = resolveHistoricTaskStartTime(taskEntity.getId());

    event.setStartTime(startTime);
    event.setEndTime(endTime);
    event.setDeleteReason(taskEntity.getDeleteReason());

    if (startTime != null) {
      event.setDurationInMillis(endTime.getTime() - startTime.getTime());
    }
  }

  private Date resolveHistoricTaskStartTime(final String taskId) {
    return Optional.ofNullable(Context.getCommandContext())
        .map(CommandContext::getDbEntityManager)
        .map(dbEntityManager -> dbEntityManager.selectById(HistoricTaskInstanceEventEntity.class, taskId))
        .map(HistoricTaskInstanceEventEntity::getStartTime)
        .orElse(null);
  }

  private BusinessTaskInstanceEventEntity createTaskInstanceEvent(final TaskEntity taskEntity, final BusinessEventTypes eventType) {
    final BusinessTaskInstanceEventEntity event = new BusinessTaskInstanceEventEntity();
    initTaskInstanceEvent(event, taskEntity, eventType);
    return event;
  }

  private void initTaskInstanceEvent(final BusinessTaskInstanceEventEntity event, final TaskEntity taskEntity, final BusinessEventTypes eventType) {
    fillProcessDefinitionData(event, taskEntity);

    final String processInstanceId = taskEntity.getProcessInstanceId();
    final String executionId = taskEntity.getExecutionId();

    event.setId(taskEntity.getId());
    event.setEventType(eventType.getEventName());
    event.setBusinessEventType(eventType.getBusinessEventName());
    event.setTimestamp(ClockUtil.getCurrentTime());
    event.setUserOperationId(Optional.ofNullable(Context.getCommandContext())
        .map(CommandContext::getOperationId)
        .orElse(null));

    event.setTaskId(taskEntity.getId());

    event.setProcessInstanceId(processInstanceId);
    event.setExecutionId(executionId);

    event.setAssignee(taskEntity.getAssignee());
    event.setDescription(taskEntity.getDescription());
    event.setDueDate(taskEntity.getDueDate());
    event.setFollowUpDate(taskEntity.getFollowUpDate());
    event.setName(taskEntity.getName());
    event.setOwner(taskEntity.getOwner());
    event.setParentTaskId(taskEntity.getParentTaskId());
    event.setPriority(taskEntity.getPriority());
    event.setTaskDefinitionKey(taskEntity.getTaskDefinitionKey());
    event.setTenantId(taskEntity.getTenantId());
    event.setTaskState(taskEntity.getTaskState());

    final ExecutionEntity execution = taskEntity.getExecution();
    if (execution != null) {
      event.setActivityInstanceId(execution.getActivityInstanceId());
      event.setRootProcessInstanceId(execution.getRootProcessInstanceId());
      initSequenceCounter(execution, event);
    }
  }

  private void fillProcessDefinitionData(final BusinessEvent event, final TaskEntity taskEntity) {
    final String processDefinitionId = taskEntity.getProcessDefinitionId();
    if (processDefinitionId != null) {
      fillProcessDefinitionData(event, processDefinitionId);
    } else {
      event.setProcessDefinitionId(taskEntity.getProcessDefinitionId());
    }
  }
}
