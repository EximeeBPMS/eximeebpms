package org.eximeebpms.bpm.businessevent.kafka.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eximeebpms.bpm.businessevent.kafka.plugin.KafkaPublisherProperties.BOOTSTRAP_SERVERS;
import static org.eximeebpms.bpm.businessevent.kafka.plugin.KafkaPublisherProperties.CLIENT_ID;
import static org.eximeebpms.bpm.businessevent.kafka.plugin.KafkaPublisherProperties.KAFKA_PROPERTY_PREFIX;
import static org.eximeebpms.bpm.businessevent.kafka.plugin.KafkaPublisherProperties.SEND_TIMEOUT_MS;
import static org.eximeebpms.bpm.businessevent.kafka.plugin.KafkaPublisherProperties.TOPIC;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublishResult;
import org.eximeebpms.bpm.commons.eventbus.Event;
import org.eximeebpms.bpm.commons.eventbus.EventMetadata;
import org.junit.jupiter.api.Test;

class KafkaBusinessEventPublisherTest {

  @Test
  void shouldRequireBootstrapServers() {
    // given
    Map<String, String> properties = Map.of(TOPIC, "events");

    try (KafkaBusinessEventPublisher publisher = new KafkaBusinessEventPublisher(
        config -> new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer())
    )) {
      // when & then
      assertThatThrownBy(() -> publisher.init(properties))
          .isInstanceOf(KafkaPublisherException.class)
          .hasMessageContaining(BOOTSTRAP_SERVERS);
    }
  }

  @Test
  void shouldRequireTopic() {
    // given
    Map<String, String> properties = Map.of(BOOTSTRAP_SERVERS, "localhost:9092");

    try (KafkaBusinessEventPublisher publisher = new KafkaBusinessEventPublisher(
        config -> new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer())
    )) {
      // when & then
      assertThatThrownBy(() -> publisher.init(properties))
          .isInstanceOf(KafkaPublisherException.class)
          .hasMessageContaining(TOPIC);
    }
  }

  @Test
  void shouldForwardKafkaClientProperties() {
    // given
    AtomicReference<Properties> capturedConfig = new AtomicReference<>();

    try (KafkaBusinessEventPublisher publisher = new KafkaBusinessEventPublisher(
        config -> {
          capturedConfig.set(config);
          return new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());
        }
    )) {
      // when
      publisher.init(Map.of(
          BOOTSTRAP_SERVERS, "localhost:9092",
          TOPIC, "events",
          CLIENT_ID, "bpms-test",
          KAFKA_PROPERTY_PREFIX + "acks", "all",
          KAFKA_PROPERTY_PREFIX + "compression.type", "zstd"
      ));

      // then
      assertThat(capturedConfig.get())
          .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
          .containsEntry(ProducerConfig.CLIENT_ID_CONFIG, "bpms-test")
          .containsEntry("acks", "all")
          .containsEntry("compression.type", "zstd");
    }
  }

  @Test
  void shouldPublishEventToKafka() throws JsonProcessingException {
    // given
    MockProducer<String, byte[]> producer =
        new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());

    try (KafkaBusinessEventPublisher publisher = new KafkaBusinessEventPublisher(
        config -> producer
    )) {
      publisher.init(Map.of(
          BOOTSTRAP_SERVERS, "localhost:9092",
          TOPIC, "events",
          SEND_TIMEOUT_MS, "1000"
      ));

      Event event = testEvent(false);

      // when
      BusinessEventPublishResult result = publisher.publish(event);

      // then
      assertThat(result.successful()).isTrue();
      assertThat(producer.history()).hasSize(1);
      assertThat(producer.history().get(0).topic()).isEqualTo("events");
      assertThat(producer.history().get(0).key()).isEqualTo(event.metadata().processInstanceId());
      assertThat(producer.history().get(0).headers().lastHeader(KafkaBusinessEventMetadata.UUID))
          .isNotNull();
    }
  }

  @Test
  void shouldUseUuidAsKeyForNoProcessContextEvent() throws JsonProcessingException {
    // given
    MockProducer<String, byte[]> producer =
        new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());

    try (KafkaBusinessEventPublisher publisher = new KafkaBusinessEventPublisher(
        config -> producer
    )) {
      publisher.init(Map.of(
          BOOTSTRAP_SERVERS, "localhost:9092",
          TOPIC, "events"
      ));

      Event event = testEvent(true);

      // when
      BusinessEventPublishResult result = publisher.publish(event);

      // then
      assertThat(result.successful()).isTrue();
      assertThat(producer.history()).hasSize(1);
      assertThat(producer.history().get(0).key()).isEqualTo(event.metadata().uuid());
    }
  }

  @Test
  void shouldReturnFailureWhenPublishIsCalledBeforeInit() throws JsonProcessingException {
    // given
    try (KafkaBusinessEventPublisher publisher = new KafkaBusinessEventPublisher(
        config -> new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer())
    )) {
      Event event = testEvent(false);

      // when
      BusinessEventPublishResult result = publisher.publish(event);

      // then
      assertThat(result.successful()).isFalse();
      assertThat(result.message()).contains("used before init");
    }
  }

  private static Event testEvent(boolean noProcessContext) throws JsonProcessingException {
    return Event.builder()
        .metadata(EventMetadata.builder()
            .timestamp(Instant.now())
            .uuid(UUID.randomUUID().toString())
            .type("TestEvent")
            .version("1.0")
            .origin("test")
            .correlationId("correlation-id")
            .processInstanceId(noProcessContext ? null : "process-key")
            .processDefinitionKey(noProcessContext ? null : "process-name")
            .noProcessContext(noProcessContext)
            .build())
        .payload(new ObjectMapper().writeValueAsString(Map.of("foo", "bar")))
        .build();
  }
}
