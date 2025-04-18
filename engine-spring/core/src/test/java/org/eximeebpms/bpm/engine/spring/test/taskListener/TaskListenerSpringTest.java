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
package org.eximeebpms.bpm.engine.spring.test.taskListener;

import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.spring.test.SpringProcessEngineTestCase;
import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.engine.test.Deployment;
import org.springframework.test.context.ContextConfiguration;


/**
 * @author Joram Barrez
 */
@ContextConfiguration("classpath:org/eximeebpms/bpm/engine/spring/test/taskListener/TaskListenerDelegateExpressionTest-context.xml")
public class TaskListenerSpringTest extends SpringProcessEngineTestCase {
  
  @Deployment
  public void testTaskListenerDelegateExpression() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerDelegateExpression");
    
    // Completing first task will set variable on process instance
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    assertEquals("task1-complete", runtimeService.getVariable(processInstance.getId(), "calledInExpression"));
    
    // Completing second task will set variable on process instance
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    assertEquals("task2-notify", runtimeService.getVariable(processInstance.getId(), "calledThroughNotify"));
  }

}
