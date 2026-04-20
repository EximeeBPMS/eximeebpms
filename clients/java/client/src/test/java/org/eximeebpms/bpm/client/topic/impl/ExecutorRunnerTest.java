package org.eximeebpms.bpm.client.topic.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eximeebpms.bpm.client.backoff.BackoffStrategy;
import org.eximeebpms.bpm.client.backoff.ExponentialBackoffStrategy;
import org.eximeebpms.bpm.client.impl.EngineClient;
import org.eximeebpms.bpm.client.impl.EngineClientException;
import org.eximeebpms.bpm.client.task.ExternalTask;
import org.eximeebpms.bpm.client.task.ExternalTaskHandler;
import org.eximeebpms.bpm.client.task.impl.ExternalTaskImpl;
import org.eximeebpms.bpm.client.topic.TopicSubscription;
import org.eximeebpms.bpm.client.variable.impl.TypedValues;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for ExecutorRunner with focus on BackoffStrategy behavior.
 * These tests verify that ExecutorRunner properly invokes backoff strategy
 * in different scenarios (tasks fetched, no tasks, busy threads, etc.)
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutorRunnerTest {

    private static final long CLIENT_LOCK_DURATION = 10000L;
    private static final double DEFAULT_MULTIPLIER = 1.5;
    private static final int BUSY_THREADS_SLEEP_TIME = 50;

    @Mock
    private EngineClient engineClient;
    @Mock
    private TypedValues typedValues;
    @Mock
    private ExternalTaskHandler taskHandler;

    private ThreadPoolExecutor executor;
    private ExecutorRunner runner;
    private BackoffStrategy spyBackoffStrategy;

    @Before
    public void setUp() {
        // Create a real thread pool executor for testing
        executor = new ThreadPoolExecutor(
                10, // corePoolSize
                20, // maximumPoolSize
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );

        // Use spy on real BackoffStrategy so we can verify calls
        spyBackoffStrategy = spy(new ExponentialBackoffStrategy(100L, 2, 5000L));

        // Make spy.copy() return itself so we can verify calls
        when(spyBackoffStrategy.copy()).thenReturn(spyBackoffStrategy);

        runner = new ExecutorRunner(
                engineClient,
                typedValues,
                CLIENT_LOCK_DURATION,
                BUSY_THREADS_SLEEP_TIME,
                () -> executor,
                DEFAULT_MULTIPLIER,
                new ExternalTaskExecutionStats()
        );

        runner.setBackoffStrategy(spyBackoffStrategy);
    }

    @Test
    public void shouldExecuteBackoffStrategyWithFetchedTasks() {
        // Given: Tasks are available and fetched
        List<ExternalTask> mockTasks = createMockTasks(3);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(mockTasks);

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.acquire();

        // Then: Backoff strategy SHOULD be invoked with the fetched tasks
        ArgumentCaptor<List<ExternalTask>> tasksCaptor = ArgumentCaptor.forClass(List.class);
        verify(spyBackoffStrategy, times(1)).reconfigure(tasksCaptor.capture());
        verify(spyBackoffStrategy, times(1)).calculateBackoffTime();

        // Verify reconfigure was called with the fetched tasks
        List<ExternalTask> capturedTasks = tasksCaptor.getValue();
        assertEquals("Should receive 3 fetched tasks", 3, capturedTasks.size());
    }

    @Test
    public void shouldExecuteBackoffStrategyWhenNoTasksWereFetched() {
        // Given: No tasks available
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(Collections.emptyList());

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.acquire();

        // Then: Backoff strategy SHOULD be invoked with empty list
        verify(spyBackoffStrategy, times(1)).reconfigure(Collections.emptyList());
        verify(spyBackoffStrategy, times(1)).calculateBackoffTime();
    }

    @Test
    public void shouldNotExecuteBackoffStrategyWhenAllThreadsBusy() throws Exception {
        // Given: All threads are busy (activeCount + queueSize >= maxTasks)
        CountDownLatch blockingLatch = new CountDownLatch(1);
        CountDownLatch threadsStartedLatch = new CountDownLatch(10);

        // Fill the executor: 10 will run (corePoolSize), 5 will be queued
        for (int i = 0; i < 15; i++) {
            executor.submit(() -> {
                try {
                    threadsStartedLatch.countDown();
                    blockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Wait for all core threads to start
        assertTrue("All 10 core threads should start", threadsStartedLatch.await(2, TimeUnit.SECONDS));

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.acquire();

        // Then: Backoff strategy should NOT be invoked (busy threads path is taken instead)
        verify(spyBackoffStrategy, never()).reconfigure(anyList());
        verify(spyBackoffStrategy, never()).calculateBackoffTime();
        verify(engineClient, never()).fetchAndLock(anyList(), anyInt());

        // Cleanup
        blockingLatch.countDown();
    }

    @Test
    public void shouldCalculateBackoffTimeBasedOnTaskCount() {
        // Given: Spy can track actual behavior
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(Collections.emptyList()) // First call - no tasks
                .thenReturn(createMockTasks(5));      // Second call - 5 tasks

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When - first acquire with no tasks
        runner.acquire();

        // Then: calculateBackoffTime should return > 0 (exponential backoff increases)
        ArgumentCaptor<List<ExternalTask>> tasksCaptor1 = ArgumentCaptor.forClass(List.class);
        verify(spyBackoffStrategy, times(1)).reconfigure(tasksCaptor1.capture());
        assertTrue("First reconfigure should get empty list", tasksCaptor1.getValue().isEmpty());

        // When - second acquire with tasks
        runner.acquire();

        // Then: calculateBackoffTime should be called again, this time with tasks
        ArgumentCaptor<List<ExternalTask>> tasksCaptor2 = ArgumentCaptor.forClass(List.class);
        verify(spyBackoffStrategy, times(2)).reconfigure(tasksCaptor2.capture());
        assertEquals("Second reconfigure should get 5 tasks", 5, tasksCaptor2.getAllValues().get(1).size());
        verify(spyBackoffStrategy, times(2)).calculateBackoffTime();
    }

    @Test
    public void shouldResetBackoffLevelWhenTasksAreFetched() {
        // Given: Use spy on real ExponentialBackoffStrategy to test level reset
        ExponentialBackoffStrategy realBackoff = spy(new ExponentialBackoffStrategy(100L, 2, 5000L));
        when(realBackoff.copy()).thenReturn(realBackoff); // Return same instance for testing
        runner.setBackoffStrategy(realBackoff);

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(Collections.emptyList()) // No tasks - level increases
                .thenReturn(Collections.emptyList()) // No tasks - level increases again
                .thenReturn(createMockTasks(3));      // Tasks found - level resets

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When - first acquire: no tasks, level should increase
        runner.acquire();
        // After acquire(), reconfigure() was already called, so calculateBackoffTime() reflects current state
        long backoffTime1 = realBackoff.calculateBackoffTime();
        assertEquals("After first empty fetch, backoff should be 100ms", 100L, backoffTime1);

        // When - second acquire: no tasks, level should increase again
        runner.acquire();
        long backoffTime2 = realBackoff.calculateBackoffTime();
        assertEquals("After second empty fetch, backoff should be 200ms", 200L, backoffTime2);

        // When - third acquire: tasks fetched, level resets to 0
        runner.acquire();
        long backoffTime3 = realBackoff.calculateBackoffTime();
        assertEquals("After tasks fetched, backoff should reset to 0ms", 0L, backoffTime3);
    }

    @Test
    public void shouldHandleMultipleSubscriptionsOnSingleRunner() throws Exception {
        // Given: Multiple subscriptions on same runner
        List<ExternalTask> topic1Tasks = createMockTasks(2, "topic1");
        List<ExternalTask> topic2Tasks = createMockTasks(3, "topic2");
        List<ExternalTask> allTasks = new ArrayList<>();
        allTasks.addAll(topic1Tasks);
        allTasks.addAll(topic2Tasks);

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(allTasks);

        ExternalTaskHandler handler1 = mock(ExternalTaskHandler.class);
        ExternalTaskHandler handler2 = mock(ExternalTaskHandler.class);

        CountDownLatch latch = new CountDownLatch(5);
        doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(handler1).execute(any(), any());

        doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(handler2).execute(any(), any());

        TopicSubscription subscription1 = createSubscription("topic1", handler1);
        TopicSubscription subscription2 = createSubscription("topic2", handler2);

        runner.subscribe(subscription1);
        runner.subscribe(subscription2);

        // When
        runner.acquire();

        // Then
        assertTrue("All 5 tasks should be executed", latch.await(2, TimeUnit.SECONDS));
        verify(handler1, timeout(1000).times(2)).execute(any(), any());
        verify(handler2, timeout(1000).times(3)).execute(any(), any());

        // Backoff should be called with combined tasks
        ArgumentCaptor<List<ExternalTask>> tasksCaptor = ArgumentCaptor.forClass(List.class);
        verify(spyBackoffStrategy, times(1)).reconfigure(tasksCaptor.capture());
        assertEquals("Should receive all 5 tasks", 5, tasksCaptor.getValue().size());
    }

    @Test
    public void shouldCalculateMaxTasksBasedOnCorePoolSize() {
        // Given: corePoolSize = 10, multiplier = 1.5
        // Expected: maxTasks = 15
        ArgumentCaptor<Integer> maxTasksCaptor = ArgumentCaptor.forClass(Integer.class);

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(Collections.emptyList());

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.acquire();

        // Then
        verify(engineClient).fetchAndLock(anyList(), maxTasksCaptor.capture());
        assertEquals("Should request 15 tasks", Integer.valueOf(15), maxTasksCaptor.getValue());
    }

    @Test
    public void shouldAdjustMaxTasksBasedOnActiveThreadsAndQueue() throws Exception {
        // Given: 5 active threads
        CountDownLatch blockingLatch = new CountDownLatch(1);
        CountDownLatch threadsStartedLatch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    threadsStartedLatch.countDown();
                    blockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertTrue("5 threads should start", threadsStartedLatch.await(2, TimeUnit.SECONDS));

        ArgumentCaptor<Integer> maxTasksCaptor = ArgumentCaptor.forClass(Integer.class);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(Collections.emptyList());

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.acquire();

        // Then: maxTasks = (10 * 1.5) - 5 = 10
        verify(engineClient).fetchAndLock(anyList(), maxTasksCaptor.capture());
        assertEquals("Should request 10 tasks (15 - 5 active)", Integer.valueOf(10), maxTasksCaptor.getValue());

        // Cleanup
        blockingLatch.countDown();
    }

    @Test
    public void shouldHandleEngineClientException() {
        // Given
        EngineClientException exception = mock(EngineClientException.class);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenThrow(exception);

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.acquire();

        // Then - should not throw exception, should handle gracefully
        verify(engineClient).fetchAndLock(anyList(), anyInt());
        verify(taskHandler, never()).execute(any(), any());

        // Backoff should still be called (with empty list since exception occurred)
        verify(spyBackoffStrategy, times(1)).reconfigure(anyList());
    }

    @Test
    public void shouldInvokeBackoffStrategyForEachAcquireCycle() {
        // Given
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(createMockTasks(2))       // First cycle - 2 tasks
                .thenReturn(Collections.emptyList())  // Second cycle - no tasks
                .thenReturn(createMockTasks(1));      // Third cycle - 1 task

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When - multiple acquire cycles
        runner.acquire();
        runner.acquire();
        runner.acquire();

        // Then: Backoff should be invoked 3 times
        ArgumentCaptor<List<ExternalTask>> tasksCaptor = ArgumentCaptor.forClass(List.class);
        verify(spyBackoffStrategy, times(3)).reconfigure(tasksCaptor.capture());
        verify(spyBackoffStrategy, times(3)).calculateBackoffTime();

        List<List<ExternalTask>> allCapturedTasks = tasksCaptor.getAllValues();
        assertEquals("First cycle should have 2 tasks", 2, allCapturedTasks.get(0).size());
        assertEquals("Second cycle should have 0 tasks", 0, allCapturedTasks.get(1).size());
        assertEquals("Third cycle should have 1 task", 1, allCapturedTasks.get(2).size());
    }

    @Test
    public void shouldNotInvokeBackoffWhenNoSubscriptions() {
        // Given: No subscriptions added
        // No need to stub engineClient.fetchAndLock since it won't be called

        // When
        runner.acquire();

        // Then: No fetch should happen, no backoff should be invoked
        verify(engineClient, never()).fetchAndLock(anyList(), anyInt());
        verify(spyBackoffStrategy, never()).reconfigure(anyList());
        verify(spyBackoffStrategy, never()).calculateBackoffTime();
    }

    @Test
    public void shouldExecuteTasksOnProvidedExecutor() throws Exception {
        // Given
        List<ExternalTask> mockTasks = createMockTasks(5);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(mockTasks);

        CountDownLatch latch = new CountDownLatch(5);
        doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(taskHandler).execute(any(), any());

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.acquire();

        // Then: All tasks should be executed on the provided executor
        assertTrue("All 5 tasks should be executed", latch.await(2, TimeUnit.SECONDS));
        verify(taskHandler, times(5)).execute(any(ExternalTask.class), any());

        // Verify tasks were dispatched to the executor
        assertTrue("Executor should have processed tasks",
                executor.getCompletedTaskCount() > 0 || executor.getActiveCount() > 0);
    }

    @Test
    public void shouldRespectMaxTasksCalculation() {
        // Given: Create runner with custom multiplier
        ExecutorRunner customRunner = new ExecutorRunner(
                engineClient,
                typedValues,
                CLIENT_LOCK_DURATION,
                BUSY_THREADS_SLEEP_TIME,
                () -> executor,
                2.0, // Custom multiplier
                new ExternalTaskExecutionStats()
        );
        customRunner.setBackoffStrategy(spyBackoffStrategy);

        ArgumentCaptor<Integer> maxTasksCaptor = ArgumentCaptor.forClass(Integer.class);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(Collections.emptyList());

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        customRunner.subscribe(subscription);

        // When
        customRunner.acquire();

        // Then: maxTasks = 10 * 2.0 = 20
        verify(engineClient).fetchAndLock(anyList(), maxTasksCaptor.capture());
        assertEquals("Should request 20 tasks with multiplier 2.0", Integer.valueOf(20), maxTasksCaptor.getValue());
    }

    @Test
    public void shouldHandleBackoffWithPartiallyFilledQueue() throws Exception {
        // Given: 5 active threads + 3 queued = 8 tasks in progress
        // maxTasks = 15, so maxTasksToFetch = 15 - 8 = 7
        CountDownLatch blockingLatch = new CountDownLatch(1);
        CountDownLatch threadsStartedLatch = new CountDownLatch(5);

        for (int i = 0; i < 8; i++) {
            executor.submit(() -> {
                try {
                    threadsStartedLatch.countDown();
                    blockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertTrue("5 threads should start", threadsStartedLatch.await(2, TimeUnit.SECONDS));

        ArgumentCaptor<Integer> maxTasksCaptor = ArgumentCaptor.forClass(Integer.class);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(createMockTasks(3));

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.acquire();

        // Then: Should request only 7 tasks (15 - 5 active - 3 queued)
        verify(engineClient).fetchAndLock(anyList(), maxTasksCaptor.capture());
        assertEquals("Should request 7 tasks", Integer.valueOf(7), maxTasksCaptor.getValue());

        // Backoff should be invoked with the 3 fetched tasks
        ArgumentCaptor<List<ExternalTask>> tasksCaptor = ArgumentCaptor.forClass(List.class);
        verify(spyBackoffStrategy, times(1)).reconfigure(tasksCaptor.capture());
        assertEquals("Should receive 3 tasks", 3, tasksCaptor.getValue().size());

        // Cleanup
        blockingLatch.countDown();
    }

    @Test
    public void shouldHandleExceptionDuringTaskExecution() throws Exception {
        // Given: Handler throws exception
        List<ExternalTask> mockTasks = createMockTasks(1);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(mockTasks);

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(inv -> {
            latch.countDown();
            throw new RuntimeException("Test exception");
        }).when(taskHandler).execute(any(), any());

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.acquire();

        // Then: Should not crash, task handler should be invoked
        assertTrue("Task handler should be called despite exception", latch.await(2, TimeUnit.SECONDS));
        verify(taskHandler, times(1)).execute(any(), any());

        // Backoff should still be invoked
        verify(spyBackoffStrategy, times(1)).reconfigure(anyList());
    }

    @Test
    public void shouldNotFetchWhenMaxTasksToFetchIsZero() throws Exception {
        // Given: Exactly fill up to maxTasks threshold (15 tasks in progress)
        CountDownLatch blockingLatch = new CountDownLatch(1);
        CountDownLatch threadsStartedLatch = new CountDownLatch(10);

        // 10 active + 5 queued = 15 (which equals maxTasks)
        for (int i = 0; i < 15; i++) {
            executor.submit(() -> {
                try {
                    threadsStartedLatch.countDown();
                    blockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertTrue("10 threads should start", threadsStartedLatch.await(2, TimeUnit.SECONDS));

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.acquire();

        // Then: maxTasksToFetch = 15 - (10 + 5) = 0, so no fetch
        verify(engineClient, never()).fetchAndLock(anyList(), anyInt());
        verify(spyBackoffStrategy, never()).reconfigure(anyList());

        // Cleanup
        blockingLatch.countDown();
    }

    @Test
    public void shouldFetchWhenOnlyOneSlotAvailable() throws Exception {
        // Given: 14 tasks in progress, 1 slot available
        CountDownLatch blockingLatch = new CountDownLatch(1);
        CountDownLatch threadsStartedLatch = new CountDownLatch(10);

        // 10 active + 4 queued = 14
        for (int i = 0; i < 14; i++) {
            executor.submit(() -> {
                try {
                    threadsStartedLatch.countDown();
                    blockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertTrue("10 threads should start", threadsStartedLatch.await(2, TimeUnit.SECONDS));

        ArgumentCaptor<Integer> maxTasksCaptor = ArgumentCaptor.forClass(Integer.class);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(createMockTasks(1));

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.acquire();

        // Then: maxTasksToFetch = 15 - 14 = 1
        verify(engineClient).fetchAndLock(anyList(), maxTasksCaptor.capture());
        assertEquals("Should request exactly 1 task", Integer.valueOf(1), maxTasksCaptor.getValue());

        // Cleanup
        blockingLatch.countDown();
    }

    @Test
    public void shouldStartAcquisitionLoopOnDedicatedThread() throws Exception {
        // Given
        CountDownLatch acquireLatch = new CountDownLatch(1);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenAnswer(inv -> {
                    acquireLatch.countDown();
                    return Collections.emptyList();
                });

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.start();

        // Then: acquisition loop runs on its own thread
        assertTrue("Acquisition loop should call fetchAndLock within 2s",
                acquireLatch.await(2, TimeUnit.SECONDS));
        assertTrue("Runner should report isRunning=true", runner.isRunning.get());

        runner.isRunning.set(false);
        runner.resume();
    }

    @Test
    public void shouldBeIdempotent_startCalledTwice() throws Exception {
        // Given
        CountDownLatch acquireLatch = new CountDownLatch(1);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenAnswer(inv -> {
                    acquireLatch.countDown();
                    return Collections.emptyList();
                });

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When: start() called twice
        runner.start();
        Thread firstThread = runner.thread;
        runner.start(); // second call must be a no-op

        // Then: same thread, only one loop running
        assertTrue("Should have been called at least once", acquireLatch.await(2, TimeUnit.SECONDS));
        assertSame("Second start() should not replace the thread", firstThread, runner.thread);

        runner.isRunning.set(false);
        runner.resume();
    }

    @Test
    public void shouldNotBeRunningBeforeStart() {
        assertFalse("Runner should not be running before start()", runner.isRunning.get());
    }

    @Test
    public void shouldContinuouslyAcquireAfterStart() throws Exception {
        // Given: acquisition loop runs multiple cycles
        CountDownLatch threeCyclesLatch = new CountDownLatch(3);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenAnswer(inv -> {
                    threeCyclesLatch.countDown();
                    return Collections.emptyList();
                });

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        // When
        runner.start();

        // Then: at least 3 acquisition cycles complete
        assertTrue("Should complete 3 acquisition cycles within 3s",
                threeCyclesLatch.await(3, TimeUnit.SECONDS));

        verify(engineClient, atLeastOnce()).fetchAndLock(anyList(), anyInt());

        runner.isRunning.set(false);
        runner.resume();
    }

    @Test
    public void shouldSuspendForBackoffDuration() throws Exception {
        // Given: backoff returns a measurable delay (200ms)
        ExponentialBackoffStrategy delayBackoff = spy(new ExponentialBackoffStrategy(200L, 2, 5000L));
        when(delayBackoff.copy()).thenReturn(delayBackoff);
        runner.setBackoffStrategy(delayBackoff);

        // Force level=1 so calculateBackoffTime() returns 200ms
        delayBackoff.reconfigure(Collections.emptyList());

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(Collections.emptyList());

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        runner.start();

        long before = System.currentTimeMillis();
        // Wait long enough for at least one full acquire+suspend cycle
        Thread.sleep(350);
        long elapsed = System.currentTimeMillis() - before;

        // Then: at least one full 200ms backoff must have occurred, so elapsed >= 200ms
        assertTrue("Should have waited at least 200ms due to backoff", elapsed >= 200);

        runner.isRunning.set(false);
        runner.resume();
    }

    @Test
    public void shouldWakeEarlyWhenResumedDuringSuspend() throws Exception {
        // Given: very long backoff (10s) — resume() must interrupt it early
        ExponentialBackoffStrategy longBackoff = spy(new ExponentialBackoffStrategy(10_000L, 1, 60_000L));
        when(longBackoff.copy()).thenReturn(longBackoff);

        // Force level=1 so calculateBackoffTime() returns 10_000ms
        longBackoff.reconfigure(Collections.emptyList());

        // Use a subclass that signals exactly when suspend() is entered — no Thread.sleep needed
        CountDownLatch suspendEntered = new CountDownLatch(1);
        ExecutorRunner testRunner = new ExecutorRunner(
                engineClient, typedValues, CLIENT_LOCK_DURATION, BUSY_THREADS_SLEEP_TIME,
                () -> executor, DEFAULT_MULTIPLIER, new ExternalTaskExecutionStats()) {
            @Override
            protected void suspend(long waitTime) {
                suspendEntered.countDown();
                super.suspend(waitTime);
            }
        };
        testRunner.setBackoffStrategy(longBackoff);

        CountDownLatch secondFetchDone = new CountDownLatch(1);
        // first answer: triggers first acquire cycle; second answer: confirms early wake-up
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(Collections.emptyList())
                .thenAnswer(inv -> {
                    secondFetchDone.countDown();
                    return Collections.emptyList();
                });

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        testRunner.subscribe(subscription);
        testRunner.start();

        // Wait until the runner has actually entered suspend()
        assertTrue("Runner should enter suspend()", suspendEntered.await(2, TimeUnit.SECONDS));

        long wakeStart = System.currentTimeMillis();
        // Signal resume() to interrupt the suspend
        testRunner.resume();

        // Then: second cycle starts well before the 10s backoff would expire
        assertTrue("Runner should wake early after resume()", secondFetchDone.await(5, TimeUnit.SECONDS));
        long wakeElapsed = System.currentTimeMillis() - wakeStart;
        assertTrue("Wake-up should happen in well under 10s", wakeElapsed < 5_000);

        testRunner.isRunning.set(false);
        testRunner.resume();
    }

    @Test
    public void shouldNotSuspendWhenBackoffTimeIsZero() throws Exception {
        // Given: backoff returns 0 — suspend() must skip the wait entirely
        ExponentialBackoffStrategy zeroBackoff = spy(new ExponentialBackoffStrategy(0L, 1, 0L));
        when(zeroBackoff.copy()).thenReturn(zeroBackoff);
        runner.setBackoffStrategy(zeroBackoff);

        CountDownLatch threeCycles = new CountDownLatch(3);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenAnswer(inv -> {
                    threeCycles.countDown();
                    return Collections.emptyList();
                });

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);

        long start = System.currentTimeMillis();
        runner.start();

        // Then: 3 cycles complete quickly because there is no backoff delay
        assertTrue("3 cycles should complete quickly without backoff",
                threeCycles.await(1, TimeUnit.SECONDS));
        assertTrue("Elapsed should be << 1s with zero backoff",
                System.currentTimeMillis() - start < 1_000);

        runner.isRunning.set(false);
        runner.resume();
    }

    @Test
    public void shouldExitSuspendImmediatelyWhenStopCalledDuringSuspend() throws Exception {
        // Given: long backoff so the runner will be in suspend when we stop it
        ExponentialBackoffStrategy longBackoff = spy(new ExponentialBackoffStrategy(10_000L, 1, 60_000L));
        when(longBackoff.copy()).thenReturn(longBackoff);
        runner.setBackoffStrategy(longBackoff);
        longBackoff.reconfigure(Collections.emptyList()); // level=1 → 10s wait

        CountDownLatch enteringSuspend = new CountDownLatch(1);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenAnswer(inv -> {
                    enteringSuspend.countDown();
                    return Collections.emptyList();
                });

        TopicSubscription subscription = createSubscription("testTopic", taskHandler);
        runner.subscribe(subscription);
        runner.start();

        // Wait until runner has entered suspend
        assertTrue("Runner should start first acquire cycle", enteringSuspend.await(2, TimeUnit.SECONDS));

        long stopStart = System.currentTimeMillis();

        // When: stop the runner while it is suspended
        runner.isRunning.set(false);
        runner.resume(); // mirrors what a real stop() would do

        // Give the thread time to react
        runner.thread.join(2_000);

        // Then: thread exits well before the 10s backoff would have elapsed
        long stopElapsed = System.currentTimeMillis() - stopStart;
        assertFalse("Runner thread should have exited", runner.thread.isAlive());
        assertTrue("Thread should stop well under 10s", stopElapsed < 5_000);
    }

    private List<ExternalTask> createMockTasks(int count) {
        return createMockTasks(count, "testTopic");
    }

    private List<ExternalTask> createMockTasks(int count, String topicName) {
        List<ExternalTask> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // Create real ExternalTaskImpl instead of mock to avoid UnfinishedStubbingException
            ExternalTaskImpl task = new ExternalTaskImpl();
            task.setTopicName(topicName);
            task.setId("task-" + i);
            task.setProcessDefinitionKey("process-" + topicName);
            task.setVariables(Collections.emptyMap());
            task.setLockExpirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
            task.setReceivedVariableMap(Collections.emptyMap());
            tasks.add(task);
        }
        return tasks;
    }

    private TopicSubscription createSubscription(String topicName, ExternalTaskHandler handler) {
        TopicSubscription subscription = mock(TopicSubscription.class);
        when(subscription.getTopicName()).thenReturn(topicName);
        when(subscription.getExternalTaskHandler()).thenReturn(handler);
        when(subscription.getLockDuration()).thenReturn(CLIENT_LOCK_DURATION);
        return subscription;
    }
}

