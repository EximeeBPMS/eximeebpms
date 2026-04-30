package org.eximeebpms.bpm.events.exception;

/**
 * Runtime exception thrown when event publishing fails.
 */
public class EventPublishException extends RuntimeException {

  public EventPublishException(String message, Throwable cause) {
    super(message, cause);
  }

  public EventPublishException(String message) {
    super(message);
  }
}
