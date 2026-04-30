package org.eximeebpms.bpm.events.kafka.starter;

import static org.assertj.core.api.Assertions.assertThat;

import org.eximeebpms.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.eximeebpms.bpm.events.kafka.plugin.KafkaEventBroadcastingProcessEnginePlugin;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class KafkaEventBroadcastingAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(KafkaEventBroadcastingAutoConfiguration.class));

  @Test
  void shouldLoadAutoConfiguration() {
    contextRunner
        .withPropertyValues(
            "eximeebpms.bpm.event-broadcasting.enabled=true",
            "eximeebpms.bpm.event-broadcasting.kafka.bootstrap-servers=localhost:9092",
            "eximeebpms.bpm.event-broadcasting.kafka.topic=bpms-events"
        )
        .run(context ->
            assertThat(context).hasSingleBean(KafkaEventBroadcastingProcessEnginePlugin.class)
        );
  }

  @Test
  void shouldCreateKafkaEventBroadcastingProcessEnginePluginBean() {
    contextRunner
        .withPropertyValues(
            "eximeebpms.bpm.event-broadcasting.enabled=true",
            "eximeebpms.bpm.event-broadcasting.kafka.bootstrap-servers=localhost:9092",
            "eximeebpms.bpm.event-broadcasting.kafka.topic=bpms-events"
        )
        .run(context -> {
          assertThat(context).hasSingleBean(ProcessEnginePlugin.class);
          assertThat(context).hasSingleBean(KafkaEventBroadcastingProcessEnginePlugin.class);
        });
  }

  @Test
  void shouldBackOffWhenCustomProcessEnginePluginBeanExists() {
    contextRunner
        .withPropertyValues("eximeebpms.bpm.event-broadcasting.enabled=true")
        .withBean(
            "customKafkaEventBroadcastingProcessEnginePlugin",
            KafkaEventBroadcastingProcessEnginePlugin.class,
            KafkaEventBroadcastingProcessEnginePlugin::new
        )
        .run(context -> {
          assertThat(context).hasSingleBean(KafkaEventBroadcastingProcessEnginePlugin.class);
          assertThat(context).hasSingleBean(ProcessEnginePlugin.class);
          assertThat(context.getBean(KafkaEventBroadcastingProcessEnginePlugin.class))
              .isSameAs(context.getBean("customKafkaEventBroadcastingProcessEnginePlugin"));
        });
  }

  @Test
  void shouldBindKafkaEventBroadcastingProperties() {
    contextRunner
        .withPropertyValues(
            "eximeebpms.bpm.event-broadcasting.enabled=true",
            "eximeebpms.bpm.event-broadcasting.fail-on-publish-error=true",
            "eximeebpms.bpm.event-broadcasting.async=false",
            "eximeebpms.bpm.event-broadcasting.kafka.bootstrap-servers=localhost:9092",
            "eximeebpms.bpm.event-broadcasting.kafka.topic=bpms-events",
            "eximeebpms.bpm.event-broadcasting.kafka.client-id=test-client",
            "eximeebpms.bpm.event-broadcasting.kafka.enable-idempotence=false",
            "eximeebpms.bpm.event-broadcasting.kafka.acks=1",
            "eximeebpms.bpm.event-broadcasting.kafka.key-header=customKey",
            "eximeebpms.bpm.event-broadcasting.kafka.wait-for-ack=true",
            "eximeebpms.bpm.event-broadcasting.kafka.retries=3",
            "eximeebpms.bpm.event-broadcasting.kafka.retry-backoff-ms=250",
            "eximeebpms.bpm.event-broadcasting.kafka.delivery-timeout-ms=30000",
            "eximeebpms.bpm.event-broadcasting.kafka.request-timeout-ms=10000"
        )
        .run(context -> {
          KafkaEventBroadcastingSpringProperties properties =
              context.getBean(KafkaEventBroadcastingSpringProperties.class);

          assertThat(properties.isEnabled()).isTrue();
          assertThat(properties.isFailOnPublishError()).isTrue();
          assertThat(properties.isAsync()).isFalse();

          KafkaEventBroadcastingSpringProperties.Kafka kafka = properties.getKafka();

          assertThat(kafka.getBootstrapServers()).isEqualTo("localhost:9092");
          assertThat(kafka.getTopic()).isEqualTo("bpms-events");
          assertThat(kafka.getClientId()).isEqualTo("test-client");
          assertThat(kafka.isEnableIdempotence()).isFalse();
          assertThat(kafka.getAcks()).isEqualTo("1");
          assertThat(kafka.getKeyHeader()).isEqualTo("customKey");
          assertThat(kafka.isWaitForAck()).isTrue();
          assertThat(kafka.getRetries()).isEqualTo(3);
          assertThat(kafka.getRetryBackoffMs()).isEqualTo(250L);
          assertThat(kafka.getDeliveryTimeoutMs()).isEqualTo(30_000);
          assertThat(kafka.getRequestTimeoutMs()).isEqualTo(10_000);
        });
  }

  @Test
  void shouldBackOffWhenCustomKafkaEventBroadcastingPluginBeanExists() {
    contextRunner
        .withBean(
            "kafkaEventBroadcastingProcessEnginePlugin",
            KafkaEventBroadcastingProcessEnginePlugin.class,
            KafkaEventBroadcastingProcessEnginePlugin::new
        )
        .run(context -> {
          assertThat(context).hasSingleBean(KafkaEventBroadcastingProcessEnginePlugin.class);
          assertThat(context).hasSingleBean(ProcessEnginePlugin.class);
        });
  }

  @Test
  void shouldNotCreateKafkaEventBroadcastingPluginWhenDisabledByDefault() {
    contextRunner.run(context -> {
      assertThat(context).doesNotHaveBean(KafkaEventBroadcastingProcessEnginePlugin.class);
      assertThat(context).doesNotHaveBean(ProcessEnginePlugin.class);
    });
  }

  @Test
  void shouldNotCreateKafkaEventBroadcastingPluginWhenExplicitlyDisabled() {
    contextRunner
        .withPropertyValues("eximeebpms.bpm.event-broadcasting.enabled=false")
        .run(context -> {
          assertThat(context).doesNotHaveBean(KafkaEventBroadcastingProcessEnginePlugin.class);
          assertThat(context).doesNotHaveBean(ProcessEnginePlugin.class);
        });
  }
}
