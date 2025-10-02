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
package org.eximeebpms.bpm.engine.rest.dto.task;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.eximeebpms.bpm.engine.BadUserRequestException;
import org.eximeebpms.bpm.engine.form.CamundaFormRef;
import org.eximeebpms.bpm.engine.rest.dto.converter.DelegationStateConverter;
import org.eximeebpms.bpm.engine.task.DelegationState;
import org.eximeebpms.bpm.engine.task.Task;

@Getter
public class TaskDto {

  @Setter
  private String id;
  @Setter
  private String name;
  @Setter
  private String assignee;
  private Date created;
  @Setter
  private Date due;
  @Setter
  private Date followUp;
  @Setter
  private Date lastUpdated;
  @Setter
  private String delegationState;
  @Setter
  private String description;
  private String executionId;
  @Setter
  private String owner;
  @Setter
  private String parentTaskId;
  @Setter
  private int priority;
  private String processDefinitionId;
  private String processInstanceId;
  private String taskDefinitionKey;
  private String caseExecutionId;
  @Setter
  private String caseInstanceId;
  private String caseDefinitionId;
  private boolean suspended;
  private String formKey;
  private CamundaFormRef camundaFormRef;
  @Setter
  private String tenantId;
  /**
   * Returns task State of task
   */
  @Setter
  private String taskState;

  public TaskDto() {
  }

  public TaskDto(Task task) {
    this.id = task.getId();
    this.name = task.getName();
    this.assignee = task.getAssignee();
    this.created = task.getCreateTime();
    this.lastUpdated = task.getLastUpdated();
    this.due = task.getDueDate();
    this.followUp = task.getFollowUpDate();

    if (task.getDelegationState() != null) {
      this.delegationState = task.getDelegationState().toString();
    }

    this.description = task.getDescription();
    this.executionId = task.getExecutionId();
    this.owner = task.getOwner();
    this.parentTaskId = task.getParentTaskId();
    this.priority = task.getPriority();
    this.processDefinitionId = task.getProcessDefinitionId();
    this.processInstanceId = task.getProcessInstanceId();
    this.taskDefinitionKey = task.getTaskDefinitionKey();
    this.caseDefinitionId = task.getCaseDefinitionId();
    this.caseExecutionId = task.getCaseExecutionId();
    this.caseInstanceId = task.getCaseInstanceId();
    this.suspended = task.isSuspended();
    this.tenantId = task.getTenantId();
    this.taskState = task.getTaskState();
    try {
      this.formKey = task.getFormKey();
      this.camundaFormRef = task.getCamundaFormRef();
    }
    catch (BadUserRequestException e) {
      // ignore (initializeFormKeys was not called)
    }
  }

  public static TaskDto fromEntity(Task task) {
    TaskDto dto = new TaskDto();
    dto.id = task.getId();
    dto.name = task.getName();
    dto.assignee = task.getAssignee();
    dto.created = task.getCreateTime();
    dto.lastUpdated = task.getLastUpdated();
    dto.due = task.getDueDate();
    dto.followUp = task.getFollowUpDate();

    if (task.getDelegationState() != null) {
      dto.delegationState = task.getDelegationState().toString();
    }

    dto.description = task.getDescription();
    dto.executionId = task.getExecutionId();
    dto.owner = task.getOwner();
    dto.parentTaskId = task.getParentTaskId();
    dto.priority = task.getPriority();
    dto.processDefinitionId = task.getProcessDefinitionId();
    dto.processInstanceId = task.getProcessInstanceId();
    dto.taskDefinitionKey = task.getTaskDefinitionKey();
    dto.caseDefinitionId = task.getCaseDefinitionId();
    dto.caseExecutionId = task.getCaseExecutionId();
    dto.caseInstanceId = task.getCaseInstanceId();
    dto.suspended = task.isSuspended();
    dto.tenantId = task.getTenantId();
    dto.taskState = task.getTaskState();

    try {
      dto.formKey = task.getFormKey();
      dto.camundaFormRef = task.getCamundaFormRef();
    }
    catch (BadUserRequestException e) {
      // ignore (initializeFormKeys was not called)
    }
    return dto;
  }

  public void updateTask(Task task) {
    task.setName(getName());
    task.setDescription(getDescription());
    task.setPriority(getPriority());
    task.setAssignee(getAssignee());
    task.setOwner(getOwner());

    DelegationState state = null;
    if (getDelegationState() != null) {
      DelegationStateConverter converter = new DelegationStateConverter();
      state = converter.convertQueryParameterToType(getDelegationState());
    }
    task.setDelegationState(state);

    task.setDueDate(getDue());
    task.setFollowUpDate(getFollowUp());
    task.setParentTaskId(getParentTaskId());
    task.setCaseInstanceId(getCaseInstanceId());
    task.setTenantId(getTenantId());
  }

}
