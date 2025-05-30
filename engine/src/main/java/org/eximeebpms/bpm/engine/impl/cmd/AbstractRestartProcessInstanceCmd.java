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
package org.eximeebpms.bpm.engine.impl.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eximeebpms.bpm.engine.BadUserRequestException;
import org.eximeebpms.bpm.engine.history.UserOperationLogEntry;
import org.eximeebpms.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.eximeebpms.bpm.engine.impl.RestartProcessInstanceBuilderImpl;
import org.eximeebpms.bpm.engine.impl.interceptor.Command;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.PropertyChange;
import org.eximeebpms.bpm.engine.impl.util.EnsureUtil;
import org.eximeebpms.bpm.engine.repository.ProcessDefinition;

public abstract class AbstractRestartProcessInstanceCmd<T> implements Command<T> {

  protected CommandExecutor commandExecutor;
  protected RestartProcessInstanceBuilderImpl builder;

  public AbstractRestartProcessInstanceCmd(CommandExecutor commandExecutor, RestartProcessInstanceBuilderImpl builder) {
    this.commandExecutor = commandExecutor;
    this.builder = builder;
  }

  protected Collection<String> collectProcessInstanceIds() {

    Set<String> collectedProcessInstanceIds = new HashSet<String>();

    List<String> processInstanceIds = builder.getProcessInstanceIds();
    if (processInstanceIds != null) {
      collectedProcessInstanceIds.addAll(processInstanceIds);
    }

    final HistoricProcessInstanceQueryImpl historicProcessInstanceQuery = (HistoricProcessInstanceQueryImpl) builder.getHistoricProcessInstanceQuery();
    if (historicProcessInstanceQuery != null) {
      collectedProcessInstanceIds.addAll(historicProcessInstanceQuery.listIds());
    }

    EnsureUtil.ensureNotEmpty(BadUserRequestException.class, "processInstanceIds", collectedProcessInstanceIds);
    return collectedProcessInstanceIds;
  }
  
  protected void writeUserOperationLog(CommandContext commandContext,
      ProcessDefinition processDefinition,
      int numInstances,
      boolean async) {

    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("nrOfInstances",
        null,
        numInstances));
    propertyChanges.add(new PropertyChange("async", null, async));

    commandContext.getOperationLogManager()
      .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_RESTART_PROCESS_INSTANCE,
          null,
          processDefinition.getId(),
          processDefinition.getKey(),
          propertyChanges);
  }

  protected ProcessDefinitionEntity getProcessDefinition(CommandContext commandContext, String processDefinitionId) {

    return commandContext
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedProcessDefinitionById(processDefinitionId);
  }
}
