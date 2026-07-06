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
package org.eximeebpms.bpm.engine.test.cmmn.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import org.eximeebpms.bpm.engine.impl.cmmn.cmd.CheckCmmnUsageCmd;
import org.eximeebpms.bpm.engine.repository.Deployment;
import org.eximeebpms.bpm.engine.test.util.PluggableProcessEngineTest;
import org.eximeebpms.commons.testing.ProcessEngineLoggingRule;
import org.eximeebpms.commons.testing.WatchLogger;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

public class CmmnDeprecationWarningTest extends PluggableProcessEngineTest {

  protected static final String CONFIG_LOGGER = "org.eximeebpms.bpm.engine.cfg";

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch(CONFIG_LOGGER)
      .level(Level.WARN);

  protected String deploymentId;

  @After
  public void tearDown() {
    if (deploymentId != null) {
      repositoryService.deleteDeployment(deploymentId, true);
      deploymentId = null;
    }
  }

  @Test
  @WatchLogger(loggerNames = {CONFIG_LOGGER}, level = "WARN")
  public void shouldLogWarningWhenDeployingCmmnResource() {
    // when
    Deployment deployment = repositoryService
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment.cmmn")
        .deploy();
    deploymentId = deployment.getId();

    // then
    assertThat(loggingRule.getFilteredLog("ENGINE-12023")).hasSize(1);
    assertThat(loggingRule.getFilteredLog("CMMN support is DEPRECATED")).hasSize(1);
  }

  @Test
  @WatchLogger(loggerNames = {CONFIG_LOGGER}, level = "WARN")
  public void shouldLogWarningWhenCmmnUsageDetectedAtStartup() {
    // given
    Deployment deployment = repositoryService
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment.cmmn")
        .deploy();
    deploymentId = deployment.getId();

    // when the startup detection command runs again (e.g. on the next process engine build)
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new CheckCmmnUsageCmd());

    // then
    assertThat(loggingRule.getFilteredLog("ENGINE-12022")).hasSize(1);
  }

  @Test
  @WatchLogger(loggerNames = {CONFIG_LOGGER}, level = "WARN")
  public void shouldNotLogWarningWhenNoCmmnDataPresent() {
    // when
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new CheckCmmnUsageCmd());

    // then
    assertThat(loggingRule.getFilteredLog("ENGINE-12022")).isEmpty();
  }

}
