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

import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.ManagementService;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbAwareScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the {@link ManagementService} into {@link DbAwareScriptSecurityPolicy} after the engine
 * starts, and writes the initial config to {@code ACT_GE_PROPERTY} if absent (first-start
 * bootstrap).
 *
 * <p>Periodic script violation cleanup (retention) is <em>not</em> handled here — it's an
 * engine-native, self-rescheduling job ({@code ScriptViolationCleanupJobHandler}, bootstrapped by
 * {@code BootstrapEngineCommand}) that runs identically on every deployment model, Spring Boot
 * included, so no Spring {@code @Scheduled} wiring is needed.
 */
@Slf4j
@Configuration
public class ScriptSecurityAutoConfiguration {

  @Bean
  public ScriptSecurityEngineWiring scriptSecurityEngineWiring(ProcessEngine processEngine) {
    return new ScriptSecurityEngineWiring(processEngine);
  }

  /**
   * Eagerly wires {@link ManagementService} into the policy and seeds initial DB properties.
   * Extracted into its own class so it can be tested independently.
   */
  public static class ScriptSecurityEngineWiring {

    public ScriptSecurityEngineWiring(ProcessEngine processEngine) {
      final ProcessEngineConfigurationImpl config =
          (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
      final ScriptSecurityPolicy policy = config.getScriptSecurityPolicy();

      if (!(policy instanceof DbAwareScriptSecurityPolicy dbAware)) {
        return;
      }

      // wireAndSeed() reads the initial mode/allowlist from the policy's own initialConfig —
      // already bound from this same eximeebpms.bpm.script-security.* environment by
      // DefaultProcessEngineConfiguration.configureScriptSecurity() — so no need to re-bind here.
      dbAware.wireAndSeed(processEngine.getManagementService());
      log.info("Wired ManagementService into DbAwareScriptSecurityPolicy; seeded initial script security config to DB if absent");
    }
  }
}
