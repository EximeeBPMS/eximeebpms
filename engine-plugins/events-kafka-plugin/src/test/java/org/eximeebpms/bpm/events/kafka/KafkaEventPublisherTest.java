package org.eximeebpms.bpm.events.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eximeebpms.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.EventSource;
import org.eximeebpms.bpm.events.EximeeBPMSEventTypes;
import org.eximeebpms.bpm.events.kafka.plugin.KafkaEventBroadcastingProcessEnginePlugin;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class KafkaEventPublisherTest {

  @Container
  static final KafkaContainer KAFKA = new KafkaContainer(
      DockerImageName.parse("apache/kafka-native:3.8.0")
  );

  @Test
  void shouldPublishEventEnvelopeToKafka() throws IOException {
    final String topic = "bpms-events-test-" + UUID.randomUUID();

    try (KafkaEventPublisher publisher = new KafkaEventPublisher(KafkaEventPublisherProperties.builder()
        .bootstrapServers(KAFKA.getBootstrapServers())
        .topic(topic)
        .build());
        KafkaConsumer<String, byte[]> consumer = createConsumer()) {

      EventEnvelope event = EventEnvelope.builder()
          .id(UUID.randomUUID().toString())
          .type(EximeeBPMSEventTypes.PROCESS_INSTANCE_STARTED)
          .source(EventSource.HISTORY.value())
          .occurredAt(Instant.now())
          .rootProcessInstanceId("root-process-instance-id")
          .processInstanceId("process-instance-id")
          .header("processKey", "root-process-instance-id")
          .build();
      consumer.subscribe(List.of(topic));

      publisher.publish(event);

      final ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofSeconds(10));

      assertThat(consumerRecords).hasSize(1);

      final ConsumerRecord<String, byte[]> consumerRecord = consumerRecords.iterator().next();

      assertThat(consumerRecord.key()).isEqualTo("root-process-instance-id");
      assertThat(consumerRecord.value()).isNotEmpty();

      final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
      final JsonNode json = objectMapper.readTree(consumerRecord.value());

      assertThat(json.get("id").asText()).isEqualTo(event.getId());
      assertThat(json.get("type").asText()).isEqualTo(EximeeBPMSEventTypes.PROCESS_INSTANCE_STARTED);
      assertThat(json.get("source").asText()).isEqualTo(EventSource.HISTORY.value());
      assertThat(json.get("headers").get("processKey").asText())
          .isEqualTo("root-process-instance-id");
    }
  }

  @Test
  void shouldUseConfiguredHeaderAsKafkaKey() {
    final String topic = "bpms-events-test-" + UUID.randomUUID();

    try (KafkaEventPublisher publisher = new KafkaEventPublisher(
        KafkaEventPublisherProperties.builder()
            .bootstrapServers(KAFKA.getBootstrapServers())
            .topic(topic)
            .keyHeader("customKey")
            .build()
    ); KafkaConsumer<String, byte[]> consumer = createConsumer()) {
      EventEnvelope event = EventEnvelope.builder()
          .id(UUID.randomUUID().toString())
          .type("test-event")
          .source("test")
          .occurredAt(Instant.now())
          .rootProcessInstanceId("root-process-instance-id")
          .header("customKey", "custom-kafka-key")
          .build();
      consumer.subscribe(List.of(topic));

      publisher.publish(event);

      final ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofSeconds(10));

      assertThat(consumerRecords).hasSize(1);
      assertThat(consumerRecords.iterator().next().key()).isEqualTo("custom-kafka-key");
    }
  }

  @Test
  void shouldInitializeWhenEnabledWithKafkaAvailable() {
    final KafkaEventBroadcastingProcessEnginePlugin plugin = new KafkaEventBroadcastingProcessEnginePlugin();

    plugin.setEnabled(true);
    plugin.setBootstrapServers(KAFKA.getBootstrapServers());
    plugin.setTopic("bpms-events-test-" + UUID.randomUUID());
    plugin.setRetries(3);
    plugin.setRetryBackoffMs(100L);
    plugin.setDeliveryTimeoutMs(30_000);
    plugin.setRequestTimeoutMs(10_000);

    assertThatCode(() -> plugin.preInit(new StandaloneProcessEngineConfiguration()))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldPublishEventEnvelopeToKafkaWhenWaitingForAck() {
    final String topic = "bpms-events-test-" + UUID.randomUUID();

    try (KafkaEventPublisher publisher = new KafkaEventPublisher(
        KafkaEventPublisherProperties.builder()
            .bootstrapServers(KAFKA.getBootstrapServers())
            .topic(topic)
            .waitForAck(true)
            .build()
    ); KafkaConsumer<String, byte[]> consumer = createConsumer()) {
      final EventEnvelope event = EventEnvelope.builder()
          .id(UUID.randomUUID().toString())
          .type(EximeeBPMSEventTypes.PROCESS_INSTANCE_STARTED)
          .source(EventSource.HISTORY.value())
          .occurredAt(Instant.now())
          .rootProcessInstanceId("root-process-instance-id")
          .processInstanceId("process-instance-id")
          .header("processKey", "root-process-instance-id")
          .build();
      consumer.subscribe(List.of(topic));

      publisher.publish(event);

      final ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofSeconds(10));

      assertThat(consumerRecords).hasSize(1);
      assertThat(consumerRecords.iterator().next().key())
          .isEqualTo("root-process-instance-id");
    }
  }

  private KafkaConsumer<String, byte[]> createConsumer() {
    final Properties properties = new Properties();

    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID());
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

    return new KafkaConsumer<>(properties);
  }
}
