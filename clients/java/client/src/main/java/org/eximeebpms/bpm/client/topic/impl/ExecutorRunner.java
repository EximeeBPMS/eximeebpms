package org.eximeebpms.bpm.client.topic.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eximeebpms.bpm.client.backoff.BackoffStrategy;
import org.eximeebpms.bpm.client.backoff.ErrorAwareBackoffStrategy;
import org.eximeebpms.bpm.client.exception.ExternalTaskClientException;
import org.eximeebpms.bpm.client.impl.EngineClient;
import org.eximeebpms.bpm.client.impl.EngineClientException;
import org.eximeebpms.bpm.client.impl.ExternalTaskClientLogger;
import org.eximeebpms.bpm.client.task.ExternalTask;
import org.eximeebpms.bpm.client.task.ExternalTaskHandler;
import org.eximeebpms.bpm.client.task.impl.ExternalTaskImpl;
import org.eximeebpms.bpm.client.task.impl.ExternalTaskServiceImpl;
import org.eximeebpms.bpm.client.topic.TopicSubscription;
import org.eximeebpms.bpm.client.topic.impl.dto.FetchAndLockResponseDto;
import org.eximeebpms.bpm.client.topic.impl.dto.TopicRequestDto;
import org.eximeebpms.bpm.client.variable.impl.TypedValueField;
import org.eximeebpms.bpm.client.variable.impl.TypedValues;
import org.eximeebpms.bpm.client.variable.impl.VariableValue;

/**
 * Manages the fetch-and-lock acquisition loop for a single {@link ThreadPoolExecutor}.
 *
 * <p>Each instance is responsible for one dedicated thread pool. It continuously polls the
 * engine for external tasks matching its registered {@link TopicSubscription}s, dispatches
 * fetched tasks asynchronously to the thread pool, and applies a {@link BackoffStrategy}
 * between acquisition cycles.
 *
 * <p>Typical lifecycle:
 * <ol>
 *   <li>Create an instance via the constructor.</li>
 *   <li>Register one or more subscriptions via {@link #subscribe(TopicSubscription)}.</li>
 *   <li>Call {@link #start()} to begin the acquisition loop on a dedicated thread.</li>
 * </ol>
 *
 * <p>The number of tasks fetched per cycle is bounded by
 * {@code floor(corePoolSize * maxFetchedTasksMultiplier) - tasksInProgress}, preventing
 * task flooding when threads are busy. If the pool is fully occupied (no fetch slots
 * remaining), the runner sleeps for {@code busyThreadsSleepTimeMs} milliseconds instead
 * of polling the engine.
 *
 * <p>Each {@code ExecutorRunner} holds its own <em>copy</em> of the {@link BackoffStrategy},
 * ensuring that multiple runners do not interfere with each other's backoff state.
 *
 * @see TopicSubscriptionManager
 * @see BackoffStrategy
 */
public class ExecutorRunner implements Runnable {
    protected static final TopicSubscriptionManagerLogger LOG = ExternalTaskClientLogger.TOPIC_SUBSCRIPTION_MANAGER_LOGGER;

    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger(0);

    private final String threadName;
    private final int busyThreadsSleepTimeMs;
    private final ThreadPoolExecutor taskExecutor;

    protected ExternalTaskServiceImpl externalTaskService;

    protected EngineClient engineClient;

    protected CopyOnWriteArrayList<TopicSubscription> subscriptions = new CopyOnWriteArrayList<>();
    protected List<TopicRequestDto> taskTopicRequests = new ArrayList<>();
    protected Map<String, ExternalTaskHandler> externalTaskHandlers = new HashMap<>();

    protected Thread thread;

    protected BackoffStrategy backoffStrategy;
    protected AtomicBoolean isBackoffStrategyDisabled = new AtomicBoolean(false);

    protected TypedValues typedValues;

    protected long clientLockDuration;

    protected ExternalTaskExecutionStats executionStats;

