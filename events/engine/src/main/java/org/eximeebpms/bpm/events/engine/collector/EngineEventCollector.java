package org.eximeebpms.bpm.events.engine.collector;

import org.eximeebpms.bpm.events.engine.EngineEvent;

public interface EngineEventCollector {

  void collect(EngineEvent event);
}
