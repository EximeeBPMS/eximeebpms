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
package org.eximeebpms.bpm.engine.test.api.mgmt.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.eximeebpms.bpm.engine.ManagementService;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.ProcessEngines;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.TaskService;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.impl.diagnostics.CommandCounter;
import org.eximeebpms.bpm.engine.impl.diagnostics.DiagnosticsRegistry;
import org.eximeebpms.bpm.engine.impl.interceptor.Command;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.metrics.Meter;
import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.engine.test.Deployment;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class TelemetryDynamicDataTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule);

  protected ProcessEngineConfigurationImpl configuration;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected ManagementService managementService;

  protected ProcessEngine processEngineInMem;

  @Before
  public void init() {
    configuration = engineRule.getProcessEngineConfiguration();
    runtimeService = configuration.getRuntimeService();
    taskService = configuration.getTaskService();
    managementService = configuration.getManagementService();
    clearMetrics();
  }

  @After
  public void tearDown() {
    clearMetrics();

    if (processEngineInMem != null) {
      ProcessEngines.unregister(processEngineInMem);
      processEngineInMem.close();
    }
  }

  public void clearMetrics() {
    configuration.getDiagnosticsRegistry().clear();
    clearMetrics(configuration.getMetricsRegistry().getDbMeters());
    clearMetrics(configuration.getMetricsRegistry().getDiagnosticsMeters());
    managementService.deleteMetrics(null);
  }

  protected void clearMetrics(Map<String, Meter> meters) {
    for (Meter meter : meters.values()) {
      meter.getAndClear();
    }
  }

  @Test
  public void shouldCountCommandsFromEngineStartAfterTelemetryActivation() {
    // when
    processEngineInMem =  new StandaloneInMemProcessEngineConfiguration()
        .setJdbcUrl("jdbc:h2:mem:eximeebpms" + getClass().getSimpleName())
        .buildProcessEngine();

    // then
    DiagnosticsRegistry telemetryRegistry = ((ProcessEngineConfigurationImpl) processEngineInMem.getProcessEngineConfiguration()).getDiagnosticsRegistry();
    Map<String, CommandCounter> entries = telemetryRegistry.getCommands();
    // note: There are more commands executed during engine start, but the
    // telemetry registry (including the command counts) is reset when telemetry is activated
    // during engine startup
    assertThat(entries.keySet()).containsExactlyInAnyOrder(
        "GetTableMetaDataCmd",
        "HistoryCleanupCmd",
        "SchemaOperationsProcessEngineBuild",
        "HistoryLevelSetupCommand",
        "BootstrapEngineCommand",
        "GetLicenseKeyCmd");
    for (String commandName : entries.keySet()) {
      assertThat(entries.get(commandName).get()).isEqualTo(1);
    }
  }

  @Test
  @Deployment(resources = "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldCountAfterCleaning() {
    // given
    clearCommandCounts();
    Map<String, CommandCounter> entries = configuration.getDiagnosticsRegistry().getCommands();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    runtimeService.setVariable(processInstanceId, "foo", "bar");
    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    taskService.complete(task.getId());

    // then
    assertThat(entries.size()).isEqualTo(4);
    String [] expectedExecutedCommands = {"StartProcessInstanceCmd",
                                         "SetExecutionVariablesCmd",
                                         "TaskQueryImpl",
                                         "CompleteTaskCmd"};
    assertThat(entries.keySet()).contains(expectedExecutedCommands);
    for (String commandName : expectedExecutedCommands) {
      assertThat(entries.get(commandName).get()).isEqualTo(1);
    }
  }

  @Test
  public void shouldCollectInnerClasses() {
    // given default configuration

    // when
    configuration.getCommandExecutorTxRequired().execute(new InnerClassCmd());
    configuration.getCommandExecutorTxRequired().execute(new InnerClassCmd());

    // then
    // the class is properly formatted
    Map<String, CommandCounter> commands = configuration.getDiagnosticsRegistry().getCommands();
    String [] expectedExecutedCommands = {"TelemetryDynamicDataTest_InnerClassCmd"};
    assertThat(commands.keySet()).contains(expectedExecutedCommands);
    assertThat(commands.get("TelemetryDynamicDataTest_InnerClassCmd").get()).isEqualTo(2L);
  }

  @Test
  public void shouldNotCollectAnonymousClasses() {
    // given default configuration

    // when
    // execute anonymous class
    configuration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        System.out.println("Test anonymous command.");
        return null;
      }
    });
    configuration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        System.out.println("Test anonymous command.");
        return null;
      }
    });

    // then
    // the class is not collected
    Map<String, CommandCounter> commands = configuration.getDiagnosticsRegistry().getCommands();
    assertThat(commands.keySet()).containsExactlyInAnyOrder("DeleteMetricsCmd");
  }

  @Test
  public void shouldNotCollectLambdas() {
    // given default configuration

    // when
    // execute command as lambda
    configuration.getCommandExecutorTxRequired().execute((Command<Void>) commandContext -> {
      System.out.println("Test lambda as command.");
      return null;
    });

    // then
    // the class is not collected
    Map<String, CommandCounter> commands = configuration.getDiagnosticsRegistry().getCommands();
    assertThat(commands.keySet()).containsExactlyInAnyOrder("DeleteMetricsCmd");
  }

  protected void clearCommandCounts() {
    configuration.getDiagnosticsRegistry().clearCommandCounts();
  }

  protected static class InnerClassCmd implements Command<Void> {

    @Override
    public Void execute(CommandContext commandContext) {
      System.out.println("Test inner class command.");
      return null;
    }
  }
}
