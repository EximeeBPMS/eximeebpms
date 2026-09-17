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
package org.eximeebpms.spin.plugin.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityMode;
import org.eximeebpms.bpm.engine.repository.Deployment;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * End-to-end demonstration of spinMapToTypeValidationEnabled with a real BPMN
 * process. The process (mapToValidationDemo.bpmn) has a Groovy script task that
 * deserializes a JSON payload into a type named by a process variable
 * (targetType) — the runtime-chosen-type shape from the security analysis.
 *
 * mapTo validation is a RUNTIME check: the process deploys either way; the
 * instance only fails when it reaches the script task with validation opted in
 * and the target type off the whitelist.
 */
public class MapToValidationProcessTest {

  protected static final String BPMN =
      "org/eximeebpms/spin/plugin/mapto/mapToValidationDemo.bpmn";
  protected static final String PAYLOAD =
      "{\"stringProperty\":\"kermit\",\"intProperty\":42,\"booleanProperty\":true}";
  // not on the default deserialization whitelist (java.lang.* + a few java.util collections)
  protected static final String TARGET =
      "org.eximeebpms.spin.plugin.variables.JsonSerializable";

  // validation opted in: deserializationTypeValidationEnabled + spinMapToTypeValidationEnabled.
  // Uses the real DefaultDeserializationTypeValidator (validator left unset). Script Guard off,
  // so the script itself is not blocked — only mapTo validation is exercised.
  protected ProcessEngineBootstrapRule validatingBootstrap = new ProcessEngineBootstrapRule(configuration -> {
    configuration.setScriptSecurityMode(ScriptSecurityMode.DISABLED.name());
    configuration
        .setDeserializationTypeValidationEnabled(true)
        .setSpinMapToTypeValidationEnabled(true)
        .setJdbcUrl("jdbc:h2:mem:mapToProcValidating");
  });
  protected ProvidedProcessEngineRule validatingRule = new ProvidedProcessEngineRule(validatingBootstrap);

  // same whitelist enabled, but mapTo opt-in OFF (the default) -> mapTo not validated
  protected ProcessEngineBootstrapRule optOutBootstrap = new ProcessEngineBootstrapRule(configuration -> {
    configuration.setScriptSecurityMode(ScriptSecurityMode.DISABLED.name());
    configuration
        .setDeserializationTypeValidationEnabled(true)
        .setSpinMapToTypeValidationEnabled(false)
        .setJdbcUrl("jdbc:h2:mem:mapToProcOptOut");
  });
  protected ProvidedProcessEngineRule optOutRule = new ProvidedProcessEngineRule(optOutBootstrap);

  @Rule
  public RuleChain ruleChain = RuleChain
      .outerRule(validatingBootstrap).around(validatingRule)
      .around(optOutBootstrap).around(optOutRule);

  protected ProcessInstance start(ProvidedProcessEngineRule rule) {
    Deployment deployment = rule.getRepositoryService().createDeployment()
        .addClasspathResource(BPMN)
        .deploy();
    rule.manageDeployment(deployment);
    Map<String, Object> vars = new HashMap<>();
    vars.put("payload", PAYLOAD);
    vars.put("targetType", TARGET);
    return rule.getRuntimeService().startProcessInstanceByKey("mapToValidationDemo", vars);
  }

  @Test
  public void shouldFailValidationWhenOptedIn() {
    assertThatThrownBy(() -> start(validatingRule))
        .hasStackTraceContaining("not whitelisted for deserialization");
  }

  @Test
  public void shouldCompleteWhenNotOptedIn() {
    ProcessInstance pi = start(optOutRule);
    // no async: the script task ran during start; with mapTo unvalidated it mapped successfully
    Object mapped = optOutRule.getHistoryService().createHistoricVariableInstanceQuery()
        .processInstanceId(pi.getId()).variableName("mapped").singleResult().getValue();
    assertThat(mapped).isEqualTo("kermit");
  }

}
