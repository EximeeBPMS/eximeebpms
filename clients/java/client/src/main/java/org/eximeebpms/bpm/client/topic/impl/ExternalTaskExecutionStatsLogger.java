package org.eximeebpms.bpm.client.topic.impl;

import java.util.Map;
import java.util.logging.Logger;

import org.eximeebpms.bpm.client.topic.impl.ExternalTaskExecutionStats.TaskStats;

/**
 * Utility class for logging external task execution statistics.
 */
public class ExternalTaskExecutionStatsLogger {

    private static final Logger LOGGER = Logger.getLogger(ExternalTaskExecutionStatsLogger.class.getName());

    private ExternalTaskExecutionStatsLogger() {
        // Utility class
    }

    /**
     * Logs all execution statistics in a formatted manner.
     *
     * @param stats the execution statistics to log
     */
    public static void logStats(ExternalTaskExecutionStats stats) {
        Map<String, TaskStats> allStats = stats.getAllStats();

        if (allStats.isEmpty()) {
            LOGGER.info("No execution statistics available");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== External Task Execution Statistics ===\n");
        sb.append(String.format("%-40s %-30s %8s %10s %10s %10s %10s%n",
                "Process Definition Key", "Topic Name", "Count", "Total(ms)", "Min(ms)", "Max(ms)", "Avg(ms)"));
        sb.append("-".repeat(130)).append("\n");

        allStats.values().stream()
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .forEach(taskStats -> sb.append(String.format("%-40s %-30s %8d %10d %10d %10d %10.2f%n",
                        truncate(taskStats.getProcessDefinitionKey(), 40),
                        truncate(taskStats.getTopicName(), 30),
                        taskStats.getCount(),
                        taskStats.getTotalTimeMs(),
                        taskStats.getMinTimeMs(),
                        taskStats.getMaxTimeMs(),
                        taskStats.getAverageTimeMs())));

        sb.append("=".repeat(130)).append("\n");
        LOGGER.info(sb::toString);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return "null";
        }
        return value.length() > maxLength ? value.substring(0, maxLength - 3) + "..." : value;
    }
}

