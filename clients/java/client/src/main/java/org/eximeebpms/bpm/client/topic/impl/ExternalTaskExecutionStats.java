package org.eximeebpms.bpm.client.topic.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Tracks execution statistics for external task handling.
 * Statistics are reset after each logging cycle (every 5 minutes).
 */
public class ExternalTaskExecutionStats {

    private final Map<String, TaskStats> statsMap = new ConcurrentHashMap<>();

    /**
     * Records the execution time for a task.
     *
     * @param processDefinitionKey the process definition key
     * @param topicName the topic name
     * @param executionTimeMs the execution time in milliseconds
     */
    public void recordExecution(String processDefinitionKey, String topicName, long executionTimeMs) {
        String key = createKey(processDefinitionKey, topicName);
        statsMap.computeIfAbsent(key, k -> new TaskStats(processDefinitionKey, topicName))
                .recordExecution(executionTimeMs);
    }

    /**
     * Resets all statistics.
     * Called after logging to start a fresh collection period.
     */
    public void reset() {
        statsMap.values().forEach(TaskStats::reset);
    }

    /**
     * Gets the statistics for a specific combination of process definition key and topic name.
     *
     * @param processDefinitionKey the process definition key
     * @param topicName the topic name
     * @return the statistics or null if no data exists
     */
    public TaskStats getStats(String processDefinitionKey, String topicName) {
        String key = createKey(processDefinitionKey, topicName);
        return statsMap.get(key);
    }

    /**
     * Gets all statistics.
     *
     * @return map of all statistics keyed by "processDefinitionKey:topicName"
     */
    public Map<String, TaskStats> getAllStats() {
        return new ConcurrentHashMap<>(statsMap);
    }

    /**
     * Clears all statistics.
     */
    public void clear() {
        statsMap.clear();
    }

    private String createKey(String processDefinitionKey, String topicName) {
        return processDefinitionKey + ":" + topicName;
    }

    /**
     * Statistics for a specific task type.
     */
    public static class TaskStats {
        private final String processDefinitionKey;
        private final String topicName;
        private final LongAdder count = new LongAdder();
        private final LongAdder totalTimeMs = new LongAdder();
        private final AtomicLong minTimeMs = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxTimeMs = new AtomicLong(Long.MIN_VALUE);

        public TaskStats(String processDefinitionKey, String topicName) {
            this.processDefinitionKey = processDefinitionKey;
            this.topicName = topicName;
        }

        void recordExecution(long executionTimeMs) {
            count.increment();
            totalTimeMs.add(executionTimeMs);
            updateMin(executionTimeMs);
            updateMax(executionTimeMs);
        }

        void reset() {
            count.reset();
            totalTimeMs.reset();
            minTimeMs.set(Long.MAX_VALUE);
            maxTimeMs.set(Long.MIN_VALUE);
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

        public String getProcessDefinitionKey() {
            return processDefinitionKey;
        }

        public String getTopicName() {
            return topicName;
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
}

