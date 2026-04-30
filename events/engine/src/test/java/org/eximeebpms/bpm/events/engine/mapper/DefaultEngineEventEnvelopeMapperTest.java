package org.eximeebpms.bpm.events.engine.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.engine.EngineEvent;
import org.junit.jupiter.api.Test;

class DefaultEngineEventEnvelopeMapperTest {

  private final DefaultEngineEventEnvelopeMapper mapper = new DefaultEngineEventEnvelopeMapper();

  @Test
  void shouldMapEngineEventToEnvelope() {
    final Instant occurredAt = Instant.now();

    final EngineEvent event = EngineEvent.builder()
        .id("event-id")
        .type("event-type")
        .source("engine")
        .occurredAt(occurredAt)
        .tenantId("tenant-id")
        .rootProcessInstanceId("root-process-instance-id")
        .processInstanceId("process-instance-id")
        .executionId("execution-id")
        .processDefinitionId("process-definition-id")
        .processDefinitionKey("process-definition-key")
        .activityId("activity-id")
        .activityInstanceId("activity-instance-id")
        .taskId("task-id")
        .jobId("job-id")
        .incidentId("incident-id")
        .deploymentId("deployment-id")
        .payload("payload")
        .header("processKey", "root-process-instance-id")
        .build();

    final EventEnvelope envelope = mapper.map(event);

    assertThat(envelope.getId()).isEqualTo("event-id");
    assertThat(envelope.getType()).isEqualTo("event-type");
    assertThat(envelope.getSource()).isEqualTo("engine");
    assertThat(envelope.getOccurredAt()).isEqualTo(occurredAt);
    assertThat(envelope.getTenantId()).isEqualTo("tenant-id");
    assertThat(envelope.getRootProcessInstanceId()).isEqualTo("root-process-instance-id");
    assertThat(envelope.getProcessInstanceId()).isEqualTo("process-instance-id");
    assertThat(envelope.getExecutionId()).isEqualTo("execution-id");
    assertThat(envelope.getProcessDefinitionId()).isEqualTo("process-definition-id");
    assertThat(envelope.getProcessDefinitionKey()).isEqualTo("process-definition-key");
    assertThat(envelope.getActivityId()).isEqualTo("activity-id");
    assertThat(envelope.getActivityInstanceId()).isEqualTo("activity-instance-id");
    assertThat(envelope.getTaskId()).isEqualTo("task-id");
    assertThat(envelope.getJobId()).isEqualTo("job-id");
    assertThat(envelope.getIncidentId()).isEqualTo("incident-id");
    assertThat(envelope.getDeploymentId()).isEqualTo("deployment-id");
    assertThat(envelope.getPayload()).isEqualTo("payload");
    assertThat(envelope.getHeaders()).containsEntry("processKey", "root-process-instance-id");
  }
}
