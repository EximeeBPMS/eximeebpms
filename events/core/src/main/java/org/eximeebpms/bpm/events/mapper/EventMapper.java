package org.eximeebpms.bpm.events.mapper;

import java.util.Optional;
import org.eximeebpms.bpm.events.EventEnvelope;

/**
 * Maps source-specific engine events to transport-neutral event envelopes.
 *
 * @param <S> source event type, for example HistoryEvent, DelegateTask,
 *            DelegateExecution or Incident
 */
@FunctionalInterface
public interface EventMapper<S> {

  /**
   * Maps a source event to an {@link EventEnvelope}.
   *
   * <p>An empty result means that the source event should not be broadcast.</p>
   *
   * @param source source event
   * @return mapped event envelope or empty result
   */
  Optional<EventEnvelope> map(S source);
}
