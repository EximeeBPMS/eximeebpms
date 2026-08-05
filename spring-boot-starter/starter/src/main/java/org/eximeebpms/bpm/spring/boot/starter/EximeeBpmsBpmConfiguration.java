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
package org.eximeebpms.bpm.spring.boot.starter;

import static org.eximeebpms.bpm.spring.boot.starter.jdbc.HistoryLevelDeterminatorJdbcTemplateImpl.createHistoryLevelDeterminator;

import java.util.List;

import org.eximeebpms.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.eximeebpms.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsAuthorizationConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsBusinessEventConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsDatasourceConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsDeploymentConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsFailedJobConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsHistoryConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsHistoryLevelAutoHandlingConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsJobConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsMetricsConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsProcessEngineConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.condition.NeedsHistoryAutoConfigurationCondition;
import org.eximeebpms.bpm.spring.boot.starter.configuration.id.IdGeneratorConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.DefaultBusinessEventConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.custom.CreateAdminUserConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.custom.CreateFilterConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.DefaultAuthorizationConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.DefaultDatasourceConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.DefaultDeploymentConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.DefaultFailedJobConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.DefaultHistoryConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.DefaultHistoryLevelAutoHandlingConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.DefaultJobConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.DefaultJobConfiguration.JobConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.DefaultMetricsConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.DefaultProcessEngineConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.GenericPropertiesConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.event.EventPublisherPlugin;
import org.eximeebpms.bpm.spring.boot.starter.jdbc.HistoryLevelDeterminator;
import org.eximeebpms.bpm.spring.boot.starter.property.EximeeBpmsBpmProperties;
import org.eximeebpms.bpm.spring.boot.starter.telemetry.EximeeBpmsIntegrationDeterminator;
import org.eximeebpms.bpm.spring.boot.starter.util.EximeeBpmsSpringBootUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import({
    JobConfiguration.class,
    IdGeneratorConfiguration.class
})
public class EximeeBpmsBpmConfiguration {

  @Bean
  @ConditionalOnMissingBean(ProcessEngineConfigurationImpl.class)
  public ProcessEngineConfigurationImpl processEngineConfigurationImpl(List<ProcessEnginePlugin> processEnginePlugins) {
    final SpringProcessEngineConfiguration configuration = EximeeBpmsSpringBootUtil.springProcessEngineConfiguration();
    configuration.getProcessEnginePlugins().add(new CompositeProcessEnginePlugin(processEnginePlugins));
    return configuration;
  }

  @Bean
  @ConditionalOnMissingBean(DefaultProcessEngineConfiguration.class)
  public static EximeeBpmsProcessEngineConfiguration eximeeBpmsProcessEngineConfiguration() {
    return new DefaultProcessEngineConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(EximeeBpmsDatasourceConfiguration.class)
  public static EximeeBpmsDatasourceConfiguration eximeeBpmsDatasourceConfiguration() {
    return new DefaultDatasourceConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(EximeeBpmsJobConfiguration.class)
  @ConditionalOnProperty(prefix = "eximeebpms.bpm.job-execution", name = "enabled", havingValue = "true", matchIfMissing = true)
  public static EximeeBpmsJobConfiguration eximeeBpmsJobConfiguration() {
    return new DefaultJobConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(EximeeBpmsHistoryConfiguration.class)
  public static EximeeBpmsHistoryConfiguration eximeeBpmsHistoryConfiguration() {
    return new DefaultHistoryConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(EximeeBpmsBusinessEventConfiguration.class)
  public static EximeeBpmsBusinessEventConfiguration eximeeBpmsBusinessEventConfiguration() {
    return new DefaultBusinessEventConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(EximeeBpmsMetricsConfiguration.class)
  public static EximeeBpmsMetricsConfiguration eximeeBpmsMetricsConfiguration() {
    return new DefaultMetricsConfiguration();
  }

  //TODO to be removed within CAM-8108
  @Bean(name = "historyLevelAutoConfiguration")
  @ConditionalOnMissingBean(EximeeBpmsHistoryLevelAutoHandlingConfiguration.class)
  @ConditionalOnProperty(prefix = "eximeebpms.bpm", name = "history-level", havingValue = "auto", matchIfMissing = false)
  @Conditional(NeedsHistoryAutoConfigurationCondition.class)
  public static EximeeBpmsHistoryLevelAutoHandlingConfiguration historyLevelAutoHandlingConfiguration() {
    return new DefaultHistoryLevelAutoHandlingConfiguration();
  }

  //TODO to be removed within CAM-8108
  @Bean(name = "historyLevelDeterminator")
  @ConditionalOnMissingBean(name = { "eximeeBpmsBpmJdbcTemplate", "camundaBpmJdbcTemplate", "historyLevelDeterminator" })
  @ConditionalOnBean(name = "historyLevelAutoConfiguration")
  public static HistoryLevelDeterminator historyLevelDeterminator(EximeeBpmsBpmProperties eximeeBpmsBpmProperties, JdbcTemplate jdbcTemplate) {
    return createHistoryLevelDeterminator(eximeeBpmsBpmProperties, jdbcTemplate);
  }

  //TODO to be removed within CAM-8108
  @Bean(name = "historyLevelDeterminator")
  @ConditionalOnBean(name = { "eximeeBpmsBpmJdbcTemplate", "camundaBpmJdbcTemplate", "historyLevelAutoConfiguration", "historyLevelDeterminator" })
  @ConditionalOnMissingBean(name = "historyLevelDeterminator")
  public static HistoryLevelDeterminator historyLevelDeterminatorMultiDatabase(EximeeBpmsBpmProperties eximeeBpmsBpmProperties,
      @Qualifier("eximeeBpmsBpmJdbcTemplate") JdbcTemplate jdbcTemplate) {
    return createHistoryLevelDeterminator(eximeeBpmsBpmProperties, jdbcTemplate);
  }

  @Bean
  @ConditionalOnMissingBean(EximeeBpmsAuthorizationConfiguration.class)
  public static EximeeBpmsAuthorizationConfiguration eximeeBpmsAuthorizationConfiguration() {
    return new DefaultAuthorizationConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(EximeeBpmsDeploymentConfiguration.class)
  public static EximeeBpmsDeploymentConfiguration eximeeBpmsDeploymentConfiguration() {
    return new DefaultDeploymentConfiguration();
  }

  @Bean
  public GenericPropertiesConfiguration genericPropertiesConfiguration() {
    return new GenericPropertiesConfiguration();
  }

  @Bean
  @ConditionalOnProperty(prefix = "eximeebpms.bpm.admin-user", name = "id")
  public CreateAdminUserConfiguration createAdminUserConfiguration() {
    return new CreateAdminUserConfiguration();
  }

  @Bean
  @ConditionalOnMissingBean(EximeeBpmsFailedJobConfiguration.class)
  public static EximeeBpmsFailedJobConfiguration failedJobConfiguration() {
    return new DefaultFailedJobConfiguration();
  }

  @Bean
  @ConditionalOnProperty(prefix = "eximeebpms.bpm.filter", name = "create")
  public CreateFilterConfiguration createFilterConfiguration() {
    return new CreateFilterConfiguration();
  }

  @Bean
  public EventPublisherPlugin eventPublisherPlugin(EximeeBpmsBpmProperties properties, ApplicationEventPublisher publisher) {
    return new EventPublisherPlugin(properties.getEventing(), publisher);
  }

  @Bean
  public EximeeBpmsIntegrationDeterminator eximeeBpmsIntegrationDeterminator() {
    return new EximeeBpmsIntegrationDeterminator();
  }
}
