package org.eximeebpms.bpm.events.engine.mapper;

import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.engine.EngineEvent;

public class DefaultEngineEventEnvelopeMapper implements EngineEventEnvelopeMapper {

  @Override
  public EventEnvelope map(EngineEvent event) {
    return EventEnvelope.builder()
        .id(event.getId())
        .type(event.getType())
        .source(event.getSource())
        .occurredAt(event.getOccurredAt())
        .tenantId(event.getTenantId())
        .rootProcessInstanceId(event.getRootProcessInstanceId())
        .processInstanceId(event.getProcessInstanceId())
        .executionId(event.getExecutionId())
        .processDefinitionId(event.getProcessDefinitionId())
        .processDefinitionKey(event.getProcessDefinitionKey())
        .activityId(event.getActivityId())
        .activityInstanceId(event.getActivityInstanceId())
        .taskId(event.getTaskId())
        .jobId(event.getJobId())
        .incidentId(event.getIncidentId())
        .deploymentId(event.getDeploymentId())
        .payload(event.getPayload())
        .headers(event.getHeaders())
        .build();
  }
}
