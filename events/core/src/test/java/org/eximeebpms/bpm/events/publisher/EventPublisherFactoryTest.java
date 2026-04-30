package org.eximeebpms.bpm.events.publisher;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.concurrent.Executor;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.cfg.EventBroadcastingProperties;
import org.junit.jupiter.api.Test;

class EventPublisherFactoryTest {

  @Test
  void shouldReturnNoopPublisherWhenEventBroadcastingIsDisabled() {
    final EventPublisher publisher = EventPublisherFactory.create(
        new CountingPublisher(),
        EventBroadcastingProperties.disabled(),
        Runnable::run
    );

    assertThat(publisher).isSameAs(NoopEventPublisher.INSTANCE);
  }

  @Test
  void shouldReturnAsyncPublisherWhenAsyncIsEnabled() {
    final EventPublisher publisher = EventPublisherFactory.create(
        new CountingPublisher(),
        EventBroadcastingProperties.builder()
            .enabled(true)
            .async(true)
            .build(),
        Runnable::run
    );

    assertThat(publisher).isInstanceOf(AsyncEventPublisher.class);
  }

  @Test
  void shouldReturnOriginalPublisherWhenAsyncIsDisabled() {
    final CountingPublisher originalPublisher = new CountingPublisher();

    final EventPublisher publisher = EventPublisherFactory.create(
        originalPublisher,
        EventBroadcastingProperties.builder()
            .enabled(true)
            .async(false)
            .build(),
        Runnable::run
    );

    assertThat(publisher).isSameAs(originalPublisher);
  }

  @Test
  void shouldDelegateThroughAsyncPublisher() {
    final CountingPublisher delegate = new CountingPublisher();
    final Executor directExecutor = Runnable::run;

    final EventPublisher publisher = EventPublisherFactory.create(
        delegate,
        EventBroadcastingProperties.builder()
            .enabled(true)
            .async(true)
            .build(),
        directExecutor
    );

    publisher.publish(testEnvelope());

    assertThat(delegate.invocations).isEqualTo(1);
  }

  private static final class CountingPublisher implements EventPublisher {

    private int invocations;

    @Override
    public void publish(EventEnvelope event) {
      invocations++;
    }
  }

  private static EventEnvelope testEnvelope() {
    return EventEnvelope.builder()
        .id("event-id")
        .type("test-event")
        .source("test")
        .occurredAt(Instant.now())
        .build();
  }
}
