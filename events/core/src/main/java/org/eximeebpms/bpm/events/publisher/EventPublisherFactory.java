package org.eximeebpms.bpm.events.publisher;

import java.util.concurrent.Executor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eximeebpms.bpm.events.cfg.EventBroadcastingProperties;

/**
 * Creates event publisher variants according to runtime configuration.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventPublisherFactory {

  public static EventPublisher create(
      EventPublisher publisher,
      EventBroadcastingProperties properties,
      Executor executor) {

    if (!properties.isEnabled()) {
      return NoopEventPublisher.INSTANCE;
    }

    if (properties.isAsync()) {
      return new AsyncEventPublisher(publisher, executor);
    }

    return publisher;
  }
}
