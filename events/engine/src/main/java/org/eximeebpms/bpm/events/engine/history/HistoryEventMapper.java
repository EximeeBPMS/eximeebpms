package org.eximeebpms.bpm.events.engine.history;

import java.util.List;
import java.util.Optional;
import org.eximeebpms.bpm.engine.impl.history.event.HistoryEvent;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.engine.mapper.CompositeHistoryEventMapper;
import org.eximeebpms.bpm.events.engine.mapper.ProcessInstanceStartedEventMapper;
import org.eximeebpms.bpm.events.mapper.EventMapper;

public class HistoryEventMapper implements EventMapper<HistoryEvent> {

  private final CompositeHistoryEventMapper delegate = new CompositeHistoryEventMapper(List.of(
          new ProcessInstanceStartedEventMapper()
      ));

  @Override
  public Optional<EventEnvelope> map(HistoryEvent source) {
    return delegate.map(source);
  }
}
