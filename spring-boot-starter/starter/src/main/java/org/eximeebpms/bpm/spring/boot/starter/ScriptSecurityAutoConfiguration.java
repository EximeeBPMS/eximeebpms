/*
 * Copyright EximeeBPMS contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbAwareScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationStore;
import org.eximeebpms.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.eximeebpms.bpm.spring.boot.starter.property.ScriptSecurityProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Wires the {@link ManagementService} into {@link DbAwareScriptSecurityPolicy} after the engine
 * starts, and writes the initial config to {@code ACT_GE_PROPERTY} if absent (first-start
 * bootstrap).
 *
 * <p>Also registers a periodic cleanup job when
 * {@code eximeebpms.bpm.script-security.retention-days} is set to a positive value.
 * Requires {@code @EnableScheduling} on the application to activate the cleanup schedule.
 */
@Slf4j
@Configuration
public class ScriptSecurityAutoConfiguration {

  @Bean
  public ScriptSecurityEngineWiring scriptSecurityEngineWiring(ProcessEngine processEngine, Environment environment) {
    return new ScriptSecurityEngineWiring(processEngine, environment);
  }

  @Bean
  @ConditionalOnProperty(prefix = CamundaBpmProperties.PREFIX + ".script-security", name = "retention-days")
  public ScriptViolationCleanupJob scriptViolationCleanupJob(ProcessEngine processEngine, Environment environment) {
    ScriptSecurityProperty props = Binder.get(environment)
        .bind(CamundaBpmProperties.PREFIX + ".script-security", ScriptSecurityProperty.class)
        .orElseGet(ScriptSecurityProperty::new);
    if (props.getRetentionDays() <= 0) {
      return null;
    }
    DbScriptViolationStore store = resolveDbStore(processEngine);
    if (store == null) {
      log.warn("Script violation cleanup configured (retentionDays={}) but no DbScriptViolationStore found — cleanup disabled",
          props.getRetentionDays());
      return null;
    }
    return new ScriptViolationCleanupJob(store, props.getRetentionDays());
  }

  private static DbScriptViolationStore resolveDbStore(ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl config =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    ScriptViolationStore store = config.getScriptViolationStore();
    if (store instanceof DbScriptViolationStore db) {
      return db;
    }
    return null;
  }

  /**
   * Eagerly wires {@link ManagementService} into the policy and seeds initial DB properties.
   * Extracted into its own class so it can be tested independently.
   */
  public static class ScriptSecurityEngineWiring {

    public ScriptSecurityEngineWiring(ProcessEngine processEngine, Environment environment) {
      final ProcessEngineConfigurationImpl config =
          (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
      final ScriptSecurityPolicy policy = config.getScriptSecurityPolicy();

      if (!(policy instanceof DbAwareScriptSecurityPolicy dbAware)) {
        return;
      }

      final ManagementService management = processEngine.getManagementService();
      dbAware.setManagementService(management);

      final Map<String, String> existingProps = management.getProperties();
      if (!existingProps.containsKey(DbAwareScriptSecurityPolicy.PROP_MODE)) {
        final ScriptSecurityProperty scriptSecurity = Binder.get(environment)
            .bind(CamundaBpmProperties.PREFIX + ".script-security", ScriptSecurityProperty.class)
            .orElseGet(ScriptSecurityProperty::new);
        management.setProperty(DbAwareScriptSecurityPolicy.PROP_MODE, scriptSecurity.getMode().name());
        management.setProperty(DbAwareScriptSecurityPolicy.PROP_ALLOWLIST,
            String.join(",", scriptSecurity.getAllowlistedProcessDefinitionKeys()));
        log.info("Wrote initial script security config to DB: mode={}", scriptSecurity.getMode());
      }
    }
  }

  /**
   * Deletes script violation records older than the configured retention period.
   * Runs daily at midnight (configurable via {@code spring.task.scheduling}).
   * Activated only when {@code retentionDays > 0}.
   */
  public record ScriptViolationCleanupJob(DbScriptViolationStore store, int retentionDays) {

    private static final Logger log = LoggerFactory.getLogger(ScriptViolationCleanupJob.class);

    @Scheduled(cron = "${eximeebpms.bpm.script-security.cleanup-cron:0 0 0 * * *}")
    public void cleanup() {
      log.debug("Running script violation cleanup, retentionDays={}", retentionDays);
      store.cleanupOlderThan(retentionDays);
      log.info("Script violation cleanup completed, records older than {} days deleted", retentionDays);
    }
  }
}
