package org.eximeebpms.bpm.events.publisher;

import java.util.Collection;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.eximeebpms.bpm.events.EventEnvelope;

/**
 * Event publisher decorator that delegates publishing to an executor.
 *
 * <p>This class keeps async behavior transport-neutral. Kafka, RabbitMQ or HTTP
 * publishers do not need to know whether broadcasting was configured as async.</p>
 */
@RequiredArgsConstructor
public class AsyncEventPublisher implements EventPublisher {

  private final EventPublisher delegate;
  private final Executor executor;

  @Override
  public void publish(EventEnvelope event) {
    executor.execute(() -> delegate.publish(event));
  }

  @Override
  public void publishAll(Collection<EventEnvelope> events) {
    executor.execute(() -> delegate.publishAll(events));
  }
}
