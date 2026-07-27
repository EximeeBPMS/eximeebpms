package org.eximeebpms.bpm.engine.impl.businessevent.activity;

import java.util.Optional;
import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.history.HistoryLevel;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.eximeebpms.bpm.engine.impl.pvm.PvmScope;
import org.eximeebpms.bpm.engine.impl.pvm.runtime.CompensationBehavior;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;

public class ActivityInstanceBusinessEventFactory extends BusinessEventFactorySupport {

  public BusinessEvent createStartEvent(final DelegateExecution execution) {
    if (!(execution instanceof ExecutionEntity executionEntity)) {
      return null;
    }

    final BusinessActivityInstanceEventEntity event = new BusinessActivityInstanceEventEntity();
    initActivityInstanceEvent(event, executionEntity, BusinessEventTypes.ACTIVITY_INSTANCE_START);
    event.setStartTime(ClockUtil.getCurrentTime());

    return event;
  }

  public BusinessEvent createEndEvent(final DelegateExecution execution) {
    if (!(execution instanceof ExecutionEntity executionEntity)) {
      return null;
    }

    final BusinessActivityInstanceEventEntity event = new BusinessActivityInstanceEventEntity();
    event.setActivityInstanceState(executionEntity.getActivityInstanceState());
    initActivityInstanceEvent(event, executionEntity, BusinessEventTypes.ACTIVITY_INSTANCE_END);

    event.setEndTime(ClockUtil.getCurrentTime());
    fillHistoricStartTime(event);

    if (event.getStartTime() != null) {
      event.setDurationInMillis(event.getEndTime().getTime() - event.getStartTime().getTime());
    }

    return event;
  }

  public BusinessEvent createUpdateEvent(final DelegateExecution execution) {
    if (!(execution instanceof ExecutionEntity executionEntity)) {
      return null;
    }

    final BusinessActivityInstanceEventEntity event = new BusinessActivityInstanceEventEntity();
    initActivityInstanceEvent(event, executionEntity, BusinessEventTypes.ACTIVITY_INSTANCE_UPDATE);
    fillHistoricStartTime(event);

    return event;
  }

  public BusinessEvent createMigrateEvent(final MigratingActivityInstance activityInstance) {
    if (activityInstance == null) {
      return null;
    }

    final ExecutionEntity executionEntity = activityInstance.resolveRepresentativeExecution();
    if (executionEntity == null) {
      return null;
    }

    final BusinessActivityInstanceEventEntity event = new BusinessActivityInstanceEventEntity();
    initActivityInstanceEvent(event, activityInstance, executionEntity, BusinessEventTypes.ACTIVITY_INSTANCE_MIGRATE);
    fillHistoricStartTime(event);

    return event;
  }

  private void initActivityInstanceEvent(
      final BusinessActivityInstanceEventEntity event,
      final ExecutionEntity execution,
      final BusinessEventTypes eventType) {

    PvmScope eventSource = execution.getActivity();
    if (eventSource == null) {
      eventSource = (PvmScope) execution.getEventSource();
    }

    initActivityInstanceEvent(
        event,
        execution,
        eventSource,
        execution.getActivityInstanceId(),
        resolveParentActivityInstanceId(execution),
        eventType);
  }

  private void initActivityInstanceEvent(
      final BusinessActivityInstanceEventEntity event,
      final MigratingActivityInstance activityInstance,
      final ExecutionEntity execution,
      final BusinessEventTypes eventType) {

    final MigratingActivityInstance parentInstance = activityInstance.getParent();
    final String parentActivityInstanceId = parentInstance != null ? parentInstance.getActivityInstanceId() : null;

    initActivityInstanceEvent(
        event,
        execution,
        activityInstance.getTargetScope(),
        activityInstance.getActivityInstanceId(),
        parentActivityInstanceId,
        eventType);
  }

  private void initActivityInstanceEvent(
      final BusinessActivityInstanceEventEntity event,
      final ExecutionEntity execution,
      final PvmScope eventSource,
      final String activityInstanceId,
      final String parentActivityInstanceId,
      final BusinessEventTypes eventType) {

    event.setId(activityInstanceId);
    event.setEventType(eventType.getEventName());
    event.setBusinessEventType(eventType.getBusinessEventName());
    event.setActivityInstanceId(activityInstanceId);
    event.setParentActivityInstanceId(parentActivityInstanceId);
    event.setProcessInstanceId(execution.getProcessInstanceId());
    event.setExecutionId(execution.getId());
    event.setTenantId(execution.getTenantId());
    event.setRootProcessInstanceId(execution.getRootProcessInstanceId());

    if (eventSource != null) {
      event.setActivityId(eventSource.getId());
      event.setActivityName((String) eventSource.getProperty("name"));
      event.setActivityType((String) eventSource.getProperty("type"));
    }

    final ExecutionEntity subProcessInstance = execution.getSubProcessInstance();
    if (subProcessInstance != null) {
      event.setCalledProcessInstanceId(subProcessInstance.getId());
    }

    fillProcessDefinitionData(event, execution);
    initSequenceCounter(execution, event);
  }

  private String resolveParentActivityInstanceId(final ExecutionEntity execution) {
    final ExecutionEntity parentExecution = execution.getParent();

    if (parentExecution != null && CompensationBehavior.isCompensationThrowing(parentExecution) && execution.getActivity() != null) {
      return CompensationBehavior.getParentActivityInstanceId(execution);
    }

    return execution.getParentActivityInstanceId();
  }

  private void fillHistoricStartTime(final BusinessActivityInstanceEventEntity event) {
    if (Context.getProcessEngineConfiguration().getHistoryLevel().equals(HistoryLevel.HISTORY_LEVEL_NONE)) {
      return;
    }

    Optional.ofNullable(Context.getCommandContext())
        .map(CommandContext::getDbEntityManager)
        .map(dbEntityManager -> dbEntityManager.selectById(HistoricActivityInstanceEntity.class, event.getActivityInstanceId()))
        .ifPresent(historicActivityInstance -> event.setStartTime(historicActivityInstance.getStartTime()));
  }
}
