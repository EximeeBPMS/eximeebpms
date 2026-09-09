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
package org.eximeebpms.bpm.spring.boot.starter.configuration.impl;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.ProcessEngines;
import org.eximeebpms.bpm.engine.impl.cfg.IdGenerator;
import org.eximeebpms.bpm.engine.impl.businessevent.script.BusinessEventScriptViolationListener;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.scripting.security.NoOpScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationListener;
import org.eximeebpms.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsProcessEngineConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.property.EximeeBpmsBpmProperties;
import org.eximeebpms.bpm.spring.boot.starter.property.ScriptSecurityProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

@Slf4j
public class DefaultProcessEngineConfiguration extends AbstractEximeeBpmsConfiguration implements EximeeBpmsProcessEngineConfiguration {

  @Autowired
  private IdGenerator idGenerator;

  @Autowired
  private ApplicationContext applicationContext;

  @Override
  public void preInit(SpringProcessEngineConfiguration configuration) {
    setProcessEngineName(configuration);
    setDefaultSerializationFormat(configuration);
    setIdGenerator(configuration);
    setJobExecutorAcquireByPriority(configuration);
    setDefaultNumberOfRetries(configuration);
    setScriptSecurity(configuration);
  }

  private void setIdGenerator(SpringProcessEngineConfiguration configuration) {
    Optional.ofNullable(idGenerator).ifPresent(configuration::setIdGenerator);
  }

  private void setDefaultSerializationFormat(SpringProcessEngineConfiguration configuration) {
    String defaultSerializationFormat = eximeeBpmsBpmProperties.getDefaultSerializationFormat();
    if (StringUtils.hasText(defaultSerializationFormat)) {
      configuration.setDefaultSerializationFormat(defaultSerializationFormat);
    } else {
      log.warn("Ignoring invalid defaultSerializationFormat='{}'", defaultSerializationFormat);
    }
  }

  private void setProcessEngineName(SpringProcessEngineConfiguration configuration) {
    String processEngineName = StringUtils.trimAllWhitespace(eximeeBpmsBpmProperties.getProcessEngineName());
    if (!StringUtils.isEmpty(processEngineName) && !processEngineName.contains("-")) {

      if (eximeeBpmsBpmProperties.getGenerateUniqueProcessEngineName()) {
        if (!processEngineName.equals(ProcessEngines.NAME_DEFAULT)) {
          throw new RuntimeException(String.format("A unique processEngineName cannot be generated "
            + "if a custom processEngineName is already set: %s", processEngineName));
        }
        processEngineName = EximeeBpmsBpmProperties.getUniqueName(EximeeBpmsBpmProperties.UNIQUE_ENGINE_NAME_PREFIX);
      }

      configuration.setProcessEngineName(processEngineName);
    } else {
      log.warn("Ignoring invalid processEngineName='{}' - must not be null, blank or contain hyphen", eximeeBpmsBpmProperties.getProcessEngineName());
    }
  }

  private void setJobExecutorAcquireByPriority(SpringProcessEngineConfiguration configuration) {
    Optional.ofNullable(eximeeBpmsBpmProperties.getJobExecutorAcquireByPriority())
        .ifPresent(configuration::setJobExecutorAcquireByPriority);
  }

  private void setDefaultNumberOfRetries(SpringProcessEngineConfiguration configuration) {
    Optional.ofNullable(eximeeBpmsBpmProperties.getDefaultNumberOfRetries())
        .ifPresent(configuration::setDefaultNumberOfRetries);
  }

  private void setScriptSecurity(SpringProcessEngineConfiguration configuration) {
    Optional.ofNullable(eximeeBpmsBpmProperties.getScriptSecurity())
        .ifPresent(scriptSecurity -> configureScriptSecurity(configuration, scriptSecurity));
  }

  private void configureScriptSecurity(SpringProcessEngineConfiguration configuration, ScriptSecurityProperty scriptSecurity) {
    configuration.setScriptSecurityMode(scriptSecurity.getMode().name());
    configuration.setScriptSecurityAllowlistedProcessDefinitionKeys(scriptSecurity.getAllowlistedProcessDefinitionKeys());
    configuration.setScriptViolationRetentionDays(scriptSecurity.getRetentionDays());

    if (scriptSecurity.isDisabled()) {
      return;
    }

    // Leave scriptSecurityPolicy unset — ProcessEngineConfigurationImpl.initScriptSecurityPolicy()
    // builds the shared DbAwareScriptSecurityPolicy itself (mode/allowlist/store/listeners set
    // here flow into that construction), the same fallback a Tomcat/plain-XML deployment uses.
    // Only replace the default NoOpScriptViolationStore — respects a ProcessEnginePlugin's own
    // preInit() choice, matching StartProcessEngineStep.preConfigureScriptSecurity() on Tomcat.
    if (configuration.getScriptViolationStore() instanceof NoOpScriptViolationStore) {
      configuration.setScriptViolationStore(new DbScriptViolationStore(configuration));
    }

    if (applicationContext != null) {
      applicationContext.getBeansOfType(ScriptViolationListener.class).values()
          .forEach(configuration::addScriptViolationListener);
    }
    configuration.addScriptViolationListener(new BusinessEventScriptViolationListener());
  }
}
