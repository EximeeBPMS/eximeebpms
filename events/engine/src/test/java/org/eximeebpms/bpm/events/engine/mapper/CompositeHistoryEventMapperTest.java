package org.eximeebpms.bpm.events.engine.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.eximeebpms.bpm.engine.impl.history.event.HistoryEvent;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.EventSource;
import org.junit.jupiter.api.Test;

class CompositeHistoryEventMapperTest {

  @Test
  void shouldReturnEmptyWhenNoMapperSupportsEvent() {
    final CompositeHistoryEventMapper mapper =
        new CompositeHistoryEventMapper(List.of(new UnsupportedMapper()));

    assertThat(mapper.map(new HistoryEvent())).isEmpty();
  }

  @Test
  void shouldUseFirstSupportingMapper() {
    final CompositeHistoryEventMapper mapper =
        new CompositeHistoryEventMapper(List.of(
            new UnsupportedMapper(),
            new SupportedMapper("first"),
            new SupportedMapper("second")
        ));

    assertThat(mapper.map(new HistoryEvent()))
        .isPresent()
        .get()
        .extracting(EventEnvelope::getId)
        .isEqualTo("first");
  }

  private static final class UnsupportedMapper implements HistoryEventMapper {

    @Override
    public boolean supports(HistoryEvent event) {
      return false;
    }

    @Override
    public EventEnvelope map(HistoryEvent event) {
      throw new AssertionError("Should not be called");
    }
  }

  private static final class SupportedMapper implements HistoryEventMapper {

    private final String id;

    private SupportedMapper(String id) {
      this.id = id;
    }

    @Override
    public boolean supports(HistoryEvent event) {
      return true;
    }

    @Override
    public EventEnvelope map(HistoryEvent event) {
      return EventEnvelope.builder()
          .id(id)
          .type("test-event")
          .source(EventSource.HISTORY.value())
          .occurredAt(Instant.now())
          .build();
    }
  }
}
