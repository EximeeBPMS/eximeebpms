package org.eximeebpms.bpm.events.engine.mapper;

import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.engine.EngineEvent;

public interface EngineEventEnvelopeMapper {

  EventEnvelope map(EngineEvent event);
}
