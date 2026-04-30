package org.eximeebpms.bpm.events.publisher;

import java.util.Collection;
import org.eximeebpms.bpm.events.EventEnvelope;

/**
 * Publishes engine events to an external or internal event sink.
 *
 * <p>Implementations may publish events to Kafka, RabbitMQ, HTTP webhooks,
 * logs, in-memory consumers or any other transport.</p>
 */
public interface EventPublisher {

  /**
   * Publishes a single event.
   *
   * @param event event envelope to publish
   */
  void publish(EventEnvelope event);

  /**
   * Publishes multiple events.
   *
   * <p>The default implementation delegates to {@link #publish(EventEnvelope)}
   * for every event.</p>
   *
   * @param events event envelopes to publish
   */
  default void publishAll(Collection<EventEnvelope> events) {
    events.forEach(this::publish);
  }
}
