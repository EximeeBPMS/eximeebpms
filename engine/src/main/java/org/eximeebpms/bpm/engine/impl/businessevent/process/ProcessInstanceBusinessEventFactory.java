package org.eximeebpms.bpm.engine.impl.businessevent.process;

import java.util.Date;
import java.util.Optional;
import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;

public class ProcessInstanceBusinessEventFactory extends BusinessEventFactorySupport {

  public BusinessEvent createStartEvent(final DelegateExecution execution) {
    if (!(execution instanceof ExecutionEntity executionEntity)) {
      return null;
    }

    final Date now = ClockUtil.getCurrentTime();
    final BusinessProcessInstanceEventEntity event = baseProcessInstanceBuilder(
        executionEntity,
        BusinessEventTypes.PROCESS_INSTANCE_START,
        now)
        .startUserId(Optional.ofNullable(Context.getCommandContext())
            .map(CommandContext::getAuthenticatedUserId)
            .orElse(null))
        .startActivityId(executionEntity.getActivityId())
        .state(BusinessProcessInstanceState.ACTIVE.getValue())
        .startTime(now)
        .build();

    fillProcessDefinitionData(event, executionEntity);
    initSequenceCounter(executionEntity, event);
    fillHistoricProcessInstanceData(event);

    return event;
  }

  public BusinessEvent createEndEvent(final DelegateExecution execution) {
    if (!(execution instanceof ExecutionEntity executionEntity)) {
      return null;
    }

    final Date now = ClockUtil.getCurrentTime();
    final BusinessProcessInstanceEventEntity event = baseProcessInstanceBuilder(
        executionEntity,
        BusinessEventTypes.PROCESS_INSTANCE_END,
        now)
        .endActivityId(executionEntity.getActivityId())
        .state(resolveEndState(executionEntity))
        .deleteReason(executionEntity.getDeleteReason())
        .endTime(now)
        .build();

    fillProcessDefinitionData(event, executionEntity);
    initSequenceCounter(executionEntity, event);
    fillHistoricProcessInstanceData(event);

    if (event.getStartTime() != null) {
      event.setDurationInMillis(event.getEndTime().getTime() - event.getStartTime().getTime());
    }

    return event;
  }

  public BusinessEvent createUpdateEvent(final DelegateExecution execution) {
    if (!(execution instanceof ExecutionEntity executionEntity)) {
      return null;
    }

    final Date now = ClockUtil.getCurrentTime();
    final BusinessProcessInstanceEventEntity event = baseProcessInstanceBuilder(
        executionEntity,
        BusinessEventTypes.PROCESS_INSTANCE_UPDATE,
        now)
        .state(resolveUpdateState(executionEntity))
        .build();

    fillProcessDefinitionData(event, executionEntity);
    initSequenceCounter(executionEntity, event);
    fillHistoricProcessInstanceData(event);

    return event;
  }

  private BusinessProcessInstanceEventEntity.BusinessProcessInstanceEventEntityBuilder<?, ?> baseProcessInstanceBuilder(
      final ExecutionEntity executionEntity,
      final BusinessEventTypes eventType,
      final Date now) {

    return BusinessProcessInstanceEventEntity.builder()
        .id(executionEntity.getProcessInstanceId())
        .eventType(eventType.getEventName())
        .businessEventType(eventType.getBusinessEventName())
        .processInstanceId(executionEntity.getProcessInstanceId())
        .executionId(executionEntity.getId())
        .rootProcessInstanceId(executionEntity.getRootProcessInstanceId())
        .businessKey(executionEntity.getProcessBusinessKey())
        .tenantId(executionEntity.getTenantId())
        .superProcessInstanceId(Optional.ofNullable(executionEntity.getSuperExecution())
            .map(ExecutionEntity::getProcessInstanceId)
            .orElse(null))
        .timestamp(now)
        .userOperationId(Optional.ofNullable(Context.getCommandContext())
            .map(CommandContext::getOperationId)
            .orElse(null));
  }

  private String resolveUpdateState(final ExecutionEntity executionEntity) {
    if (executionEntity.isSuspended()) {
      return BusinessProcessInstanceState.SUSPENDED.getValue();
    }

    return BusinessProcessInstanceState.ACTIVE.getValue();
  }

  private String resolveEndState(final ExecutionEntity executionEntity) {
    if (executionEntity.getActivity() != null) {
      return BusinessProcessInstanceState.COMPLETED.getValue();
    }

    if (executionEntity.isExternallyTerminated()) {
      return BusinessProcessInstanceState.EXTERNALLY_TERMINATED.getValue();
    }

    return BusinessProcessInstanceState.INTERNALLY_TERMINATED.getValue();
  }

  private void fillHistoricProcessInstanceData(final BusinessProcessInstanceEventEntity event) {
    final HistoricProcessInstanceEventEntity historicProcessInstance = findHistoricProcessInstance(event.getProcessInstanceId());
    if (historicProcessInstance == null) {
      return;
    }

    if (!BusinessEventTypes.PROCESS_INSTANCE_START.getBusinessEventName().equals(event.getBusinessEventType())) {
      event.setStartTime(historicProcessInstance.getStartTime());
    }
  }

  private HistoricProcessInstanceEventEntity findHistoricProcessInstance(final String processInstanceId) {
    if (processInstanceId == null) {
      return null;
    }

    return Optional.ofNullable(Context.getCommandContext())
        .map(CommandContext::getDbEntityManager)
        .map(dbEntityManager -> dbEntityManager.selectById(HistoricProcessInstanceEventEntity.class, processInstanceId))
        .orElse(null);
  }
}
