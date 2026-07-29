package org.eximeebpms.bpm.businessevent.kafka.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eximeebpms.bpm.commons.eventbus.Event;
import org.eximeebpms.bpm.commons.eventbus.EventMetadata;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class KafkaPublisherIntegrationTest {

  private static final String TOPIC = "eximeebpms.business-events";
  private static final DockerImageName IMAGE = DockerImageName.parse("apache/kafka-native:3.8.0");

  @Container
  static final KafkaContainer KAFKA = new KafkaContainer(IMAGE);

  private static KafkaBusinessEventPublisher publisher;

  @BeforeAll
  static void startPublisher() {
    publisher = new KafkaBusinessEventPublisher();

    Map<String, String> props = new HashMap<>();
    props.put(KafkaPublisherProperties.BOOTSTRAP_SERVERS, KAFKA.getBootstrapServers());
    props.put(KafkaPublisherProperties.TOPIC, TOPIC);
    props.put(KafkaPublisherProperties.CLIENT_ID, "kafka-it");
    props.put(KafkaPublisherProperties.SEND_TIMEOUT_MS, "10000");

    publisher.init(props);
  }

  @AfterAll
  static void stopPublisher() {
    if (publisher != null) {
      publisher.close();
    }
  }

  @Test
  void publishesEventThroughRealBroker() throws Exception {
    String processInstanceId = "proc-" + UUID.randomUUID();
    String type = "OrderCreated";
    Event event = buildEvent(processInstanceId, type);

    publisher.publish(event);

    ConsumerRecord<String, byte[]> consumerRecord = consumeOne();

    assertThat(consumerRecord.topic()).isEqualTo(TOPIC);
    assertThat(consumerRecord.key())
        .as("partition key must equal processInstanceId for per-process ordering")
        .isEqualTo(processInstanceId);

    Map<String, String> headers = headersAsMap(consumerRecord);

    assertThat(headers)
        .containsEntry(KafkaBusinessEventMetadata.UUID, event.metadata().uuid())
        .containsEntry(KafkaBusinessEventMetadata.TYPE, type)
        .containsEntry(KafkaBusinessEventMetadata.VERSION, "1.0")
        .containsEntry(KafkaBusinessEventMetadata.ORIGIN, "kafka-it")
        .containsEntry(KafkaBusinessEventMetadata.PROCESS_INSTANCE_ID, processInstanceId)
        .containsEntry(KafkaBusinessEventMetadata.PROCESS_DEFINITION_KEY, "OrderProcess")
        .containsEntry(KafkaBusinessEventMetadata.NO_PROCESS_CONTEXT, "false");

    assertThat(headers.get(KafkaBusinessEventMetadata.TIMESTAMP))
        .as("timestamp header must be ISO-8601 and parseable by java.time.Instant")
        .isNotBlank();

    Instant.parse(headers.get(KafkaBusinessEventMetadata.TIMESTAMP));

    JsonNode root = new ObjectMapper().readTree(consumerRecord.value());

    assertThat(root.path("orderId").asText()).isEqualTo("ORD-42");
  }

  private static Event buildEvent(String processInstanceId, String type) throws JsonProcessingException {
    EventMetadata metadata = EventMetadata.builder()
        .uuid(UUID.randomUUID().toString())
        .type(type)
        .version("1.0")
        .origin("kafka-it")
        .timestamp(Instant.now())
        .processInstanceId(processInstanceId)
        .processDefinitionKey("OrderProcess")
        .noProcessContext(false)
        .build();

    return Event.builder()
        .metadata(metadata)
        .payload(new ObjectMapper().writeValueAsString(Map.of("orderId", "ORD-42")))
        .build();
  }

  private static Map<String, String> headersAsMap(ConsumerRecord<String, byte[]> record) {
    Map<String, String> result = new HashMap<>();

    for (Header header : record.headers()) {
      result.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
    }

    return result;
  }

  private static ConsumerRecord<String, byte[]> consumeOne() {
    Properties config = new Properties();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-it-" + UUID.randomUUID());
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

    try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(config)) {
      consumer.subscribe(List.of(TOPIC));

      long deadline = System.nanoTime() + Duration.ofSeconds(15).toNanos();

      while (System.nanoTime() < deadline) {
        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));

        if (!records.isEmpty()) {
          return records.iterator().next();
        }
      }
    }

    throw new AssertionError("No record received on topic " + TOPIC + " within 15s");
  }
}
