package org.eximeebpms.bpm.engine.impl.businessevent.identitylink;

import java.util.Optional;
import org.eximeebpms.bpm.engine.ProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.TaskEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;

public class IdentityLinkBusinessEventFactory extends BusinessEventFactorySupport {

  public static final String DELETE_OPERATION_TYPE = "delete";
  public static final String ADD_OPERATION_TYPE = "add";

  public BusinessEvent createAddEvent(final IdentityLinkEntity identityLinkEntity) {
    return createIdentityLinkEvent(identityLinkEntity, BusinessEventTypes.IDENTITY_LINK_ADD);
  }

  public BusinessEvent createDeleteEvent(final IdentityLinkEntity identityLinkEntity) {
    return createIdentityLinkEvent(identityLinkEntity, BusinessEventTypes.IDENTITY_LINK_DELETE);
  }

  private BusinessIdentityLinkEventEntity createIdentityLinkEvent(final IdentityLinkEntity identityLinkEntity, final BusinessEventTypes eventType) {
    final BusinessIdentityLinkEventEntity event = new BusinessIdentityLinkEventEntity();
    initIdentityLinkEvent(event, identityLinkEntity, eventType);
    return event;
  }

  private void initIdentityLinkEvent(final BusinessIdentityLinkEventEntity event, final IdentityLinkEntity identityLinkEntity, final BusinessEventTypes eventType) {
    if (identityLinkEntity.getTaskId() != null) {
      fillTaskRelatedData(event, identityLinkEntity);
    }

    if (identityLinkEntity.getProcessDefId() != null) {
      fillProcessDefinitionData(event, identityLinkEntity);
    }

    event.setTime(ClockUtil.getCurrentTime());
    event.setType(identityLinkEntity.getType());
    event.setUserId(identityLinkEntity.getUserId());
    event.setGroupId(identityLinkEntity.getGroupId());
    event.setTaskId(identityLinkEntity.getTaskId());
    event.setTenantId(identityLinkEntity.getTenantId());

    event.setOperationType(BusinessEventTypes.IDENTITY_LINK_DELETE.equals(eventType)
        ? DELETE_OPERATION_TYPE
        : ADD_OPERATION_TYPE);
    event.setEventType(eventType.getEventName());
    event.setBusinessEventType(eventType.getBusinessEventName());
    event.setAssignerId(Optional.ofNullable(Context.getCommandContext())
        .map(CommandContext::getAuthenticatedUserId)
        .orElse(null));
  }

  private void fillTaskRelatedData(BusinessIdentityLinkEventEntity event, IdentityLinkEntity identityLinkEntity) {
    Optional.ofNullable(Context.getCommandContext())
        .map(CommandContext::getTaskManager)
        .map(taskManager -> taskManager.findTaskById(identityLinkEntity.getTaskId()))
        .ifPresent(task -> {
          if (task.getProcessDefinition() != null) {
            fillProcessDefinitionData(event, task);
          }

          final ExecutionEntity execution = task.getExecution();
          if (execution != null) {
            event.setRootProcessInstanceId(execution.getRootProcessInstanceId());

            if (isHistoryRemovalTimeStrategyStart()) {
              provideRemovalTime(event);
            }
          }
        });
  }

  private void fillProcessDefinitionData(BusinessEvent event, TaskEntity task) {
    final String processDefinitionId = task.getProcessDefinitionId();

    if (processDefinitionId != null) {
      fillProcessDefinitionData(event, processDefinitionId);
    } else {
      event.setProcessDefinitionId(task.getProcessDefinitionId());
    }
  }

  private void fillProcessDefinitionData(final BusinessEvent event, final IdentityLinkEntity identityLink) {
    final String processDefinitionId = identityLink.getProcessDefId();

    if (processDefinitionId != null) {
      fillProcessDefinitionData(event, processDefinitionId);
    } else {
      event.setProcessDefinitionId(identityLink.getProcessDefId());
    }
  }

  private boolean isHistoryRemovalTimeStrategyStart() {
    return Optional.ofNullable(Context.getProcessEngineConfiguration())
        .map(ProcessEngineConfigurationImpl::getHistoryRemovalTimeStrategy)
        .map(ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START::equals)
        .orElse(false);
  }

  private void provideRemovalTime(BusinessEvent event) {
    final String rootProcessInstanceId = event.getRootProcessInstanceId();

    if (rootProcessInstanceId == null) {
      return;
    }

    Optional.ofNullable(Context.getCommandContext())
        .map(CommandContext::getDbEntityManager)
        .map(dbEntityManager -> dbEntityManager.selectById(HistoricProcessInstanceEventEntity.class, rootProcessInstanceId))
        .ifPresent(historicProcessInstance -> event.setRemovalTime(historicProcessInstance.getRemovalTime()));
  }
}