    /**
     * Max fetched tasks in relation to core pool size
     * 1.5 means that if the core pool size is 10, up to 15 tasks will be fetched and locked.
     * This allows to have some tasks in the queue while all threads are busy.
     */
    private final double maxFetchedTasksMultiplier;

    protected ReentrantLock acquisitionMonitor = new ReentrantLock(false);
    protected Condition isWaiting = acquisitionMonitor.newCondition();

    protected AtomicBoolean isRunning = new AtomicBoolean(false);

    public ExecutorRunner(EngineClient engineClient, TypedValues typedValues, long clientLockDuration,
                          int busyThreadsSleepTimeMs, ThreadPoolExecutor taskExecutor, double maxFetchedTasksMultiplier,
                          ExternalTaskExecutionStats executionStats) {
        this.engineClient = engineClient;
        this.clientLockDuration = clientLockDuration;
        this.typedValues = typedValues;
        this.externalTaskService = new ExternalTaskServiceImpl(engineClient);
        this.executionStats = executionStats;
        this.busyThreadsSleepTimeMs = busyThreadsSleepTimeMs;
        this.taskExecutor = taskExecutor;
        this.maxFetchedTasksMultiplier = maxFetchedTasksMultiplier;
        this.threadName = ExecutorRunner.class.getSimpleName() + "-" + INSTANCE_COUNTER.incrementAndGet();
    }

    /**
     * Executes a single fetch-and-lock acquisition cycle.
     *
     * <p>The cycle consists of:
     * <ol>
     *   <li>Building the list of topic requests from active subscriptions.</li>
     *   <li>Calculating the number of tasks to fetch based on current pool utilization.</li>
     *   <li>Fetching and locking tasks from the engine (skipped if pool is full).</li>
     *   <li>Submitting each fetched task asynchronously to the thread pool.</li>
     *   <li>Running the backoff strategy (unless disabled).</li>
     * </ol>
     *
     * <p>If there are no subscriptions, or if all thread-pool slots are occupied, the
     * method returns without contacting the engine.
     */
    protected void acquire() {
        taskTopicRequests.clear();
        externalTaskHandlers.clear();
        subscriptions.forEach(this::prepareAcquisition);
        if (!taskTopicRequests.isEmpty()) {
            int maxTasks = (int) (taskExecutor.getCorePoolSize() * maxFetchedTasksMultiplier);
            int tasksInProgress = taskExecutor.getActiveCount() + taskExecutor.getQueue().size();
            int maxTasksToFetch = maxTasks - tasksInProgress;
            if (maxTasksToFetch > 0) {
                FetchAndLockResponseDto fetchAndLockResponse = fetchAndLock(taskTopicRequests, maxTasksToFetch);

                fetchAndLockResponse.getExternalTasks().forEach(externalTask -> {
                    String topicName = externalTask.getTopicName();
                    ExternalTaskHandler taskHandler = externalTaskHandlers.get(topicName);

                    if (taskHandler != null) {
                        CompletableFuture.runAsync(() -> handleExternalTask(externalTask, taskHandler),
                                taskExecutor);
                    } else {
                        LOG.taskHandlerIsNull(topicName);
                    }
                });

                if (!isBackoffStrategyDisabled.get()) {
                    runBackoffStrategy(fetchAndLockResponse);
                }
            } else {
                LOG.allThreadsAreBusy(taskExecutor.getActiveCount(), taskExecutor.getQueue().size(),
                        externalTaskHandlers.keySet().stream().sorted().collect(java.util.stream.Collectors.joining(", ")));
                CompletableFuture.runAsync(() ->{}, taskExecutor).join(); // wait for currently running tasks to complete
            }
        }
    }

    /**
     * Reconfigures the {@link BackoffStrategy} with the result of the last fetch cycle and
     * suspends the runner for the calculated wait time.
     */
    protected void runBackoffStrategy(FetchAndLockResponseDto fetchAndLockResponse) {
        try {
            List<ExternalTask> externalTasks = fetchAndLockResponse.getExternalTasks();
            if (backoffStrategy instanceof ErrorAwareBackoffStrategy errorAwareBackoffStrategy) {
                ExternalTaskClientException exception = fetchAndLockResponse.getError();
                errorAwareBackoffStrategy.reconfigure(externalTasks, exception);
            } else {
                backoffStrategy.reconfigure(externalTasks);
            }

            long waitTime = backoffStrategy.calculateBackoffTime();
            suspend(waitTime);
        } catch (Exception e) {
            LOG.exceptionWhileExecutingBackoffStrategyMethod(e);
        }
    }

