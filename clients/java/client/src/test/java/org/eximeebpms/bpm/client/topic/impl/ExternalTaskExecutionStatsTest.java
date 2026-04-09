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
package org.eximeebpms.bpm.client.topic.impl;

import org.eximeebpms.bpm.client.topic.impl.ExternalTaskExecutionStats.TaskStats;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for ExternalTaskExecutionStats to verify time-windowed statistics tracking.
 */
public class ExternalTaskExecutionStatsTest {

    @Test
    public void shouldRecordExecutionStatistics() {
        ExternalTaskExecutionStats stats = new ExternalTaskExecutionStats();
        
        // Record some executions
        stats.recordExecution("process1", "topic1", 100);
        stats.recordExecution("process1", "topic1", 200);
        stats.recordExecution("process1", "topic1", 150);
        
        // Get stats
        TaskStats taskStats = stats.getStats("process1", "topic1");
        
        assertNotNull(taskStats);
        assertEquals(3, taskStats.getCount());
        assertEquals(450, taskStats.getTotalTimeMs());
        assertEquals(100, taskStats.getMinTimeMs());
        assertEquals(200, taskStats.getMaxTimeMs());
        assertEquals(150.0, taskStats.getAverageTimeMs(), 0.01);
    }

    @Test
    public void shouldTrackMultipleTaskTypes() {
        ExternalTaskExecutionStats stats = new ExternalTaskExecutionStats();
        
        stats.recordExecution("process1", "topic1", 100);
        stats.recordExecution("process1", "topic2", 200);
        stats.recordExecution("process2", "topic1", 300);
        
        assertEquals(3, stats.getAllStats().size());
        
        TaskStats stats1 = stats.getStats("process1", "topic1");
        TaskStats stats2 = stats.getStats("process1", "topic2");
        TaskStats stats3 = stats.getStats("process2", "topic1");
        
        assertEquals(1, stats1.getCount());
        assertEquals(100, stats1.getMinTimeMs());
        
        assertEquals(1, stats2.getCount());
        assertEquals(200, stats2.getMinTimeMs());
        
        assertEquals(1, stats3.getCount());
        assertEquals(300, stats3.getMinTimeMs());
    }

    @Test
    public void shouldResetStatistics() {
        ExternalTaskExecutionStats stats = new ExternalTaskExecutionStats();
        
        // Record executions
        stats.recordExecution("process1", "topic1", 100);
        stats.recordExecution("process1", "topic1", 200);
        
        TaskStats taskStats = stats.getStats("process1", "topic1");
        assertEquals(2, taskStats.getCount());
        assertEquals(300, taskStats.getTotalTimeMs());
        
        // Reset should clear the statistics
        stats.reset();
        
        taskStats = stats.getStats("process1", "topic1");
        assertEquals(0, taskStats.getCount());
        assertEquals(0, taskStats.getTotalTimeMs());
        assertEquals(0, taskStats.getMinTimeMs());
        assertEquals(0, taskStats.getMaxTimeMs());
    }

    @Test
    public void shouldClearAllStatistics() {
        ExternalTaskExecutionStats stats = new ExternalTaskExecutionStats();
        
        stats.recordExecution("process1", "topic1", 100);
        stats.recordExecution("process2", "topic2", 200);
        
        assertEquals(2, stats.getAllStats().size());
        
        stats.clear();
        
        assertEquals(0, stats.getAllStats().size());
    }
}

