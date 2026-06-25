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
package org.eximeebpms.bpm.client.spi;

import java.util.Map;

import org.eximeebpms.bpm.client.TaskStats;

/**
 * SPI for receiving periodic external task execution statistics.
 *
 * <p>Implement this interface and register it via
 * {@link org.eximeebpms.bpm.client.ExternalTaskClientBuilder#addStatsListener(ExternalTaskExecutionStatsListener)}
 * to integrate execution metrics with external monitoring systems such as
 * Micrometer, Spring Boot Actuator, Prometheus, InfluxDB, etc.
 *
 * <p>The listener is invoked on the stats scheduler thread at a fixed interval
 * (every 5 minutes by default) regardless of whether the built-in log output
 * ({@link org.eximeebpms.bpm.client.ExternalTaskClientBuilder#statsSchedulerEnabled(boolean)})
 * is enabled. This means you can disable log-based stats output and still receive
 * the data via this listener for export to your monitoring backend.
 *
 * <p><strong>Threading note:</strong> The listener is called from a single daemon
 * scheduler thread. Implementations should be non-blocking. If heavy processing is
 * required, hand off to a separate executor.
 *
 * @see org.eximeebpms.bpm.client.ExternalTaskClientBuilder#addStatsListener(ExternalTaskExecutionStatsListener)
 * @see org.eximeebpms.bpm.client.ExternalTaskExecutionStats
 */
@FunctionalInterface
public interface ExternalTaskExecutionStatsListener {

    /**
     * Called periodically with execution statistics collected since the last invocation.
     * After all registered listeners have been notified the statistics are reset,
     * so each listener receives only the delta since the previous call.
     *
     * <p>The map key is {@code "processDefinitionKey:topicName"}.
     * The map itself is a copy of the internal index – adding or removing entries has no
     * effect on internal state. However, the {@link TaskStats} values inside the map are
     * <strong>live objects</strong>: their counters will be zeroed by {@code reset()} after the stats
     * reporting cycle completes. <strong>Read all values synchronously within this callback;
     * do not store {@link TaskStats} references for later access.</strong>
     *
     * @param statsSnapshot current-interval statistics, never {@code null};
     *                      may be empty if no tasks were processed in the current interval
     */
    void onStats(Map<String, TaskStats> statsSnapshot);
}

