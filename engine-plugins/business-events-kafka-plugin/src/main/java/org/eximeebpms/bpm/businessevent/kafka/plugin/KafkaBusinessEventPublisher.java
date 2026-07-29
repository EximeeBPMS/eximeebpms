package org.eximeebpms.bpm.businessevent.kafka.plugin;

import static org.eximeebpms.bpm.businessevent.kafka.plugin.KafkaPublisherProperties.BOOTSTRAP_SERVERS;
import static org.eximeebpms.bpm.businessevent.kafka.plugin.KafkaPublisherProperties.CLIENT_ID;
import static org.eximeebpms.bpm.businessevent.kafka.plugin.KafkaPublisherProperties.DEFAULT_SEND_TIMEOUT_MS;
import static org.eximeebpms.bpm.businessevent.kafka.plugin.KafkaPublisherProperties.KAFKA_PROPERTY_PREFIX;
import static org.eximeebpms.bpm.businessevent.kafka.plugin.KafkaPublisherProperties.PUBLISHER_NAME;
import static org.eximeebpms.bpm.businessevent.kafka.plugin.KafkaPublisherProperties.SEND_TIMEOUT_MS;
import static org.eximeebpms.bpm.businessevent.kafka.plugin.KafkaPublisherProperties.TOPIC;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublishResult;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublisher;
import org.eximeebpms.bpm.commons.eventbus.Event;
import org.eximeebpms.bpm.commons.eventbus.EventMetadata;

/**
 * {@link BusinessEventPublisher} implementation that publishes business events to Apache Kafka.
 *
 * <p>This class is intentionally transport-only. It does not collect events and
 * does not own retry/outbox logic. The engine business-events subsystem is responsible for deciding when this publisher is called.</p>
 *
 * <h3>Record structure</h3>
 * <ul>
 *   <li>Kafka topic: configured by {@code kafka.topic}</li>
 *   <li>Kafka key: {@code processKey}, or event UUID for events without process context</li>
 *   <li>Kafka value: serialized {@link Event} envelope</li>
 *   <li>Kafka headers: selected {@link EventMetadata} fields</li>
 * </ul>
 */
@Slf4j
public class KafkaBusinessEventPublisher implements BusinessEventPublisher {

  private final KafkaProducerFactory producerFactory;
  private final AtomicBoolean initialized = new AtomicBoolean(false);
  private final AtomicReference<Producer<String, byte[]>> producerRef = new AtomicReference<>();
  private volatile String topic;
  private volatile long sendTimeoutMs;

  /**
   * Constructor used by {@link java.util.ServiceLoader}.
   */
  public KafkaBusinessEventPublisher() {
    this(KafkaProducerFactory.defaultFactory());
  }

  /**
   * Constructor for tests and advanced wiring.
   */
  public KafkaBusinessEventPublisher(KafkaProducerFactory producerFactory) {
    this.producerFactory = producerFactory;
  }

  private static EventMetadata requireMetadata(Event event) {
    if (event == null) {
      throw new KafkaPublisherException("Business event must not be null.");
    }

    if (event.metadata() == null) {
      throw new KafkaPublisherException("Business event headers must not be null.");
    }

    return event.metadata();
  }

  private static void putHeader(Headers headers, String name, String value) {
    if (value == null) {
      return;
    }

    headers.add(name, value.getBytes(StandardCharsets.UTF_8));
  }

  private static Properties buildKafkaConfig(Map<String, String> props) {
    Properties kafkaConfig = new Properties();

    // Production-oriented defaults. They may be overridden through kafka.client.* properties.
    kafkaConfig.put(ProducerConfig.ACKS_CONFIG, "all");
    kafkaConfig.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    kafkaConfig.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
    kafkaConfig.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
    kafkaConfig.put(ProducerConfig.LINGER_MS_CONFIG, 5);
    kafkaConfig.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");

    kafkaConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, requireProperty(props, BOOTSTRAP_SERVERS));

    Optional.ofNullable(props.get(CLIENT_ID))
        .filter(value -> !value.isBlank())
        .ifPresent(clientId -> kafkaConfig.put(ProducerConfig.CLIENT_ID_CONFIG, clientId));

    props.forEach((key, value) -> {
      if (key != null && value != null && key.startsWith(KAFKA_PROPERTY_PREFIX)) {
        String kafkaKey = key.substring(KAFKA_PROPERTY_PREFIX.length());
        if (!kafkaKey.isBlank()) {
          kafkaConfig.put(kafkaKey, value);
        }
      }
    });

