/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eximeebpms.bpm.client.topic.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eximeebpms.bpm.client.backoff.BackoffStrategy;
import org.eximeebpms.bpm.client.impl.EngineClient;
import org.eximeebpms.bpm.client.impl.ExternalTaskClientLogger;
import org.eximeebpms.bpm.client.task.ExternalTaskHandlerWithSpecificExecutor;
import org.eximeebpms.bpm.client.task.impl.ExternalTaskServiceImpl;
import org.eximeebpms.bpm.client.topic.TopicSubscription;
import org.eximeebpms.bpm.client.variable.impl.TypedValues;

/**
 * Manager that supports multiple, independent
 * {@link java.util.concurrent.ThreadPoolExecutor}s for parallel external task processing.
 * <p>
 * Maintains one {@link ExecutorRunner} per distinct
 * {@link ThreadPoolExecutorSupplier}. Each runner has its own fetch-and-lock cycle,
 * thread pool, and {@link BackoffStrategy} copy, so executors do not block or interfere
 * with each other.
 *
 * <p>Subscription routing works as follows:
 * <ul>
 *   <li>If the handler implements {@link ExternalTaskHandlerWithSpecificExecutor}, the
 *       subscription is assigned to the executor returned by
 *       {@link ExternalTaskHandlerWithSpecificExecutor#getThreadPoolExecutorSupplier()}.</li>
 *   <li>Otherwise the subscription is assigned to the shared
 *       {@code defaultThreadPoolExecutorSupplier}.</li>
 * </ul>
 *
 * <p>Two handlers that return the <em>same</em> {@link ThreadPoolExecutorSupplier} instance
 * share a single {@link ExecutorRunner} and thread pool. Two handlers that return
 * different supplier instances each get their own runner.
 *
 * @author Tassilo Weidner
 * @see ExecutorRunner
 * @see ExternalTaskHandlerWithSpecificExecutor
 */
public class TopicSubscriptionManager {

    protected static final TopicSubscriptionManagerLogger LOG = ExternalTaskClientLogger.TOPIC_SUBSCRIPTION_MANAGER_LOGGER;

    protected final AtomicBoolean isRunning = new AtomicBoolean(false);
    protected final ExternalTaskServiceImpl externalTaskService;
    protected final EngineClient engineClient;
    protected final TypedValues typedValues;
    protected final long clientLockDuration;

    protected final int busyThreadsSleepTimeMs;
    protected final ThreadPoolExecutorSupplier defaultThreadPoolExecutorSupplier;
    protected final double maxFetchedTasksMultiplier;

    protected final Map<ThreadPoolExecutorSupplier, ExecutorRunner> runnersByExecutor = new ConcurrentHashMap<>();
    protected final AtomicBoolean isBackoffStrategyDisabled;
    protected final ExternalTaskExecutionStats executionStats;
    protected BackoffStrategy backoffStrategy;
    protected ScheduledExecutorService statsScheduler;
    private boolean statsSchedulerEnabled;

    public TopicSubscriptionManager(EngineClient engineClient, TypedValues typedValues, long clientLockDuration,
                                    ThreadPoolExecutorSupplier defaultThreadPoolExecutorSupplier, double maxFetchedTasksMultiplier, int busyThreadsSleepTimeMs,
                                    boolean statsSchedulerEnabled) {
        this.engineClient = engineClient;
        this.clientLockDuration = clientLockDuration;
        this.typedValues = typedValues;
        this.externalTaskService = new ExternalTaskServiceImpl(engineClient);
        this.statsSchedulerEnabled = statsSchedulerEnabled;
        this.isBackoffStrategyDisabled = new AtomicBoolean(false);
        this.executionStats = new ExternalTaskExecutionStats();
        this.defaultThreadPoolExecutorSupplier = defaultThreadPoolExecutorSupplier;
        if (maxFetchedTasksMultiplier < 1) {
            throw new IllegalArgumentException("maxFetchedTasksMultiplier parameter must be >=1");
        }
        this.maxFetchedTasksMultiplier = maxFetchedTasksMultiplier;
        this.busyThreadsSleepTimeMs = busyThreadsSleepTimeMs;
    }

    /**
     * Routes the subscription to the appropriate {@link ExecutorRunner} based on its handler type.
     *
     * <p>If the handler implements {@link ExternalTaskHandlerWithSpecificExecutor} the
     * subscription is assigned to the executor declared by that handler; otherwise it is
     * assigned to the default executor. A new runner is created lazily the first time a
     * given executor supplier is encountered.
     *
     * @param subscription the subscription to register; must not be {@code null}
     */
    protected void subscribe(TopicSubscription subscription) {
        if (subscription.getExternalTaskHandler() instanceof ExternalTaskHandlerWithSpecificExecutor externalTaskHandlerWithSpecificExecutor) {
            addSubscription(externalTaskHandlerWithSpecificExecutor.getThreadPoolExecutorSupplier(), subscription);
        } else {
            addSubscription(defaultThreadPoolExecutorSupplier, subscription);
        }
    }

