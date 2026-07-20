package org.eximeebpms.bpm.engine.impl.businessevent.incident;

import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;

import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.eximeebpms.bpm.engine.incident.IncidentState;
import org.eximeebpms.bpm.engine.runtime.Incident;

public class IncidentBusinessEventFactory extends BusinessEventFactorySupport {

  public BusinessEvent createBusinessIncidentEvt(Incident incident, BusinessEventTypes eventType) {
    // create event
    BusinessIncidentEventEntity evt = new BusinessIncidentEventEntity();
    // initialize
    initBusinessIncidentEvent(evt, incident, eventType);

    if (BusinessEventTypes.INCIDENT_RESOLVE.equals(eventType) || BusinessEventTypes.INCIDENT_DELETE.equals(eventType)) {
      evt.setEndTime(ClockUtil.getCurrentTime());
    }

    return evt;
  }

  protected void initBusinessIncidentEvent(BusinessIncidentEventEntity evt, Incident incident, BusinessEventTypes eventType) {
    // init properties
    fillProcessDefinitionData(evt, incident);
    evt.setId(incident.getId());
    evt.setProcessInstanceId(incident.getProcessInstanceId());
    evt.setExecutionId(incident.getExecutionId());
    evt.setCreateTime(incident.getIncidentTimestamp());
    evt.setIncidentType(incident.getIncidentType());
    evt.setActivityId(incident.getActivityId());
    evt.setCauseIncidentId(incident.getCauseIncidentId());
    evt.setRootCauseIncidentId(incident.getRootCauseIncidentId());
    evt.setConfiguration(incident.getConfiguration());
    evt.setIncidentMessage(incident.getIncidentMessage());
    evt.setTenantId(incident.getTenantId());
    evt.setJobDefinitionId(incident.getJobDefinitionId());
    evt.setHistoryConfiguration(incident.getHistoryConfiguration());
    evt.setFailedActivityId(incident.getFailedActivityId());
    evt.setAnnotation(incident.getAnnotation());

    IncidentEntity incidentEntity = (IncidentEntity) incident;

    ExecutionEntity execution = incidentEntity.getExecution();
    if (execution != null) {
      evt.setRootProcessInstanceId(execution.getRootProcessInstanceId());
    }

    // init event type
    evt.setEventType(eventType.getEventName());
    evt.setBusinessEventType(eventType.getBusinessEventName());

    // init state
    IncidentState incidentState = IncidentState.DEFAULT;
    if (BusinessEventTypes.INCIDENT_DELETE.equals(eventType)) {
      incidentState = IncidentState.DELETED;
    } else if (BusinessEventTypes.INCIDENT_RESOLVE.equals(eventType)) {
      incidentState = IncidentState.RESOLVED;
    }
    evt.setIncidentState(incidentState.getStateCode());
  }

}
