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
package org.eximeebpms.bpm.engine.test.api.multitenancy.query;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eximeebpms.bpm.engine.IdentityService;
import org.eximeebpms.bpm.engine.ManagementService;
import org.eximeebpms.bpm.engine.ProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.management.IncidentStatistics;
import org.eximeebpms.bpm.engine.management.ProcessDefinitionStatistics;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.api.multitenancy.StaticTenantIdTestProvider;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineTestRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.eximeebpms.bpm.model.bpmn.Bpmn;
import org.eximeebpms.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */
public class MultiTenancySharedProcessDefinitionStatisticsQueryTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String ONE_TASK_PROCESS_DEFINITION_KEY = "oneTaskProcess";
  protected static final String FAILED_JOBS_PROCESS_DEFINITION_KEY = "ExampleProcess";

  protected static StaticTenantIdTestProvider tenantIdProvider;

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
    tenantIdProvider = new StaticTenantIdTestProvider(TENANT_ONE);
    configuration.setTenantIdProvider(tenantIdProvider);
  });
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain tenantRuleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected IdentityService identityService;
  protected ProcessEngineConfiguration processEngineConfiguration;

  protected static final BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(ONE_TASK_PROCESS_DEFINITION_KEY)
    .startEvent()
    .userTask()
    .done();

  protected static final BpmnModelInstance failingProcess = Bpmn.createExecutableProcess(FAILED_JOBS_PROCESS_DEFINITION_KEY)
    .startEvent()
    .serviceTask()
      .camundaClass("org.eximeebpms.bpm.engine.test.api.multitenancy.FailingDelegate")
      .camundaAsyncBefore()
    .done();


  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    identityService = engineRule.getIdentityService();
    managementService = engineRule.getManagementService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }
  
  @Test
  public void activeProcessInstancesCountWithNoAuthenticatedTenant() {

    testRule.deploy(oneTaskProcess);

    startProcessInstances(ONE_TASK_PROCESS_DEFINITION_KEY);

    identityService.setAuthentication("user", null, null);
    
    List<ProcessDefinitionStatistics> processDefinitionsStatistics = managementService
      .createProcessDefinitionStatisticsQuery()
      .list();
 
    // then
    assertEquals(1, processDefinitionsStatistics.size());
    // user must see only the process instances that belongs to no tenant
    assertEquals(1, processDefinitionsStatistics.get(0).getInstances());
    
  }

  @Test
  public void activeProcessInstancesCountWithAuthenticatedTenant() {

    testRule.deploy(oneTaskProcess);
    
    startProcessInstances(ONE_TASK_PROCESS_DEFINITION_KEY);
    
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));
    
    List<ProcessDefinitionStatistics> processDefinitionsStatistics = managementService
      .createProcessDefinitionStatisticsQuery()
      .list();
 
    // then
    assertEquals(1, processDefinitionsStatistics.size());
    // user can see the process instances that belongs to tenant1 and instances that have no tenant  
    assertEquals(2, processDefinitionsStatistics.get(0).getInstances());
    
  }

  @Test
  public void activeProcessInstancesCountWithDisabledTenantCheck() {

    testRule.deploy(oneTaskProcess);
    
    startProcessInstances(ONE_TASK_PROCESS_DEFINITION_KEY);
    
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);
    
    List<ProcessDefinitionStatistics> processDefinitionsStatistics = managementService
      .createProcessDefinitionStatisticsQuery()
      .list();
 
    // then
    assertEquals(1, processDefinitionsStatistics.size());
    assertEquals(3, processDefinitionsStatistics.get(0).getInstances());
  }

  @Test
  public void activeProcessInstancesCountWithMultipleAuthenticatedTenants() {

    testRule.deploy(oneTaskProcess);

    startProcessInstances(ONE_TASK_PROCESS_DEFINITION_KEY);
    
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));
    
    List<ProcessDefinitionStatistics> processDefinitionsStatistics = managementService
      .createProcessDefinitionStatisticsQuery()
      .list();
 
    // then
    assertEquals(1, processDefinitionsStatistics.size());
    // user can see all the active process instances 
    assertEquals(3, processDefinitionsStatistics.get(0).getInstances());
    
  }

  @Test
  public void failedJobsCountWithWithNoAuthenticatedTenant() {

    testRule.deploy(failingProcess);
    
    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    testRule.executeAvailableJobs();

    identityService.setAuthentication("user", null, null);

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    assertEquals(1, processDefinitionsStatistics.get(0).getFailedJobs());
    
  }

  @Test
  public void failedJobsCountWithWithDisabledTenantCheck() {

    testRule.deploy(failingProcess);
    
    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    testRule.executeAvailableJobs();

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    assertEquals(3, processDefinitionsStatistics.get(0).getFailedJobs());
    
  }

  @Test
  public void failedJobsCountWithAuthenticatedTenant() {

    testRule.deploy(failingProcess);

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    testRule.executeAvailableJobs();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    assertEquals(2, processDefinitionsStatistics.get(0).getFailedJobs());
  }

  @Test
  public void failedJobsCountWithMultipleAuthenticatedTenants() {

    testRule.deploy(failingProcess);

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    testRule.executeAvailableJobs();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    assertEquals(3, processDefinitionsStatistics.get(0).getFailedJobs());
  }

  @Test
  public void incidentsCountWithNoAuthenticatedTenant() {

    testRule.deploy(failingProcess);

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    testRule.executeAvailableJobs();

    identityService.setAuthentication("user", null, null);

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    
    List<IncidentStatistics> incidentStatistics = processDefinitionsStatistics.get(0).getIncidentStatistics();
    assertEquals(1, incidentStatistics.size());
    assertEquals(1, incidentStatistics.get(0).getIncidentCount());
  }

  @Test
  public void incidentsCountWithDisabledTenantCheck() {

    testRule.deploy(failingProcess);

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    testRule.executeAvailableJobs();

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    
    List<IncidentStatistics> incidentStatistics = processDefinitionsStatistics.get(0).getIncidentStatistics();
    assertEquals(1, incidentStatistics.size());
    assertEquals(3, incidentStatistics.get(0).getIncidentCount());
  }

  @Test
  public void incidentsCountWithAuthenticatedTenant() {

    testRule.deploy(failingProcess);

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    testRule.executeAvailableJobs();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    
    List<IncidentStatistics> incidentStatistics = processDefinitionsStatistics.get(0).getIncidentStatistics();
    assertEquals(1, incidentStatistics.size());
    assertEquals(2, incidentStatistics.get(0).getIncidentCount());
  }

  @Test
  public void incidentsCountWithMultipleAuthenticatedTenants() {

    testRule.deploy(failingProcess);

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    testRule.executeAvailableJobs();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    List<IncidentStatistics> incidentStatistics = processDefinitionsStatistics.get(0).getIncidentStatistics();
    assertEquals(1, incidentStatistics.size());
    assertEquals(3, incidentStatistics.get(0).getIncidentCount());
  }

  @Test
  public void incidentsCountWithIncidentTypeAndAuthenticatedTenant() {

    testRule.deploy(failingProcess);

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    testRule.executeAvailableJobs();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidentsForType("failedJob")
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    
    List<IncidentStatistics> incidentStatistics = processDefinitionsStatistics.get(0).getIncidentStatistics();
    assertEquals(1, incidentStatistics.size());
    assertEquals(2, incidentStatistics.get(0).getIncidentCount());
  }

  @Test
  public void instancesFailedJobsAndIncidentsCountWithAuthenticatedTenant() {

    testRule.deploy(failingProcess);

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    testRule.executeAvailableJobs();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .includeIncidents()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    ProcessDefinitionStatistics processDefinitionStatistics = processDefinitionsStatistics.get(0);
    assertEquals(2, processDefinitionStatistics.getInstances());
    assertEquals(2, processDefinitionStatistics.getFailedJobs());
    
    List<IncidentStatistics> incidentStatistics = processDefinitionStatistics.getIncidentStatistics();
    assertEquals(1, incidentStatistics.size());
    assertEquals(2, incidentStatistics.get(0).getIncidentCount());
  }

  protected void startProcessInstances(String key) {
    setTenantIdProvider(null);
    runtimeService.startProcessInstanceByKey(key);

    setTenantIdProvider(TENANT_ONE);
    runtimeService.startProcessInstanceByKey(key);
    
    setTenantIdProvider(TENANT_TWO);
    runtimeService.startProcessInstanceByKey(key);  
  }

  protected void setTenantIdProvider(String tenantId) {
    tenantIdProvider.setTenantIdProvider(tenantId);
  }
}
