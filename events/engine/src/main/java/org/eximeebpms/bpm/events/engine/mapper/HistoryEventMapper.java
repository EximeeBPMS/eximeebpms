package org.eximeebpms.bpm.events.engine.mapper;

import org.eximeebpms.bpm.engine.impl.history.event.HistoryEvent;
import org.eximeebpms.bpm.events.EventEnvelope;

public interface HistoryEventMapper {

  boolean supports(HistoryEvent event);

  EventEnvelope map(HistoryEvent event);
}
