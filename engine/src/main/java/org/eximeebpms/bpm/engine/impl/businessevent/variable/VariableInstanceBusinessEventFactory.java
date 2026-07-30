package org.eximeebpms.bpm.engine.impl.businessevent.variable;

import java.util.Optional;
import org.eximeebpms.bpm.engine.delegate.VariableScope;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.TaskEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;

public class VariableInstanceBusinessEventFactory extends BusinessEventFactorySupport {

  public BusinessEvent createCreateEvent(final VariableInstanceEntity variableInstance, final VariableScope sourceVariableScope) {
    return createVariableEvent(variableInstance, sourceVariableScope, BusinessEventTypes.VARIABLE_INSTANCE_CREATE);
  }

  public BusinessEvent createUpdateEvent(final VariableInstanceEntity variableInstance, final VariableScope sourceVariableScope) {
    return createVariableEvent(variableInstance, sourceVariableScope, BusinessEventTypes.VARIABLE_INSTANCE_UPDATE);
  }

  public BusinessEvent createDeleteEvent(final VariableInstanceEntity variableInstance, final VariableScope sourceVariableScope) {
    return createVariableEvent(variableInstance, sourceVariableScope, BusinessEventTypes.VARIABLE_INSTANCE_DELETE);
  }

  public BusinessEvent createMigrateEvent(final VariableInstanceEntity variableInstance) {
    return createVariableEvent(variableInstance, null, BusinessEventTypes.VARIABLE_INSTANCE_MIGRATE);
  }

  protected BusinessEvent createVariableEvent(final VariableInstanceEntity variableInstance, final VariableScope sourceVariableScope, final BusinessEventTypes eventType) {
    final String scopeActivityInstanceId = resolveScopeActivityInstanceId(variableInstance);
    final VariableSourceContext sourceContext = resolveSourceContext(sourceVariableScope);
    final BusinessVariableUpdateEventEntity event = new BusinessVariableUpdateEventEntity();

    initVariableEvent(event, variableInstance, eventType);
    initVariableContext(event, scopeActivityInstanceId, sourceContext);
    markInitialVariableIfNeeded(event, variableInstance, sourceContext, eventType);

    return event;
  }

  protected void initVariableEvent(final BusinessVariableUpdateEventEntity event, final VariableInstanceEntity variableInstance, final BusinessEventTypes eventType) {
    event.setTimestamp(ClockUtil.getCurrentTime());
    event.setUserOperationId(Optional.ofNullable(Context.getCommandContext())
        .map(CommandContext::getOperationId)
        .orElse(null));
    event.setEventType(eventType.getEventName());
    event.setBusinessEventType(eventType.getBusinessEventName());

    event.setVariableInstanceId(variableInstance.getId());
    event.setProcessInstanceId(variableInstance.getProcessInstanceId());
    event.setExecutionId(variableInstance.getExecutionId());
    event.setCaseInstanceId(variableInstance.getCaseInstanceId());
    event.setCaseExecutionId(variableInstance.getCaseExecutionId());
    event.setTaskId(variableInstance.getTaskId());
    event.setRevision(variableInstance.getRevision());
    event.setVariableName(variableInstance.getName());
    event.setSerializerName(variableInstance.getSerializerName());
    event.setTenantId(variableInstance.getTenantId());

    fillProcessInstanceData(event, variableInstance);
    fillCaseDefinitionData(event, variableInstance);
    copyVariableValue(event, variableInstance);
    initSequenceCounter(variableInstance.getSequenceCounter(), event);
  }

  void initVariableContext(final BusinessVariableUpdateEventEntity event, final String scopeActivityInstanceId, final VariableSourceContext sourceContext) {
    event.setScopeActivityInstanceId(scopeActivityInstanceId);
    event.setActivityInstanceId(sourceContext.sourceActivityInstanceId());
  }

