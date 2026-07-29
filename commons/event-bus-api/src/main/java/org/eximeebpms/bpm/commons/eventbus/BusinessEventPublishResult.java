package org.eximeebpms.bpm.commons.eventbus;

public record BusinessEventPublishResult(
    boolean successful,
    String message,
    Throwable cause
) {

  public static BusinessEventPublishResult success() {
    return new BusinessEventPublishResult(true, null, null);
  }

  public static BusinessEventPublishResult failure(String message) {
    return new BusinessEventPublishResult(false, message, null);
  }

  public static BusinessEventPublishResult failure(String message, Throwable cause) {
    return new BusinessEventPublishResult(false, message, cause);
  }
}
