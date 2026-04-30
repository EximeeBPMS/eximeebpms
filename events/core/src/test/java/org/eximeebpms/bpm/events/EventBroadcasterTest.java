package org.eximeebpms.bpm.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;
import org.eximeebpms.bpm.events.cfg.EventBroadcastingProperties;
import org.eximeebpms.bpm.events.exception.EventPublishException;
import org.eximeebpms.bpm.events.mapper.EventMapper;
import org.eximeebpms.bpm.events.publisher.EventPublisher;
import org.junit.jupiter.api.Test;

class EventBroadcasterTest {

  @Test
  void shouldNotInvokeMapperOrPublisherWhenDisabled() {
    final CountingMapper mapper = new CountingMapper();
    final CountingPublisher publisher = new CountingPublisher();

    final EventBroadcaster<String> broadcaster = new EventBroadcaster<>(
        EventBroadcastingProperties.disabled(),
        mapper,
        publisher
    );

    broadcaster.broadcast("source-event");

    assertThat(mapper.invocations).isZero();
    assertThat(publisher.invocations).isZero();
  }

  @Test
  void shouldPublishMappedEventWhenEnabled() {
    final CountingMapper mapper = new CountingMapper();
    final CountingPublisher publisher = new CountingPublisher();

    final EventBroadcaster<String> broadcaster = new EventBroadcaster<>(
        EventBroadcastingProperties.enabledWithDefaults(),
        mapper,
        publisher
    );

    broadcaster.broadcast("source-event");

    assertThat(mapper.invocations).isEqualTo(1);
    assertThat(publisher.invocations).isEqualTo(1);
    assertThat(publisher.lastEvent).isNotNull();
    assertThat(publisher.lastEvent.getType()).isEqualTo("test-event");
  }

  @Test
  void shouldIgnorePublisherExceptionWhenFailOnPublishErrorIsDisabled() {
    final EventBroadcaster<String> broadcaster = new EventBroadcaster<>(
        EventBroadcastingProperties.builder()
            .enabled(true)
            .failOnPublishError(false)
            .build(),
        new CountingMapper(),
        new FailingPublisher()
    );

    assertThatCode(() -> broadcaster.broadcast("source-event"))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldThrowEventPublishExceptionWhenFailOnPublishErrorIsEnabled() {
    final EventBroadcaster<String> broadcaster = new EventBroadcaster<>(
        EventBroadcastingProperties.builder()
            .enabled(true)
            .failOnPublishError(true)
            .build(),
        new CountingMapper(),
        new FailingPublisher()
    );

    assertThatThrownBy(() -> broadcaster.broadcast("source-event"))
        .isInstanceOf(EventPublishException.class)
        .hasMessageContaining("Failed to broadcast event");
  }

  @Test
  void shouldNotPublishWhenMapperReturnsEmpty() {
    final EventBroadcaster<String> broadcaster = new EventBroadcaster<>(
        EventBroadcastingProperties.enabledWithDefaults(),
        source -> Optional.empty(),
        new CountingPublisher()
    );

    assertThatCode(() -> broadcaster.broadcast("source-event"))
        .doesNotThrowAnyException();
  }

  private static final class CountingMapper implements EventMapper<String> {

    private int invocations;

    @Override
    public Optional<EventEnvelope> map(String source) {
      invocations++;
      return Optional.of(testEnvelope());
    }
  }

  private static final class CountingPublisher implements EventPublisher {

    private int invocations;
    private EventEnvelope lastEvent;

    @Override
    public void publish(EventEnvelope event) {
      invocations++;
      lastEvent = event;
    }
  }

  private static final class FailingPublisher implements EventPublisher {

    @Override
    public void publish(EventEnvelope event) {
      throw new RuntimeException("publisher failed");
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
