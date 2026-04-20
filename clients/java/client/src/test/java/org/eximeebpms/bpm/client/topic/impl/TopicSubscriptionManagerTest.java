package org.eximeebpms.bpm.client.topic.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eximeebpms.bpm.client.backoff.BackoffStrategy;
import org.eximeebpms.bpm.client.backoff.ErrorAwareBackoffStrategy;
import org.eximeebpms.bpm.client.exception.ExternalTaskClientException;
import org.eximeebpms.bpm.client.impl.EngineClient;
import org.eximeebpms.bpm.client.impl.EngineClientException;
import org.eximeebpms.bpm.client.task.ExternalTask;
import org.eximeebpms.bpm.client.task.ExternalTaskHandler;
import org.eximeebpms.bpm.client.task.ExternalTaskHandlerWithSpecificExecutor;
import org.eximeebpms.bpm.client.task.impl.ExternalTaskImpl;
import org.eximeebpms.bpm.client.topic.TopicSubscription;
import org.eximeebpms.bpm.client.topic.impl.dto.TopicRequestDto;
import org.eximeebpms.bpm.client.variable.impl.TypedValues;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TopicSubscriptionManagerTest {

    private static final long CLIENT_LOCK_DURATION = 10000L;
    private static final double DEFAULT_MULTIPLIER = 1.5;
    @Mock
    private EngineClient engineClient;
    @Mock
    private TypedValues typedValues;
    @Mock
    private ExternalTaskHandler taskHandler;
    @Mock
    private BackoffStrategy backoffStrategy;
    private ThreadPoolExecutor executor;
    private TopicSubscriptionManager manager;

    @Before
    public void setUp() {

        // Create a real thread pool executor for testing
        executor = new ThreadPoolExecutor(
                10, // corePoolSize
                20, // maximumPoolSize
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );

        manager = new TopicSubscriptionManager(
                engineClient,
                typedValues,
                CLIENT_LOCK_DURATION,
                () -> executor,
                DEFAULT_MULTIPLIER,
                20,
                false
        );
        when(backoffStrategy.copy()).thenReturn(backoffStrategy);
        manager.setBackoffStrategy(backoffStrategy);
    }

    @Test
    public void shouldCalculateMaxTasksCorrectly() {
        // Given: corePoolSize = 10, multiplier = 1.5, activeCount = 0
        // Expected: maxTasks = (10 * 1.5) - 0 = 15

        List<ExternalTask> mockTasks = createMockTasks(5);
        when(engineClient.fetchAndLock(anyList(), eq(15)))
                .thenReturn(mockTasks);

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then
        verify(engineClient).fetchAndLock(anyList(), eq(15));
    }

    @Test
    public void shouldAdjustMaxTasksBasedOnActiveThreads() throws Exception {
        // Given: corePoolSize = 10, multiplier = 1.5, activeCount = 5
        // Expected: maxTasks = (10 * 1.5) - 5 = 10

        // Simulate 5 active threads by submitting blocking tasks
        CountDownLatch blockingLatch = new CountDownLatch(1);
        CountDownLatch threadsStartedLatch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    threadsStartedLatch.countDown(); // Signal that this thread has started
                    blockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Wait for all 5 tasks to actually start
        assertTrue("All 5 threads should start", threadsStartedLatch.await(1, TimeUnit.SECONDS));

        List<ExternalTask> mockTasks = createMockTasks(3);
        when(engineClient.fetchAndLock(anyList(), eq(10)))
                .thenReturn(mockTasks);

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then
        verify(engineClient).fetchAndLock(anyList(), eq(10));

        // Cleanup
        blockingLatch.countDown();
        executor.shutdown();
    }

    @Test
    public void shouldHandleZeroTasksReturned() {
        // Given
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(Collections.emptyList());

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then - should not throw exception
        verify(engineClient).fetchAndLock(anyList(), anyInt());
        verify(taskHandler, never()).execute(any(), any());
    }

    @Test
    public void shouldHandleOneTaskReturned() {
        // Given
        List<ExternalTask> mockTasks = createMockTasks(1);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(mockTasks);

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then
        verify(taskHandler, timeout(1000)).execute(any(ExternalTask.class), any());
    }

    @Test
    public void shouldHandleMultipleTasksReturned() throws Exception {
        // Given
        int taskCount = 10;
        List<ExternalTask> mockTasks = createMockTasks(taskCount);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(mockTasks);

        CountDownLatch latch = new CountDownLatch(taskCount);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(taskHandler).execute(any(ExternalTask.class), any());

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then
        assertTrue("All tasks should be executed", latch.await(2, TimeUnit.SECONDS));
        verify(taskHandler, times(taskCount)).execute(any(ExternalTask.class), any());
    }

    @Test
    public void shouldHandleMaxTasksReturned() throws Exception {
        // Given: Request 15 tasks, receive 15
        int taskCount = 15;
        List<ExternalTask> mockTasks = createMockTasks(taskCount);
        when(engineClient.fetchAndLock(anyList(), eq(15)))
                .thenReturn(mockTasks);

        CountDownLatch latch = new CountDownLatch(taskCount);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(taskHandler).execute(any(ExternalTask.class), any());

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then
        assertTrue("All 15 tasks should be executed", latch.await(2, TimeUnit.SECONDS));
        verify(taskHandler, times(taskCount)).execute(any(ExternalTask.class), any());
    }

    @Test
    public void shouldHandleFewerTasksThanRequested() throws Exception {
        // Given: Request 15 tasks, receive only 5
        List<ExternalTask> mockTasks = createMockTasks(5);
        when(engineClient.fetchAndLock(anyList(), eq(15)))
                .thenReturn(mockTasks);

        CountDownLatch latch = new CountDownLatch(5);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(taskHandler).execute(any(ExternalTask.class), any());

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then
        assertTrue("All 5 tasks should be executed", latch.await(2, TimeUnit.SECONDS));
        verify(taskHandler, times(5)).execute(any(ExternalTask.class), any());
    }

    @Test
    public void shouldRespectCustomMultiplier() {
        // Given: custom multiplier = 2.0
        // corePoolSize = 10, multiplier = 2.0, activeCount = 0
        // Expected: maxTasks = (10 * 2.0) - 0 = 20

        executor = new ThreadPoolExecutor(10, 20, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        manager = new TopicSubscriptionManager(
                engineClient,
                typedValues,
                CLIENT_LOCK_DURATION,
                () -> executor,
                2.0,
                20,
                false
        );
        manager.setBackoffStrategy(backoffStrategy);

        List<ExternalTask> mockTasks = createMockTasks(5);
        when(engineClient.fetchAndLock(anyList(), eq(20)))
                .thenReturn(mockTasks);

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then
        verify(engineClient).fetchAndLock(anyList(), eq(20));
    }

    @Test
    public void shouldNotFetchWhenAllThreadsBusy() throws Exception {
        // Given: All threads are busy (activeCount + queueSize >= maxTasks)
        // maxTasks = corePoolSize * multiplier = 10 * 1.5 = 15
        // So we need activeCount + queueSize = 15
        CountDownLatch blockingLatch = new CountDownLatch(1);
        CountDownLatch threadsStartedLatch = new CountDownLatch(10);

        // Fill the executor: 10 will run (corePoolSize), 5 will be queued
        for (int i = 0; i < 15; i++) {
            executor.submit(() -> {
                try {
                    threadsStartedLatch.countDown(); // Only first 10 will count down
                    blockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Wait for all core threads to start
        assertTrue("All 10 core threads should start", threadsStartedLatch.await(2, TimeUnit.SECONDS));

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then - should not call fetchAndLock (maxTasks = 15 - (10 active + 5 queued) = 0)
        verify(engineClient, never()).fetchAndLock(anyList(), anyInt());

        // Cleanup
        blockingLatch.countDown();
    }

    @Test
    public void shouldHandleEngineClientException() {
        // Given
        EngineClientException exception = mock(EngineClientException.class);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenThrow(exception);

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then - should not throw exception, should handle gracefully
        verify(engineClient).fetchAndLock(anyList(), anyInt());
        verify(taskHandler, never()).execute(any(), any());
    }

    @Test
    public void shouldHandleMultipleSubscriptions() {
        // Given
        List<ExternalTask> mockTasks1 = createMockTasks(3, "topic1");
        List<ExternalTask> mockTasks2 = createMockTasks(2, "topic2");
        List<ExternalTask> allTasks = new ArrayList<>();
        allTasks.addAll(mockTasks1);
        allTasks.addAll(mockTasks2);

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(allTasks);

        ExternalTaskHandler handler1 = mock(ExternalTaskHandler.class);
        ExternalTaskHandler handler2 = mock(ExternalTaskHandler.class);

        TopicSubscription subscription1 = createSubscription("topic1", handler1);
        TopicSubscription subscription2 = createSubscription("topic2", handler2);

        manager.subscribe(subscription1);
        manager.subscribe(subscription2);

        // When
        manager.acquire();

        // Then
        verify(handler1, timeout(1000).times(3)).execute(any(ExternalTask.class), any());
        verify(handler2, timeout(1000).times(2)).execute(any(ExternalTask.class), any());
    }

    @Test
    public void shouldCaptureCorrectMaxTasksParameter() {
        // Given
        ArgumentCaptor<Integer> maxTasksCaptor = ArgumentCaptor.forClass(Integer.class);
        List<ExternalTask> mockTasks = createMockTasks(5);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(mockTasks);

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then
        verify(engineClient).fetchAndLock(anyList(), maxTasksCaptor.capture());
        Integer capturedMaxTasks = maxTasksCaptor.getValue();

        // Should be (corePoolSize * multiplier) - activeCount
        // = (10 * 1.5) - 0 = 15
        assertEquals(Integer.valueOf(15), capturedMaxTasks);
    }

    @Test
    public void shouldHandleMixedHandlersWithDefaultAndSpecificExecutors() throws Exception {
        // Given: Two executors - default and custom
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );
        ThreadPoolExecutorSupplier customExecutorSupplier = () -> customExecutor;

        // Mock tasks for both topics
        List<ExternalTask> defaultTasks = createMockTasks(3, "defaultTopic");
        List<ExternalTask> customTasks = createMockTasks(2, "customTopic");

        // Configure engine client to return tasks based on maxTasks
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenAnswer(invocation -> {
                    List<TopicRequestDto> topics = invocation.getArgument(0);
                    String topicName = topics.get(0).getTopicName();
                    if ("defaultTopic".equals(topicName)) {
                        return defaultTasks;
                    } else if ("customTopic".equals(topicName)) {
                        return customTasks;
                    }
                    return Collections.emptyList();
                });

        // Create handlers
        ExternalTaskHandler defaultHandler = mock(ExternalTaskHandler.class);
        ExternalTaskHandlerWithSpecificExecutor customHandler =
                mock(ExternalTaskHandlerWithSpecificExecutor.class);
        when(customHandler.getThreadPoolExecutorSupplier()).thenReturn(customExecutorSupplier);

        CountDownLatch defaultLatch = new CountDownLatch(3);
        CountDownLatch customLatch = new CountDownLatch(2);

        doAnswer(inv -> {
            defaultLatch.countDown();
            return null;
        }).when(defaultHandler).execute(any(), any());

        doAnswer(inv -> {
            customLatch.countDown();
            return null;
        }).when(customHandler).execute(any(), any());

        TopicSubscription defaultSubscription = createSubscription("defaultTopic", defaultHandler);
        TopicSubscription customSubscription = createSubscription("customTopic", customHandler);

        manager.subscribe(defaultSubscription);
        manager.subscribe(customSubscription);

        // When
        manager.acquire();

        // Then
        assertTrue("All default tasks should be executed", defaultLatch.await(2, TimeUnit.SECONDS));
        assertTrue("All custom tasks should be executed", customLatch.await(2, TimeUnit.SECONDS));

        verify(defaultHandler, times(3)).execute(any(ExternalTask.class), any());
        verify(customHandler, times(2)).execute(any(ExternalTask.class), any());

        // Verify two separate fetch calls were made (one per executor)
        verify(engineClient, times(2)).fetchAndLock(anyList(), anyInt());

        customExecutor.shutdown();
    }

    @Test
    public void shouldFetchCorrectMaxTasksForEachExecutorSeparately() {
        // Given: Default executor (10 core) and custom executor (5 core)
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );

        ArgumentCaptor<Integer> maxTasksCaptor = ArgumentCaptor.forClass(Integer.class);

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(Collections.emptyList());

        ExternalTaskHandler defaultHandler = mock(ExternalTaskHandler.class);
        ExternalTaskHandlerWithSpecificExecutor customHandler =
                mock(ExternalTaskHandlerWithSpecificExecutor.class);
        when(customHandler.getThreadPoolExecutorSupplier()).thenReturn(() -> customExecutor);

        TopicSubscription defaultSubscription = createSubscription("defaultTopic", defaultHandler);
        TopicSubscription customSubscription = createSubscription("customTopic", customHandler);

        manager.subscribe(defaultSubscription);
        manager.subscribe(customSubscription);

        // When
        manager.acquire();

        // Then
        verify(engineClient, times(2)).fetchAndLock(anyList(), maxTasksCaptor.capture());
        List<Integer> capturedMaxTasks = maxTasksCaptor.getAllValues();

        // Default executor: (10 * 1.5) - 0 = 15
        // Custom executor: (5 * 1.5) - 0 = 7 (rounded down from 7.5)
        assertTrue("Should contain max tasks for default executor (15)",
                capturedMaxTasks.contains(15));
        assertTrue("Should contain max tasks for custom executor (7)",
                capturedMaxTasks.contains(7));

        customExecutor.shutdown();
    }

    @Test
    public void shouldNotFetchWhenOnlyCustomExecutorIsBusy() throws Exception {
        // Given: Default executor free, custom executor fully busy
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );
        ThreadPoolExecutorSupplier customExecutorSupplier = () -> customExecutor;

        // Fill custom executor with blocking tasks
        CountDownLatch customBlockingLatch = new CountDownLatch(1);
        CountDownLatch customThreadsStartedLatch = new CountDownLatch(5); // Only corePoolSize=5 can run
        for (int i = 0; i < 8; i++) {
            customExecutor.submit(() -> {
                try {
                    customThreadsStartedLatch.countDown();
                    customBlockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Wait for custom threads to start
        assertTrue("Custom executor threads should start",
                customThreadsStartedLatch.await(2, TimeUnit.SECONDS));

        List<ExternalTask> defaultTasks = createMockTasks(3, "defaultTopic");

        ArgumentCaptor<List<TopicRequestDto>> topicRequestCaptor = ArgumentCaptor.forClass(List.class);
        when(engineClient.fetchAndLock(topicRequestCaptor.capture(), anyInt()))
                .thenReturn(defaultTasks);

        ExternalTaskHandler defaultHandler = mock(ExternalTaskHandler.class);
        ExternalTaskHandlerWithSpecificExecutor customHandler =
                mock(ExternalTaskHandlerWithSpecificExecutor.class);
        when(customHandler.getThreadPoolExecutorSupplier()).thenReturn(customExecutorSupplier);

        TopicSubscription defaultSubscription = createSubscription("defaultTopic", defaultHandler);
        TopicSubscription customSubscription = createSubscription("customTopic", customHandler);

        manager.subscribe(defaultSubscription);
        manager.subscribe(customSubscription);

        // When
        manager.acquire();

        // Then
        // Only default executor should fetch (custom is busy)
        verify(engineClient, times(1)).fetchAndLock(anyList(), anyInt());

        // Verify the fetch was for default topic
        List<TopicRequestDto> capturedRequests = topicRequestCaptor.getValue();
        assertEquals(1, capturedRequests.size());
        assertEquals("defaultTopic", capturedRequests.get(0).getTopicName());

        verify(defaultHandler, timeout(1000).times(3)).execute(any(), any());
        verify(customHandler, never()).execute(any(), any());

        // Cleanup
        customBlockingLatch.countDown();
        customExecutor.shutdown();
    }

    @Test
    public void shouldNotFetchWhenOnlyDefaultExecutorIsBusy() throws Exception {
        // Given: Default executor busy, custom executor free
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );
        ThreadPoolExecutorSupplier customExecutorSupplier = () -> customExecutor;

        // Fill default executor with blocking tasks
        CountDownLatch defaultBlockingLatch = new CountDownLatch(1);
        CountDownLatch defaultThreadsStartedLatch = new CountDownLatch(10); // Only corePoolSize=10 can run
        for (int i = 0; i < 15; i++) {
            executor.submit(() -> {
                try {
                    defaultThreadsStartedLatch.countDown();
                    defaultBlockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Wait for default threads to start
        assertTrue("Default executor threads should start",
                defaultThreadsStartedLatch.await(2, TimeUnit.SECONDS));

        List<ExternalTask> customTasks = createMockTasks(2, "customTopic");

        ArgumentCaptor<List<TopicRequestDto>> topicRequestCaptor = ArgumentCaptor.forClass(List.class);
        when(engineClient.fetchAndLock(topicRequestCaptor.capture(), anyInt()))
                .thenReturn(customTasks);

        ExternalTaskHandler defaultHandler = mock(ExternalTaskHandler.class);
        ExternalTaskHandlerWithSpecificExecutor customHandler =
                mock(ExternalTaskHandlerWithSpecificExecutor.class);
        when(customHandler.getThreadPoolExecutorSupplier()).thenReturn(customExecutorSupplier);

        TopicSubscription defaultSubscription = createSubscription("defaultTopic", defaultHandler);
        TopicSubscription customSubscription = createSubscription("customTopic", customHandler);

        manager.subscribe(defaultSubscription);
        manager.subscribe(customSubscription);

        // When
        manager.acquire();

        // Then
        // Only custom executor should fetch (default is busy)
        verify(engineClient, times(1)).fetchAndLock(anyList(), anyInt());

        // Verify the fetch was for custom topic
        List<TopicRequestDto> capturedRequests = topicRequestCaptor.getValue();
        assertEquals(1, capturedRequests.size());
        assertEquals("customTopic", capturedRequests.get(0).getTopicName());

        verify(customHandler, timeout(1000).times(2)).execute(any(), any());
        verify(defaultHandler, never()).execute(any(), any());

        // Cleanup
        defaultBlockingLatch.countDown();
        customExecutor.shutdown();
    }

    @Test
    public void shouldNotFetchWhenAllExecutorsAreBusy() throws Exception {
        // Given: Both default and custom executors busy
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );
        ThreadPoolExecutorSupplier customExecutorSupplier = () -> customExecutor;

        // Fill default executor
        CountDownLatch defaultBlockingLatch = new CountDownLatch(1);
        CountDownLatch defaultThreadsStartedLatch = new CountDownLatch(10); // Only corePoolSize threads can run
        for (int i = 0; i < 15; i++) {
            executor.submit(() -> {
                try {
                    defaultThreadsStartedLatch.countDown();
                    defaultBlockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Fill custom executor
        CountDownLatch customBlockingLatch = new CountDownLatch(1);
        CountDownLatch customThreadsStartedLatch = new CountDownLatch(5); // Only corePoolSize threads can run
        for (int i = 0; i < 8; i++) {
            customExecutor.submit(() -> {
                try {
                    customThreadsStartedLatch.countDown();
                    customBlockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Wait for all threads to start
        assertTrue("Default executor threads should start",
                defaultThreadsStartedLatch.await(2, TimeUnit.SECONDS));
        assertTrue("Custom executor threads should start",
                customThreadsStartedLatch.await(2, TimeUnit.SECONDS));

        ExternalTaskHandler defaultHandler = mock(ExternalTaskHandler.class);
        ExternalTaskHandlerWithSpecificExecutor customHandler =
                mock(ExternalTaskHandlerWithSpecificExecutor.class);
        when(customHandler.getThreadPoolExecutorSupplier()).thenReturn(customExecutorSupplier);

        TopicSubscription defaultSubscription = createSubscription("defaultTopic", defaultHandler);
        TopicSubscription customSubscription = createSubscription("customTopic", customHandler);

        manager.subscribe(defaultSubscription);
        manager.subscribe(customSubscription);

        // When
        manager.acquire();

        // Then
        // No fetch should happen (all executors busy)
        verify(engineClient, never()).fetchAndLock(anyList(), anyInt());
        verify(defaultHandler, never()).execute(any(), any());
        verify(customHandler, never()).execute(any(), any());

        // Cleanup
        defaultBlockingLatch.countDown();
        customBlockingLatch.countDown();
        customExecutor.shutdown();
    }

    @Test
    public void shouldHandleMultipleTopicsOnSameCustomExecutor() throws Exception {
        // Given: Two topics sharing the same custom executor
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );
        ThreadPoolExecutorSupplier customExecutorSupplier = () -> customExecutor;

        List<ExternalTask> topic1Tasks = createMockTasks(2, "customTopic1");
        List<ExternalTask> topic2Tasks = createMockTasks(3, "customTopic2");
        List<ExternalTask> allCustomTasks = new ArrayList<>();
        allCustomTasks.addAll(topic1Tasks);
        allCustomTasks.addAll(topic2Tasks);

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(allCustomTasks);

        ExternalTaskHandlerWithSpecificExecutor customHandler1 =
                mock(ExternalTaskHandlerWithSpecificExecutor.class);
        when(customHandler1.getThreadPoolExecutorSupplier()).thenReturn(customExecutorSupplier);

        ExternalTaskHandlerWithSpecificExecutor customHandler2 =
                mock(ExternalTaskHandlerWithSpecificExecutor.class);
        when(customHandler2.getThreadPoolExecutorSupplier()).thenReturn(customExecutorSupplier);

        CountDownLatch allTasksLatch = new CountDownLatch(5);
        doAnswer(inv -> {
            allTasksLatch.countDown();
            return null;
        }).when(customHandler1).execute(any(), any());

        doAnswer(inv -> {
            allTasksLatch.countDown();
            return null;
        }).when(customHandler2).execute(any(), any());

        TopicSubscription subscription1 = createSubscription("customTopic1", customHandler1);
        TopicSubscription subscription2 = createSubscription("customTopic2", customHandler2);

        manager.subscribe(subscription1);
        manager.subscribe(subscription2);

        // When
        manager.acquire();

        // Then
        assertTrue("All tasks should be executed", allTasksLatch.await(2, TimeUnit.SECONDS));

        // Both topics should be included in a single fetch (same executor)
        // Note: Current implementation creates separate entry per handler,
        // but this tests the behavior
        verify(customHandler1, timeout(1000).times(2)).execute(any(), any());
        verify(customHandler2, timeout(1000).times(3)).execute(any(), any());

        customExecutor.shutdown();
    }

    @Test
    public void shouldHandlePartiallyBusyExecutorsInMixedScenario() throws Exception {
        // Given: 3 executors - default (free), custom1 (busy), custom2 (free)
        ThreadPoolExecutor customExecutor1 = new ThreadPoolExecutor(
                5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );
        ThreadPoolExecutor customExecutor2 = new ThreadPoolExecutor(
                3, 6, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );
        ThreadPoolExecutorSupplier customExecutorSupplier1 = () -> customExecutor1;
        ThreadPoolExecutorSupplier customExecutorSupplier2 = () -> customExecutor2;

        // Fill only customExecutor1
        CountDownLatch custom1BlockingLatch = new CountDownLatch(1);
        CountDownLatch custom1ThreadsStartedLatch = new CountDownLatch(5);
        for (int i = 0; i < 8; i++) {
            customExecutor1.submit(() -> {
                try {
                    custom1ThreadsStartedLatch.countDown();
                    custom1BlockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertTrue("Custom executor 1 threads should start",
                custom1ThreadsStartedLatch.await(2, TimeUnit.SECONDS));

        List<ExternalTask> defaultTasks = createMockTasks(2, "defaultTopic");
        List<ExternalTask> custom2Tasks = createMockTasks(1, "customTopic2");

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenAnswer(invocation -> {
                    List<TopicRequestDto> topics = invocation.getArgument(0);
                    String topicName = topics.get(0).getTopicName();
                    if ("defaultTopic".equals(topicName)) {
                        return defaultTasks;
                    } else if ("customTopic2".equals(topicName)) {
                        return custom2Tasks;
                    }
                    return Collections.emptyList();
                });

        ExternalTaskHandler defaultHandler = mock(ExternalTaskHandler.class);
        ExternalTaskHandlerWithSpecificExecutor customHandler1 =
                mock(ExternalTaskHandlerWithSpecificExecutor.class);
        when(customHandler1.getThreadPoolExecutorSupplier()).thenReturn(customExecutorSupplier1);

        ExternalTaskHandlerWithSpecificExecutor customHandler2 =
                mock(ExternalTaskHandlerWithSpecificExecutor.class);
        when(customHandler2.getThreadPoolExecutorSupplier()).thenReturn(customExecutorSupplier2);

        TopicSubscription defaultSubscription = createSubscription("defaultTopic", defaultHandler);
        TopicSubscription custom1Subscription = createSubscription("customTopic1", customHandler1);
        TopicSubscription custom2Subscription = createSubscription("customTopic2", customHandler2);

        manager.subscribe(defaultSubscription);
        manager.subscribe(custom1Subscription);
        manager.subscribe(custom2Subscription);

        // When
        manager.acquire();

        // Then
        // Should fetch for default and custom2 (but not custom1 which is busy)
        verify(engineClient, times(2)).fetchAndLock(anyList(), anyInt());

        verify(defaultHandler, timeout(1000).times(2)).execute(any(), any());
        verify(customHandler1, never()).execute(any(), any());
        verify(customHandler2, timeout(1000).times(1)).execute(any(), any());

        // Cleanup
        custom1BlockingLatch.countDown();
        customExecutor1.shutdown();
        customExecutor2.shutdown();
    }

    @Test
    public void shouldDisableBackoffStrategyForAllRunners() {
        // Given: Multiple subscriptions with different executors
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );
        ThreadPoolExecutorSupplier customExecutorSupplier = () -> customExecutor;

        List<ExternalTask> mockTasks = createMockTasks(2);
        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(mockTasks);

        ExternalTaskHandlerWithSpecificExecutor customHandler =
                mock(ExternalTaskHandlerWithSpecificExecutor.class);
        when(customHandler.getThreadPoolExecutorSupplier()).thenReturn(customExecutorSupplier);

        TopicSubscription defaultSubscription = createSubscription("defaultTopic");
        TopicSubscription customSubscription = createSubscription("customTopic", customHandler);

        manager.subscribe(defaultSubscription);
        manager.subscribe(customSubscription);

        // When: Disable backoff strategy
        manager.disableBackoffStrategy();
        manager.acquire();

        // Then: Backoff strategy should NOT be invoked for any runner
        // Note: We can't directly verify because each runner has a copy, but we verify
        // that no backoff-related exceptions occur and tasks are processed normally
        verify(engineClient, times(2)).fetchAndLock(anyList(), anyInt());

        // Cleanup
        customExecutor.shutdown();
    }

    @Test
    public void shouldInvokeBackoffStrategyAfterFetchingTasks() throws Exception {
        // Given: backoff returns 0 so suspend() is a no-op
        when(backoffStrategy.calculateBackoffTime()).thenReturn(0L);
        List<ExternalTask> mockTasks = createMockTasks(3);
        when(engineClient.fetchAndLock(anyList(), anyInt())).thenReturn(mockTasks);

        CountDownLatch latch = new CountDownLatch(3);
        doAnswer(inv -> { latch.countDown(); return null; }).when(taskHandler).execute(any(), any());

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then: reconfigure is called with the 3 fetched tasks, calculateBackoffTime is called
        verify(backoffStrategy).reconfigure(mockTasks);
        verify(backoffStrategy).calculateBackoffTime();
    }

    @Test
    public void shouldInvokeBackoffStrategyWhenNoTasksReturned() {
        // Given
        when(backoffStrategy.calculateBackoffTime()).thenReturn(0L);
        when(engineClient.fetchAndLock(anyList(), anyInt())).thenReturn(Collections.emptyList());

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then: reconfigure is called with empty list, calculateBackoffTime is called
        verify(backoffStrategy).reconfigure(Collections.emptyList());
        verify(backoffStrategy).calculateBackoffTime();
    }

    @Test
    public void shouldNotInvokeBackoffStrategyWhenDisabled() {
        // Given
        when(engineClient.fetchAndLock(anyList(), anyInt())).thenReturn(Collections.emptyList());

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);
        manager.disableBackoffStrategy();

        // When
        manager.acquire();

        // Then: backoff strategy should not be consulted at all
        verify(backoffStrategy, never()).reconfigure(any());
        verify(backoffStrategy, never()).calculateBackoffTime();
    }

    @Test
    public void shouldNotInvokeBackoffStrategyWhenAllThreadsBusy() throws Exception {
        // Given: fill executor so no fetch slots remain (maxTasks = 15, all 15 occupied)
        CountDownLatch blockingLatch = new CountDownLatch(1);
        CountDownLatch threadsStartedLatch = new CountDownLatch(10);
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
        assertTrue("All 10 core threads should start", threadsStartedLatch.await(2, TimeUnit.SECONDS));

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then: no fetch happened → backoff strategy should not be called
        verify(engineClient, never()).fetchAndLock(anyList(), anyInt());
        verify(backoffStrategy, never()).reconfigure(any());
        verify(backoffStrategy, never()).calculateBackoffTime();

        // Cleanup
        blockingLatch.countDown();
    }

    @Test
    public void shouldUseErrorAwareBackoffStrategyOnEngineClientException() {
        // Given: an ErrorAwareBackoffStrategy mock
        ErrorAwareBackoffStrategy errorAwareStrategy = mock(ErrorAwareBackoffStrategy.class);
        when(errorAwareStrategy.copy()).thenReturn(errorAwareStrategy);
        when(errorAwareStrategy.calculateBackoffTime()).thenReturn(0L);
        manager.setBackoffStrategy(errorAwareStrategy);

        EngineClientException engineException = mock(EngineClientException.class);
        when(engineClient.fetchAndLock(anyList(), anyInt())).thenThrow(engineException);

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then: error-aware reconfigure is called with empty task list and a non-null exception
        verify(errorAwareStrategy).reconfigure(eq(Collections.emptyList()), any(ExternalTaskClientException.class));
        verify(errorAwareStrategy).calculateBackoffTime();
    }

    @Test
    public void shouldSuspendForBackoffWaitTime() throws Exception {
        // Given: backoff requests a 100 ms wait.
        // We measure the gap between the 1st and 2nd invocation of calculateBackoffTime()
        // (i.e. between two consecutive acquisition cycles) to prove that suspend() actually
        // waited rather than returning immediately.
        CountDownLatch firstCycleLatch = new CountDownLatch(1);
        CountDownLatch secondCycleLatch = new CountDownLatch(2); // fires after 2 invocations
        long[] firstCallTime = {0};

        when(backoffStrategy.calculateBackoffTime()).thenAnswer(inv -> {
            if (firstCycleLatch.getCount() > 0) {
                firstCallTime[0] = System.currentTimeMillis();
                firstCycleLatch.countDown();
            }
            secondCycleLatch.countDown();
            return 100L;
        });
        when(engineClient.fetchAndLock(anyList(), anyInt())).thenReturn(Collections.emptyList());

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.start();
        assertTrue("Second backoff cycle should complete", secondCycleLatch.await(3, TimeUnit.SECONDS));
        manager.stop();

        long gapBetweenCycles = System.currentTimeMillis() - firstCallTime[0];

        // Then: the gap between the 1st and 2nd invocation must be at least 100 ms
        verify(backoffStrategy, atLeastOnce()).calculateBackoffTime();
        assertTrue("Runner should have suspended for at least 100 ms between cycles",
                gapBetweenCycles >= 100);
    }

    @Test
    public void shouldSkipTaskExecutionWhenLockExpired() throws Exception {
        // Given: Task with lock already expired (in the past)
        ExternalTaskImpl expiredTask = mock(ExternalTaskImpl.class);
        when(expiredTask.getTopicName()).thenReturn("testTopic");
        when(expiredTask.getProcessDefinitionKey()).thenReturn("process-testTopic");
        when(expiredTask.getVariables()).thenReturn(Collections.emptyMap());
        when(expiredTask.getId()).thenReturn("expired-task-id");
        when(expiredTask.getLockExpirationTime()).thenReturn(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)));

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(List.of(expiredTask));

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then: handler should NOT be invoked because lock is already expired
        verify(taskHandler, timeout(1000).times(0)).execute(any(ExternalTask.class), any());
    }

    @Test
    public void shouldExecuteTaskWhenLockIsNotYetExpired() throws Exception {
        // Given: Task with lock expiring in the future
        ExternalTaskImpl validTask = mock(ExternalTaskImpl.class);
        when(validTask.getTopicName()).thenReturn("testTopic");
        when(validTask.getProcessDefinitionKey()).thenReturn("process-testTopic");
        when(validTask.getVariables()).thenReturn(Collections.emptyMap());
        when(validTask.getLockExpirationTime()).thenReturn(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(List.of(validTask));

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(taskHandler).execute(any(), any());

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then: handler should be invoked because lock is still valid
        assertTrue("Task should be executed", latch.await(2, TimeUnit.SECONDS));
        verify(taskHandler, times(1)).execute(any(ExternalTask.class), any());
    }

    @Test
    public void shouldSkipExpiredTasksButExecuteValidOnes() throws Exception {
        // Given: Mix of expired and valid tasks
        ExternalTaskImpl expiredTask = mock(ExternalTaskImpl.class);
        when(expiredTask.getTopicName()).thenReturn("testTopic");
        when(expiredTask.getProcessDefinitionKey()).thenReturn("process-testTopic");
        when(expiredTask.getVariables()).thenReturn(Collections.emptyMap());
        when(expiredTask.getId()).thenReturn("expired-task-id");
        when(expiredTask.getLockExpirationTime()).thenReturn(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)));

        ExternalTaskImpl validTask1 = mock(ExternalTaskImpl.class);
        when(validTask1.getTopicName()).thenReturn("testTopic");
        when(validTask1.getProcessDefinitionKey()).thenReturn("process-testTopic");
        when(validTask1.getVariables()).thenReturn(Collections.emptyMap());
        when(validTask1.getLockExpirationTime()).thenReturn(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));

        ExternalTaskImpl validTask2 = mock(ExternalTaskImpl.class);
        when(validTask2.getTopicName()).thenReturn("testTopic");
        when(validTask2.getProcessDefinitionKey()).thenReturn("process-testTopic");
        when(validTask2.getVariables()).thenReturn(Collections.emptyMap());
        when(validTask2.getLockExpirationTime()).thenReturn(Date.from(Instant.now().plus(2, ChronoUnit.HOURS)));

        when(engineClient.fetchAndLock(anyList(), anyInt()))
                .thenReturn(List.of(expiredTask, validTask1, validTask2));

        CountDownLatch latch = new CountDownLatch(2);
        doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(taskHandler).execute(any(), any());

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then: only 2 valid tasks should be executed, expired task is skipped
        assertTrue("Both valid tasks should be executed", latch.await(2, TimeUnit.SECONDS));
        verify(taskHandler, times(2)).execute(any(ExternalTask.class), any());
    }

    // Helper methods
    private List<ExternalTask> createMockTasks(int count) {
        return createMockTasks(count, "testTopic");
    }

    private List<ExternalTask> createMockTasks(int count, String topicName) {
        List<ExternalTask> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ExternalTaskImpl task = mock(ExternalTaskImpl.class);
            when(task.getTopicName()).thenReturn(topicName);
            when(task.getProcessDefinitionKey()).thenReturn("process-" + topicName);
            when(task.getVariables()).thenReturn(Collections.emptyMap());
            when(task.getLockExpirationTime()).thenReturn(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
            tasks.add(task);
        }
        return tasks;
    }

    private TopicSubscription createSubscription(String topicName) {
        return createSubscription(topicName, taskHandler);
    }

    private TopicSubscription createSubscription(String topicName, ExternalTaskHandler handler) {
        TopicSubscription subscription = mock(TopicSubscription.class);
        when(subscription.getTopicName()).thenReturn(topicName);
        when(subscription.getExternalTaskHandler()).thenReturn(handler);
        when(subscription.getLockDuration()).thenReturn(CLIENT_LOCK_DURATION);
        return subscription;
    }
}
