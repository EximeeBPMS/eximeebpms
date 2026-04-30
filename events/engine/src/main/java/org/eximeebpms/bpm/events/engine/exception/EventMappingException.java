package org.eximeebpms.bpm.events.engine.exception;

/**
 * Runtime exception thrown when source event mapping fails.
 */
public class EventMappingException extends RuntimeException {

  public EventMappingException(String message, Throwable cause) {
    super(message, cause);
  }

  public EventMappingException(String message) {
    super(message);
  }
}
