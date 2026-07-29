package org.eximeebpms.bpm.engine.impl.businessevent;

import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;

import static org.eximeebpms.bpm.engine.businessevent.BusinessEventDispatcher.DEFAULT_BUSINESS_EVENT_DISPATCHER_BATCH_SIZE;
import static org.eximeebpms.bpm.engine.businessevent.BusinessEventDispatcher.DEFAULT_DISPATCH_INTERVAL_MS;
import static org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup.BusinessEventOutboxCleanupJobHandler.DEFAULT_BUSINESS_EVENT_OUTBOX_RETENTION_MS;
import static org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup.BusinessEventOutboxCleanupJobHandler.DEFAULT_CLEANUP_INTERVAL_MILLIS;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class BusinessEventConfiguration {

  /**
   * Enables native business event publishing.
   *
   * <p>Default: {@code false}.</p>
   */
  @Builder.Default
  protected boolean enabled = false;

  /**
   * Retention period (in milliseconds) for processed business-event outbox entries.
   * Processed entries older than this value will be periodically deleted by the
   * business-event outbox cleanup job.  Stored as milliseconds to ease testing.
   * Default: 7 days.
   */
  @Builder.Default
  protected long outboxRetentionMs = DEFAULT_BUSINESS_EVENT_OUTBOX_RETENTION_MS;
  /**
   * Interval (in milliseconds) at which the business-event outbox cleanup job should run.
   */
  @Builder.Default
  protected long outboxCleanupIntervalMs = DEFAULT_CLEANUP_INTERVAL_MILLIS;

  /** Interval (ms) between the end of one dispatcher cycle and the start of the next.
   *  When {@code null} the dispatcher's own default is used. */
  @Builder.Default
  protected long dispatchIntervalMs = DEFAULT_DISPATCH_INTERVAL_MS;

  /** Maximum number of outbox records fetched per DB round-trip.
   *  When {@code null} the dispatcher's own default is used. */
  @Builder.Default
  protected int dispatcherBatchSize = DEFAULT_BUSINESS_EVENT_DISPATCHER_BATCH_SIZE;

  /**
   * Symbolic publisher name resolved through business event publisher SPI.
   *
   * <p>Examples: {@code noop}, {@code kafka}.</p>
   *
   * <p>Default: {@code noop}.</p>
   */
  @Builder.Default
  protected String publisher = NoopBusinessEventPublisher.NAME;

  /**
   * Raw publisher-specific properties.
   *
   * <p>For Kafka these will include keys such as:</p>
   * <ul>
   *   <li>{@code kafka.bootstrap-servers}</li>
   *   <li>{@code kafka.topic}</li>
   *   <li>{@code kafka.client-id}</li>
   * </ul>
   *
   * <p>For REST these will include keys such as:</p>
   * <ul>
   *   <li>{@code rest.url}</li>
   * </ul>
   */
  @Singular("publisherProperty")
  protected Map<String, String> publisherProperties;

  /**
   * Comma-separated allowlist of enabled event types.
   *
   * <p>Default: {@code *}, meaning all supported event types are enabled when
   * {@link #enabled} is {@code true}.</p>
   */
  @Builder.Default
  protected String enabledEventTypes = "*";

  public Map<String, String> getPublisherProperties() {
    return publisherProperties == null ? Map.of() : Map.copyOf(publisherProperties);
  }

  public boolean isPublisherNoop() {
    return NoopBusinessEventPublisher.NAME.equalsIgnoreCase(publisher);
  }
}
