package org.eximeebpms.bpm.events;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

/**
 * Transport-neutral event representation used by event broadcasting providers.
 *
 * <p>This class must not depend on Kafka, Spring, application server APIs or engine internals.
 * Provider modules should map this envelope to their native message format.</p>
 */
@Value
@Builder(toBuilder = true)
public class EventEnvelope {

  /**
   * Globally unique event identifier.
   */
  @NonNull
  String id;

  /**
   * Logical event type (np. camunda7:process-instance:started).
   */
  @NonNull
  String type;

  /**
   * Source component (np. history, task, execution).
   */
  @NonNull
  String source;

  /**
   * Moment when the original engine event occurred.
   */
  @NonNull
  Instant occurredAt;

  String tenantId;

  String rootProcessInstanceId;

  String processInstanceId;

  String executionId;

  String processDefinitionId;

  String processDefinitionKey;

  String caseInstanceId;

  String caseExecutionId;

  String caseDefinitionId;

  String activityInstanceId;

  String activityId;

  String taskId;

  String jobId;

  String incidentId;

  String deploymentId;

  /**
   * Provider-neutral event payload (i.e. EximeeBPMSProcessInstanceStartedEvent).
   */
  Object payload;

  /**
   * Transport metadata (np. Kafka key hints, correlation id, itp.).
   */
  @Singular("header")
  Map<String, String> headers;
}
