package org.eximeebpms.bpm.events.engine.outbox;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.engine.mapper.EngineEventEnvelopeMapper;
import org.eximeebpms.bpm.events.publisher.EventPublisher;

@Slf4j
@RequiredArgsConstructor
public class OutboxEventDispatcher {

  private final OutboxEventStore outboxEventStore;
  private final EngineEventEnvelopeMapper envelopeMapper;
  private final EventPublisher eventPublisher;

  public int dispatch(int limit) {
    final List<OutboxEvent> events = outboxEventStore.findPending(limit);

    for (OutboxEvent outboxEvent : events) {
      dispatch(outboxEvent);
    }

    return events.size();
  }

  private void dispatch(OutboxEvent outboxEvent) {
    try {
      final EventEnvelope envelope = envelopeMapper.map(outboxEvent.getEvent());

      eventPublisher.publish(envelope);

      outboxEventStore.markPublished(outboxEvent.getId());
    } catch (RuntimeException exception) {
      outboxEventStore.markFailed(outboxEvent.getId(), exception);
      log.warn("Failed to dispatch outbox event [id={}]", outboxEvent.getId(), exception);
    }
  }
}