    private void addSubscription(ThreadPoolExecutorSupplier threadPoolExecutorSupplier, TopicSubscription subscription) {
        runnersByExecutor.computeIfAbsent(threadPoolExecutorSupplier,
                k -> prepareExecutorRunner(threadPoolExecutorSupplier)).subscribe(subscription);
    }

    protected void unsubscribe(TopicSubscriptionImpl subscription) {
        runnersByExecutor.values().forEach(runner -> runner.unsubscribe(subscription));
    }

    /**
     * Stops all registered {@link ExecutorRunner}s and the statistics scheduler.
     *
     * <p>This method is idempotent: if the manager is not running the call has no effect.
     * It is {@code synchronized} to prevent concurrent stop attempts.
     */
    public synchronized void stop() {
        if (isRunning.compareAndSet(true, false)) {
            runnersByExecutor.values().forEach(ExecutorRunner::stop);
            if (statsScheduler != null) {
                statsScheduler.shutdown();
                try {
                    if (!statsScheduler.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                        statsScheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    statsScheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Disables the backoff strategy for this manager and all registered runners.
     */
    public void disableBackoffStrategy() {
        this.isBackoffStrategyDisabled.set(true);
        runnersByExecutor.values().forEach(ExecutorRunner::disableBackoffStrategy);
    }

    /**
     * Starts all registered {@link ExecutorRunner}s and the statistics scheduler.
     *
     * <p>This method is idempotent: if the manager is already running the call has no
     * effect. It is {@code synchronized} to prevent concurrent start attempts.
     */
    public synchronized void start() {
        if (isRunning.compareAndSet(false, true)) {
            runnersByExecutor.values().forEach(ExecutorRunner::start);
            if (statsSchedulerEnabled) {
                startStatsScheduler();
            }
        }
    }

    /**
     * Creates and configures a new {@link ExecutorRunner} for the given executor supplier.
     *
     * <p>The runner receives an independent copy of the current {@link BackoffStrategy}
     * via {@link BackoffStrategy#copy()}, ensuring isolated backoff state per executor.
     * If the manager is already running, the runner is started immediately.
     */
    private ExecutorRunner prepareExecutorRunner(ThreadPoolExecutorSupplier supplier) {
        ExecutorRunner executorRunner = new ExecutorRunner(engineClient, typedValues,
                clientLockDuration, busyThreadsSleepTimeMs, supplier, maxFetchedTasksMultiplier);
        executorRunner.setBackoffStrategy(backoffStrategy.copy());
        if (isBackoffStrategyDisabled.get()) {
            executorRunner.disableBackoffStrategy();
        }
        if (isRunning.get()) {
            executorRunner.start();
        }
        return executorRunner;
    }

    /**
     * Updates the {@link BackoffStrategy} for this manager and propagates it to all
     * existing runners.
     *
     * <p>Each runner receives an independent copy via
     * {@link ExecutorRunner#setBackoffStrategy(BackoffStrategy)}, which internally calls
     * {@link BackoffStrategy#copy()}, so runners never share mutable backoff state.
     */
    public void setBackoffStrategy(BackoffStrategy backOffStrategy) {
        this.backoffStrategy = backOffStrategy;
        runnersByExecutor.values().forEach(runner -> runner.setBackoffStrategy(backOffStrategy));
    }

    /**
     * Triggers one acquisition cycle on every registered {@link ExecutorRunner}.
     *
     * <p><strong>For testing only.</strong> In production the runners drive their own
     * loops internally via {@link ExecutorRunner#run()}.
     */
    protected void acquire() {
        runnersByExecutor.values().forEach(ExecutorRunner::acquire);
    }


    protected void startStatsScheduler() {
        // Start scheduled task for stats logging and cleanup every 5 minutes
        statsScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ExternalTaskStatsLogger");
            t.setDaemon(true);
            return t;
        });

        statsScheduler.scheduleAtFixedRate(() -> {
            try {
                // Log current statistics
                ExternalTaskExecutionStatsLogger.logStats(executionStats);
                // Reset statistics for next period
                executionStats.reset();
            } catch (Exception e) {
                LOG.exceptionWhileExecutingBackoffStrategyMethod(e);
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    public EngineClient getEngineClient() {
        return engineClient;
    }

    public List<TopicSubscription> getSubscriptions() {
        return runnersByExecutor.values().stream()
                .flatMap(runner -> runner.getSubscriptions().stream())
                .toList();
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public ExternalTaskExecutionStats getExecutionStats() {
        return executionStats;
    }

}
