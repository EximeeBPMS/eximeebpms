package org.eximeebpms.bpm.events.kafka.plugin;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eximeebpms.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.junit.jupiter.api.Test;

class KafkaEventBroadcastingProcessEnginePluginTest {

  @Test
  void shouldNotRequireKafkaConfigurationWhenDisabled() {
    final KafkaEventBroadcastingProcessEnginePlugin plugin = new KafkaEventBroadcastingProcessEnginePlugin();

    assertThatCode(() -> plugin.preInit(new StandaloneProcessEngineConfiguration()))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldFailWhenEnabledWithoutBootstrapServers() {
    final KafkaEventBroadcastingProcessEnginePlugin plugin = new KafkaEventBroadcastingProcessEnginePlugin();

    plugin.setEnabled(true);
    plugin.setTopic("bpms-events");

    assertThatThrownBy(() -> plugin.preInit(new StandaloneProcessEngineConfiguration()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("bootstrapServers");
  }

  @Test
  void shouldFailWhenEnabledWithoutTopic() {
    final KafkaEventBroadcastingProcessEnginePlugin plugin = new KafkaEventBroadcastingProcessEnginePlugin();

    plugin.setEnabled(true);
    plugin.setBootstrapServers("localhost:9092");

    assertThatThrownBy(() -> plugin.preInit(new StandaloneProcessEngineConfiguration()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("topic");
  }

  @Test
  void shouldInitializeWhenEnabledWithRequiredKafkaProperties() {
    final KafkaEventBroadcastingProcessEnginePlugin plugin = new KafkaEventBroadcastingProcessEnginePlugin();

    plugin.setEnabled(true);
    plugin.setBootstrapServers("localhost:9092");
    plugin.setTopic("bpms-events");

    assertThatCode(plugin::validate).doesNotThrowAnyException();
  }

  @Test
  void shouldValidateSuccessfullyWhenEnabledWithRequiredKafkaProperties() {
    final KafkaEventBroadcastingProcessEnginePlugin plugin = new KafkaEventBroadcastingProcessEnginePlugin();

    plugin.setEnabled(true);
    plugin.setBootstrapServers("localhost:9092");
    plugin.setTopic("bpms-events");

    assertThatCode(plugin::validate).doesNotThrowAnyException();
  }
}
