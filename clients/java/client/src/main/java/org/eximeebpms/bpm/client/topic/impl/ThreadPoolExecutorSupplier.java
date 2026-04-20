package org.eximeebpms.bpm.client.topic.impl;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

/**
 * A typed {@link Supplier} that provides a {@link ThreadPoolExecutor}.
 *
 * <p>This interface serves as a handle for identifying and grouping
 * {@link org.eximeebpms.bpm.client.topic.TopicSubscription}s by their target executor.
 * The {@link TopicSubscriptionManager} uses supplier instances as map keys:
 * two subscriptions backed by the <em>same</em> supplier instance share one
 * {@link ExecutorRunner}, while subscriptions backed by <em>different</em> supplier
 * instances each get their own independent runner and fetch-and-lock cycle.
 *
 * @see TopicSubscriptionManager
 * @see org.eximeebpms.bpm.client.task.ExternalTaskHandlerWithSpecificExecutor
 */
public interface ThreadPoolExecutorSupplier extends Supplier<ThreadPoolExecutor> {
}
