package org.eximeebpms.bpm.businessevent.kafka.plugin;

public class KafkaPublisherException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public KafkaPublisherException(String message) {
    super(message);
  }

  public KafkaPublisherException(String message, Throwable cause) {
    super(message, cause);
  }
}
