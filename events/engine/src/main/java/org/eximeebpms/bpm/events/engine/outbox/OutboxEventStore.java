package org.eximeebpms.bpm.events.engine.outbox;

import java.util.Collection;
import java.util.List;
import org.eximeebpms.bpm.events.engine.EngineEvent;

public interface OutboxEventStore {

  void save(EngineEvent event);

  default void saveAll(Collection<EngineEvent> events) {
    events.forEach(this::save);
  }

  List<OutboxEvent> findPending(int limit);

  void markPublished(String eventId);

  void markFailed(String eventId, Throwable failure);
}
