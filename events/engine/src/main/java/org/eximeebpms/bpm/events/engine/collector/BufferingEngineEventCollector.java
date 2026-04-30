package org.eximeebpms.bpm.events.engine.collector;

import java.util.ArrayList;
import java.util.List;
import org.eximeebpms.bpm.events.engine.EngineEvent;

public class BufferingEngineEventCollector implements EngineEventCollector {

  private final List<EngineEvent> events = new ArrayList<>();

  @Override
  public void collect(EngineEvent event) {
    events.add(event);
  }

  public List<EngineEvent> drain() {
    final List<EngineEvent> drainedEvents = List.copyOf(events);
    events.clear();
    return drainedEvents;
  }

  public List<EngineEvent> getEvents() {
    return List.copyOf(events);
  }

  public boolean isEmpty() {
    return events.isEmpty();
  }
}
