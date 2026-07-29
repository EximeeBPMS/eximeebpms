package org.eximeebpms.bpm.engine.test.businessevent;

import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublisher;
import org.eximeebpms.bpm.commons.eventbus.Event;
import org.eximeebpms.bpm.commons.eventbus.EventMetadata;
import org.eximeebpms.bpm.engine.businessevent.BusinessEventDispatcher;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublishResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration tests for {@link BusinessEventDispatcher}.
 *
 * <p>Each test inserts {@link BusinessEventOutboxEntity} records directly into
 * {@code ACT_RU_BUS_EVT_OBX} via the engine's DB session, runs a single dispatcher
 * cycle with a capturing publisher stub, and then asserts on publication order,
 * the {@code PROCESSED_} flag in the database, and the structure of the built {@link Event}.
 */
public class BusinessEventDispatcherIT extends AbstractBusinessEventIT {

    /** aiming high than to succeed aiming low. And we of Spurs have set our sights very high, so high in fact that even failure will have in it an echo of glory


     * Returns the {@code type} header values of all published events, in publication order.
     */
    private static List<String> publishedTypes(CapturingPublisher publisher) {
        return publisher.getPublished().stream()
                .map(e -> e.metadata().type())
                .toList();
    }

    /**
     * GIVEN 5 outbox records written in insertion order
     * WHEN  the dispatcher runs one cycle
     * THEN  they are published in ascending {@code ID_} order
     */
    @Test
    public void shouldPublishEventsInAscendingIdOrder() {
        for (int i = 0; i < 5; i++) {
            insertOutboxRecord("TASK_EVENT_" + i);
        }
        List<String> expectedOrder = getAllUnprocessedEventTypes();

        CapturingPublisher publisher = new CapturingPublisher();
        new BusinessEventDispatcher(commandExecutor, publisher, businessEventConfiguration).run();

        assertThat(publishedTypes(publisher))
                .as("events must be published in ascending ID_ order")
                .containsExactlyElementsOf(expectedOrder);
    }

    /**
     * GIVEN 3 outbox records where the second one causes a publish exception
     * WHEN  the dispatcher runs
     * THEN  only the first event is published and marked processed;
     * the second and third remain unprocessed (ordering guarantee: no skipping)
     */
    @Test
    public void shouldStopOnFirstPublishFailure_andNotPublishSubsequentEvents() {
        insertOutboxRecord("EVENT_OK_1");
        insertOutboxRecord("EVENT_FAIL");
        insertOutboxRecord("EVENT_OK_2");
        List<Long> ids = getAllUnprocessedIds();
        String id1 = String.valueOf(ids.get(0));
        String id2 = String.valueOf(ids.get(1));
        String id3 = String.valueOf(ids.get(2));

        CapturingPublisher publisher = new CapturingPublisher();
        publisher.failOnEventType("EVENT_FAIL");
        new BusinessEventDispatcher(commandExecutor, publisher, businessEventConfiguration).run();

        assertThat(publishedTypes(publisher))
                .as("only the first event (before the failure) should have been published")
                .containsExactly("EVENT_OK_1");
        assertThat(isProcessed(id1)).as("id1 should be marked processed").isTrue();
        assertThat(isProcessed(id2)).as("id2 must NOT be marked processed — dispatcher stopped before it").isFalse();
        assertThat(isProcessed(id3)).as("id3 must NOT be marked processed — dispatcher stopped before it").isFalse();
    }

    /**
     * GIVEN 1 outbox record
     * WHEN  the dispatcher publishes it successfully
     * THEN  the record is marked {@code processed = true} and {@code processed_date} is set
     */
    @Test
    public void shouldMarkEventAsProcessed_afterSuccessfulPublish() {
        insertOutboxRecord("PROCESS_STARTED");
        String id = String.valueOf(getAllUnprocessedIds().get(0));

        new BusinessEventDispatcher(commandExecutor, new CapturingPublisher(), businessEventConfiguration).run();

        assertThat(isProcessed(id)).as("record should be marked processed after dispatch").isTrue();
        assertThat(getProcessedDate(id)).as("processed_date must be set after dispatch").isNotNull();
    }

    /**
     * GIVEN more records than the dispatcher's batch size
     * WHEN  the dispatcher runs one cycle
     * THEN  all records are published across multiple DB round-trips, still in ID order
     */
    @Test
    public void shouldDrainOutboxAcrossMultipleBatches_inIdOrder() {
        int batchSize = 3;
        int totalEvents = 8;

        for (int i = 0; i < totalEvents; i++) {
            insertOutboxRecord("MULTI_BATCH_EVENT_" + i);
        }
        List<String> expectedOrder = getAllUnprocessedEventTypes();

        CapturingPublisher publisher = new CapturingPublisher();
        BusinessEventDispatcher dispatcher = new BusinessEventDispatcher(commandExecutor, publisher, businessEventConfiguration);
        engineRule.getProcessEngineConfiguration().getBusinessEventConfiguration().setDispatcherBatchSize(batchSize);
        dispatcher.run();

        assertThat(publishedTypes(publisher))
                .as("all %d events must be dispatched across multiple batches, in ID order", totalEvents)
                .containsExactlyElementsOf(expectedOrder);
    }

