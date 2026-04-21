package org.eximeebpms.bpm.client.task;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Extension of {@link ExternalTaskHandler} that allows a handler to declare a dedicated
 * {@link ThreadPoolExecutor} for task execution.
 *
 * <p>When a topic subscription uses an implementation of this interface, the
 * {@link org.eximeebpms.bpm.client.topic.impl.TopicSubscriptionManager}
 * will route all tasks belonging to that subscription to the executor provided by
 * {@link #getThreadPoolExecutor()}, instead of the client's default thread pool.
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
 *         public ThreadPoolExecutor getThreadPoolExecutor() {
 *             return heavyTaskExecutor;
 *         }
 *     })
 *     .open();
 * }</pre>
 *
 * <p>Two handlers that return the <em>same</em> {@link ThreadPoolExecutor} instance share
 * a single {@link org.eximeebpms.bpm.client.topic.impl.ExecutorRunner} and fetch-and-lock
 * cycle. Two handlers returning different executor instances each get their own runner.
 *
 * @see ExternalTaskHandler
 * @see org.eximeebpms.bpm.client.topic.impl.TopicSubscriptionManager
 */
public interface ExternalTaskHandlerWithSpecificExecutor extends ExternalTaskHandler {

    /**
     * Returns the {@link ThreadPoolExecutor} to be used for executing tasks handled by
     * this handler.
     *
     * <p>The returned instance is used as a map key to group subscriptions by executor:
     * two handlers returning the same instance share one
     * {@link org.eximeebpms.bpm.client.topic.impl.ExecutorRunner} and thread pool.
     *
     * @return the dedicated executor; must not be {@code null}
     */
    ThreadPoolExecutor getThreadPoolExecutor();

}
