package org.eximeebpms.bpm.events.engine.collector;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eximeebpms.bpm.events.engine.EngineEvent;

@RequiredArgsConstructor
public class EngineEventContext {

  private final BufferingEngineEventCollector collector;

  public void collect(EngineEvent event) {
    collector.collect(event);
  }

  public List<EngineEvent> drainEvents() {
    return collector.drain();
  }

  public List<EngineEvent> getEvents() {
    return collector.getEvents();
  }

  public boolean isEmpty() {
    return collector.isEmpty();
  }
}
