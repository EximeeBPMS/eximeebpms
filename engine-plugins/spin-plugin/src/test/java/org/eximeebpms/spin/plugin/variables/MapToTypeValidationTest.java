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
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.interceptor.Command;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.runtime.DeserializationTypeValidator;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.eximeebpms.spin.Spin;
import org.eximeebpms.spin.SpinRuntimeException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * Verifies that {@code mapTo} object mapping consults the engine's deserialization type
 * validator only when {@code spinMapToTypeValidationEnabled} is opted in alongside
 * {@code deserializationTypeValidationEnabled}, resolving the validator from the executing
 * engine at call time.
 */
public class MapToTypeValidationTest {

  protected static final String JSON = "{\"stringProperty\":\"kermit\",\"intProperty\":42,\"booleanProperty\":true}";
  protected static final Class<?> TARGET = JsonSerializable.class;

  // both flags on, validator rejects -> mapTo must be denied
  protected ProcessEngineBootstrapRule denyBootstrap = new ProcessEngineBootstrapRule(configuration -> {
    DeserializationTypeValidator validator = mock(DeserializationTypeValidator.class);
    when(validator.validate(anyString())).thenReturn(false);
    configuration
        .setDeserializationTypeValidator(validator)
        .setDeserializationTypeValidationEnabled(true)
        .setSpinMapToTypeValidationEnabled(true)
        .setJdbcUrl("jdbc:h2:mem:mapToDeny");
  });
  protected ProvidedProcessEngineRule denyRule = new ProvidedProcessEngineRule(denyBootstrap);

  // validation enabled but mapTo opt-in OFF -> mapTo stays unvalidated (default behaviour)
  protected ProcessEngineBootstrapRule optOutBootstrap = new ProcessEngineBootstrapRule(configuration -> {
    DeserializationTypeValidator validator = mock(DeserializationTypeValidator.class);
    when(validator.validate(anyString())).thenReturn(false);
    configuration
        .setDeserializationTypeValidator(validator)
        .setDeserializationTypeValidationEnabled(true)
        .setSpinMapToTypeValidationEnabled(false)
        .setJdbcUrl("jdbc:h2:mem:mapToOptOut");
  });
  protected ProvidedProcessEngineRule optOutRule = new ProvidedProcessEngineRule(optOutBootstrap);

  // both flags on, validator allows -> mapTo succeeds
  protected ProcessEngineBootstrapRule allowBootstrap = new ProcessEngineBootstrapRule(configuration -> {
    DeserializationTypeValidator validator = mock(DeserializationTypeValidator.class);
    when(validator.validate(anyString())).thenReturn(true);
    configuration
        .setDeserializationTypeValidator(validator)
        .setDeserializationTypeValidationEnabled(true)
        .setSpinMapToTypeValidationEnabled(true)
        .setJdbcUrl("jdbc:h2:mem:mapToAllow");
  });
  protected ProvidedProcessEngineRule allowRule = new ProvidedProcessEngineRule(allowBootstrap);

  @Rule
  public RuleChain ruleChain = RuleChain
      .outerRule(denyBootstrap).around(denyRule)
      .around(optOutBootstrap).around(optOutRule)
      .around(allowBootstrap).around(allowRule);

  protected Object mapTo(ProvidedProcessEngineRule rule) {
    ProcessEngineConfigurationImpl configuration =
        (ProcessEngineConfigurationImpl) rule.getProcessEngine().getProcessEngineConfiguration();
    return configuration.getCommandExecutorTxRequired().execute(new Command<Object>() {
      @Override
      public Object execute(CommandContext commandContext) {
        return Spin.JSON(JSON).mapTo(TARGET.getName());
      }
    });
  }

  @Test
  public void shouldDenyMapToWhenOptedInAndValidatorRejects() {
    assertThatThrownBy(() -> mapTo(denyRule)).isInstanceOf(SpinRuntimeException.class);
  }

  @Test
  public void shouldNotValidateMapToWhenNotOptedIn() {
    Object result = mapTo(optOutRule);
    assertThat(result).isInstanceOf(JsonSerializable.class);
  }

  @Test
  public void shouldAllowMapToWhenOptedInAndValidatorAllows() {
    Object result = mapTo(allowRule);
    assertThat(result).isInstanceOf(JsonSerializable.class);
  }

}
