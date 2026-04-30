package org.eximeebpms.bpm.events.engine.history;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eximeebpms.bpm.engine.impl.history.event.HistoryEvent;
import org.eximeebpms.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.eximeebpms.bpm.events.EventBroadcaster;

/**
 * History event handler that forwards history events to event broadcasting pipeline.
 *
 * <p>It intentionally does not replace the default history database handler.
 * It should be registered as an additional handler through a composite handler.</p>
 */
@RequiredArgsConstructor
public class BroadcastingHistoryEventHandler implements HistoryEventHandler {

  private final EventBroadcaster<HistoryEvent> eventBroadcaster;

  @Override
  public void handleEvent(HistoryEvent historyEvent) {
    eventBroadcaster.broadcast(historyEvent);
  }

  @Override
  public void handleEvents(List<HistoryEvent> historyEvents) {
    historyEvents.forEach(this::handleEvent);
  }
}
