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
package org.eximeebpms.bpm.qa.upgrade.scenarios120.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.qa.upgrade.Origin;
import org.eximeebpms.bpm.qa.upgrade.ScenarioUnderTest;
import org.eximeebpms.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("OneTaskScenario.init")
@Origin("1.2.0")
public class OneTaskScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("1")
  public void testTaskActiveAndCompletable() {
    Task task = rule.taskQuery().singleResult();

    assertNotNull("User task should still exist after 1.2-to-1.3 migration", task);
    assertEquals("userTask", task.getTaskDefinitionKey());

    rule.getTaskService().complete(task.getId());

    rule.assertScenarioEnded();
  }
}
