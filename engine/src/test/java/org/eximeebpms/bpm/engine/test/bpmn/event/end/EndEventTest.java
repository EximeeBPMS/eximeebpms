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
package org.eximeebpms.bpm.engine.test.bpmn.event.end;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.eximeebpms.bpm.engine.OptimisticLockingException;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.engine.test.Deployment;
import org.eximeebpms.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class EndEventTest extends PluggableProcessEngineTest {

  // Test case for ACT-1259
  @Deployment
  @Test
  public void testConcurrentEndOfSameProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskWithDelay");
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    
    // We will now start two threads that both complete the task.
    // In the process, the task is followed by a delay of three seconds
    // This will cause both threads to call the taskService.complete method with enough time,
    // before ending the process. Both threads will now try to end the process
    // and only one should succeed (due to optimistic locking).
    TaskCompleter taskCompleter1 = new TaskCompleter(task.getId());
    TaskCompleter taskCompleter2 = new TaskCompleter(task.getId());

    assertFalse(taskCompleter1.isSucceeded());
    assertFalse(taskCompleter2.isSucceeded());
    
    taskCompleter1.start();
    taskCompleter2.start();
    taskCompleter1.join();
    taskCompleter2.join();
    
    int successCount = 0;
    if (taskCompleter1.isSucceeded()) {
      successCount++;
    }
    if (taskCompleter2.isSucceeded()) {
      successCount++;
    }
    
    assertEquals("(Only) one thread should have been able to successfully end the process", 1, successCount);
    testRule.assertProcessEnded(processInstance.getId());
  }
  
  /** Helper class for concurrent testing */
  class TaskCompleter extends Thread {

    protected String taskId;
    protected boolean succeeded;

    public TaskCompleter(String taskId) {
      this.taskId = taskId;
    }
    
    public boolean isSucceeded() {
      return succeeded;
    }

    public void run() {
      try {
        taskService.complete(taskId);
        succeeded = true;
      } catch (OptimisticLockingException ae) {
        // Exception is expected for one of the threads
      }
    }
  }

}
