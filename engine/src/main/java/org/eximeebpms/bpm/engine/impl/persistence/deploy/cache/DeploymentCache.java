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
package org.eximeebpms.bpm.engine.impl.persistence.deploy.cache;

import org.eximeebpms.bpm.engine.ProcessEngineException;
import org.eximeebpms.bpm.engine.impl.ProcessEngineLogger;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionQueryImpl;
import org.eximeebpms.bpm.engine.impl.persistence.deploy.Deployer;
import org.eximeebpms.bpm.engine.impl.persistence.entity.CamundaFormDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.repository.DecisionDefinition;
import org.eximeebpms.bpm.engine.repository.DecisionRequirementsDefinition;
import org.eximeebpms.bpm.model.bpmn.BpmnModelInstance;
import org.eximeebpms.bpm.model.dmn.DmnModelInstance;
import org.eximeebpms.commons.utils.cache.Cache;

import java.util.List;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class DeploymentCache {

  protected ProcessDefinitionCache processDefinitionEntityCache;
  protected DecisionDefinitionCache decisionDefinitionCache;
  protected DecisionRequirementsDefinitionCache decisionRequirementsDefinitionCache;
  protected CamundaFormDefinitionCache camundaFormDefinitionCache;


  protected BpmnModelInstanceCache bpmnModelInstanceCache;
  protected DmnModelInstanceCache dmnModelInstanceCache;
  protected CacheDeployer cacheDeployer = new CacheDeployer();

  public DeploymentCache(CacheFactory factory, int cacheCapacity) {
    processDefinitionEntityCache = new ProcessDefinitionCache(factory, cacheCapacity, cacheDeployer);
    decisionDefinitionCache = new DecisionDefinitionCache(factory, cacheCapacity, cacheDeployer);
    decisionRequirementsDefinitionCache = new DecisionRequirementsDefinitionCache(factory, cacheCapacity, cacheDeployer);
    camundaFormDefinitionCache = new CamundaFormDefinitionCache(factory, cacheCapacity, cacheDeployer);

    bpmnModelInstanceCache = new BpmnModelInstanceCache(factory, cacheCapacity, processDefinitionEntityCache);
    dmnModelInstanceCache = new DmnModelInstanceCache(factory, cacheCapacity, decisionDefinitionCache);
  }

  public void deploy(final DeploymentEntity deployment) {
    cacheDeployer.deploy(deployment);
  }

  // PROCESS DEFINITION ////////////////////////////////////////////////////////////////////////////////

  public ProcessDefinitionEntity findProcessDefinitionFromCache(String processDefinitionId) {
    return processDefinitionEntityCache.findDefinitionFromCache(processDefinitionId);
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionById(String processDefinitionId) {
    return processDefinitionEntityCache.findDeployedDefinitionById(processDefinitionId);
  }

  /**
   * @return the latest version of the process definition with the given key (from any tenant)
   * @throws ProcessEngineException if more than one tenant has a process definition with the given key
   * @see #findDeployedLatestProcessDefinitionByKeyAndTenantId(String, String)
   */
  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKey(String processDefinitionKey) {
    return processDefinitionEntityCache.findDeployedLatestDefinitionByKey(processDefinitionKey);
  }

  /**
   * @return the latest version of the process definition with the given key and tenant id
   */
  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKeyAndTenantId(String processDefinitionKey, String tenantId) {
    return processDefinitionEntityCache.findDeployedLatestDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionByKeyVersionAndTenantId(final String processDefinitionKey, final Integer processDefinitionVersion, final String tenantId) {
    return processDefinitionEntityCache.findDeployedDefinitionByKeyVersionAndTenantId(processDefinitionKey, processDefinitionVersion, tenantId);
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionByKeyVersionTagAndTenantId(String processDefinitionKey, String processDefinitionVersionTag, String tenantId) {
    return processDefinitionEntityCache.findDeployedDefinitionByKeyVersionTagAndTenantId(processDefinitionKey, processDefinitionVersionTag, tenantId);
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    return processDefinitionEntityCache.findDeployedDefinitionByDeploymentAndKey(deploymentId, processDefinitionKey);
  }

  public ProcessDefinitionEntity resolveProcessDefinition(ProcessDefinitionEntity processDefinition) {
    return processDefinitionEntityCache.resolveDefinition(processDefinition);
  }

  public BpmnModelInstance findBpmnModelInstanceForProcessDefinition(ProcessDefinitionEntity processDefinitionEntity) {
    return bpmnModelInstanceCache.findBpmnModelInstanceForDefinition(processDefinitionEntity);
  }

  public BpmnModelInstance findBpmnModelInstanceForProcessDefinition(String processDefinitionId) {
    return bpmnModelInstanceCache.findBpmnModelInstanceForDefinition(processDefinitionId);
  }

  public void addProcessDefinition(ProcessDefinitionEntity processDefinition) {
    processDefinitionEntityCache.addDefinition(processDefinition);
  }

  public void removeProcessDefinition(String processDefinitionId) {
    processDefinitionEntityCache.removeDefinitionFromCache(processDefinitionId);
    bpmnModelInstanceCache.remove(processDefinitionId);
  }

  public void discardProcessDefinitionCache() {
    processDefinitionEntityCache.clear();
    bpmnModelInstanceCache.clear();
  }

  // CAMUNDA FORM DEFINITION ////////////////////////////////////////////////////////////////////////

  public void addCamundaFormDefinition(CamundaFormDefinitionEntity camundaFormDefinition) {
    camundaFormDefinitionCache.addDefinition(camundaFormDefinition);
  }

  public void removeCamundaFormDefinition(String camundaFormDefinitionId) {
    camundaFormDefinitionCache.removeDefinitionFromCache(camundaFormDefinitionId);
  }

  public void discardCamundaFormDefinitionCache() {
    camundaFormDefinitionCache.clear();
  }

  // DECISION DEFINITION ////////////////////////////////////////////////////////////////////////////

  public DecisionDefinitionEntity findDecisionDefinitionFromCache(String decisionDefinitionId) {
    return decisionDefinitionCache.findDefinitionFromCache(decisionDefinitionId);
  }

  public DecisionDefinitionEntity findDeployedDecisionDefinitionById(String decisionDefinitionId) {
    return decisionDefinitionCache.findDeployedDefinitionById(decisionDefinitionId);
  }

  public DecisionDefinition findDeployedLatestDecisionDefinitionByKey(String decisionDefinitionKey) {
    return decisionDefinitionCache.findDeployedLatestDefinitionByKey(decisionDefinitionKey);
  }

  public DecisionDefinition findDeployedLatestDecisionDefinitionByKeyAndTenantId(String decisionDefinitionKey, String tenantId) {
    return decisionDefinitionCache.findDeployedLatestDefinitionByKeyAndTenantId(decisionDefinitionKey, tenantId);
  }

  public DecisionDefinition findDeployedDecisionDefinitionByDeploymentAndKey(String deploymentId, String decisionDefinitionKey) {
    return decisionDefinitionCache.findDeployedDefinitionByDeploymentAndKey(deploymentId, decisionDefinitionKey);
  }

  public DecisionDefinition findDeployedDecisionDefinitionByKeyAndVersion(String decisionDefinitionKey, Integer decisionDefinitionVersion) {
    return decisionDefinitionCache.findDeployedDefinitionByKeyAndVersion(decisionDefinitionKey, decisionDefinitionVersion);
  }

  public DecisionDefinition findDeployedDecisionDefinitionByKeyVersionAndTenantId(String decisionDefinitionKey, Integer decisionDefinitionVersion, String tenantId) {
    return decisionDefinitionCache.findDeployedDefinitionByKeyVersionAndTenantId(decisionDefinitionKey, decisionDefinitionVersion, tenantId);
  }

  public DecisionDefinition findDeployedDecisionDefinitionByKeyVersionTagAndTenantId(String decisionDefinitionKey, String decisionDefinitionVersionTag, String tenantId) {
    return decisionDefinitionCache.findDeployedDefinitionByKeyVersionTagAndTenantId(decisionDefinitionKey, decisionDefinitionVersionTag, tenantId);
  }

  public DecisionDefinitionEntity resolveDecisionDefinition(DecisionDefinitionEntity decisionDefinition) {
    return decisionDefinitionCache.resolveDefinition(decisionDefinition);
  }

  public DmnModelInstance findDmnModelInstanceForDecisionDefinition(String decisionDefinitionId) {
    return dmnModelInstanceCache.findBpmnModelInstanceForDefinition(decisionDefinitionId);
  }

  public void addDecisionDefinition(DecisionDefinitionEntity decisionDefinition) {
    decisionDefinitionCache.addDefinition(decisionDefinition);
  }

  public void removeDecisionDefinition(String decisionDefinitionId) {
    decisionDefinitionCache.removeDefinitionFromCache(decisionDefinitionId);
    dmnModelInstanceCache.remove(decisionDefinitionId);
  }

  public void discardDecisionDefinitionCache() {
    decisionDefinitionCache.clear();
    dmnModelInstanceCache.clear();
  }

  //DECISION REQUIREMENT DEFINITION ////////////////////////////////////////////////////////////////////////////

  public void addDecisionRequirementsDefinition(DecisionRequirementsDefinitionEntity decisionRequirementsDefinition) {
    decisionRequirementsDefinitionCache.addDefinition(decisionRequirementsDefinition);
  }

  public DecisionRequirementsDefinitionEntity findDecisionRequirementsDefinitionFromCache(String decisionRequirementsDefinitionId) {
    return decisionRequirementsDefinitionCache.findDefinitionFromCache(decisionRequirementsDefinitionId);
  }

  public DecisionRequirementsDefinitionEntity findDeployedDecisionRequirementsDefinitionById(String decisionRequirementsDefinitionId) {
    return decisionRequirementsDefinitionCache.findDeployedDefinitionById(decisionRequirementsDefinitionId);
  }

  public DecisionRequirementsDefinitionEntity resolveDecisionRequirementsDefinition(DecisionRequirementsDefinitionEntity decisionRequirementsDefinition) {
    return decisionRequirementsDefinitionCache.resolveDefinition(decisionRequirementsDefinition);
  }

  public void discardDecisionRequirementsDefinitionCache() {
    decisionDefinitionCache.clear();
  }

  public void removeDecisionRequirementsDefinition(String decisionRequirementsDefinitionId) {
    decisionRequirementsDefinitionCache.removeDefinitionFromCache(decisionRequirementsDefinitionId);
  }

  // getters and setters //////////////////////////////////////////////////////

  public Cache<String, BpmnModelInstance> getBpmnModelInstanceCache() {
    return bpmnModelInstanceCache.getCache();
  }

  public Cache<String, DmnModelInstance> getDmnDefinitionCache() {
    return dmnModelInstanceCache.getCache();
  }

  public Cache<String, DecisionDefinitionEntity> getDecisionDefinitionCache() {
    return decisionDefinitionCache.getCache();
  }

  public Cache<String, DecisionRequirementsDefinitionEntity> getDecisionRequirementsDefinitionCache() {
    return decisionRequirementsDefinitionCache.getCache();
  }

  public Cache<String, ProcessDefinitionEntity> getProcessDefinitionCache() {
    return processDefinitionEntityCache.getCache();
  }

  public void setDeployers(List<Deployer> deployers) {
    this.cacheDeployer.setDeployers(deployers);
  }

  public void removeDeployment(String deploymentId) {
    bpmnModelInstanceCache.removeAllDefinitionsByDeploymentId(deploymentId);
    if (Context.getProcessEngineConfiguration().isDmnEnabled()) {
      dmnModelInstanceCache.removeAllDefinitionsByDeploymentId(deploymentId);
      removeAllDecisionRequirementsDefinitionsByDeploymentId(deploymentId);
    }
  }

  protected void removeAllDecisionRequirementsDefinitionsByDeploymentId(String deploymentId) {
    // remove all decision requirements definitions for a specific deployment

    List<DecisionRequirementsDefinition> allDefinitionsForDeployment = new DecisionRequirementsDefinitionQueryImpl()
        .deploymentId(deploymentId)
        .list();

    for (DecisionRequirementsDefinition decisionRequirementsDefinition : allDefinitionsForDeployment) {
      try {
        removeDecisionDefinition(decisionRequirementsDefinition.getId());
      } catch (Exception e) {
        ProcessEngineLogger.PERSISTENCE_LOGGER
            .removeEntryFromDeploymentCacheFailure("decision requirement", decisionRequirementsDefinition.getId(), e);
      }
    }
  }

  public CachePurgeReport purgeCache() {

    CachePurgeReport result = new CachePurgeReport();
    Cache<String, ProcessDefinitionEntity> processDefinitionCache = getProcessDefinitionCache();
    if (!processDefinitionCache.isEmpty()) {
      result.addPurgeInformation(CachePurgeReport.PROCESS_DEF_CACHE, processDefinitionCache.keySet());
      processDefinitionCache.clear();
    }

    Cache<String, BpmnModelInstance> bpmnModelInstanceCache = getBpmnModelInstanceCache();
    if (!bpmnModelInstanceCache.isEmpty()) {
      result.addPurgeInformation(CachePurgeReport.BPMN_MODEL_INST_CACHE, bpmnModelInstanceCache.keySet());
      bpmnModelInstanceCache.clear();
    }

    Cache<String, DecisionDefinitionEntity> decisionDefinitionCache = getDecisionDefinitionCache();
    if (!decisionDefinitionCache.isEmpty()) {
      result.addPurgeInformation(CachePurgeReport.DMN_DEF_CACHE, decisionDefinitionCache.keySet());
      decisionDefinitionCache.clear();
    }

    Cache<String, DmnModelInstance> dmnModelInstanceCache = getDmnDefinitionCache();
    if (!dmnModelInstanceCache.isEmpty()) {
      result.addPurgeInformation(CachePurgeReport.DMN_MODEL_INST_CACHE, dmnModelInstanceCache.keySet());
      dmnModelInstanceCache.clear();
    }

    Cache<String, DecisionRequirementsDefinitionEntity> decisionRequirementsDefinitionCache = getDecisionRequirementsDefinitionCache();
    if (!decisionRequirementsDefinitionCache.isEmpty()) {
      result.addPurgeInformation(CachePurgeReport.DMN_REQ_DEF_CACHE, decisionRequirementsDefinitionCache.keySet());
      decisionRequirementsDefinitionCache.clear();
    }

    return result;
  }

}
