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
package org.eximeebpms.bpm.qa.upgrade.gson.batch;

import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.batch.Batch;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.test.Deployment;
import org.eximeebpms.bpm.qa.upgrade.DescribesScenario;
import org.eximeebpms.bpm.qa.upgrade.ScenarioSetup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class DeleteProcessInstancesBatchScenario {

  @Deployment
  public static String deploy() {
    return "org/eximeebpms/bpm/qa/upgrade/gson/oneTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("initDeleteProcessBatch")
  public static ScenarioSetup initDeleteProcessBatch() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {

        for (int i = 0; i < 10; i++) {
          engine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess_710", "DeleteProcessInstancesBatchScenario");
        }

        List<String> processInstanceIds = new ArrayList<>();

        List<ProcessInstance> processInstances = engine.getRuntimeService().createProcessInstanceQuery()
          .processDefinitionKey("oneTaskProcess_710")
          .list();

        for (ProcessInstance processInstance : processInstances) {
          processInstanceIds.add(processInstance.getId());
        }

        Batch batch = engine.getRuntimeService().deleteProcessInstancesAsync(processInstanceIds, null);
        engine.getManagementService().setProperty("DeleteProcessInstancesBatchScenario.batchId", batch.getId());
      }
    };
  }
}