    /**
     * GIVEN a failing first event that blocks the dispatcher
     * WHEN  the underlying cause is resolved and the dispatcher runs again
     * THEN  the previously blocked event is retried first, then the subsequent ones — in ID order
     */
    @Test
    public void shouldRetryBlockingEvent_onNextCycle_andThenContinueInOrder() {
        insertOutboxRecord("BLOCKED_EVENT");
        insertOutboxRecord("QUEUED_EVENT");
        List<Long> ids = getAllUnprocessedIds();
        String id1 = String.valueOf(ids.get(0));
        String id2 = String.valueOf(ids.get(1));

        CapturingPublisher cycleOne = new CapturingPublisher();
        cycleOne.failOnEventType("BLOCKED_EVENT");
        new BusinessEventDispatcher(commandExecutor, cycleOne, businessEventConfiguration).run();

        assertThat(isProcessed(id1)).as("BLOCKED_EVENT must not be processed after failed cycle").isFalse();
        assertThat(isProcessed(id2)).as("QUEUED_EVENT must not be skipped ahead of BLOCKED_EVENT").isFalse();

        CapturingPublisher cycleTwo = new CapturingPublisher();
        new BusinessEventDispatcher(commandExecutor, cycleTwo, businessEventConfiguration).run();

        assertThat(publishedTypes(cycleTwo))
                .as("retry cycle must dispatch events in ascending ID order")
                .containsExactly("BLOCKED_EVENT", "QUEUED_EVENT");
        assertThat(isProcessed(id1)).isTrue();
        assertThat(isProcessed(id2)).isTrue();
    }

    /**
     * GIVEN 5 outbox records where the 4th causes a publish exception
     * WHEN  the dispatcher runs a first cycle (fails on record 4)
     * THEN  records 1–3 are published and persisted as {@code processed = true} in the database;
     * records 4–5 remain {@code processed = false}
     * WHEN  the dispatcher runs a second cycle with a healthy publisher
     * THEN  only records 4–5 are dispatched — records 1–3 are <strong>not</strong> re-published,
     * proving that the {@code processed} flag written in the first cycle is respected
     */
    @Test
    public void shouldNotRePublishAlreadyProcessedEvents_onNextCycle_afterPublisherFailure() {
        insertOutboxRecord("EVENT_1");
        insertOutboxRecord("EVENT_2");
        insertOutboxRecord("EVENT_3");
        insertOutboxRecord("EVENT_FAIL");
        insertOutboxRecord("EVENT_5");

        List<Long> ids = getAllUnprocessedIds();
        String id1 = String.valueOf(ids.get(0));
        String id2 = String.valueOf(ids.get(1));
        String id3 = String.valueOf(ids.get(2));
        String id4 = String.valueOf(ids.get(3));
        String id5 = String.valueOf(ids.get(4));

        // ---- cycle 1: publisher fails on the 4th record ----
        CapturingPublisher cycle1Publisher = new CapturingPublisher();
        cycle1Publisher.failOnEventType("EVENT_FAIL");
        new BusinessEventDispatcher(commandExecutor, cycle1Publisher, businessEventConfiguration).run();

        assertThat(publishedTypes(cycle1Publisher))
                .as("cycle 1: only records before the failure must have been published")
                .containsExactly("EVENT_1", "EVENT_2", "EVENT_3");

        assertThat(isProcessed(id1)).as("id1 must be persisted as processed after cycle 1").isTrue();
        assertThat(isProcessed(id2)).as("id2 must be persisted as processed after cycle 1").isTrue();
        assertThat(isProcessed(id3)).as("id3 must be persisted as processed after cycle 1").isTrue();
        assertThat(isProcessed(id4)).as("id4 must remain unprocessed — dispatcher stopped before it").isFalse();
        assertThat(isProcessed(id5)).as("id5 must remain unprocessed — dispatcher stopped before it").isFalse();

        // ---- cycle 2: healthy publisher — must NOT re-publish records 1–3 ----
        CapturingPublisher cycle2Publisher = new CapturingPublisher();
        new BusinessEventDispatcher(commandExecutor, cycle2Publisher, businessEventConfiguration).run();

        assertThat(publishedTypes(cycle2Publisher))
                .as("cycle 2: only the previously unprocessed records must be dispatched; "
                        + "records already marked processed must not be re-published")
                .containsExactly("EVENT_FAIL", "EVENT_5");

        assertThat(isProcessed(id4)).as("id4 must be persisted as processed after cycle 2").isTrue();
        assertThat(isProcessed(id5)).as("id5 must be persisted as processed after cycle 2").isTrue();
    }

