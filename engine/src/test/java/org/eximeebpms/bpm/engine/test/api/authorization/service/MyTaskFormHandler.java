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
package org.eximeebpms.bpm.engine.test.api.authorization.service;

import org.eximeebpms.bpm.engine.IdentityService;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.delegate.VariableScope;
import org.eximeebpms.bpm.engine.form.TaskFormData;
import org.eximeebpms.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.form.TaskFormDataImpl;
import org.eximeebpms.bpm.engine.impl.form.handler.TaskFormHandler;
import org.eximeebpms.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.TaskEntity;
import org.eximeebpms.bpm.engine.impl.util.xml.Element;
import org.eximeebpms.bpm.engine.variable.VariableMap;

/**
 * @author Roman Smirnov
 *
 */
public class MyTaskFormHandler extends MyDelegationService implements TaskFormHandler {

  public void parseConfiguration(Element activityElement, DeploymentEntity deployment, ProcessDefinitionEntity processDefinition, BpmnParse bpmnParse) {
    // do nothing
  }

  public void submitFormVariables(VariableMap properties, VariableScope variableScope) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    IdentityService identityService = processEngineConfiguration.getIdentityService();
    RuntimeService runtimeService = processEngineConfiguration.getRuntimeService();

    logAuthentication(identityService);
    logInstancesCount(runtimeService);
  }

  public TaskFormData createTaskForm(TaskEntity task) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    IdentityService identityService = processEngineConfiguration.getIdentityService();
    RuntimeService runtimeService = processEngineConfiguration.getRuntimeService();

    logAuthentication(identityService);
    logInstancesCount(runtimeService);

    TaskFormDataImpl result = new TaskFormDataImpl();
    result.setTask(task);
    return result;
  }

}
