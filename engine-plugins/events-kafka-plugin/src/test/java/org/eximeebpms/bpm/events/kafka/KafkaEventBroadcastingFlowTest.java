package org.eximeebpms.bpm.events.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoryEvent;
import org.eximeebpms.bpm.events.EventBroadcaster;
import org.eximeebpms.bpm.events.EventSource;
import org.eximeebpms.bpm.events.EximeeBPMSEventTypes;
import org.eximeebpms.bpm.events.cfg.EventBroadcastingProperties;
import org.eximeebpms.bpm.events.engine.history.HistoryEventMapper;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class KafkaEventBroadcastingFlowTest {

  @Container
  static final KafkaContainer KAFKA = new KafkaContainer(
      DockerImageName.parse("apache/kafka-native:3.8.0")
  );

  @Test
  void shouldMapAndPublishHistoryEventToKafka() throws IOException {
    final String topic = "bpms-events-flow-test-" + UUID.randomUUID();

    try (KafkaEventPublisher publisher = new KafkaEventPublisher(
        KafkaEventPublisherProperties.builder()
            .bootstrapServers(KAFKA.getBootstrapServers())
            .topic(topic)
            .waitForAck(true)
            .build()
    );
        KafkaConsumer<String, byte[]> consumer = createConsumer()) {

      final EventBroadcaster<HistoryEvent> broadcaster =
          new EventBroadcaster<>(
              EventBroadcastingProperties.enabledWithDefaults(),
              new HistoryEventMapper(),
              publisher
          );

      final HistoricProcessInstanceEventEntity historyEvent = processInstanceStartedEvent();

      consumer.subscribe(List.of(topic));

      broadcaster.broadcast(historyEvent);

      final ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(10));

      assertThat(records).hasSize(1);

      final ConsumerRecord<String, byte[]> consumerRecord = records.iterator().next();

      assertThat(consumerRecord.key()).isEqualTo("root-process-instance-id");

      final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
      final JsonNode json = objectMapper.readTree(consumerRecord.value());

      assertThat(json.get("type").asText()).isEqualTo(EximeeBPMSEventTypes.PROCESS_INSTANCE_STARTED);
      assertThat(json.get("source").asText()).isEqualTo(EventSource.HISTORY.value());
      assertThat(json.get("rootProcessInstanceId").asText()).isEqualTo("root-process-instance-id");
      assertThat(json.get("processInstanceId").asText()).isEqualTo("process-instance-id");
      assertThat(json.get("headers").get("processKey").asText()).isEqualTo("root-process-instance-id");

      final JsonNode payload = json.get("payload");

      assertThat(payload.get("businessKey").asText()).isEqualTo("business-key-1");
      assertThat(payload.get("startUserId").asText()).isEqualTo("demo");
      assertThat(payload.get("processDefinitionKey").asText()).isEqualTo("process-definition-key");
    }
  }

  private HistoricProcessInstanceEventEntity processInstanceStartedEvent() {
    final HistoricProcessInstanceEventEntity historyEvent =
        new HistoricProcessInstanceEventEntity();

    historyEvent.setId("history-event-id");
    historyEvent.setBusinessKey("business-key-1");
    historyEvent.setStartUserId("demo");
    historyEvent.setStartActivityId("StartEvent_1");
    historyEvent.setTenantId("tenant-1");
    historyEvent.setStartTime(new Date());
    historyEvent.setRootProcessInstanceId("root-process-instance-id");
    historyEvent.setProcessInstanceId("process-instance-id");
    historyEvent.setExecutionId("execution-id");
    historyEvent.setProcessDefinitionId("process-definition-id");
    historyEvent.setProcessDefinitionKey("process-definition-key");
    historyEvent.setProcessDefinitionName("Process definition name");
    historyEvent.setProcessDefinitionVersion(3);
    historyEvent.setEventType("create");
    historyEvent.setSequenceCounter(42L);

    return historyEvent;
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
