package org.eximeebpms.bpm.client.task;

import org.eximeebpms.bpm.client.topic.impl.ThreadPoolExecutorSupplier;

/**
 * Extension of {@link ExternalTaskHandler} that allows a handler to declare a dedicated
 * {@link java.util.concurrent.ThreadPoolExecutor} for task execution.
 *
 * <p>When a topic subscription uses an implementation of this interface, the
 * {@link org.eximeebpms.bpm.client.topic.impl.MultithreadedTopicSubscriptionManager}
 * will route all tasks belonging to that subscription to the executor provided by
 * {@link #getThreadPoolExecutorSupplier()}, instead of the client's default thread pool.
 *
 * <p>This is useful when different task types have different concurrency requirements,
 * resource constraints, or isolation needs. For example:
 * <pre>{@code
 * ThreadPoolExecutor heavyTaskExecutor = new ThreadPoolExecutor(2, 4, ...);
 *
 * client.subscribe("heavyTopic")
 *     .handler(new ExternalTaskHandlerWithSpecificExecutor() {
 *         public void execute(ExternalTask task, ExternalTaskService service) {
 *             // handle heavy task
 *         }
 *         public ThreadPoolExecutorSupplier getThreadPoolExecutorSupplier() {
 *             return () -> heavyTaskExecutor;
 *         }
 *     })
 *     .open();
 * }</pre>
 *
 * <p>Each distinct {@link ThreadPoolExecutorSupplier} instance gets its own
 * {@link org.eximeebpms.bpm.client.topic.impl.ExecutorRunner}, with an independent
 * fetch-and-lock cycle and backoff strategy.
 *
 * @see ExternalTaskHandler
 * @see ThreadPoolExecutorSupplier
 * @see org.eximeebpms.bpm.client.topic.impl.MultithreadedTopicSubscriptionManager
 */
public interface ExternalTaskHandlerWithSpecificExecutor extends ExternalTaskHandler {

    /**
     * Returns the {@link ThreadPoolExecutorSupplier} that provides the dedicated
     * {@link java.util.concurrent.ThreadPoolExecutor} to be used for executing tasks
     * handled by this handler.
     *
     * <p>The supplier is used as a key to group subscriptions by executor, so two handlers
     * sharing the same supplier instance will share the same
     * {@link org.eximeebpms.bpm.client.topic.impl.ExecutorRunner} and thread pool.
     *
     * @return the supplier of the dedicated executor; must not be {@code null}
     */
    ThreadPoolExecutorSupplier getThreadPoolExecutorSupplier();

}