    /**
     * Suspends the acquisition loop for the given duration using a {@link Condition#await}.
     *
     * <p>The wait is skipped if {@code waitTime} is zero or if the runner is no longer
     * running. The condition can be signalled early by {@link #resume()} (e.g. when a new
     * subscription is added).
     */
    protected void suspend(long waitTime) {
        if (waitTime > 0 && isRunning.get()) {
            acquisitionMonitor.lock();
            try {
                while (isRunning.get()) {
                    if (!isWaiting.await(waitTime, TimeUnit.MILLISECONDS)) {
                        break; // timeout elapsed — backoff finished normally
                    }
                    return; // signalled early by resume() — finally still runs, lock is released
                }
            } catch (InterruptedException e) {
                LOG.exceptionWhileExecutingBackoffStrategyMethod(e);
                Thread.currentThread().interrupt();
            }
            finally {
                acquisitionMonitor.unlock();
            }
        }
    }

    /**
     * Prepares and executes a single {@link org.eximeebpms.bpm.client.task.ExternalTask}.
     *
     * <p>Before delegating to the handler, typed variable values are resolved and the task's
     * lock expiry is checked. If the lock has already expired the task is silently skipped.
     * All exceptions thrown by the handler are caught and logged; execution statistics are
     * always recorded in the {@code finally} block.
     */
    protected void handleExternalTask(ExternalTask externalTask, ExternalTaskHandler taskHandler) {
        ExternalTaskImpl task = (ExternalTaskImpl) externalTask;

        Map<String, TypedValueField> variables = task.getVariables();
        Map<String, VariableValue<?>> wrappedVariables = typedValues.wrapVariables(task, variables);
        task.setReceivedVariableMap(wrappedVariables);

        long startTime = System.currentTimeMillis();
        try {
            if (checkLockExpired(task)) {
                return;
            }
            taskHandler.execute(task, externalTaskService);
        } catch (ExternalTaskClientException e) {
            LOG.exceptionOnExternalTaskServiceMethodInvocation(task.getTopicName(), e);
        } catch (Exception e) {
            LOG.exceptionWhileExecutingExternalTaskHandler(task.getTopicName(), e);
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            executionStats.recordExecution(task.getProcessDefinitionKey(), task.getTopicName(), executionTime);
        }
    }

    /**
     * Checks whether the lock on the given task has expired.
     *
     * <p>Expired tasks are logged and skipped to avoid processing work that can no longer
     * be completed within the lock window.
     */
    private static boolean checkLockExpired(ExternalTaskImpl task) {
        long timeUntilLockExpires = task.getLockExpirationTime().getTime() - System.currentTimeMillis();
        if (timeUntilLockExpires <= 0) {
            LOG.taskLockAlreadyExpired(task.getId(), task.getTopicName(), task.getLockExpirationTime());
            return true;
        }
        return false;
    }

    /**
     * Converts a {@link TopicSubscription} into a {@link TopicRequestDto} and registers
     * its handler in the local handler map for the current acquisition cycle.
     */
    protected void prepareAcquisition(TopicSubscription subscription) {
        TopicRequestDto taskTopicRequest = TopicRequestDto.fromTopicSubscription(subscription, clientLockDuration);
        taskTopicRequests.add(taskTopicRequest);

        String topicName = subscription.getTopicName();
        ExternalTaskHandler externalTaskHandler = subscription.getExternalTaskHandler();
        externalTaskHandlers.put(topicName, externalTaskHandler);
    }

