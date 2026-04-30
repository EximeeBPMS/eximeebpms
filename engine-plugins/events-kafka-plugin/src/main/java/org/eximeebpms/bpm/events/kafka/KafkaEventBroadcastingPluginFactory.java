package org.eximeebpms.bpm.events.kafka;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.eximeebpms.bpm.events.cfg.EventBroadcastingProperties;
import org.eximeebpms.bpm.events.engine.EventBroadcastingProcessEnginePlugin;
import org.eximeebpms.bpm.events.engine.history.HistoryEventMapper;

/**
 * Factory for creating event broadcasting engine plugin backed by Kafka.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaEventBroadcastingPluginFactory {

  public static ProcessEnginePlugin create(
      EventBroadcastingProperties eventBroadcastingProperties,
      KafkaEventPublisherProperties kafkaProperties) {

    return new EventBroadcastingProcessEnginePlugin(
        eventBroadcastingProperties,
        new HistoryEventMapper(),
        new KafkaEventPublisher(kafkaProperties)
    );
  }
}
