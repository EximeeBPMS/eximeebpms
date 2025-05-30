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
package org.eximeebpms.bpm.engine.test.api.multitenancy.cmmn.query.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.eximeebpms.bpm.engine.test.api.runtime.TestOrderingUtil.historicCaseActivityInstanceByTenantId;
import static org.eximeebpms.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.eximeebpms.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;

import java.util.Arrays;
import java.util.List;

import org.eximeebpms.bpm.engine.CaseService;
import org.eximeebpms.bpm.engine.HistoryService;
import org.eximeebpms.bpm.engine.IdentityService;
import org.eximeebpms.bpm.engine.ProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.exception.NullValueException;
import org.eximeebpms.bpm.engine.history.HistoricCaseActivityInstance;
import org.eximeebpms.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.eximeebpms.bpm.engine.runtime.CaseInstanceBuilder;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.RequiredHistoryLevel;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineTestRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
public class MultiTenancyHistoricCaseActivityInstanceQueryTest {

  protected final static String CMMN_FILE = "org/eximeebpms/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  protected final static String TENANT_NULL = null;
  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected HistoryService historyService;
  protected CaseService caseService;
  protected IdentityService identityService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void setUp() {
    historyService = engineRule.getHistoryService();
    caseService = engineRule.getCaseService();
    identityService = engineRule.getIdentityService();

    // given
    testRule.deployForTenant(TENANT_NULL, CMMN_FILE);
    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);
    testRule.deployForTenant(TENANT_TWO, CMMN_FILE);

    createCaseInstance(TENANT_NULL);
    createCaseInstance(TENANT_ONE);
    createCaseInstance(TENANT_TWO);
  }

  @Test
  public void shouldQueryWithoutTenantId() {
    // when
    HistoricCaseActivityInstanceQuery query = historyService
        .createHistoricCaseActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void shouldQueryFilterWithoutTenantId() {
    // when
    HistoricCaseActivityInstanceQuery query = historyService
        .createHistoricCaseActivityInstanceQuery()
        .withoutTenantId();

    // then
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void shouldQueryByTenantId() {
    // when
    HistoricCaseActivityInstanceQuery queryTenantOne = historyService
        .createHistoricCaseActivityInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    HistoricCaseActivityInstanceQuery queryTenantTwo = historyService
        .createHistoricCaseActivityInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    // then
    assertThat(queryTenantOne.count()).isEqualTo(1L);
    assertThat(queryTenantTwo.count()).isEqualTo(1L);
  }

  @Test
  public void shouldQueryByTenantIds() {
    // when
    HistoricCaseActivityInstanceQuery query = historyService
        .createHistoricCaseActivityInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    // then
    assertThat(query.count()).isEqualTo(2L);
  }

  @Test
  public void shouldQueryByNonExistingTenantId() {
    // when
    HistoricCaseActivityInstanceQuery query = historyService
        .createHistoricCaseActivityInstanceQuery()
        .tenantIdIn("nonExisting");

    // then
    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void shouldFailQueryByTenantIdNull() {
    try {
      // when
      historyService.createHistoricCaseActivityInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");

      // then
    } catch (NullValueException e) {
    }
  }

  @Test
  public void shouldQuerySortingAsc() {
    // when
    List<HistoricCaseActivityInstance> historicCaseActivityInstances = historyService
        .createHistoricCaseActivityInstanceQuery()
        .orderByTenantId()
        .asc()
        .list();

    // then
    assertThat(historicCaseActivityInstances.size()).isEqualTo(3);
    verifySorting(historicCaseActivityInstances, historicCaseActivityInstanceByTenantId());
  }

  @Test
  public void shouldQuerySortingDesc() {
    // when
    List<HistoricCaseActivityInstance> historicCaseActivityInstances = historyService.createHistoricCaseActivityInstanceQuery()
        .orderByTenantId()
        .desc()
        .list();

    // then
    assertThat(historicCaseActivityInstances.size()).isEqualTo(3);
    verifySorting(historicCaseActivityInstances, inverted(historicCaseActivityInstanceByTenantId()));
  }

  @Test
  public void shouldQueryNoAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, null);

    // when
    HistoricCaseActivityInstanceQuery query = historyService.createHistoricCaseActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(1L); // null-tenant instances are still included
  }

  @Test
  public void shouldQueryAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    HistoricCaseActivityInstanceQuery query = historyService.createHistoricCaseActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(2L);  // null-tenant instances are still included
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void shouldQueryAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    // when
    HistoricCaseActivityInstanceQuery query = historyService.createHistoricCaseActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3L); // null-tenant instances are still included
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void shouldQueryDisabledTenantCheck() {
    // given
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    // when
    HistoricCaseActivityInstanceQuery query = historyService.createHistoricCaseActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3L);
  }

  protected void createCaseInstance(String tenantId) {
    CaseInstanceBuilder builder = caseService.withCaseDefinitionByKey("oneTaskCase");
    builder.caseDefinitionTenantId(tenantId).create();
  }

}
