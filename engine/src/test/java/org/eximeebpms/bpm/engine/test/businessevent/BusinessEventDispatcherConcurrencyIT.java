package org.eximeebpms.bpm.engine.test.businessevent;

import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublishResult;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublisher;
import org.eximeebpms.bpm.commons.eventbus.Event;
import org.eximeebpms.bpm.engine.businessevent.BusinessEventDispatcher;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Concurrency tests for {@link BusinessEventDispatcher}.
 *
 * <h3>How duplicate delivery is prevented</h3>
 * The fetch query uses {@code SELECT … FOR UPDATE NOWAIT}: if two dispatcher threads fetch
 * at exactly the same instant, the one that loses the row lock immediately receives an
 * exception and returns an empty batch, so it publishes nothing. Because records are fetched
 * in strict ascending {@code ID_} order both dispatchers always target the same rows, so
 * there is no interleaving scenario in which each dispatcher could acquire a disjoint subset.
 */
public class BusinessEventDispatcherConcurrencyIT extends AbstractBusinessEventIT {

    // -------------------------------------------------------------------------
    // Concurrency test
    // -------------------------------------------------------------------------

    private static List<String> eventTypes(List<Event> events) {
        return events.stream()
                .map(e -> e.metadata().type())
                .toList();
    }

    // -------------------------------------------------------------------------
    // Helper: simple capturing publisher
    // -------------------------------------------------------------------------

    /**
     * GIVEN 5 unprocessed outbox records
     * AND two {@link BusinessEventDispatcher} instances sharing the same database
     * WHEN both dispatchers run concurrently in separate threads, released simultaneously
     * via a start-gun {@link CountDownLatch}
     * THEN each of the 5 events should be published exactly once (total = 5)
     *
     * <p>Duplicate delivery is prevented by {@code SELECT … FOR UPDATE NOWAIT}: if both
     * dispatchers' {@code fetchUnprocessed()} calls overlap, the one that cannot acquire
     * the row lock receives an exception, returns an empty batch, and publishes nothing.
     */
    @Test
    public void shouldPublishEachEventExactlyOnce_whenTwoDispatchersRunConcurrently()
            throws InterruptedException {

        // Given: 5 outbox records, all unprocessed
        int eventCount = 5;
        for (int i = 0; i < eventCount; i++) {
            insertOutboxRecord("CONCURRENT_EVENT_" + i);
        }

        List<Event> capturedByDispatcher1 = Collections.synchronizedList(new ArrayList<>());
        List<Event> capturedByDispatcher2 = Collections.synchronizedList(new ArrayList<>());

        BusinessEventPublisher publisher1 = createCapturingPublisher(capturedByDispatcher1);
        BusinessEventPublisher publisher2 = createCapturingPublisher(capturedByDispatcher2);

        // Start-gun latch: both threads wait here and are released simultaneously,
        // maximising the chance that their fetch calls overlap and the FOR UPDATE NOWAIT
        // coordination is exercised.
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch bothDone = new CountDownLatch(2);
        List<Throwable> threadErrors = Collections.synchronizedList(new ArrayList<>());

        Thread dispatcher1Thread = new Thread(() -> {
            try {
                startGun.await();
                new BusinessEventDispatcher(commandExecutor, publisher1, businessEventConfiguration).run();
            } catch (Throwable t) {
                threadErrors.add(t);
            } finally {
                bothDone.countDown();
            }
        }, "test-dispatcher-1");

        Thread dispatcher2Thread = new Thread(() -> {
            try {
                startGun.await();
                new BusinessEventDispatcher(commandExecutor, publisher2, businessEventConfiguration).run();
            } catch (Throwable t) {
                threadErrors.add(t);
            } finally {
                bothDone.countDown();
            }
        }, "test-dispatcher-2");

        // When: both dispatchers start simultaneously
        dispatcher1Thread.start();
        dispatcher2Thread.start();
        startGun.countDown(); // release both at once

        boolean finished = bothDone.await(30, TimeUnit.SECONDS);
        assertThat(finished)
                .as("both dispatcher threads must complete within 30 seconds")
                .isTrue();
        assertThat(threadErrors)
                .as("no unexpected exceptions in dispatcher threads")
                .isEmpty();

        // Then: each event should have been published exactly once (total = eventCount).
        // FOR UPDATE NOWAIT ensures that when two fetches overlap, the losing dispatcher
        // gets an exception, returns an empty batch, and publishes nothing — so one
        // dispatcher publishes all 5 and the other publishes 0.
        List<String> allPublishedTypes = new ArrayList<>();
        allPublishedTypes.addAll(eventTypes(capturedByDispatcher1));
        allPublishedTypes.addAll(eventTypes(capturedByDispatcher2));

        List<String> expectedTypes = new ArrayList<>();
        for (int i = 0; i < eventCount; i++) {
            expectedTypes.add("CONCURRENT_EVENT_" + i);
        }

        assertThat(allPublishedTypes)
                .as("each outbox record must be published exactly once across both dispatchers — no duplicates, no missing events")
                .hasSize(eventCount)
                .doesNotHaveDuplicates()
                .containsExactlyInAnyOrderElementsOf(expectedTypes);
    }

    // -------------------------------------------------------------------------
    // DB helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a {@link BusinessEventPublisher} that records every published
     * event in {@code received} without any synchronisation barrier.
     */
    private BusinessEventPublisher createCapturingPublisher(List<Event> received) {
        return new BusinessEventPublisher() {
            @Override
            public String getName() {
                return "capturing-publisher";
            }

            @Override
            public BusinessEventPublishResult publish(Event event) {
                received.add(event);
                return null;
            }
        };
    }

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
}
