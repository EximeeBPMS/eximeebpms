package org.eximeebpms.bpm.events.engine.outbox;

public enum OutboxEventStatus {

  NEW,
  PUBLISHING,
  PUBLISHED,
  FAILED
}
