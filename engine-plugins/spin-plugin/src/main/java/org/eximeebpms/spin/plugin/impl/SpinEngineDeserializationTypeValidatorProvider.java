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
package org.eximeebpms.spin.plugin.impl;

import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.spin.DeserializationTypeValidator;
import org.eximeebpms.spin.spi.DeserializationTypeValidatorProvider;

/**
 * Resolves, per {@code mapTo} call, the {@link DeserializationTypeValidator} to apply to
 * Spin object mapping, from the currently executing process engine.
 *
 * <p>Returns {@code null} — meaning no validation, i.e. today's behaviour — unless an engine
 * command is in progress <em>and</em> that engine has both
 * {@link ProcessEngineConfigurationImpl#isDeserializationTypeValidationEnabled()} and
 * {@link ProcessEngineConfigurationImpl#isSpinMapToTypeValidationEnabled()} enabled. Reading
 * the configuration from {@link Context} at call time (rather than storing a validator) keeps
 * the JVM-wide {@code DataFormats} singleton correct when several engines share a JVM: whichever
 * engine's command is running supplies its own whitelist.
 */
public class SpinEngineDeserializationTypeValidatorProvider implements DeserializationTypeValidatorProvider {

  @Override
  public DeserializationTypeValidator getValidator() {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    if (configuration == null
        || !configuration.isDeserializationTypeValidationEnabled()
        || !configuration.isSpinMapToTypeValidationEnabled()) {
      return null;
    }
    return adapt(configuration);
  }

  /**
   * Adapts the engine's own {@code DeserializationTypeValidator} to Spin's, so both the
   * {@code ObjectValue} serializer and {@code mapTo} share one wrapping.
   */
  protected static DeserializationTypeValidator adapt(final ProcessEngineConfigurationImpl configuration) {
    return type -> configuration.getDeserializationTypeValidator().validate(type);
  }

}
