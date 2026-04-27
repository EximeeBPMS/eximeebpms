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

import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.ProcessEngines;
import org.eximeebpms.bpm.engine.impl.cfg.IdGenerator;
import org.eximeebpms.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.CamundaProcessEngineConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.eximeebpms.bpm.spring.boot.starter.property.ScriptSecurityProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
public class DefaultProcessEngineConfiguration extends AbstractCamundaConfiguration implements CamundaProcessEngineConfiguration {

  @Autowired
  private IdGenerator idGenerator;

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
    String defaultSerializationFormat = camundaBpmProperties.getDefaultSerializationFormat();
    if (StringUtils.hasText(defaultSerializationFormat)) {
      configuration.setDefaultSerializationFormat(defaultSerializationFormat);
    } else {
      log.warn("Ignoring invalid defaultSerializationFormat='{}'", defaultSerializationFormat);
    }
  }

  private void setProcessEngineName(SpringProcessEngineConfiguration configuration) {
    String processEngineName = StringUtils.trimAllWhitespace(camundaBpmProperties.getProcessEngineName());
    if (!StringUtils.isEmpty(processEngineName) && !processEngineName.contains("-")) {

      if (camundaBpmProperties.getGenerateUniqueProcessEngineName()) {
        if (!processEngineName.equals(ProcessEngines.NAME_DEFAULT)) {
          throw new RuntimeException(String.format("A unique processEngineName cannot be generated "
            + "if a custom processEngineName is already set: %s", processEngineName));
        }
        processEngineName = CamundaBpmProperties.getUniqueName(CamundaBpmProperties.UNIQUE_ENGINE_NAME_PREFIX);
      }

      configuration.setProcessEngineName(processEngineName);
    } else {
      log.warn("Ignoring invalid processEngineName='{}' - must not be null, blank or contain hyphen", camundaBpmProperties.getProcessEngineName());
    }
  }

  private void setJobExecutorAcquireByPriority(SpringProcessEngineConfiguration configuration) {
    Optional.ofNullable(camundaBpmProperties.getJobExecutorAcquireByPriority())
      .ifPresent(configuration::setJobExecutorAcquireByPriority);
  }

  private void setDefaultNumberOfRetries(SpringProcessEngineConfiguration configuration) {
    Optional.ofNullable(camundaBpmProperties.getDefaultNumberOfRetries())
      .ifPresent(configuration::setDefaultNumberOfRetries);
  }

  private void setScriptSecurity(SpringProcessEngineConfiguration configuration) {
    Optional.ofNullable(camundaBpmProperties.getScriptSecurity())
        .map(ScriptSecurityProperty::isEnabled)
        .ifPresent(configuration::setScriptSecurityEnabled);
  }
}
