package org.eximeebpms.bpm.engine.impl.businessevent.externaltask;

import org.eximeebpms.bpm.engine.history.ExternalTaskState;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.businessevent.InvalidBusinessEventTypeException;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;

import java.util.Date;

public class ExternalTaskBusinessEventFactory extends BusinessEventFactorySupport {

    public BusinessEvent createCreatedEvent(ExternalTaskEntity externalTaskEntity) {
        return initExternalTaskEvent(externalTaskEntity, BusinessEventTypes.EXTERNAL_TASK_CREATE);
    }

    public BusinessEvent createFailedEvent(ExternalTaskEntity externalTaskEntity) {
        ExternalTaskBusinessEvent event = initExternalTaskEvent(externalTaskEntity, BusinessEventTypes.EXTERNAL_TASK_FAIL);
        event.setErrorMessage(externalTaskEntity.getErrorMessage());
        String errorDetails = externalTaskEntity.getErrorDetails();
        if (errorDetails != null) {
            event.setErrorDetails(errorDetails);
        }
        return event;
    }

    public BusinessEvent createSuccessfulEvent(ExternalTaskEntity externalTaskEntity) {
        return initExternalTaskEvent(externalTaskEntity, BusinessEventTypes.EXTERNAL_TASK_SUCCESS);
    }

    public BusinessEvent createDeletedEvent(ExternalTaskEntity externalTaskEntity) {
        return initExternalTaskEvent(externalTaskEntity, BusinessEventTypes.EXTERNAL_TASK_DELETE);
    }

    protected ExternalTaskBusinessEvent initExternalTaskEvent(ExternalTaskEntity entity, BusinessEventTypes eventType) {
        ExternalTaskBusinessEvent event = new ExternalTaskBusinessEvent();

        fillProcessDefinitionData(event, entity);

        event.setTimestamp(getTimestamp(entity, eventType));
        event.setExternalTaskId(entity.getId());
        event.setTopicName(entity.getTopicName());
        event.setWorkerId(entity.getWorkerId());

        event.setPriority(entity.getPriority());
        event.setRetries(entity.getRetries());

        event.setActivityId(entity.getActivityId());
        event.setActivityInstanceId(entity.getActivityInstanceId());
        event.setExecutionId(entity.getExecutionId());

        event.setProcessInstanceId(entity.getProcessInstanceId());
        event.setTenantId(entity.getTenantId());
        event.setState(switch (eventType) {
            case EXTERNAL_TASK_CREATE -> ExternalTaskState.CREATED.getStateCode();
            case EXTERNAL_TASK_FAIL -> ExternalTaskState.FAILED.getStateCode();
            case EXTERNAL_TASK_SUCCESS -> ExternalTaskState.SUCCESSFUL.getStateCode();
            case EXTERNAL_TASK_DELETE -> ExternalTaskState.DELETED.getStateCode();
            default -> throw new InvalidBusinessEventTypeException("Invalid business event type: " + eventType);
        });

        event.setEventType(eventType.getEventName());
        event.setBusinessEventType(eventType.getBusinessEventName());

        ExecutionEntity execution = entity.getExecution();
        if (execution != null) {
            event.setRootProcessInstanceId(execution.getRootProcessInstanceId());
        }

        return event;
    }

    protected Date getTimestamp(ExternalTaskEntity entity, BusinessEventTypes eventType) {
        return eventType == BusinessEventTypes.EXTERNAL_TASK_CREATE ? entity.getCreateTime() : ClockUtil.getCurrentTime();
    }
}
