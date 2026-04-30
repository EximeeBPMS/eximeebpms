package org.eximeebpms.bpm.events;

import lombok.RequiredArgsConstructor;

/**
 * Source component that produced an event.
 */
@RequiredArgsConstructor
public enum EventSource {

  HISTORY("history"),
  TASK("task"),
  EXECUTION("execution"),
  JOB("job"),
  INCIDENT("incident"),
  DEPLOYMENT("deployment");

  private final String value;

  public String value() {
    return value;
  }
}
