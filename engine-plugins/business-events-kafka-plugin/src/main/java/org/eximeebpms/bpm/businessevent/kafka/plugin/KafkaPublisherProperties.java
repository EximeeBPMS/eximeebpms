package org.eximeebpms.bpm.businessevent.kafka.plugin;

public final class KafkaPublisherProperties {

  /**
   * Symbolic SPI name used in {@code BusinessEventsConfiguration#publisher}.
   */
  public static final String PUBLISHER_NAME = "kafka";

  /**
   * Common prefix for every Kafka business event publisher property.
   */
  public static final String PROPERTY_PREFIX = PUBLISHER_NAME + ".";

  /**
   * Comma-separated Kafka bootstrap servers list. Required.
   */
  public static final String BOOTSTRAP_SERVERS = PROPERTY_PREFIX + "bootstrap-servers";

  /**
   * Default Kafka topic to publish to. Required.
   */
  public static final String TOPIC = PROPERTY_PREFIX + "topic";

  /**
   * Optional Kafka client id.
   */
  public static final String CLIENT_ID = PROPERTY_PREFIX + "client-id";

  /**
   * Timeout in milliseconds for waiting for Kafka acknowledgement.
   */
  public static final String SEND_TIMEOUT_MS = PROPERTY_PREFIX + "send-timeout-ms";

  /**
   * Default value for {@link #SEND_TIMEOUT_MS}.
   */
  public static final long DEFAULT_SEND_TIMEOUT_MS = 30_000L;

  /**
   * Prefix for raw Kafka producer properties.
   *
   * <p>For example:</p>
   * <pre>
   * business-events.kafka.client.acks=all
   * </pre>
   *
   * <p>is forwarded as:</p>
   * <pre>
   * acks=all
   * </pre>
   */
  public static final String KAFKA_PROPERTY_PREFIX = PROPERTY_PREFIX + "client.";

  private KafkaPublisherProperties() {
    throw new UnsupportedOperationException("constants only");
  }
}