    return kafkaConfig;
  }

  private static String requireProperty(Map<String, String> props, String key) {
    String value = props.get(key);

    if (value == null || value.isBlank()) {
      throw new KafkaPublisherException("Required Kafka publisher property '" + key + "' is missing or blank.");
    }

    return value;
  }

  private static long parseLong(String raw, long fallback) {
    if (raw == null || raw.isBlank()) {
      return fallback;
    }

    try {
      return Long.parseLong(raw.trim());
    } catch (NumberFormatException e) {
      throw new KafkaPublisherException("Kafka publisher property must be a number, got '" + raw + "'", e);
    }
  }

  @Override
  public String getName() {
    return PUBLISHER_NAME;
  }

  @Override
  public void init(Map<String, String> properties) {
    if (!initialized.compareAndSet(false, true)) {
      return;
    }

    try {
      final Map<String, String> props = properties == null
          ? Map.of()
          : Map.copyOf(properties);

      this.topic = requireProperty(props, TOPIC);
      this.sendTimeoutMs = parseLong(props.get(SEND_TIMEOUT_MS), DEFAULT_SEND_TIMEOUT_MS);

      Properties kafkaConfig = buildKafkaConfig(props);

      log.info(
          "Initializing Kafka business event publisher [topic={}, bootstrapServers={}, clientId={}]",
          topic,
          kafkaConfig.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG),
          kafkaConfig.get(ProducerConfig.CLIENT_ID_CONFIG)
      );

      Producer<String, byte[]> createdProducer = producerFactory.create(kafkaConfig);
      producerRef.set(createdProducer);
    } catch (RuntimeException e) {
      initialized.set(false);
      throw e;
    }
  }

  @Override
  public BusinessEventPublishResult publish(Event event) {
    Producer<String, byte[]> producer = producerRef.get();

    if (producer == null) {
      return BusinessEventPublishResult.failure("KafkaBusinessEventPublisher used before init(...) was called.");
    }

    try {
      final ProducerRecord<String, byte[]> producerRecord = toRecord(event);
      producer.send(producerRecord).get(sendTimeoutMs, TimeUnit.MILLISECONDS);
      return BusinessEventPublishResult.success();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return BusinessEventPublishResult.failure("Interrupted while waiting for Kafka acknowledgement", e);
    } catch (ExecutionException e) {
      return BusinessEventPublishResult.failure("Kafka broker rejected business event publish", e.getCause());
    } catch (TimeoutException e) {
      return BusinessEventPublishResult.failure("Timed out waiting for Kafka acknowledgement after " + sendTimeoutMs + " ms", e);
    } catch (RuntimeException e) {
      return BusinessEventPublishResult.failure("Unexpected error while publishing event to Kafka", e);
    }
  }

  @Override
  public void close() {
    final Producer<String, byte[]> producer = producerRef.getAndSet(null);

    if (producer == null) {
      initialized.set(false);
      return;
    }

    try {
      producer.flush();
    } catch (RuntimeException e) {
      log.warn("Failed to flush Kafka producer during close", e);
    }

    try {
      producer.close();
    } catch (RuntimeException e) {
      log.warn("Failed to close Kafka producer cleanly", e);
    } finally {
      initialized.set(false);
    }
  }

  private ProducerRecord<String, byte[]> toRecord(Event event) {
    EventMetadata eventMetadata = requireMetadata(event);

    String key = eventMetadata.noProcessContext()
        ? eventMetadata.uuid()
        : Optional.ofNullable(eventMetadata.processInstanceId())
            .filter(value -> !value.isBlank())
            .orElseGet(eventMetadata::uuid);

    ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(topic, key, event.payload().getBytes(StandardCharsets.UTF_8));
    Headers kafkaHeaders = producerRecord.headers();

    putHeader(kafkaHeaders, KafkaBusinessEventMetadata.UUID, eventMetadata.uuid());
    putHeader(kafkaHeaders, KafkaBusinessEventMetadata.TYPE, eventMetadata.type());
    putHeader(kafkaHeaders, KafkaBusinessEventMetadata.VERSION, eventMetadata.version());
    putHeader(kafkaHeaders, KafkaBusinessEventMetadata.ORIGIN, eventMetadata.origin());
    putHeader(kafkaHeaders, KafkaBusinessEventMetadata.CORRELATION_ID, eventMetadata.correlationId());
    putHeader(kafkaHeaders, KafkaBusinessEventMetadata.PROCESS_INSTANCE_ID, eventMetadata.processInstanceId());
    putHeader(kafkaHeaders, KafkaBusinessEventMetadata.PROCESS_DEFINITION_KEY, eventMetadata.processDefinitionKey());
    putHeader(kafkaHeaders, KafkaBusinessEventMetadata.NO_PROCESS_CONTEXT, Boolean.toString(eventMetadata.noProcessContext()));

    if (eventMetadata.timestamp() != null) {
      putHeader(kafkaHeaders, KafkaBusinessEventMetadata.TIMESTAMP, eventMetadata.timestamp().toString());
    }

    return producerRecord;
  }
}