    /**
     * Sends a fetch-and-lock request to the engine and returns the response.
     *
     * <p>If the engine call fails with an {@link EngineClientException}, the error is
     * logged and a response containing only the exception (and an empty task list) is
     * returned so that the backoff strategy can react to the failure.
     */
    protected FetchAndLockResponseDto fetchAndLock(List<TopicRequestDto> subscriptions, int maxTasks) {
        List<ExternalTask> externalTasks;

        try {
            LOG.fetchAndLock(subscriptions, maxTasks);
            externalTasks = engineClient.fetchAndLock(subscriptions, maxTasks);

        } catch (EngineClientException ex) {
            LOG.exceptionWhilePerformingFetchAndLock(ex);
            return new FetchAndLockResponseDto(LOG.handledEngineClientException("fetching and locking task", ex));
        }

        return new FetchAndLockResponseDto(externalTasks);
    }

    /**
     * The acquisition loop executed on the runner's dedicated thread.
     *
     * <p>Repeatedly calls {@link #acquire()} until {@link #isRunning} is set to
     * {@code false}. Unexpected exceptions are caught and logged to keep the loop alive.
     */
    @Override
    public void run() {
        while (isRunning.get()) {
            try {
                acquire();
            }
            catch (Exception e) {
                LOG.exceptionWhileAcquiringTasks(e);
            }
        }
    }

    /**
     * Registers a new {@link TopicSubscription} with this runner.
     *
     * <p>If a subscription for the same topic is already registered an exception is thrown.
     * After a successful registration, {@link #resume()} is called to wake the runner
     * immediately so it can start processing tasks for the new topic without waiting for
     * the current backoff period to elapse.
     */
    public void subscribe(TopicSubscription subscription) {
        if (!subscriptions.addIfAbsent(subscription)) {
            String topicName = subscription.getTopicName();
            throw LOG.topicNameAlreadySubscribedException(topicName);
        }

        resume();
    }

    /**
     * Removes the given subscription from this runner.
     */
    protected void unsubscribe(TopicSubscriptionImpl subscription) {
        subscriptions.remove(subscription);
    }

    /**
     * Signals the acquisition loop to wake up early from a {@link #suspend} wait.
     *
     * <p>Called after a new subscription is added or when the runner should re-evaluate
     * its state without waiting for the full backoff period.
     */
    protected void resume() {
        acquisitionMonitor.lock();
        try {
            isWaiting.signal();
        }
        finally {
            acquisitionMonitor.unlock();
        }
    }

    /**
     * Starts the acquisition loop on a new dedicated thread.
     *
     * <p>If the runner is already running this method has no effect. The method is
     * {@code synchronized} to prevent concurrent start attempts.
     */
    public synchronized void start() {
        if (isRunning.compareAndSet(false, true)) {
            thread = new Thread(this, threadName);
            thread.start();
        }
    }

    /**
     * Stops the acquisition loop and waits for the runner's thread to finish.
     *
     * <p>If the runner is not running this method has no effect. It is {@code synchronized}
     * to prevent concurrent stop attempts.
     */
    public synchronized void stop() {
        if (isRunning.compareAndSet(true, false)) {
            resume();
            try {
                if (thread != null) {
                    thread.join();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.exceptionWhileShuttingDown(e);
            }
        }
    }

    /**
     * Sets the {@link BackoffStrategy} for this runner.
     *
     * <p>An independent copy of the provided strategy is stored via {@link BackoffStrategy#copy()},
     * ensuring that multiple runners never share mutable backoff state.
     */
    public void setBackoffStrategy(BackoffStrategy backOffStrategy) {
        this.backoffStrategy = backOffStrategy.copy();
    }

    /**
     * Disables the backoff strategy for this runner.
     *
     * <p>When disabled, the runner will not call {@link BackoffStrategy#reconfigure} or
     * {@link BackoffStrategy#calculateBackoffTime()} and will poll the engine as fast
     * as possible.
     */
    public void disableBackoffStrategy() {
        this.isBackoffStrategyDisabled.set(true);
    }

    public List<TopicSubscription> getSubscriptions() {
        return subscriptions;
    }
}
