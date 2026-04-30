package org.eximeebpms.bpm.events.engine.mapper;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.eximeebpms.bpm.engine.impl.history.event.HistoryEvent;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.mapper.EventMapper;

@RequiredArgsConstructor
public class CompositeHistoryEventMapper implements EventMapper<HistoryEvent> {

  private final List<HistoryEventMapper> mappers;

  @Override
  public Optional<EventEnvelope> map(HistoryEvent event) {
    return mappers.stream()
        .filter(mapper -> mapper.supports(event))
        .findFirst()
        .map(mapper -> mapper.map(event));
  }
}
