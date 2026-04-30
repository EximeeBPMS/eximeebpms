package org.eximeebpms.bpm.events.kafka.plugin;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.eximeebpms.bpm.events.cfg.EventBroadcastingProperties;
import org.eximeebpms.bpm.events.engine.EventBroadcastingProcessEnginePlugin;
import org.eximeebpms.bpm.events.engine.history.HistoryEventMapper;
import org.eximeebpms.bpm.events.kafka.KafkaEventPublisher;
import org.eximeebpms.bpm.events.kafka.KafkaEventPublisherProperties;
import org.eximeebpms.bpm.events.publisher.NoopEventPublisher;

/**
 * Ready-to-use ProcessEnginePlugin for Kafka event broadcasting.
 * <p>
 * Can be registered in bpm-platform.xml or Spring Boot configuration.
 * </p>
 */
@Setter
@NoArgsConstructor
public class KafkaEventBroadcastingProcessEnginePlugin implements ProcessEnginePlugin {

  private EventBroadcastingProcessEnginePlugin delegate;
  private boolean enabled = false;
  private boolean failOnPublishError = false;
  private boolean async = true;

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

  public KafkaEventBroadcastingProcessEnginePlugin(EventBroadcastingProperties eventProperties, KafkaEventPublisherProperties kafkaProperties) {
    this.delegate = new EventBroadcastingProcessEnginePlugin(
        eventProperties,
        new HistoryEventMapper(),
        new KafkaEventPublisher(kafkaProperties)
    );
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    if (delegate == null) {
      delegate = createDelegateFromFields();
    }

    delegate.preInit(processEngineConfiguration);
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl configuration) {
    if (delegate != null) {
      delegate.postInit(configuration);
    }
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    if (delegate != null) {
      delegate.postProcessEngineBuild(processEngine);
    }
  }

  private EventBroadcastingProcessEnginePlugin createDelegateFromFields() {
    if (!enabled) {
      return EventBroadcastingProcessEnginePlugin.createDefault(
          EventBroadcastingProperties.disabled(),
          NoopEventPublisher.INSTANCE
      );
    }

    validate();

    EventBroadcastingProperties eventProperties = EventBroadcastingProperties.builder()
        .enabled(true)
        .failOnPublishError(failOnPublishError)
        .async(async)
        .build();

    KafkaEventPublisherProperties kafkaProperties = KafkaEventPublisherProperties.builder()
        .bootstrapServers(bootstrapServers)
        .topic(topic)
        .clientId(clientId)
        .enableIdempotence(enableIdempotence)
        .acks(acks)
        .keyHeader(keyHeader)
        .waitForAck(waitForAck)
        .retries(retries)
        .retryBackoffMs(retryBackoffMs)
        .deliveryTimeoutMs(deliveryTimeoutMs)
        .requestTimeoutMs(requestTimeoutMs)
        .build();

    return new EventBroadcastingProcessEnginePlugin(
        eventProperties,
        new HistoryEventMapper(),
        new KafkaEventPublisher(kafkaProperties)
    );
  }

  void validate() {
    if (!enabled) {
      return;
    }

    if (bootstrapServers == null || bootstrapServers.isBlank()) {
      throw new IllegalArgumentException("Kafka event broadcasting is enabled, but bootstrapServers is not configured");
    }

    if (topic == null || topic.isBlank()) {
      throw new IllegalArgumentException("Kafka event broadcasting is enabled, but topic is not configured");
    }
  }
}
