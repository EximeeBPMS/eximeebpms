package org.eximeebpms.bpm.events;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.events.cfg.EventBroadcastingProperties;
import org.eximeebpms.bpm.events.exception.EventPublishException;
import org.eximeebpms.bpm.events.mapper.EventMapper;
import org.eximeebpms.bpm.events.publisher.EventPublisher;

/**
 * Coordinates event mapping and publishing.
 *
 * <p>This class is transport-neutral and framework-neutral. It can be used by
 * engine plugins, Spring Boot auto-configuration and application-server
 * integrations.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class EventBroadcaster<S> {

  private final EventBroadcastingProperties properties;
  private final EventMapper<S> mapper;
  private final EventPublisher publisher;

  public void broadcast(S source) {
    if (!properties.isEnabled()) {
      return;
    }

    try {
      Optional<EventEnvelope> envelope = mapper.map(source);
      envelope.ifPresent(publisher::publish);
    } catch (Exception exception) {
      handleFailure(source, exception);
    }
  }

  private void handleFailure(S source, Exception exc) {
    if (properties.isFailOnPublishError()) {
      throw new EventPublishException("Failed to broadcast event: " + source, exc);
    }

    log.warn("Failed to broadcast event: {}", source, exc);
  }
}
