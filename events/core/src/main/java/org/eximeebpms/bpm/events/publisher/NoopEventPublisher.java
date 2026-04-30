package org.eximeebpms.bpm.events.publisher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eximeebpms.bpm.events.EventEnvelope;

/**
 * Event publisher that intentionally ignores all events.
 *
 * <p>This implementation is useful as a safe default when event broadcasting
 * is disabled or no transport-specific publisher is configured.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NoopEventPublisher implements EventPublisher {

  public static final NoopEventPublisher INSTANCE = new NoopEventPublisher();

  @Override
  public void publish(EventEnvelope event) {
    // intentionally ignored
  }
}
