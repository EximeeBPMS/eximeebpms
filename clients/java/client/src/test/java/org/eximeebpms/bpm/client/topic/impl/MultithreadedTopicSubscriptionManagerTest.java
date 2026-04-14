package org.eximeebpms.bpm.client.topic.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eximeebpms.bpm.client.backoff.BackoffStrategy;
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

@RunWith(MockitoJUnitRunner.class)
public class MultithreadedTopicSubscriptionManagerTest {

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
    private MultithreadedTopicSubscriptionManager manager;

    @Before
    public void setUp() {

        // Create a real thread pool executor for testing
        executor = new ThreadPoolExecutor(
                10, // corePoolSize
                20, // maximumPoolSize
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );

        manager = new MultithreadedTopicSubscriptionManager(
                engineClient,
                typedValues,
                CLIENT_LOCK_DURATION,
                () -> executor,
                DEFAULT_MULTIPLIER,
                20
        );

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
        manager = new MultithreadedTopicSubscriptionManager(
                engineClient,
                typedValues,
                CLIENT_LOCK_DURATION,
                () -> executor,
                2.0,
                20
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
        // Given: All threads are busy (activeCount >= corePoolSize)
        CountDownLatch blockingLatch = new CountDownLatch(1);
        CountDownLatch threadsStartedLatch = new CountDownLatch(15);

        // Fill the executor with blocking tasks
        for (int i = 0; i < 15; i++) {
            executor.submit(() -> {
                try {
                    threadsStartedLatch.countDown(); // Signal that this thread has started
                    blockingLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Wait for all threads to be active
        assertTrue("All 15 threads should start", threadsStartedLatch.await(2, TimeUnit.SECONDS));

        TopicSubscription subscription = createSubscription("testTopic");
        manager.subscribe(subscription);

        // When
        manager.acquire();

        // Then - should not call fetchAndLock
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