    // -------------------------------------------------------------------------
    // Event-building tests
    // -------------------------------------------------------------------------

    /**
     * GIVEN an empty outbox table
     * WHEN  the dispatcher runs
     * THEN  nothing is published
     */
    @Test
    public void shouldPublishNothing_whenOutboxIsEmpty() {
        CapturingPublisher publisher = new CapturingPublisher();
        new BusinessEventDispatcher(commandExecutor, publisher, businessEventConfiguration).run();

        assertThat(publisher.getPublished()).isEmpty();
    }

    /**
     * GIVEN an outbox record with full process context
     * (rootProcessInstanceId, processInstanceId, processDefinitionKey, eventType, payload)
     * persisted in H2
     * WHEN  the dispatcher runs one cycle and builds the {@link Event}
     * THEN  the published event's headers and payload faithfully reflect the stored entity:
     * <ul>
     *   <li>uuid  — valid UUID string (random per dispatch, format-only check)</li>
     *   <li>type  = eventType</li>
     *   <li>version = "1.0", origin = "bpms", correlationId = null</li>
     *   <li>timestamp = createdDate</li>
     *   <li>noProcessContext = false</li>
     *   <li>processKey  = rootProcessInstanceId  (preferred over processInstanceId)</li>
     *   <li>processName = processDefinitionKey</li>
     *   <li>payload = raw businessEvent JSON string (unchanged)</li>
     * </ul>
     */
    @Test
    public void shouldBuildEvent_withAllHeadersAndPayload_whenFullProcessContextIsStored() {
        Date createdDate = new Date();
        String rawPayload = "{\"taskId\":\"t-1\",\"assignee\":\"john\"}";

        commandExecutor.execute(ctx -> {
            ctx.getDbEntityManager().insertWithoutId(
                    BusinessEventOutboxEntity.builder()
                            .createdDate(createdDate)
                            .eventType("TASK_CREATED")
                            .businessEvent(rawPayload)
                            .processInstanceId("pi-1")
                            .rootProcessInstanceId("root-pi-1")
                            .processDefinitionKey("invoice-process")
                            .taskId("t-1")
                            .build());
            return null;
        });

        CapturingPublisher publisher = new CapturingPublisher();
        new BusinessEventDispatcher(commandExecutor, publisher, businessEventConfiguration).run();

        assertThat(publisher.getPublished()).hasSize(1);
        Event event = publisher.getPublished().get(0);
        EventMetadata h = event.metadata();

        // uuid is random per dispatch — only the format matters
        assertThatCode(() -> UUID.fromString(h.uuid()))
                .as("uuid must be a valid UUID")
                .doesNotThrowAnyException();

        assertThat(h.type()).isEqualTo("TASK_CREATED");
        assertThat(h.version()).isEqualTo("1.0");
        assertThat(h.origin()).isEqualTo("bpms");
        assertThat(h.correlationId()).isNull();
        assertThat(h.timestamp()).isEqualTo(createdDate);
        assertThat(h.noProcessContext()).isFalse();
        // rootProcessInstanceId takes precedence as processKey
        assertThat(h.processInstanceId()).isEqualTo("root-pi-1");
        assertThat(h.processDefinitionKey()).isEqualTo("invoice-process");
        assertThat(event.payload()).isEqualTo(rawPayload);
    }

