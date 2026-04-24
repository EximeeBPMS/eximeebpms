package org.eximeebpms.bpm.client.topic.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eximeebpms.bpm.client.backoff.BackoffStrategy;
import org.eximeebpms.bpm.client.backoff.ExponentialBackoffStrategy;
import org.eximeebpms.bpm.client.impl.EngineClient;
import org.eximeebpms.bpm.client.task.ExternalTaskHandler;
import org.eximeebpms.bpm.client.task.ExternalTaskHandlerWithSpecificExecutor;
import org.eximeebpms.bpm.client.topic.TopicSubscription;
import org.eximeebpms.bpm.client.variable.impl.TypedValues;
import org.junit.Test;

/**
 * Test to verify that each ExecutorRunner gets its own isolated copy of BackoffStrategy,
 * ensuring that runners don't interfere with each other's backoff state.
 */
public class BackoffStrategyIsolationTest {

    private static final long CLIENT_LOCK_DURATION = 10000L;

    @Test
    public void shouldProvideIsolatedBackoffStrategyCopiesToEachRunner() {
        // Given: A shared BackoffStrategy instance
        ExponentialBackoffStrategy sharedStrategy = new ExponentialBackoffStrategy(100L, 2, 5000L);

        EngineClient engineClient = mock(EngineClient.class);
        TypedValues typedValues = mock(TypedValues.class);

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(Collections.emptyList());

        // Create two executors
        ThreadPoolExecutor executor1 = new ThreadPoolExecutor(5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        ThreadPoolExecutor executor2 = new ThreadPoolExecutor(5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        TopicSubscriptionManager manager = new TopicSubscriptionManager(
                engineClient, typedValues, CLIENT_LOCK_DURATION, executor1, 1.5, false
        );

        manager.setBackoffStrategy(sharedStrategy);

        // Subscribe to default executor
        TopicSubscription defaultSubscription = createSubscription("topic1", mock(ExternalTaskHandler.class));
        manager.subscribe(defaultSubscription);

        // Subscribe to custom executor
        ExternalTaskHandlerWithSpecificExecutor customHandler = mock(ExternalTaskHandlerWithSpecificExecutor.class);
        when(customHandler.getThreadPoolExecutor()).thenReturn(executor2);
        TopicSubscription customSubscription = createSubscription("topic2", customHandler);
        manager.subscribe(customSubscription);

        // Get the runners - they should be created now
        assertEquals("Should have 2 runners (one per executor)", 2, manager.runnersByExecutor.size());

        ExecutorRunner runner1 = manager.runnersByExecutor.values().stream().findFirst().orElse(null);
        ExecutorRunner runner2 = manager.runnersByExecutor.values().stream()
                .filter(r -> r != runner1).findFirst().orElse(null);

        // Then: Each runner should have its own BackoffStrategy instance
        BackoffStrategy strategy1 = runner1.backoffStrategy;
        BackoffStrategy strategy2 = runner2.backoffStrategy;

        assertNotSame("Runners should have different BackoffStrategy instances", strategy1, strategy2);
        assertNotSame("Runner 1 should not share BackoffStrategy with original", sharedStrategy, strategy1);
        assertNotSame("Runner 2 should not share BackoffStrategy with original", sharedStrategy, strategy2);

        // Verify they behave independently by triggering different states manually
        // Without calling manager.acquire() which would affect both
        strategy1.reconfigure(Collections.emptyList()); // level 1
        strategy2.reconfigure(Collections.emptyList()); // level 1
        strategy2.reconfigure(Collections.emptyList()); // level 2 (one more call)

        long backoff1 = strategy1.calculateBackoffTime(); // Should be 100ms (level 1)
        long backoff2 = strategy2.calculateBackoffTime(); // Should be 200ms (level 2)

        assertEquals("Runner 1 backoff should be 100ms", 100L, backoff1);
        assertEquals("Runner 2 backoff should be 200ms", 200L, backoff2);

        // Cleanup
        executor1.shutdown();
        executor2.shutdown();
    }

    @Test
    public void shouldCreateNewCopyWhenSettingBackoffStrategyAfterRunnersCreated() {
        // Given: Manager with existing runners
        EngineClient engineClient = mock(EngineClient.class);
        TypedValues typedValues = mock(TypedValues.class);

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(Collections.emptyList());

        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        TopicSubscriptionManager manager = new TopicSubscriptionManager(
                engineClient, typedValues, CLIENT_LOCK_DURATION, executor, 1.5, false
        );

        ExponentialBackoffStrategy initialStrategy = new ExponentialBackoffStrategy(100L, 2, 5000L);
        manager.setBackoffStrategy(initialStrategy);

        TopicSubscription subscription = createSubscription("topic1", mock(ExternalTaskHandler.class));
        manager.subscribe(subscription);

        ExecutorRunner runner = manager.runnersByExecutor.values().iterator().next();
        BackoffStrategy runnerStrategy1 = runner.backoffStrategy;

        // When: Change the BackoffStrategy on the manager
        ExponentialBackoffStrategy newStrategy = new ExponentialBackoffStrategy(500L, 3, 10000L);
        manager.setBackoffStrategy(newStrategy);

        // Then: Runner should have a NEW copy (different instance)
        BackoffStrategy runnerStrategy2 = runner.backoffStrategy;

        assertNotSame("Runner should receive a new BackoffStrategy instance", runnerStrategy1, runnerStrategy2);
        assertNotSame("Runner should not share the new strategy", newStrategy, runnerStrategy2);

        // Verify the new strategy has different configuration
        runnerStrategy2.reconfigure(Collections.emptyList());
        long backoffTime = runnerStrategy2.calculateBackoffTime();
        assertEquals("New strategy should use 500ms base", 500L, backoffTime);

        // Cleanup
        executor.shutdown();
    }

    private TopicSubscription createSubscription(String topicName, ExternalTaskHandler handler) {
        TopicSubscription subscription = mock(TopicSubscription.class);
        when(subscription.getTopicName()).thenReturn(topicName);
        when(subscription.getExternalTaskHandler()).thenReturn(handler);
        when(subscription.getLockDuration()).thenReturn(CLIENT_LOCK_DURATION);
        return subscription;
    }
}


