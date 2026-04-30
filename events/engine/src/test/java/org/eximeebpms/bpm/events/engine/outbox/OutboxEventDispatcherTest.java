package org.eximeebpms.bpm.events.engine.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.engine.EngineEvent;
import org.eximeebpms.bpm.events.engine.mapper.DefaultEngineEventEnvelopeMapper;
import org.eximeebpms.bpm.events.publisher.EventPublisher;
import org.junit.jupiter.api.Test;

class OutboxEventDispatcherTest {

  @Test
  void shouldPublishPendingEventsAndMarkThemAsPublished() {
    final InMemoryOutboxEventStore store = new InMemoryOutboxEventStore();
    final CountingPublisher publisher = new CountingPublisher();

    store.save(testEvent("event-1"));
    store.save(testEvent("event-2"));

    final OutboxEventDispatcher dispatcher = new OutboxEventDispatcher(
        store,
        new DefaultEngineEventEnvelopeMapper(),
        publisher
    );

    final int dispatched = dispatcher.dispatch(10);

    assertThat(dispatched).isEqualTo(2);
    assertThat(publisher.invocations).isEqualTo(2);
    assertThat(store.findPending(10)).isEmpty();
  }

  @Test
  void shouldMarkEventAsFailedWhenPublishingFails() {
    final InMemoryOutboxEventStore store = new InMemoryOutboxEventStore();

    store.save(testEvent("event-1"));

    final OutboxEventDispatcher dispatcher = new OutboxEventDispatcher(
        store,
        new DefaultEngineEventEnvelopeMapper(),
        new FailingPublisher()
    );

    final int dispatched = dispatcher.dispatch(10);

    assertThat(dispatched).isEqualTo(1);

    final OutboxEvent failedEvent = store.findPending(10).get(0);

    assertThat(failedEvent.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
    assertThat(failedEvent.getAttempts()).isEqualTo(1);
    assertThat(failedEvent.getLastError()).contains("publisher failed");
  }

  private EngineEvent testEvent(String id) {
    return EngineEvent.builder()
        .id(id)
        .type("event-type")
        .source("engine")
        .occurredAt(Instant.now())
        .rootProcessInstanceId("root-process-instance-id")
        .payload("payload")
        .build();
  }

  private static final class CountingPublisher implements EventPublisher {

    private int invocations;

    @Override
    public void publish(EventEnvelope event) {
      invocations++;
    }
  }

  private static final class FailingPublisher implements EventPublisher {

    @Override
    public void publish(EventEnvelope event) {
      throw new RuntimeException("publisher failed");
    }
  }
}
