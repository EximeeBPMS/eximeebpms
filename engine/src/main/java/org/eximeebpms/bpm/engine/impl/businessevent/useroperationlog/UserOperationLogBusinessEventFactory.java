package org.eximeebpms.bpm.engine.impl.businessevent.useroperationlog;

import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.oplog.UserOperationLogContext;
import org.eximeebpms.bpm.engine.impl.oplog.UserOperationLogContextEntry;
import org.eximeebpms.bpm.engine.impl.persistence.entity.PropertyChange;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;

import java.util.ArrayList;
import java.util.List;

public class UserOperationLogBusinessEventFactory extends BusinessEventFactorySupport {

  public List<BusinessEvent> createEvents(UserOperationLogContext context) {
    List<BusinessEvent> events = new ArrayList<>();

    for (UserOperationLogContextEntry entry : context.getEntries()) {
      for (PropertyChange propertyChange : entry.getPropertyChanges()) {
        events.add(createEvent(context, entry, propertyChange));
      }
    }

    return events;
  }

  protected UserOperationLogBusinessEvent createEvent(UserOperationLogContext context,
      UserOperationLogContextEntry entry, PropertyChange propertyChange) {

    UserOperationLogBusinessEvent event = new UserOperationLogBusinessEvent();
    event.setEventType(BusinessEventTypes.USER_OPERATION_LOG.getEventName());
    event.setBusinessEventType(BusinessEventTypes.USER_OPERATION_LOG.getBusinessEventName());

    fillProcessDefinitionData(event, entry);
    event.setProcessInstanceId(entry.getProcessInstanceId());
    event.setExecutionId(entry.getExecutionId());
    event.setRootProcessInstanceId(entry.getRootProcessInstanceId());

    event.setOperationId(context.getOperationId());
    event.setUserId(context.getUserId());
    event.setOperationType(entry.getOperationType());
    event.setEntityType(entry.getEntityType());
    event.setTaskId(entry.getTaskId());
    event.setJobId(entry.getJobId());
    event.setJobDefinitionId(entry.getJobDefinitionId());
    event.setDeploymentId(entry.getDeploymentId());
    event.setBatchId(entry.getBatchId());
    event.setExternalTaskId(entry.getExternalTaskId());
    event.setCategory(entry.getCategory());
    event.setAnnotation(entry.getAnnotation());
    event.setTenantId(entry.getTenantId());
    event.setTimestamp(ClockUtil.getCurrentTime());

    event.setProperty(propertyChange.getPropertyName());
    event.setOrgValue(propertyChange.getOrgValueString());
    event.setNewValue(propertyChange.getNewValueString());

    return event;
  }

}
