package org.eximeebpms.bpm.events.cfg;

import lombok.Builder;
import lombok.Value;
import org.eximeebpms.bpm.events.EventFamily;

/**
 * Enables or disables event families.
 *
 * <p>Defaults are intentionally true. The whole feature is controlled by
 * {@link EventBroadcastingProperties#isEnabled()}.</p>
 */
@Value
@Builder(toBuilder = true)
public class EventTypeProperties {

  @Builder.Default
  boolean history = true;

  @Builder.Default
  boolean task = true;

  @Builder.Default
  boolean execution = true;

  @Builder.Default
  boolean job = true;

  @Builder.Default
  boolean incident = true;

  @Builder.Default
  boolean deployment = true;

  public static EventTypeProperties defaults() {
    return EventTypeProperties.builder().build();
  }

  public boolean isEnabled(EventFamily family) {
    return switch (family) {
      case HISTORY -> history;
      case TASK -> task;
      case EXECUTION -> execution;
      case JOB -> job;
      case INCIDENT -> incident;
      case DEPLOYMENT -> deployment;
    };
  }
}
