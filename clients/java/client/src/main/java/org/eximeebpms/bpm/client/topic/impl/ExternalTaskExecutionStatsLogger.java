package org.eximeebpms.bpm.client.topic.impl;

import java.util.Map;

import org.eximeebpms.bpm.client.topic.impl.ExternalTaskExecutionStats.TaskStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for logging external task execution statistics.
 */
public class ExternalTaskExecutionStatsLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalTaskExecutionStatsLogger.class);
    public static final String COLUMNS_HEADER = String.format("%-40s %-30s %8s %10s %10s %10s %10s%n",
            "Process Definition Key", "Topic Name", "Count", "Total(ms)", "Min(ms)", "Max(ms)", "Avg(ms)");
    public static final String STATS_DATA_FORMAT = "%-40s %-30s %8d %10d %10d %10d %10.2f%n";

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
        sb.append(COLUMNS_HEADER);
        sb.append("-".repeat(130)).append("\n");

        allStats.values().stream()
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .forEach(taskStats -> sb.append(String.format(STATS_DATA_FORMAT,
                        truncate(taskStats.getProcessDefinitionKey(), 40),
                        truncate(taskStats.getTopicName(), 30),
                        taskStats.getCount(),
                        taskStats.getTotalTimeMs(),
                        taskStats.getMinTimeMs(),
                        taskStats.getMaxTimeMs(),
                        taskStats.getAverageTimeMs())));

        sb.append("=".repeat(130)).append("\n");
        LOGGER.info("{}", sb);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return "null";
        }
        return value.length() > maxLength ? value.substring(0, maxLength - 3) + "..." : value;
    }
}

