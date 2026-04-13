package org.eximeebpms.bpm.client.topic.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import org.eximeebpms.bpm.client.impl.EngineClient;
import org.eximeebpms.bpm.client.impl.EngineClientException;
import org.eximeebpms.bpm.client.task.ExternalTask;
import org.eximeebpms.bpm.client.task.ExternalTaskHandler;
import org.eximeebpms.bpm.client.topic.impl.dto.FetchAndLockResponseDto;
import org.eximeebpms.bpm.client.topic.impl.dto.TopicRequestDto;
import org.eximeebpms.bpm.client.variable.impl.TypedValues;

public class MultithreadedTopicSubscriptionManager extends TopicSubscriptionManager {

    private final int busyThreadsSleepTimeMs;
    private final ThreadPoolExecutor executor;
    /**
     * Max fetched tasks in relation to core pool size
     * 1.5 means that if the core pool size is 10, up to 15 tasks will be fetched and locked.
     * This allows to have some tasks in the queue while all threads are busy.
     */
    private final double maxFetchedTasksMultiplier;

    public MultithreadedTopicSubscriptionManager(EngineClient engineClient, TypedValues typedValues, long clientLockDuration,
                                                 ThreadPoolTaskExecutorSupplier executorSupplier, double maxFetchedTasksMultiplier, int busyThreadsSleepTimeMs) {
        super(engineClient, typedValues, clientLockDuration);
        this.executor = executorSupplier.get();
        this.maxFetchedTasksMultiplier = maxFetchedTasksMultiplier;
        this.busyThreadsSleepTimeMs = busyThreadsSleepTimeMs;
    }

    @Override
    protected void acquire() {
        taskTopicRequests.clear();
        externalTaskHandlers.clear();
        subscriptions.forEach(this::prepareAcquisition);

        if (!taskTopicRequests.isEmpty()) {
            if (executor.getActiveCount() < executor.getCorePoolSize()) {
                int maxTasksToFetch = (int) (executor.getCorePoolSize() * maxFetchedTasksMultiplier) - executor.getActiveCount();
                FetchAndLockResponseDto fetchAndLockResponse = fetchAndLock(taskTopicRequests, maxTasksToFetch);

                fetchAndLockResponse.getExternalTasks().forEach(externalTask -> {
                    String topicName = externalTask.getTopicName();
                    ExternalTaskHandler taskHandler = externalTaskHandlers.get(topicName);

                    if (taskHandler != null) {
                        CompletableFuture.runAsync(() -> handleExternalTask(externalTask, taskHandler), executor);
                    } else {
                        LOG.taskHandlerIsNull(topicName);
                    }
                });

                if (!isBackoffStrategyDisabled.get()) {
                    runBackoffStrategy(fetchAndLockResponse);
                }
            } else {
                LOG.allThreadsAreBusy(executor.getActiveCount(), executor.getQueue().size());
                sleep();
            }
        }
    }

    protected FetchAndLockResponseDto fetchAndLock(List<TopicRequestDto> subscriptions, int maxTasks) {
        List<ExternalTask> externalTasks = null;

        try {
            LOG.fetchAndLock(subscriptions, maxTasks);
            externalTasks = engineClient.fetchAndLock(subscriptions, maxTasks);

        } catch (EngineClientException ex) {
            LOG.exceptionWhilePerformingFetchAndLock(ex);
            return new FetchAndLockResponseDto(LOG.handledEngineClientException("fetching and locking task", ex));
        }

        return new FetchAndLockResponseDto(externalTasks);
    }

    private void sleep() {
        try {
            Thread.sleep(busyThreadsSleepTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
