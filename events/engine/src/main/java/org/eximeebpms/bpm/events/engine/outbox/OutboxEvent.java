package org.eximeebpms.bpm.events.engine.outbox;

import java.time.Instant;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.eximeebpms.bpm.events.engine.EngineEvent;

@Value
@Builder(toBuilder = true)
public class OutboxEvent {

  @NonNull
  String id;

  @NonNull
  EngineEvent event;

  @NonNull
  OutboxEventStatus status;

  @NonNull
  Instant createdAt;

  Instant publishedAt;

  String lastError;

  int attempts;
}
