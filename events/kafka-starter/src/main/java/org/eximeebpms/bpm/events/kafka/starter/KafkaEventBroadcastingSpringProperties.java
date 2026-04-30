package org.eximeebpms.bpm.events.kafka.starter;

import lombok.Getter;
import lombok.Setter;
import org.eximeebpms.bpm.events.cfg.EventBroadcastingProperties;
import org.eximeebpms.bpm.events.kafka.KafkaEventPublisherProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "eximeebpms.bpm.event-broadcasting")
public class KafkaEventBroadcastingSpringProperties {

  private boolean enabled = false;
  private boolean failOnPublishError = false;
  private boolean async = true;

  private Kafka kafka = new Kafka();

  @Getter
  @Setter
  public static class Kafka {

    private String bootstrapServers;
    private String topic;
    private String clientId = "eximeebpms-events";
    private boolean enableIdempotence = true;
    private String acks = "all";
    private String keyHeader = "processKey";
    private boolean waitForAck = false;
    private int retries = Integer.MAX_VALUE;
    private long retryBackoffMs = 100L;
    private int deliveryTimeoutMs = 120_000;
    private int requestTimeoutMs = 30_000;
  }

  public EventBroadcastingProperties toEventBroadcastingProperties() {
    return EventBroadcastingProperties.builder()
        .enabled(enabled)
        .failOnPublishError(failOnPublishError)
        .async(async)
        .build();
  }

  public KafkaEventPublisherProperties toKafkaEventPublisherProperties() {
    return KafkaEventPublisherProperties.builder()
        .bootstrapServers(kafka.getBootstrapServers())
        .topic(kafka.getTopic())
        .clientId(kafka.getClientId())
        .enableIdempotence(kafka.isEnableIdempotence())
        .acks(kafka.getAcks())
        .keyHeader(kafka.getKeyHeader())
        .waitForAck(kafka.isWaitForAck())
        .retries(kafka.getRetries())
        .retryBackoffMs(kafka.getRetryBackoffMs())
        .deliveryTimeoutMs(kafka.getDeliveryTimeoutMs())
        .requestTimeoutMs(kafka.getRequestTimeoutMs())
        .build();
  }
}
