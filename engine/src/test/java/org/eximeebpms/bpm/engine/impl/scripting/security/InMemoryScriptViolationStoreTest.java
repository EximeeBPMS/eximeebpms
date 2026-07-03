package org.eximeebpms.bpm.engine.impl.scripting.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class InMemoryScriptViolationStoreTest {

  @Test
  public void shouldReturnEmptyStateWhenNoEventsRecorded() {
    InMemoryScriptViolationStore store = new InMemoryScriptViolationStore(10);

    assertThat(store.getRecent(10)).isEmpty();
    assertThat(store.getTotalCount()).isEqualTo(0);
  }

  @Test
  public void shouldRecordAndReturnSingleEvent() {
    InMemoryScriptViolationStore store = new InMemoryScriptViolationStore(10);
    ScriptViolationEvent event = event("groovy", "SCRIPT_SECURITY_RUNTIME");

    store.record(event);

    assertThat(store.getRecent(10)).containsExactly(event);
    assertThat(store.getTotalCount()).isEqualTo(1);
  }

  @Test
  public void shouldReturnRecentEventsNewestFirst() {
    InMemoryScriptViolationStore store = new InMemoryScriptViolationStore(10);
    Instant older = Instant.parse("2026-01-01T10:00:00Z");
    Instant newer = Instant.parse("2026-01-01T10:00:01Z");
    ScriptViolationEvent oldEvent = event(older, "SCRIPT_SECURITY_RUNTIME");
    ScriptViolationEvent newEvent = event(newer, "SCRIPT_SECURITY_JAVA_IO");

    store.record(oldEvent);
    store.record(newEvent);

    List<ScriptViolationEvent> recent = store.getRecent(10);
    assertThat(recent).hasSize(2);
    assertThat(recent.get(0)).isEqualTo(newEvent);
    assertThat(recent.get(1)).isEqualTo(oldEvent);
  }

  @Test
  public void shouldReturnAtMostRequestedLimit() {
    InMemoryScriptViolationStore store = new InMemoryScriptViolationStore(10);
    for (int i = 0; i < 5; i++) {
      store.record(event("javascript", "RULE_" + i));
    }

    assertThat(store.getRecent(3)).hasSize(3);
    assertThat(store.getRecent(10)).hasSize(5);
  }

  @Test
  public void shouldEvictOldestEventWhenCapacityExceeded() {
    InMemoryScriptViolationStore store = new InMemoryScriptViolationStore(3);
    ScriptViolationEvent first = event("javascript", "RULE_1");
    store.record(first);
    store.record(event("javascript", "RULE_2"));
    store.record(event("javascript", "RULE_3"));
    store.record(event("javascript", "RULE_4"));  // evicts RULE_1

    List<ScriptViolationEvent> recent = store.getRecent(10);
    assertThat(recent).hasSize(3);
    assertThat(recent).doesNotContain(first);
  }

  @Test
  public void shouldTrackTotalCountAcrossEvictions() {
    InMemoryScriptViolationStore store = new InMemoryScriptViolationStore(2);
    for (int i = 0; i < 10; i++) {
      store.record(event("javascript", "RULE_" + i));
    }

    assertThat(store.getTotalCount()).isEqualTo(10);
    assertThat(store.getRecent(100)).hasSize(2);
  }

  @Test
  public void shouldThrowForZeroCapacity() {
    assertThatThrownBy(() -> new InMemoryScriptViolationStore(0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("capacity");
  }

  @Test
  public void shouldThrowForNegativeCapacity() {
    assertThatThrownBy(() -> new InMemoryScriptViolationStore(-1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("capacity");
  }

  @Test
  public void shouldThrowForNullEvent() {
    InMemoryScriptViolationStore store = new InMemoryScriptViolationStore(10);

    assertThatThrownBy(() -> store.record(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void shouldHandleConcurrentWrites() throws InterruptedException {
    int threadCount = 10;
    int eventsPerThread = 20;
    InMemoryScriptViolationStore store = new InMemoryScriptViolationStore(threadCount * eventsPerThread);
    CountDownLatch start = new CountDownLatch(1);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    for (int t = 0; t < threadCount; t++) {
      executor.submit(() -> {
        try {
          start.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
        for (int i = 0; i < eventsPerThread; i++) {
          store.record(event("javascript", "RULE"));
        }
      });
    }

    start.countDown();
    executor.shutdown();
    assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

    assertThat(store.getTotalCount()).isEqualTo((long) threadCount * eventsPerThread);
    assertThat(store.getRecent(threadCount * eventsPerThread)).hasSize(threadCount * eventsPerThread);
  }

  private ScriptViolationEvent event(String language, String ruleCode) {
    return event(Instant.now(), ruleCode, language);
  }

  private ScriptViolationEvent event(Instant timestamp, String ruleCode) {
    return event(timestamp, ruleCode, "javascript");
  }

  private ScriptViolationEvent event(Instant timestamp, String ruleCode, String language) {
    return new ScriptViolationEvent(
        timestamp,
        "testProcess",
        null,
        "testActivity",
        language,
        ScriptSourceType.INLINE_SOURCE,
        ScriptOrigin.USER,
        ruleCode,
        "Test reason for " + ruleCode);
  }
}
