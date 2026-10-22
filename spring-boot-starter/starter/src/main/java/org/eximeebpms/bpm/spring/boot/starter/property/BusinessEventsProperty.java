package org.eximeebpms.bpm.spring.boot.starter.property;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import static org.eximeebpms.bpm.engine.businessevent.BusinessEventDispatcher.DEFAULT_BUSINESS_EVENT_DISPATCHER_BATCH_SIZE;
import static org.eximeebpms.bpm.engine.businessevent.BusinessEventDispatcher.DEFAULT_DISPATCH_INTERVAL_MS;
import static org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventType.BUSINESS_EVENT_PREFIX;
import static org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypeFilter.ALL;
import static org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup.BusinessEventOutboxCleanupJobHandler.DEFAULT_BUSINESS_EVENT_OUTBOX_RETENTION_MS;
import static org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup.BusinessEventOutboxCleanupJobHandler.DEFAULT_CLEANUP_INTERVAL_MILLIS;


@Setter
@Getter
public class BusinessEventsProperty {

  private boolean enabled = false;

  private String publisher = "noop";

  protected long outboxRetentionMs = DEFAULT_BUSINESS_EVENT_OUTBOX_RETENTION_MS;
  protected long outboxCleanupIntervalMs = DEFAULT_CLEANUP_INTERVAL_MILLIS;
  protected long dispatchIntervalMs = DEFAULT_DISPATCH_INTERVAL_MS;
  protected int dispatcherBatchSize = DEFAULT_BUSINESS_EVENT_DISPATCHER_BATCH_SIZE;
  protected String prefix = BUSINESS_EVENT_PREFIX;

  /**
   * Allowlist of published event types, as {@code <entity>:<event>} tokens. Defaults to every type.
   */
  protected Set<String> enabledEventTypes = new LinkedHashSet<>(Set.of(ALL));

  /**
   * Denylist of published event types, applied after {@link #enabledEventTypes} and winning over it.
   */
  protected Set<String> disabledEventTypes = new LinkedHashSet<>();

  private Map<String, String> publisherProperties = new HashMap<>();

}