  protected String resolveScopeActivityInstanceId(VariableInstanceEntity variableInstance) {
    if (variableInstance.getExecutionId() != null) {
      final ExecutionEntity scopeExecution = Optional.ofNullable(Context.getCommandContext())
          .map(CommandContext::getDbEntityManager)
          .map(manager -> manager.selectById(ExecutionEntity.class, variableInstance.getExecutionId()))
          .orElse(null);

      if (scopeExecution == null) {
        return null;
      }

      if (variableInstance.getTaskId() == null && !variableInstance.isConcurrentLocal()) {
        return scopeExecution.getParentActivityInstanceId();
      }

      return scopeExecution.getActivityInstanceId();
    }

    if (variableInstance.getCaseExecutionId() != null) {
      return variableInstance.getCaseExecutionId();
    }

    return null;
  }

  VariableSourceContext resolveSourceContext(final VariableScope sourceVariableScope) {
    ExecutionEntity sourceExecution = null;
    String sourceActivityInstanceId = null;

    if (sourceVariableScope instanceof ExecutionEntity executionEntity) {
      sourceExecution = executionEntity;
      sourceActivityInstanceId = executionEntity.getActivityInstanceId();

    } else if (sourceVariableScope instanceof TaskEntity taskEntity) {
      sourceExecution = taskEntity.getExecution();

      if (sourceExecution != null) {
        sourceActivityInstanceId = sourceExecution.getActivityInstanceId();
      } else {
        final CaseExecutionEntity sourceCaseExecution = taskEntity.getCaseExecution();

        if (sourceCaseExecution != null) {
          sourceActivityInstanceId = sourceCaseExecution.getId();
        }
      }

    } else if (sourceVariableScope instanceof CaseExecutionEntity caseExecutionEntity) {
      sourceActivityInstanceId = caseExecutionEntity.getId();
    }

    return new VariableSourceContext(sourceExecution, sourceActivityInstanceId);
  }

  protected void fillProcessInstanceData(final BusinessVariableUpdateEventEntity event, final VariableInstanceEntity variableInstance) {
    final ExecutionEntity execution = variableInstance.getExecution();

    if (execution == null) {
      return;
    }

    fillProcessDefinitionData(event, execution);
    event.setRootProcessInstanceId(execution.getRootProcessInstanceId());
  }

  protected void fillCaseDefinitionData(final BusinessVariableUpdateEventEntity event, final VariableInstanceEntity variableInstance) {
    final CaseExecutionEntity caseExecution = variableInstance.getCaseExecution();

    if (caseExecution == null) {
      return;
    }

    if (caseExecution.getCaseDefinition() instanceof CaseDefinitionEntity definition) {
      event.setCaseDefinitionId(definition.getId());
      event.setCaseDefinitionKey(definition.getKey());
      event.setCaseDefinitionName(definition.getName());
    }
  }

  protected void copyVariableValue(final BusinessVariableUpdateEventEntity event, final VariableInstanceEntity variableInstance) {
    event.setTextValue(variableInstance.getTextValue());
    event.setTextValue2(variableInstance.getTextValue2());
    event.setDoubleValue(variableInstance.getDoubleValue());
    event.setLongValue(variableInstance.getLongValue());
    event.setByteArrayId(variableInstance.getByteArrayValueId());

    if (variableInstance.getByteArrayValueId() != null) {
      event.setByteValue(variableInstance.getByteArrayValue());
    }
  }

  void markInitialVariableIfNeeded(
      final BusinessVariableUpdateEventEntity event,
      final VariableInstanceEntity variableInstance,
      final VariableSourceContext sourceContext,
      final BusinessEventTypes eventType) {
    final ExecutionEntity sourceExecution = sourceContext.sourceExecution();

    if (sourceExecution == null) {
      return;
    }

    if (!sourceExecution.isProcessInstanceStarting()) {
      return;
    }

    if (!BusinessEventTypes.VARIABLE_INSTANCE_CREATE.equals(eventType)) {
      return;
    }

    if (variableInstance.getSequenceCounter() == 1) {
      event.setInitial(true);
    }

    if (sourceContext.sourceActivityInstanceId() == null
        && sourceExecution.getActivity() != null
        && sourceExecution.getTransition() == null) {
      event.setActivityInstanceId(sourceExecution.getProcessInstanceId());
    }
  }
}
