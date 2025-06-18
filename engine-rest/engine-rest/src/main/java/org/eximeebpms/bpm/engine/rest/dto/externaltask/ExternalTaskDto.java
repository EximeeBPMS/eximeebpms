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
package org.eximeebpms.bpm.engine.rest.dto.externaltask;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eximeebpms.bpm.engine.externaltask.ExternalTask;
import org.eximeebpms.commons.utils.ExcludeFromTestCoverage;

/**
 * @author Thorben Lindhauer
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class ExternalTaskDto {

  protected String activityId;
  protected String activityInstanceId;
  protected String errorMessage;
  protected String executionId;
  protected String id;
  protected Date lockExpirationTime;
  private Date createTime;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionVersionTag;
  protected String processInstanceId;
  protected Integer retries;
  protected boolean suspended;
  protected String workerId;
  protected String topicName;
  protected String tenantId;
  protected long priority;
  protected String businessKey;

  @ExcludeFromTestCoverage(reason = "Simple mapping")
  public static ExternalTaskDto fromExternalTask(ExternalTask task) {
    ExternalTaskDto dto = new ExternalTaskDto();
    dto.activityId = task.getActivityId();
    dto.activityInstanceId = task.getActivityInstanceId();
    dto.errorMessage = task.getErrorMessage();
    dto.executionId = task.getExecutionId();
    dto.id = task.getId();
    dto.lockExpirationTime = task.getLockExpirationTime();
    dto.createTime = task.getCreateTime();
    dto.processDefinitionId = task.getProcessDefinitionId();
    dto.processDefinitionKey = task.getProcessDefinitionKey();
    dto.processDefinitionVersionTag = task.getProcessDefinitionVersionTag();
    dto.processInstanceId = task.getProcessInstanceId();
    dto.retries = task.getRetries();
    dto.suspended = task.isSuspended();
    dto.topicName = task.getTopicName();
    dto.workerId = task.getWorkerId();
    dto.tenantId = task.getTenantId();
    dto.priority = task.getPriority();
    dto.businessKey = task.getBusinessKey();

    return dto;
  }

}
