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
package org.eximeebpms.bpm.run;

import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.eximeebpms.bpm.engine.impl.plugin.AdministratorAuthorizationPlugin;
import org.eximeebpms.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin;
import org.eximeebpms.bpm.run.property.EximeeBpmsBpmRunAdministratorAuthorizationProperties;
import org.eximeebpms.bpm.run.property.EximeeBpmsBpmRunLdapProperties;
import org.eximeebpms.bpm.run.property.EximeeBpmsBpmRunProperties;
import org.eximeebpms.bpm.spring.boot.starter.EximeeBPMSBpmAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@EnableConfigurationProperties(EximeeBpmsBpmRunProperties.class)
@Configuration
@AutoConfigureAfter({ EximeeBPMSBpmAutoConfiguration.class })
public class EximeeBpmsBpmRunConfiguration {

  @Bean
  @ConditionalOnProperty(name = "enabled", havingValue = "true", prefix = EximeeBpmsBpmRunLdapProperties.PREFIX)
  public LdapIdentityProviderPlugin ldapIdentityProviderPlugin(EximeeBpmsBpmRunProperties properties) {
    return properties.getLdap();
  }

  @Bean
  @ConditionalOnProperty(name = "enabled", havingValue = "true", prefix = EximeeBpmsBpmRunAdministratorAuthorizationProperties.PREFIX)
  public AdministratorAuthorizationPlugin administratorAuthorizationPlugin(EximeeBpmsBpmRunProperties properties) {
    return properties.getAdminAuth();
  }

  @Bean
  public ProcessEngineConfigurationImpl processEngineConfigurationImpl(List<ProcessEnginePlugin> processEnginePluginsFromContext,
                                                                       EximeeBpmsBpmRunProperties properties,
                                                                       EximeeBpmsBpmRunDeploymentConfiguration deploymentConfig) {
    String normalizedDeploymentDir = deploymentConfig.getNormalizedDeploymentDir();
    boolean deployChangedOnly = properties.getDeployment().isDeployChangedOnly();
    var processEnginePluginsFromYaml = properties.getProcessEnginePlugins();

    return new EximeeBpmsBpmRunProcessEngineConfiguration(normalizedDeploymentDir, deployChangedOnly,
        processEnginePluginsFromContext, processEnginePluginsFromYaml);
  }

  @Bean
  public EximeeBpmsBpmRunDeploymentConfiguration camundaDeploymentConfiguration(@Value("${eximeebpms.deploymentDir:#{null}}") String deploymentDir) {
    return new EximeeBpmsBpmRunDeploymentConfiguration(deploymentDir);
  }

}
