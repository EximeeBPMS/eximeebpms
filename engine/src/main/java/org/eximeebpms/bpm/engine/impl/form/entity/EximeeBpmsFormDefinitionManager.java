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
package org.eximeebpms.bpm.engine.impl.form.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eximeebpms.bpm.engine.impl.ProcessEngineLogger;
import org.eximeebpms.bpm.engine.impl.db.EnginePersistenceLogger;
import org.eximeebpms.bpm.engine.impl.db.ListQueryParameterObject;
import org.eximeebpms.bpm.engine.impl.persistence.AbstractManager;
import org.eximeebpms.bpm.engine.impl.persistence.AbstractResourceDefinitionManager;
import org.eximeebpms.bpm.engine.impl.persistence.entity.EximeeBpmsFormDefinitionEntity;

public class EximeeBpmsFormDefinitionManager extends AbstractManager
    implements AbstractResourceDefinitionManager<EximeeBpmsFormDefinitionEntity> {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected static final String KEY_PARAMETER = "camundaFormDefinitionKey";

  @Override
  public EximeeBpmsFormDefinitionEntity findLatestDefinitionByKey(String key) {
    @SuppressWarnings("unchecked")
    List<EximeeBpmsFormDefinitionEntity> camundaFormDefinitions = getDbEntityManager()
        .selectList("selectLatestEximeeBpmsFormDefinitionByKey", configureParameterizedQuery(key));

    if (camundaFormDefinitions.isEmpty()) {
      return null;

    } else if (camundaFormDefinitions.size() == 1) {
      return camundaFormDefinitions.iterator().next();

    } else {
      throw LOG.multipleTenantsForEximeeBpmsFormDefinitionKeyException(key);
    }
  }

  @Override
  public EximeeBpmsFormDefinitionEntity findLatestDefinitionById(String id) {
    return getDbEntityManager().selectById(EximeeBpmsFormDefinitionEntity.class, id);
  }

  @Override
  public EximeeBpmsFormDefinitionEntity findLatestDefinitionByKeyAndTenantId(String definitionKey, String tenantId) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(KEY_PARAMETER, definitionKey);
    parameters.put("tenantId", tenantId);

    if (tenantId == null) {
      return (EximeeBpmsFormDefinitionEntity) getDbEntityManager()
          .selectOne("selectLatestEximeeBpmsFormDefinitionByKeyWithoutTenantId", parameters);
    } else {
      return (EximeeBpmsFormDefinitionEntity) getDbEntityManager()
          .selectOne("selectLatestEximeeBpmsFormDefinitionByKeyAndTenantId", parameters);
    }
  }

  @Override
  public EximeeBpmsFormDefinitionEntity findDefinitionByKeyVersionAndTenantId(String definitionKey,
      Integer definitionVersion, String tenantId) {

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("camundaFormDefinitionVersion", definitionVersion);
    parameters.put(KEY_PARAMETER, definitionKey);
    parameters.put("tenantId", tenantId);
    if (tenantId == null) {
      return (EximeeBpmsFormDefinitionEntity) getDbEntityManager()
          .selectOne("selectEximeeBpmsFormDefinitionByKeyVersionWithoutTenantId", parameters);
    } else {
      return (EximeeBpmsFormDefinitionEntity) getDbEntityManager()
          .selectOne("selectEximeeBpmsFormDefinitionByKeyVersionAndTenantId", parameters);
    }
  }

  @Override
  public EximeeBpmsFormDefinitionEntity findDefinitionByDeploymentAndKey(String deploymentId, String definitionKey) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("deploymentId", deploymentId);
    parameters.put(KEY_PARAMETER, definitionKey);
    return (EximeeBpmsFormDefinitionEntity) getDbEntityManager().selectOne("selectEximeeBpmsFormDefinitionByDeploymentAndKey",
        parameters);
  }

  @SuppressWarnings("unchecked")
  public List<EximeeBpmsFormDefinitionEntity> findDefinitionsByDeploymentId(String deploymentId) {
    return getDbEntityManager().selectList("selectEximeeBpmsFormDefinitionByDeploymentId", deploymentId);
  }

  @Override
  public EximeeBpmsFormDefinitionEntity getCachedResourceDefinitionEntity(String definitionId) {
    return getDbEntityManager().getCachedEntity(EximeeBpmsFormDefinitionEntity.class, definitionId);
  }

  @Override
  public EximeeBpmsFormDefinitionEntity findDefinitionByKeyVersionTagAndTenantId(String definitionKey,
      String definitionVersionTag, String tenantId) {
    throw new UnsupportedOperationException(
        "Currently finding Camunda Form definition by version tag and tenant is not implemented.");
  }

  public void deleteEximeeBpmsFormDefinitionsByDeploymentId(String deploymentId) {
    getDbEntityManager().delete(EximeeBpmsFormDefinitionEntity.class, "deleteEximeeBpmsFormDefinitionsByDeploymentId",
        deploymentId);
  }

  protected ListQueryParameterObject configureParameterizedQuery(Object parameter) {
    return getTenantManager().configureQuery(parameter);
  }

}