    /**
     * GIVEN an outbox record where only {@code processInstanceId} is set
     * (rootProcessInstanceId is null)
     * WHEN  the dispatcher builds the event
     * THEN  processKey falls back to processInstanceId
     */
    @Test
    public void shouldBuildEvent_usingProcessInstanceIdAsProcessKey_whenRootProcessInstanceIdIsNull() {
        commandExecutor.execute(ctx -> {
            ctx.getDbEntityManager().insertWithoutId(
                    BusinessEventOutboxEntity.builder()
                            .createdDate(new Date())
                            .eventType("PROCESS_STARTED")
                            .businessEvent("{\"info\":\"started\"}")
                            .processInstanceId("pi-2")
                            .rootProcessInstanceId(null)
                            .processDefinitionKey("order-process")
                            .build());
            return null;
        });

        CapturingPublisher publisher = new CapturingPublisher();
        new BusinessEventDispatcher(commandExecutor, publisher, businessEventConfiguration).run();

        EventMetadata h = publisher.getPublished().get(0).metadata();
        assertThat(h.noProcessContext()).isFalse();
        assertThat(h.processInstanceId()).isEqualTo("pi-2");
        assertThat(h.processDefinitionKey()).isEqualTo("order-process");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * GIVEN an outbox record with neither processInstanceId nor rootProcessInstanceId
     * WHEN  the dispatcher builds the event
     * THEN  noProcessContext = true, and the {@link EventMetadata} builder sets both
     * processKey and processName to the sentinel value {@code "no-process-context"}
     */
    @Test
    public void shouldBuildEvent_withNoProcessContextSentinel_whenBothInstanceIdsAreNull() {
        commandExecutor.execute(ctx -> {
            ctx.getDbEntityManager().insertWithoutId(
                    BusinessEventOutboxEntity.builder()
                            .createdDate(new Date())
                            .eventType("SYSTEM_EVENT")
                            .businessEvent("{\"info\":\"system\"}")
                            .processInstanceId(null)
                            .rootProcessInstanceId(null)
                            .processDefinitionKey(null)
                            .build());
            return null;
        });

        CapturingPublisher publisher = new CapturingPublisher();
        new BusinessEventDispatcher(commandExecutor, publisher, businessEventConfiguration).run();

        EventMetadata h = publisher.getPublished().get(0).metadata();
        assertThat(h.noProcessContext()).isTrue();
        // EventMetadata.EventMetadataBuilder.build() overrides both to the sentinel when noProcessContext=true
        assertThat(h.processInstanceId()).isEqualTo("no-process-context");
        assertThat(h.processDefinitionKey()).isEqualTo("no-process-context");
    }

    /**
     * Inserts a single outbox record using the identity-column INSERT path
     * (no application-side ID generation, no entity cache).
     */
    private void insertOutboxRecord(String eventType) {
        commandExecutor.execute(ctx -> {
            BusinessEventOutboxEntity entity = BusinessEventOutboxEntity.builder()
                    .createdDate(new Date())
                    .eventType(eventType)
                    .businessEvent("{\"eventType\":\"" + eventType + "\"}")
                    .build();
            ctx.getDbEntityManager().insertWithoutId(entity);
            return null;
        });
    }

    /**
     * Returns DB-assigned IDs of all unprocessed records in ascending order.
     */
    private List<Long> getAllUnprocessedIds() {
        return commandExecutor.execute(ctx ->
                ctx.getBusinessEventManager()
                        .findUnprocessedEventsForDispatch(Integer.MAX_VALUE)
                        .stream()
                        .map(e -> Long.valueOf(e.getId()))
                        .toList()
        );
    }

    /**
     * Returns the {@code eventType} of all unprocessed records in ascending {@code ID_} order
     * — the same sequence the dispatcher will publish them.
     */
    private List<String> getAllUnprocessedEventTypes() {
        return commandExecutor.execute(ctx ->
                ctx.getBusinessEventManager()
                        .findUnprocessedEventsForDispatch(Integer.MAX_VALUE)
                        .stream()
                        .map(BusinessEventOutboxEntity::getEventType)
                        .toList()
        );
    }

    private boolean isProcessed(String id) {
        return commandExecutor.execute(ctx -> {
            BusinessEventOutboxEntity entity = ctx.getDbEntityManager()
                    .selectById(BusinessEventOutboxEntity.class, id);
            return entity != null && entity.isProcessed();
        });
    }

    private Date getProcessedDate(String id) {
        return commandExecutor.execute(ctx -> {
            BusinessEventOutboxEntity entity = ctx.getDbEntityManager()
                    .selectById(BusinessEventOutboxEntity.class, id);
            return entity == null ? null : entity.getProcessedDate();
        });
    }

    // -------------------------------------------------------------------------
    // Test doubles
    // -------------------------------------------------------------------------

    /**
     * A {@link BusinessEventPublisher} that records every published event.
     * Optionally throws a {@link RuntimeException} for events whose type matches
     * a configured fail-set — simulating a publish failure mid-cycle.
     */
    @Getter
    private static final class CapturingPublisher implements BusinessEventPublisher {

        private final List<Event> published = new ArrayList<>();
        private final Set<String> failOnTypes = new HashSet<>();

        public void failOnEventType(String eventType) {
            failOnTypes.add(eventType);
        }

      @Override
        public String getName() {
            return "test-capturing-publisher";
        }

        @Override
        public BusinessEventPublishResult publish(Event event) {
            String type = event.metadata().type();
            if (failOnTypes.contains(type)) {
                throw new RuntimeException("Simulated publish failure for event type: " + type);
            }
            published.add(event);
            return BusinessEventPublishResult.success();
        }
    }
}

