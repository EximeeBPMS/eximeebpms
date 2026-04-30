package org.eximeebpms.bpm.events.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.exception.EventPublishException;
import org.eximeebpms.bpm.events.exception.EventSerializationException;
import org.eximeebpms.bpm.events.publisher.EventPublisher;

/**
 * Kafka-based implementation of {@link EventPublisher}.
 */
@Slf4j
public class KafkaEventPublisher implements EventPublisher, AutoCloseable {

  private final KafkaProducer<String, byte[]> producer;
  private final KafkaEventPublisherProperties properties;
  private final ObjectMapper objectMapper;

  public KafkaEventPublisher(KafkaEventPublisherProperties properties) {
    this(properties, defaultObjectMapper());
  }

  public KafkaEventPublisher(
      KafkaEventPublisherProperties properties,
      ObjectMapper objectMapper) {

    this.properties = properties;
    this.objectMapper = objectMapper;
    this.producer = new KafkaProducer<>(kafkaProps(properties));
  }

  @Override
  public void publish(EventEnvelope event) {
    final String key = resolveKey(event);
    final byte[] payload = serialize(event);

    final ProducerRecord<String, byte[]> producerRecord =
        new ProducerRecord<>(properties.getTopic(), key, payload);

    if (properties.isWaitForAck()) {
      sendSync(event, producerRecord);
      return;
    }

    producer.send(producerRecord, (metadata, exception) ->
        handleResult(event, metadata, exception)
    );
  }

  @Override
  public void close() {
    producer.close();
  }

  private String resolveKey(EventEnvelope event) {
    final Map<String, String> headers = event.getHeaders();

    if (headers != null) {
      final String keyFromHeader = headers.get(properties.getKeyHeader());
      if (keyFromHeader != null && !keyFromHeader.isBlank()) {
        return keyFromHeader;
      }
    }

    if (event.getRootProcessInstanceId() != null) {
      return event.getRootProcessInstanceId();
    }

    if (event.getProcessInstanceId() != null) {
      return event.getProcessInstanceId();
    }

    return event.getId();
  }

  private static Properties kafkaProps(KafkaEventPublisherProperties properties) {
    Properties props = new Properties();

    props.put("bootstrap.servers", properties.getBootstrapServers());
    props.put("client.id", properties.getClientId());
    props.put("acks", properties.getAcks());
    props.put("enable.idempotence", properties.isEnableIdempotence());
    props.put("retries", properties.getRetries());
    props.put("retry.backoff.ms", properties.getRetryBackoffMs());
    props.put("delivery.timeout.ms", properties.getDeliveryTimeoutMs());
    props.put("request.timeout.ms", properties.getRequestTimeoutMs());

    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", ByteArraySerializer.class.getName());

    return props;
  }

  private static ObjectMapper defaultObjectMapper() {
    return new ObjectMapper().findAndRegisterModules();
  }

  private void handleResult(final EventEnvelope event, final RecordMetadata metadata, final Exception exception) {

    if (exception != null) {
      log.error("Failed to publish event to Kafka [id={}]", event.getId(), exception);
      return;
    }
    log.debug("Event published to Kafka [topic={}, partition={}, offset={}]", metadata.topic(), metadata.partition(), metadata.offset());
  }

  private byte[] serialize(EventEnvelope event) {
    try {
      return objectMapper.writeValueAsBytes(event);
    } catch (Exception exc) {
      throw new EventSerializationException("Failed to serialize event [id=" + event.getId() + "]", exc);
    }
  }

  private void sendSync(EventEnvelope event, ProducerRecord<String, byte[]> record) {
    try {
      producer.send(record).get();
    } catch (InterruptedException exc) {
      Thread.currentThread().interrupt();
      throw new EventPublishException("Interrupted while publishing event [id=" + event.getId() + "]", exc);
    } catch (Exception exc) {
      throw new EventPublishException("Failed to publish event to Kafka [id=" + event.getId() + "]", exc);
    }
  }
}
