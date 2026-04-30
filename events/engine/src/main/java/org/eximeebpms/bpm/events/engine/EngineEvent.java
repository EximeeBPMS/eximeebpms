package org.eximeebpms.bpm.events.engine;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

/**
 * Lightweight engine event collected during process engine execution.
 *
 * <p>This model is intentionally independent from history/audit entities and DAO models.
 * It represents an event that should be persisted to the transactional outbox.</p>
 */
@Value
@Builder(toBuilder = true)
public class EngineEvent {

  /**
   * Stable event identifier.
   */
  @NonNull
  String id;

  /**
   * Logical event type, for example eximeebpms:process-instance:started.
   */
  @NonNull
  String type;

  /**
   * Engine component that produced the event.
   */
  @NonNull
  String source;

  /**
   * Time when the engine event occurred.
   */
  @NonNull
  Instant occurredAt;

  String tenantId;

  String rootProcessInstanceId;

  String processInstanceId;

  String executionId;

  String processDefinitionId;

  String processDefinitionKey;

  String activityId;

  String activityInstanceId;

  String taskId;

  String jobId;

  String incidentId;

  String deploymentId;

  /**
   * Lightweight event payload.
   */
  Object payload;

  /**
   * Additional metadata useful for routing, ordering and correlation.
   */
  @Singular("header")
  Map<String, String> headers;
}
