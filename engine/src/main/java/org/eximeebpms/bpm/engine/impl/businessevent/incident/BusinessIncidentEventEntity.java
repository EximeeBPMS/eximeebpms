package org.eximeebpms.bpm.engine.impl.businessevent.incident;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eximeebpms.bpm.engine.incident.IncidentState;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessIncidentEventEntity extends BusinessEvent {

    protected Date createTime;
    protected Date endTime;
    protected String incidentType;
    protected String activityId;
    protected String causeIncidentId;
    protected String rootCauseIncidentId;
    protected String configuration;
    protected String incidentMessage;
    protected int incidentState;
    protected String tenantId;
    protected String jobDefinitionId;
    protected String historyConfiguration;
    protected String failedActivityId;
    protected String annotation;

    public boolean isOpen() {
        return IncidentState.DEFAULT.getStateCode() == incidentState;
    }

    public boolean isDeleted() {
        return IncidentState.DELETED.getStateCode() == incidentState;
    }

    public boolean isResolved() {
        return IncidentState.RESOLVED.getStateCode() == incidentState;
    }

}
