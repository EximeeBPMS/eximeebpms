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

import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.ProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.junit.After;
import org.junit.Test;

/**
 * Verifies the "test your environment's readiness for 1.4.0" scenario described in the
 * CMMN migration guide: an engine started with {@code cmmnEnabled=false} must start up
 * successfully even when CMMN data was left behind by a previous session, rather than
 * failing fast (fail-fast on active case instances is reserved for 1.4.0, not 1.3.0).
 */
public class CmmnDisableReadinessTest {

  protected static final String JDBC_URL = "jdbc:h2:mem:cmmn-disable-readiness-test;DB_CLOSE_DELAY=1000";
  protected static final String ENGINE_NAME = "cmmn-disable-readiness-test-engine";

  // keeps the in-memory database alive across the engine close()/rebuild in the test below
  protected ProcessEngine schemaHoldingEngine;
  protected ProcessEngine processEngine;

  @After
  public void tearDown() {
    if (processEngine != null) {
      processEngine.close();
    }
    if (schemaHoldingEngine != null) {
      schemaHoldingEngine.close();
    }
  }

  @Test
  public void shouldStartSuccessfullyWithCmmnDisabledWhenLegacyCmmnDataExists() {
    // given a schema-holding engine so the in-memory database survives the engine rebuild below
    StandaloneInMemProcessEngineConfiguration schemaConfiguration = new StandaloneInMemProcessEngineConfiguration();
    schemaConfiguration.setProcessEngineName("cmmn-disable-readiness-schema-holder");
    schemaConfiguration.setJdbcUrl(JDBC_URL);
    schemaHoldingEngine = schemaConfiguration.buildProcessEngine();

    // and a first engine session (CMMN enabled, the 1.3.0 default) that deploys a case definition
    // and starts a case instance, leaving CMMN data behind for the next session
    StandaloneProcessEngineConfiguration firstSessionConfiguration = new StandaloneProcessEngineConfiguration();
    firstSessionConfiguration.setProcessEngineName(ENGINE_NAME);
    firstSessionConfiguration.setJdbcUrl(JDBC_URL);
    firstSessionConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
    firstSessionConfiguration.setJobExecutorActivate(false);
    firstSessionConfiguration.setEnforceHistoryTimeToLive(false);
    processEngine = firstSessionConfiguration.buildProcessEngine();

    processEngine.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment.cmmn")
        .deploy();
    processEngine.getCaseService().createCaseInstanceByKey("Case_1");

    processEngine.close();

    // when a new engine session starts with CMMN disabled, simulating a 1.4.0 readiness test
    StandaloneProcessEngineConfiguration secondSessionConfiguration = new StandaloneProcessEngineConfiguration();
    secondSessionConfiguration.setProcessEngineName(ENGINE_NAME);
    secondSessionConfiguration.setJdbcUrl(JDBC_URL);
    secondSessionConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
    secondSessionConfiguration.setJobExecutorActivate(false);
    secondSessionConfiguration.setCmmnEnabled(false);
    processEngine = secondSessionConfiguration.buildProcessEngine();

    // then startup succeeds without failing fast, and the leftover CMMN data is ignored
    assertThat(processEngine).isNotNull();
    assertThat(processEngine.getRepositoryService().createCaseDefinitionQuery().count()).isEqualTo(0);
  }

}
