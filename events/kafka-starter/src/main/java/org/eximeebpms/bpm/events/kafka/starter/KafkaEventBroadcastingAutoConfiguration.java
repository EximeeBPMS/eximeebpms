package org.eximeebpms.bpm.events.kafka.starter;

import org.eximeebpms.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(KafkaEventBroadcastingProcessEnginePlugin.class)
@EnableConfigurationProperties(KafkaEventBroadcastingSpringProperties.class)
public class KafkaEventBroadcastingAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(KafkaEventBroadcastingProcessEnginePlugin.class)
  @ConditionalOnProperty(
      prefix = "eximeebpms.bpm.event-broadcasting",
      name = "enabled",
      havingValue = "true"
  )
  public ProcessEnginePlugin kafkaEventBroadcastingProcessEnginePlugin(final KafkaEventBroadcastingSpringProperties properties) {
    return new KafkaEventBroadcastingProcessEnginePlugin(
        properties.toEventBroadcastingProperties(),
        properties.toKafkaEventPublisherProperties()
    );
  }
}
