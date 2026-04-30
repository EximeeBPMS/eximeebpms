package org.eximeebpms.bpm.events.engine.outbox;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.eximeebpms.bpm.events.engine.EngineEvent;

public class InMemoryOutboxEventStore implements OutboxEventStore {

  private final List<OutboxEvent> records = new ArrayList<>();

  @Override
  public void save(EngineEvent event) {
    records.add(OutboxEvent.builder()
        .id(event.getId())
        .event(event)
        .status(OutboxEventStatus.NEW)
        .createdAt(Instant.now())
        .attempts(0)
        .build());
  }

  @Override
  public List<OutboxEvent> findPending(int limit) {
    return records.stream()
        .filter(outboxEvent -> outboxEvent.getStatus() == OutboxEventStatus.NEW
            || outboxEvent.getStatus() == OutboxEventStatus.FAILED)
        .limit(limit)
        .toList();
  }

  @Override
  public void markPublished(String eventId) {
    replace(eventId, OutboxEventStatus.PUBLISHED, null);
  }

  @Override
  public void markFailed(String eventId, Throwable failure) {
    replace(eventId, OutboxEventStatus.FAILED, failure.getMessage());
  }

  private void replace(String eventId, OutboxEventStatus status, String error) {
    for (int index = 0; index < records.size(); index++) {
      final OutboxEvent outboxEvent = records.get(index);

      if (outboxEvent.getId().equals(eventId)) {
        records.set(index, outboxEvent.toBuilder()
            .status(status)
            .publishedAt(status == OutboxEventStatus.PUBLISHED ? Instant.now() : null)
            .lastError(error)
            .attempts(outboxEvent.getAttempts() + 1)
            .build());
        return;
      }
    }
  }
}
