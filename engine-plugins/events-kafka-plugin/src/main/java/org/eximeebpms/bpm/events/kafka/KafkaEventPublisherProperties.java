package org.eximeebpms.bpm.events.kafka;

import lombok.Builder;
import lombok.Value;

/**
 * Kafka publisher configuration for event broadcasting.
 */
@Value
@Builder(toBuilder = true)
public class KafkaEventPublisherProperties {

  /**
   * Kafka bootstrap servers, for example localhost:9092.
   */
  String bootstrapServers;

  /**
   * Target topic for all BPMS events.
   */
  String topic;

  /**
   * Kafka client id.
   */
  @Builder.Default
  String clientId = "eximeebpms-events";

  /**
   * Enables producer idempotence.
   */
  @Builder.Default
  boolean enableIdempotence = true;

  /**
   * Kafka acknowledgements mode.
   */
  @Builder.Default
  String acks = "all";

  /**
   * Event header used as Kafka message key fallback strategy.
   */
  @Builder.Default
  String keyHeader = "processKey";

  /**
   * Whether publishing should wait for Kafka broker acknowledgement.
   *
   * <p>When enabled, {@link KafkaEventPublisher#publish(org.eximeebpms.bpm.events.EventEnvelope)}
   * blocks until Kafka acknowledges the record or throws an exception. This is required
   * if publishing failures should be propagated to the process engine transaction.</p>
   */
  @Builder.Default
  boolean waitForAck = false;

  /**
   * Number of retries performed by Kafka producer before failing delivery.
   */
  @Builder.Default
  int retries = Integer.MAX_VALUE;

  /**
   * Delay between Kafka producer retry attempts in milliseconds.
   */
  @Builder.Default
  long retryBackoffMs = 100L;

  /**
   * Upper bound for total time to report success or failure for a record send.
   */
  @Builder.Default
  int deliveryTimeoutMs = 120_000;

  /**
   * Maximum time to wait for a broker response for a single request.
   */
  @Builder.Default
  int requestTimeoutMs = 30_000;
}
