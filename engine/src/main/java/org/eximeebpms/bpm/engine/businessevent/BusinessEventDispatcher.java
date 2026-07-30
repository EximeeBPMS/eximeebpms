package org.eximeebpms.bpm.engine.businessevent;

import java.time.ZoneOffset;
import java.util.Date;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublishResult;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublisher;
import org.eximeebpms.bpm.commons.eventbus.Event;
import org.eximeebpms.bpm.commons.eventbus.EventMetadata;
import org.eximeebpms.bpm.engine.ProcessEnginePersistenceException;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventConfiguration;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.eximeebpms.bpm.engine.impl.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Periodically reads unprocessed records from {@code ACT_RU_BUS_EVT_OBX} and publishes
 * them through a {@link BusinessEventPublisher}.
 *
 * <p>Records are processed in strict ascending {@code ID_} order. If publishing a record
 * fails the dispatch stops the current cycle immediately — subsequent records are not touched
 * until the failing one succeeds on the next cycle. This preserves the delivery-order
 * guarantee documented on {@link BusinessEventOutboxEntity}.
 *
 * <h3>Threading</h3>
 * Call {@link #start()} to begin continuous background processing and {@link #stop()} to
 * shut it down gracefully. The dispatch is driven by a single-threaded
 * {@link ScheduledExecutorService} using {@code scheduleWithFixedDelay}: the next cycle
 * only starts {@link #dispatchIntervalMs} milliseconds <em>after the previous one completes</em>,
 * so there is no risk of concurrent dispatch runs even under load.
 *
 * <p>Each cycle calls {@link #run()} which drains the outbox in batches of
 * {@link #batchSize} until the table is empty or a publish failure occurs.
 */
public class BusinessEventDispatcher implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessEventDispatcher.class);

    /**
     * Records fetched from the outbox per DB round-trip.
     */
    public static final int DEFAULT_BUSINESS_EVENT_DISPATCHER_BATCH_SIZE = 100;

    /**
     * Default delay between the end of one cycle and the start of the next (5 seconds).
     */
    public static final long DEFAULT_DISPATCH_INTERVAL_MS = 5_000L;

    private final CommandExecutor commandExecutor;
    private final BusinessEventPublisher publisher;
    private final BusinessEventConfiguration businessEventConfiguration;
    private final AtomicReference<ScheduledExecutorService> schedulerRef = new AtomicReference<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public BusinessEventDispatcher(CommandExecutor commandExecutor, BusinessEventPublisher publisher, BusinessEventConfiguration businessEventConfiguration) {
        this.commandExecutor = commandExecutor;
        this.publisher = publisher;
        this.businessEventConfiguration = businessEventConfiguration;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Starts the background dispatcher thread. Safe to call multiple times — subsequent
     * calls are no-ops if already running.
     */
    public synchronized void start() {
        ScheduledExecutorService scheduler = schedulerRef.get();
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        ScheduledExecutorService newScheduler = Executors.newSingleThreadScheduledExecutor(new DispatcherThreadFactory());
        long dispatchIntervalMs = businessEventConfiguration.getDispatchIntervalMs();
        int batchSize = businessEventConfiguration.getDispatcherBatchSize();
        schedulerRef.set(newScheduler);
        newScheduler.scheduleWithFixedDelay(this, 0, dispatchIntervalMs, TimeUnit.MILLISECONDS);
        LOG.info("BusinessEventDispatcher started (batchSize={}, intervalMs={})", batchSize, dispatchIntervalMs);
    }

    /**
     * Stops the background dispatcher thread and waits up to 30 seconds for the current
     * cycle to finish. Performs one final drain of the outbox before returning.
     */
    public synchronized void stop() {
        ScheduledExecutorService scheduler = schedulerRef.getAndSet(null);
        if (scheduler == null) {
            return;
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        // Final drain so no events are left behind after engine shutdown
        run();
        LOG.info("BusinessEventDispatcher stopped");
    }

    // -------------------------------------------------------------------------
    // Dispatcher cycle — called by the scheduler and directly on shutdown
    // -------------------------------------------------------------------------

    /**
     * Processes one full dispatcher cycle within a single database transaction: drains
     * the outbox in batches until it is empty or a publish failure occurs.
     */
    @Override
    public void run() {
        try {
            int totalProcessed = commandExecutor.execute(ctx -> {
                int processed = 0;
                List<BusinessEventOutboxEntity> batch = fetchUnprocessed(ctx);
                while (!batch.isEmpty()) {
                    int published = publishBatch(ctx, batch);
                    processed += published;
                    batch = (published == batch.size()) ? fetchUnprocessed(ctx) : List.of();
                }
                return processed;
            });
            if (totalProcessed > 0) {
                LOG.debug("BusinessEventDispatcher: cycle complete, {} record(s) dispatched", totalProcessed);
            }
        } catch (Exception e) {
            LOG.error("BusinessEventDispatcher: unexpected error during dispatch cycle", e);
        }
    }

    /**
     * Publishes and marks as processed every entity in the batch in order, within the
     * provided {@link CommandContext} (i.e. the same database transaction as the caller).
     * Stops immediately on the first failure (ordering guarantee).
     *
     * @return the number of records successfully dispatched (may be less than {@code batch.size()} on failure)
     */
    private int publishBatch(CommandContext ctx, List<BusinessEventOutboxEntity> batch) {
        int count = 0;
        for (BusinessEventOutboxEntity entity : batch) {
            try {
                LOG.debug("Publishing event [id={}]", entity.getId());
                final BusinessEventPublishResult result = publisher.publish(buildEvent(entity));
                if (!result.successful()) {
                  LOG.error("BusinessEventDispatcher: failed to dispatch outbox record id={}, stopping cycle: {}",
                      entity.getId(), result.message());
                }
                markProcessed(ctx, entity);
                count++;
            } catch (Exception e) {
                LOG.error("BusinessEventDispatcher: failed to dispatch outbox record id={}, stopping cycle",
                        entity.getId(), e);
                return count;
            }
        }
        return count;
    }

    private List<BusinessEventOutboxEntity> fetchUnprocessed(CommandContext ctx) {
        int batchSize = businessEventConfiguration.getDispatcherBatchSize();
        try {
            return ctx.getBusinessEventManager().findUnprocessedEventsForDispatch(batchSize);
        } catch (ProcessEnginePersistenceException e) {
            if (ExceptionUtil.checkNowaitLockException(e)) {
                LOG.debug("BusinessEventDispatcher: FOR UPDATE NOWAIT lock contention detected " +
                        "— another session holds the lock; skipping this cycle");
                return List.of();
            }
            throw e;
        }
    }

    private void markProcessed(CommandContext ctx, BusinessEventOutboxEntity entity) {
        ctx.getBusinessEventManager().markAsProcessed(entity);
    }

    private Event buildEvent(BusinessEventOutboxEntity entity) {
        final boolean noProcessContext = entity.getProcessInstanceId() == null && entity.getRootProcessInstanceId() == null;
        final String processKey = Optional.ofNullable(entity.getRootProcessInstanceId()).orElse(entity.getProcessInstanceId());
        final String eventUuid = UUID.randomUUID().toString();

      EventMetadata metadata = EventMetadata.builder()
                .uuid(eventUuid)
                .type(entity.getEventType())
                .version("1.0")
                .origin("bpms")
                .correlationId(null)
                .timestamp(entity.getCreatedDate().toInstant())
                .noProcessContext(noProcessContext)
                .processInstanceId(processKey)
                .processDefinitionKey(entity.getProcessDefinitionKey())
                .build();

        return Event.builder()
                .metadata(metadata)
                .payload(entity.getBusinessEvent())
                .build();
    }

    public boolean isRunning() {
        ScheduledExecutorService scheduler = schedulerRef.get();
        return scheduler != null && !scheduler.isShutdown();
    }

    // -------------------------------------------------------------------------
    // Thread factory
    // -------------------------------------------------------------------------

    private static final class DispatcherThreadFactory implements ThreadFactory {
        private static final AtomicInteger COUNTER = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "BPM-BusinessEventDispatcher-" + COUNTER.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}
