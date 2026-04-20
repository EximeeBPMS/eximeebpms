package org.eximeebpms.bpm.client.topic.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eximeebpms.bpm.client.backoff.BackoffStrategy;
import org.eximeebpms.bpm.client.impl.EngineClient;
import org.eximeebpms.bpm.client.task.ExternalTaskHandlerWithSpecificExecutor;
import org.eximeebpms.bpm.client.topic.TopicSubscription;
import org.eximeebpms.bpm.client.variable.impl.TypedValues;

/**
 * A {@link TopicSubscriptionManager} that supports multiple, independent
 * {@link java.util.concurrent.ThreadPoolExecutor}s for parallel external task processing.
 *
 * <p>While the base {@link TopicSubscriptionManager} uses a single acquisition thread,
 * this implementation maintains one {@link ExecutorRunner} per distinct
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
 * @see TopicSubscriptionManager
 * @see ExecutorRunner
 * @see ExternalTaskHandlerWithSpecificExecutor
 */
public class MultithreadedTopicSubscriptionManager extends TopicSubscriptionManager {

    private final int busyThreadsSleepTimeMs;
    private final ThreadPoolExecutorSupplier defaultThreadPoolExecutorSupplier;
    private final double maxFetchedTasksMultiplier;

    protected Map<ThreadPoolExecutorSupplier, ExecutorRunner> runnersByExecutor = new ConcurrentHashMap<>();

    public MultithreadedTopicSubscriptionManager(EngineClient engineClient, TypedValues typedValues, long clientLockDuration,
                                                 ThreadPoolExecutorSupplier defaultThreadPoolExecutorSupplier, double maxFetchedTasksMultiplier, int busyThreadsSleepTimeMs) {
        super(engineClient, typedValues, clientLockDuration);
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
    @Override
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

    @Override
    protected void unsubscribe(TopicSubscriptionImpl subscription) {
        runnersByExecutor.values().forEach(runner -> runner.unsubscribe(subscription));
    }

    /**
     * Stops all registered {@link ExecutorRunner}s and the statistics scheduler.
     *
     * <p>This method is idempotent: if the manager is not running the call has no effect.
     * It is {@code synchronized} to prevent concurrent stop attempts.
     */
    @Override
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
    @Override
    public void disableBackoffStrategy() {
        super.disableBackoffStrategy();
        runnersByExecutor.values().forEach(ExecutorRunner::disableBackoffStrategy);
    }

    /**
     * Starts all registered {@link ExecutorRunner}s and the statistics scheduler.
     *
     * <p>This method is idempotent: if the manager is already running the call has no
     * effect. It is {@code synchronized} to prevent concurrent start attempts.
     */
    @Override
    public synchronized void start() {
        if (isRunning.compareAndSet(false, true)) {
            runnersByExecutor.values().forEach(ExecutorRunner::start);
            startStatsScheduler();
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
    @Override
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
    @Override
    protected void acquire() {
        runnersByExecutor.values().forEach(ExecutorRunner::acquire);
    }
}
