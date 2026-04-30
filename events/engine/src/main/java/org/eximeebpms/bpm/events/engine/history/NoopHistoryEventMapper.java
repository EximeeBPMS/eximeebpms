package org.eximeebpms.bpm.events.engine.history;

import java.util.Optional;
import org.eximeebpms.bpm.engine.impl.history.event.HistoryEvent;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.mapper.EventMapper;

/**
 * History event mapper that intentionally skips all history events.
 *
 * <p>Useful as a safe default until concrete Camunda 7 history event mappings
 * are registered.</p>
 */
public class NoopHistoryEventMapper implements EventMapper<HistoryEvent> {

  @Override
  public Optional<EventEnvelope> map(HistoryEvent source) {
    return Optional.empty();
  }
}
