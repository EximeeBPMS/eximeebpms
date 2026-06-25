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

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Execution statistics for a single (processDefinitionKey, topicName) combination.
 * <p>
 * Counters are thread-safe and updated concurrently by worker threads.
 * All values reflect activity since the last {@link #reset()} call.
 */
@RequiredArgsConstructor
public class TaskStats {

    @Getter
    private final String processDefinitionKey;
    @Getter
    private final String topicName;
    private final LongAdder count = new LongAdder();
    private final LongAdder totalTimeMs = new LongAdder();
    private final AtomicLong minTimeMs = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxTimeMs = new AtomicLong(Long.MIN_VALUE);

    public void recordExecution(long executionTimeMs) {
        count.increment();
        totalTimeMs.add(executionTimeMs);
        updateMin(executionTimeMs);
        updateMax(executionTimeMs);
    }

    public void reset() {
        count.reset();
        totalTimeMs.reset();
        minTimeMs.set(Long.MAX_VALUE);
        maxTimeMs.set(Long.MIN_VALUE);
    }

    public long getCount() {
        return count.sum();
    }

    public long getTotalTimeMs() {
        return totalTimeMs.sum();
    }

    public long getMinTimeMs() {
        long min = minTimeMs.get();
        return min == Long.MAX_VALUE ? 0 : min;
    }

    public long getMaxTimeMs() {
        long max = maxTimeMs.get();
        return max == Long.MIN_VALUE ? 0 : max;
    }

    public double getAverageTimeMs() {
        long currentCount = count.sum();
        return currentCount > 0 ? (double) totalTimeMs.sum() / currentCount : 0.0;
    }

    private void updateMin(long value) {
        long currentMin;
        do {
            currentMin = minTimeMs.get();
            if (value >= currentMin) {
                return;
            }
        } while (!minTimeMs.compareAndSet(currentMin, value));
    }

    private void updateMax(long value) {
        long currentMax;
        do {
            currentMax = maxTimeMs.get();
            if (value <= currentMax) {
                return;
            }
        } while (!maxTimeMs.compareAndSet(currentMax, value));
    }

    public TaskStats copy() {
        TaskStats snapshot = new TaskStats(processDefinitionKey, topicName);
        snapshot.count.add(this.count.sum());
        snapshot.totalTimeMs.add(this.totalTimeMs.sum());
        snapshot.minTimeMs.set(this.minTimeMs.get());
        snapshot.maxTimeMs.set(this.maxTimeMs.get());
        return snapshot;
    }

    @Override
    public String toString() {
        return "TaskStats{" +
                "processDefinitionKey='" + processDefinitionKey + '\'' +
                ", topicName='" + topicName + '\'' +
                ", count=" + getCount() +
                ", totalTimeMs=" + getTotalTimeMs() +
                ", minTimeMs=" + getMinTimeMs() +
                ", maxTimeMs=" + getMaxTimeMs() +
                ", avgTimeMs=" + String.format("%.2f", getAverageTimeMs()) +
                '}';
    }
}
