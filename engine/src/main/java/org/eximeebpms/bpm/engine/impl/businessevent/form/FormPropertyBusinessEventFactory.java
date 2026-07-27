package org.eximeebpms.bpm.engine.impl.businessevent.form;

import java.util.Optional;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;

public class FormPropertyBusinessEventFactory extends BusinessEventFactorySupport {

  public BusinessEvent createUpdateEvent(final ExecutionEntity execution, final String propertyId, final String propertyValue, final String taskId) {
    final BusinessFormPropertyEventEntity event = new BusinessFormPropertyEventEntity();

    event.setEventType(BusinessEventTypes.FORM_PROPERTY_UPDATE.getEventName());
    event.setBusinessEventType(BusinessEventTypes.FORM_PROPERTY_UPDATE.getBusinessEventName());
    event.setTimestamp(ClockUtil.getCurrentTime());
    event.setExecutionId(execution.getId());
    event.setProcessInstanceId(execution.getProcessInstanceId());
    event.setPropertyId(propertyId);
    event.setPropertyValue(propertyValue);
    event.setTaskId(taskId);
    event.setTenantId(execution.getTenantId());
    event.setUserOperationId(Optional.ofNullable(Context.getCommandContext())
        .map(CommandContext::getOperationId)
        .orElse(null));
    event.setRootProcessInstanceId(execution.getRootProcessInstanceId());

    fillProcessDefinitionData(event, execution);
    initSequenceCounter(execution, event);

    if (execution.isProcessInstanceStarting()) {
      event.setActivityInstanceId(execution.getProcessInstanceId());
    } else {
      event.setActivityInstanceId(execution.getActivityInstanceId());
    }

    return event;
  }
}
