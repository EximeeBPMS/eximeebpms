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
package org.eximeebpms.bpm.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks execution statistics for external task handling, aggregated per
 * (processDefinitionKey, topicName) pair.
 * <p>
 * Statistics are reset after each reporting cycle. Can also be reset manually via
 * {@link #reset()}. The object is always available regardless of whether periodic
 * logging or listener callbacks are enabled.
 */
public class ExternalTaskExecutionStats {

    private final Map<String, TaskStats> statsMap = new ConcurrentHashMap<>();

    public void recordExecution(String processDefinitionKey, String topicName, long executionTimeMs) {
        String key = createKey(processDefinitionKey, topicName);
        statsMap.computeIfAbsent(key, k -> new TaskStats(processDefinitionKey, topicName))
                .recordExecution(executionTimeMs);
    }

    /**
     * Atomically snapshots all current stats and resets each entry to zero.
     * <p>
     * For each key the existing {@link TaskStats} is captured and replaced with a fresh
     * instance. Concurrent {@link #recordExecution} calls either land in the old object
     * (counted in the returned snapshot) or in the new object (counted in the next
     * interval) — no updates are lost.
     *
     * @return a map containing the stats accumulated since the last reset
     */
    public Map<String, TaskStats> snapshotAndReset() {
        Map<String, TaskStats> snapshot = new HashMap<>();
        statsMap.replaceAll((key, old) -> {
            snapshot.put(key, old);
            return new TaskStats(old.getProcessDefinitionKey(), old.getTopicName());
        });
        return snapshot;
    }

    public void reset() {
        snapshotAndReset();
    }

    public TaskStats getStats(String processDefinitionKey, String topicName) {
        String key = createKey(processDefinitionKey, topicName);
        return statsMap.get(key);
    }

    public Map<String, TaskStats> getAllStats() {
        Map<String, TaskStats> snapshot = new HashMap<>();
        statsMap.forEach((k, v) -> snapshot.put(k, v.copy()));
        return snapshot;
    }

    public void clear() {
        statsMap.clear();
    }

    private String createKey(String processDefinitionKey, String topicName) {
        return processDefinitionKey + ":" + topicName;
    }
}
