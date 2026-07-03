package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryScriptViolationStore implements ScriptViolationStore {

  private final LinkedBlockingDeque<ScriptViolationEvent> events;
  private final AtomicLong totalCount = new AtomicLong(0);

  public InMemoryScriptViolationStore(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must be greater than 0");
    }
    this.events = new LinkedBlockingDeque<>(capacity);
  }

  @Override
  public void record(ScriptViolationEvent event) {
    Objects.requireNonNull(event, "event must not be null");
    while (!events.offerLast(event)) {
      events.pollFirst();
    }
    totalCount.incrementAndGet();
  }

  @Override
  public List<ScriptViolationEvent> getRecent(int limit) {
    return events.stream()
        .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
        .limit(limit)
        .toList();
  }

  @Override
  public long getTotalCount() {
    return totalCount.get();
  }
}
