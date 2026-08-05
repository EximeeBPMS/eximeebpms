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

import static java.lang.Boolean.TRUE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.QueryEntityRelationCondition;
import org.eximeebpms.bpm.engine.impl.QueryOrderingProperty;
import org.eximeebpms.bpm.engine.impl.TaskQueryImpl;
import org.eximeebpms.bpm.engine.impl.TaskQueryProperty;
import org.eximeebpms.bpm.engine.impl.TaskQueryVariableValue;
import org.eximeebpms.bpm.engine.impl.VariableInstanceQueryProperty;
import org.eximeebpms.bpm.engine.impl.VariableOrderProperty;
import org.eximeebpms.bpm.engine.impl.persistence.entity.SuspensionState;
import org.eximeebpms.bpm.engine.query.Query;
import org.eximeebpms.bpm.engine.query.QueryProperty;
import org.eximeebpms.bpm.engine.rest.dto.AbstractQueryDto;
import org.eximeebpms.bpm.engine.rest.dto.EximeeBpmsQueryParam;
import org.eximeebpms.bpm.engine.rest.dto.SortingDto;
import org.eximeebpms.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.eximeebpms.bpm.engine.rest.dto.VariableValueDto;
import org.eximeebpms.bpm.engine.rest.dto.converter.BooleanConverter;
import org.eximeebpms.bpm.engine.rest.dto.converter.DateConverter;
import org.eximeebpms.bpm.engine.rest.dto.converter.DelegationStateConverter;
import org.eximeebpms.bpm.engine.rest.dto.converter.IntegerConverter;
import org.eximeebpms.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.eximeebpms.bpm.engine.rest.dto.converter.StringListConverter;
import org.eximeebpms.bpm.engine.rest.dto.converter.VariableListConverter;
import org.eximeebpms.bpm.engine.rest.exception.InvalidRequestException;
import org.eximeebpms.bpm.engine.rest.exception.RestException;
import org.eximeebpms.bpm.engine.task.DelegationState;
import org.eximeebpms.bpm.engine.task.TaskQuery;
import org.eximeebpms.bpm.engine.variable.type.ValueType;
import org.eximeebpms.bpm.engine.variable.type.ValueTypeResolver;

@Getter
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TaskQueryDto extends AbstractQueryDto<TaskQuery> {

  public static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "instanceId";
  public static final String SORT_BY_DUE_DATE_VALUE = "dueDate";
  public static final String SORT_BY_FOLLOW_UP_VALUE = "followUpDate";
  public static final String SORT_BY_EXECUTION_ID_VALUE = "executionId";
  public static final String SORT_BY_ASSIGNEE_VALUE = "assignee";
  public static final String SORT_BY_CREATE_TIME_VALUE = "created";
  public static final String SORT_BY_LAST_UPDATED_VALUE = "lastUpdated";
  public static final String SORT_BY_DESCRIPTION_VALUE = "description";
  public static final String SORT_BY_ID_VALUE = "id";
  public static final String SORT_BY_NAME_VALUE = "name";
  public static final String SORT_BY_NAME_CASE_INSENSITIVE_VALUE = "nameCaseInsensitive";
  public static final String SORT_BY_PRIORITY_VALUE = "priority";
  public static final String SORT_BY_TENANT_ID_VALUE = "tenantId";

  public static final String SORT_BY_PROCESS_VARIABLE = "processVariable";
  public static final String SORT_BY_EXECUTION_VARIABLE = "executionVariable";
  public static final String SORT_BY_TASK_VARIABLE = "taskVariable";

  public static final List<String> VALID_SORT_BY_VALUES;

  static {
    VALID_SORT_BY_VALUES = new ArrayList<>();
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DUE_DATE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_FOLLOW_UP_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXECUTION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ASSIGNEE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CREATE_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_LAST_UPDATED_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DESCRIPTION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_NAME_CASE_INSENSITIVE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PRIORITY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID_VALUE);
  }

  public static final String SORT_PARAMETERS_VARIABLE_NAME = "variable";
  public static final String SORT_PARAMETERS_VALUE_TYPE = "type";

  private String processInstanceBusinessKey;
  private String processInstanceBusinessKeyExpression;
  private String[] processInstanceBusinessKeyIn;
  private String processInstanceBusinessKeyLike;
  private String processInstanceBusinessKeyLikeExpression;
  private String processDefinitionKey;
  private String[] processDefinitionKeyIn;
  private String processDefinitionId;
  private String executionId;
  private String[] activityInstanceIdIn;
  private String processDefinitionName;
  private String processDefinitionNameLike;
  private String processInstanceId;
  private String[] processInstanceIdIn;
  private String assignee;
  private String assigneeExpression;
  private String assigneeLike;
  private String assigneeLikeExpression;
  private String[] assigneeIn;
  private String[] assigneeNotIn;
  private String candidateGroup;
  private String candidateGroupExpression;
  private String candidateGroupLike;
  private String candidateUser;
  private String candidateUserExpression;
  private Boolean includeAssignedTasks;
  private String taskDefinitionKey;
  private String[] taskDefinitionKeyIn;
  private String[] taskDefinitionKeyNotIn;
  private String taskDefinitionKeyLike;
  private String taskId;
  private String[] taskIdIn;
  private String description;
  private String descriptionLike;
  private String involvedUser;
  private String involvedUserExpression;
  private Integer maxPriority;
  private Integer minPriority;
  private String name;
  private String nameNotEqual;
  private String nameLike;
  private String nameNotLike;
  private String owner;
  private String ownerExpression;
  private Integer priority;
  private String parentTaskId;
  protected Boolean assigned;
  private Boolean unassigned;
  private Boolean active;
  private Boolean suspended;

  private Date dueAfter;
  private String dueAfterExpression;
  private Date dueBefore;
  private String dueBeforeExpression;
  private Date dueDate;
  private String dueDateExpression;
  private Boolean withoutDueDate;
  private Date followUpAfter;
  private String followUpAfterExpression;
  private Date followUpBefore;
  private String followUpBeforeExpression;
  private Date followUpBeforeOrNotExistent;
  private String followUpBeforeOrNotExistentExpression;
  private Date followUpDate;
  private String followUpDateExpression;
  private Date createdAfter;
  private String createdAfterExpression;
  private Date createdBefore;
  private String createdBeforeExpression;
  private Date createdOn;
  private String createdOnExpression;
  private Date updatedAfter;
  private String updatedAfterExpression;

  private String delegationState;

  private String[] tenantIdIn;
  private Boolean withoutTenantId;

  private List<String> candidateGroups;
  private String candidateGroupsExpression;
  protected Boolean withCandidateGroups;
  protected Boolean withoutCandidateGroups;
  protected Boolean withCandidateUsers;
  protected Boolean withoutCandidateUsers;

  protected Boolean variableNamesIgnoreCase;
  protected Boolean variableValuesIgnoreCase;

  private List<VariableQueryParameterDto> taskVariables;
  private List<VariableQueryParameterDto> processVariables;

  private List<TaskQueryDto> orQueries;

  private Boolean withCommentAttachmentInfo;

  private Boolean withTaskVariablesInReturn;

  private Boolean withTaskLocalVariablesInReturn;

  public TaskQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @EximeeBpmsQueryParam("orQueries")
  public void setOrQueries(List<TaskQueryDto> orQueries) {
    this.orQueries = orQueries;
  }

  @EximeeBpmsQueryParam("processInstanceBusinessKey")
  public void setProcessInstanceBusinessKey(String businessKey) {
    this.processInstanceBusinessKey = businessKey;
  }

  @EximeeBpmsQueryParam("processInstanceBusinessKeyExpression")
  public void setProcessInstanceBusinessKeyExpression(String businessKeyExpression) {
    this.processInstanceBusinessKeyExpression = businessKeyExpression;
  }

  @EximeeBpmsQueryParam(value = "processInstanceBusinessKeyIn", converter = StringArrayConverter.class)
  public void setProcessInstanceBusinessKeyIn(String[] processInstanceBusinessKeyIn) {
    this.processInstanceBusinessKeyIn = processInstanceBusinessKeyIn;
  }

  @EximeeBpmsQueryParam("processInstanceBusinessKeyLike")
  public void setProcessInstanceBusinessKeyLike(String businessKeyLike) {
    this.processInstanceBusinessKeyLike = businessKeyLike;
  }

  @EximeeBpmsQueryParam("processInstanceBusinessKeyLikeExpression")
  public void setProcessInstanceBusinessKeyLikeExpression(String businessKeyLikeExpression) {
    this.processInstanceBusinessKeyLikeExpression = businessKeyLikeExpression;
  }

  @EximeeBpmsQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @EximeeBpmsQueryParam(value = "processDefinitionKeyIn", converter = StringArrayConverter.class)
  public void setProcessDefinitionKeyIn(String[] processDefinitionKeyIn) {
    this.processDefinitionKeyIn = processDefinitionKeyIn;
  }

  @EximeeBpmsQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @EximeeBpmsQueryParam("executionId")
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @EximeeBpmsQueryParam(value = "activityInstanceIdIn", converter = StringArrayConverter.class)
  public void setActivityInstanceIdIn(String[] activityInstanceIdIn) {
    this.activityInstanceIdIn = activityInstanceIdIn;
  }

  @EximeeBpmsQueryParam(value = "tenantIdIn", converter = StringArrayConverter.class)
  public void setTenantIdIn(String[] tenantIdIn) {
    this.tenantIdIn = tenantIdIn;
  }

  @EximeeBpmsQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @EximeeBpmsQueryParam("processDefinitionName")
  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  @EximeeBpmsQueryParam("processDefinitionNameLike")
  public void setProcessDefinitionNameLike(String processDefinitionNameLike) {
    this.processDefinitionNameLike = processDefinitionNameLike;
  }

  @EximeeBpmsQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @EximeeBpmsQueryParam(value = "processInstanceIdIn", converter = StringArrayConverter.class)
  public void setProcessInstanceIdIn(String[] processInstanceIdIn) {
    this.processInstanceIdIn = processInstanceIdIn;
  }

  @EximeeBpmsQueryParam("assignee")
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  @EximeeBpmsQueryParam("assigneeExpression")
  public void setAssigneeExpression(String assigneeExpression) {
    this.assigneeExpression = assigneeExpression;
  }

  @EximeeBpmsQueryParam("assigneeLike")
  public void setAssigneeLike(String assigneeLike) {
    this.assigneeLike = assigneeLike;
  }

  @EximeeBpmsQueryParam(value = "assigneeIn", converter = StringArrayConverter.class)
  public void setAssigneeIn(String[] assigneeIn) {
    this.assigneeIn = assigneeIn;
  }

  @EximeeBpmsQueryParam(value = "assigneeNotIn", converter = StringArrayConverter.class)
  public void setAssigneeNotIn(String[] assigneeNotIn) {
    this.assigneeNotIn = assigneeNotIn;
  }

  @EximeeBpmsQueryParam("assigneeLikeExpression")
  public void setAssigneeLikeExpression(String assigneeLikeExpression) {
    this.assigneeLikeExpression = assigneeLikeExpression;
  }

  @EximeeBpmsQueryParam("candidateGroup")
  public void setCandidateGroup(String candidateGroup) {
    this.candidateGroup = candidateGroup;
  }

  @EximeeBpmsQueryParam("candidateGroupExpression")
  public void setCandidateGroupExpression(String candidateGroupExpression) {
    this.candidateGroupExpression = candidateGroupExpression;
  }

  @EximeeBpmsQueryParam("candidateGroupLike")
  public void setCandidateGroupLike(String candidateGroupLike) {
    this.candidateGroupLike = candidateGroupLike;
  }

  @EximeeBpmsQueryParam(value = "withCandidateGroups", converter = BooleanConverter.class)
  public void setWithCandidateGroups(Boolean withCandidateGroups) {
    this.withCandidateGroups = withCandidateGroups;
  }

  @EximeeBpmsQueryParam(value = "withoutCandidateGroups", converter = BooleanConverter.class)
  public void setWithoutCandidateGroups(Boolean withoutCandidateGroups) {
    this.withoutCandidateGroups = withoutCandidateGroups;
  }

  @EximeeBpmsQueryParam(value = "withCandidateUsers", converter = BooleanConverter.class)
  public void setWithCandidateUsers(Boolean withCandidateUsers) {
    this.withCandidateUsers = withCandidateUsers;
  }

  @EximeeBpmsQueryParam(value = "withoutCandidateUsers", converter = BooleanConverter.class)
  public void setWithoutCandidateUsers(Boolean withoutCandidateUsers) {
    this.withoutCandidateUsers = withoutCandidateUsers;
  }

  @EximeeBpmsQueryParam("candidateUser")
  public void setCandidateUser(String candidateUser) {
    this.candidateUser = candidateUser;
  }

  @EximeeBpmsQueryParam("candidateUserExpression")
  public void setCandidateUserExpression(String candidateUserExpression) {
    this.candidateUserExpression = candidateUserExpression;
  }

  @EximeeBpmsQueryParam(value = "includeAssignedTasks", converter = BooleanConverter.class)
  public void setIncludeAssignedTasks(Boolean includeAssignedTasks) {
    this.includeAssignedTasks = includeAssignedTasks;
  }

  @EximeeBpmsQueryParam("taskId")
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  @EximeeBpmsQueryParam(value = "taskIdIn", converter = StringArrayConverter.class)
  public void setTaskIdIn(String[] taskIdIn) {
    this.taskIdIn = taskIdIn;
  }

  @EximeeBpmsQueryParam("taskDefinitionKey")
  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }

  @EximeeBpmsQueryParam(value = "taskDefinitionKeyIn", converter = StringArrayConverter.class)
  public void setTaskDefinitionKeyIn(String[] taskDefinitionKeyIn) {
    this.taskDefinitionKeyIn = taskDefinitionKeyIn;
  }

  @EximeeBpmsQueryParam(value = "taskDefinitionKeyNotIn", converter = StringArrayConverter.class)
  public void setTaskDefinitionKeyNotIn(String[] taskDefinitionKeyNotIn) {
    this.taskDefinitionKeyNotIn = taskDefinitionKeyNotIn;
  }

  @EximeeBpmsQueryParam("taskDefinitionKeyLike")
  public void setTaskDefinitionKeyLike(String taskDefinitionKeyLike) {
    this.taskDefinitionKeyLike = taskDefinitionKeyLike;
  }

  @EximeeBpmsQueryParam("description")
  public void setDescription(String description) {
    this.description = description;
  }

  @EximeeBpmsQueryParam("descriptionLike")
  public void setDescriptionLike(String descriptionLike) {
    this.descriptionLike = descriptionLike;
  }

  @EximeeBpmsQueryParam("involvedUser")
  public void setInvolvedUser(String involvedUser) {
    this.involvedUser = involvedUser;
  }

  @EximeeBpmsQueryParam("involvedUserExpression")
  public void setInvolvedUserExpression(String involvedUserExpression) {
    this.involvedUserExpression = involvedUserExpression;
  }

  @EximeeBpmsQueryParam(value = "maxPriority", converter = IntegerConverter.class)
  public void setMaxPriority(Integer maxPriority) {
    this.maxPriority = maxPriority;
  }

  @EximeeBpmsQueryParam(value = "minPriority", converter = IntegerConverter.class)
  public void setMinPriority(Integer minPriority) {
    this.minPriority = minPriority;
  }

  @EximeeBpmsQueryParam("name")
  public void setName(String name) {
    this.name = name;
  }

  @EximeeBpmsQueryParam("nameNotEqual")
  public void setNameNotEqual(String nameNotEqual) {
    this.nameNotEqual = nameNotEqual;
  }

  @EximeeBpmsQueryParam("nameLike")
  public void setNameLike(String nameLike) {
    this.nameLike = nameLike;
  }

  @EximeeBpmsQueryParam("nameNotLike")
  public void setNameNotLike(String nameNotLike) {
    this.nameNotLike = nameNotLike;
  }

  @EximeeBpmsQueryParam("owner")
  public void setOwner(String owner) {
    this.owner = owner;
  }

  @EximeeBpmsQueryParam("ownerExpression")
  public void setOwnerExpression(String ownerExpression) {
    this.ownerExpression = ownerExpression;
  }

  @EximeeBpmsQueryParam(value = "priority", converter = IntegerConverter.class)
  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  @EximeeBpmsQueryParam("parentTaskId")
  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }

  @EximeeBpmsQueryParam(value = "assigned", converter = BooleanConverter.class)
  public void setAssigned(Boolean assigned) {
    this.assigned = assigned;
  }

  @EximeeBpmsQueryParam(value = "unassigned", converter = BooleanConverter.class)
  public void setUnassigned(Boolean unassigned) {
    this.unassigned = unassigned;
  }

  @EximeeBpmsQueryParam(value = "active", converter = BooleanConverter.class)
  public void setActive(Boolean active) {
    this.active = active;
  }

  @EximeeBpmsQueryParam(value = "suspended", converter = BooleanConverter.class)
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @EximeeBpmsQueryParam(value = "dueAfter", converter = DateConverter.class)
  public void setDueAfter(Date dueAfter) {
    this.dueAfter = dueAfter;
  }

  @EximeeBpmsQueryParam(value = "dueAfterExpression")
  public void setDueAfterExpression(String dueAfterExpression) {
    this.dueAfterExpression = dueAfterExpression;
  }

  @EximeeBpmsQueryParam(value = "dueBefore", converter = DateConverter.class)
  public void setDueBefore(Date dueBefore) {
    this.dueBefore = dueBefore;
  }

  @EximeeBpmsQueryParam(value = "dueBeforeExpression")
  public void setDueBeforeExpression(String dueBeforeExpression) {
    this.dueBeforeExpression = dueBeforeExpression;
  }

  @EximeeBpmsQueryParam(value = "dueDate", converter = DateConverter.class)
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  @Deprecated
  @EximeeBpmsQueryParam(value = "due", converter = DateConverter.class)
  public void setDue(Date dueDate) {
    this.dueDate = dueDate;
  }

  @EximeeBpmsQueryParam(value = "dueDateExpression")
  public void setDueDateExpression(String dueDateExpression) {
    this.dueDateExpression = dueDateExpression;
  }

  @EximeeBpmsQueryParam(value = "withoutDueDate", converter = BooleanConverter.class)
  public void setWithoutDueDate(Boolean withoutDueDate) {
    this.withoutDueDate = withoutDueDate;
  }

  @EximeeBpmsQueryParam(value = "followUpAfter", converter = DateConverter.class)
  public void setFollowUpAfter(Date followUpAfter) {
    this.followUpAfter = followUpAfter;
  }

  @EximeeBpmsQueryParam(value = "followUpAfterExpression")
  public void setFollowUpAfterExpression(String followUpAfterExpression) {
    this.followUpAfterExpression = followUpAfterExpression;
  }

  @EximeeBpmsQueryParam(value = "followUpBefore", converter = DateConverter.class)
  public void setFollowUpBefore(Date followUpBefore) {
    this.followUpBefore = followUpBefore;
  }

  @EximeeBpmsQueryParam(value = "followUpBeforeOrNotExistentExpression")
  public void setFollowUpBeforeOrNotExistentExpression(String followUpBeforeExpression) {
    this.followUpBeforeOrNotExistentExpression = followUpBeforeExpression;
  }

  @EximeeBpmsQueryParam(value = "followUpBeforeOrNotExistent", converter = DateConverter.class)
  public void setFollowUpBeforeOrNotExistent(Date followUpBefore) {
    this.followUpBeforeOrNotExistent = followUpBefore;
  }

  @EximeeBpmsQueryParam(value = "followUpBeforeExpression")
  public void setFollowUpBeforeExpression(String followUpBeforeExpression) {
    this.followUpBeforeExpression = followUpBeforeExpression;
  }

  @EximeeBpmsQueryParam(value = "followUpDate", converter = DateConverter.class)
  public void setFollowUpDate(Date followUpDate) {
    this.followUpDate = followUpDate;
  }

  @Deprecated
  @EximeeBpmsQueryParam(value = "followUp", converter = DateConverter.class)
  public void setFollowUp(Date followUpDate) {
    this.followUpDate = followUpDate;
  }

  @EximeeBpmsQueryParam(value = "followUpDateExpression")
  public void setFollowUpDateExpression(String followUpDateExpression) {
    this.followUpDateExpression = followUpDateExpression;
  }

  @EximeeBpmsQueryParam(value = "createdAfter", converter = DateConverter.class)
  public void setCreatedAfter(Date createdAfter) {
    this.createdAfter = createdAfter;
  }

  @EximeeBpmsQueryParam(value = "createdAfterExpression")
  public void setCreatedAfterExpression(String createdAfterExpression) {
    this.createdAfterExpression = createdAfterExpression;
  }

  @EximeeBpmsQueryParam(value = "createdBefore", converter = DateConverter.class)
  public void setCreatedBefore(Date createdBefore) {
    this.createdBefore = createdBefore;
  }

  @EximeeBpmsQueryParam(value = "createdBeforeExpression")
  public void setCreatedBeforeExpression(String createdBeforeExpression) {
    this.createdBeforeExpression = createdBeforeExpression;
  }

  @EximeeBpmsQueryParam(value = "createdOn", converter = DateConverter.class)
  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  @Deprecated
  @EximeeBpmsQueryParam(value = "created", converter = DateConverter.class)
  public void setCreated(Date createdOn) {
    this.createdOn = createdOn;
  }

  @EximeeBpmsQueryParam(value = "createdOnExpression")
  public void setCreatedOnExpression(String createdOnExpression) {
    this.createdOnExpression = createdOnExpression;
  }

  @EximeeBpmsQueryParam(value = "updatedAfter", converter = DateConverter.class)
  public void setUpdatedAfter(Date updatedAfter) {
    this.updatedAfter = updatedAfter;
  }

  @EximeeBpmsQueryParam(value = "updatedAfterExpression")
  public void setUpdatedAfterExpression(String updatedAfterExpression) {
    this.updatedAfterExpression = updatedAfterExpression;
  }

  @EximeeBpmsQueryParam(value = "delegationState")
  public void setDelegationState(String taskDelegationState) {
    this.delegationState = taskDelegationState;
  }

  @EximeeBpmsQueryParam(value = "candidateGroups", converter = StringListConverter.class)
  public void setCandidateGroups(List<String> candidateGroups) {
    this.candidateGroups = candidateGroups;
  }

  @EximeeBpmsQueryParam(value = "candidateGroupsExpression")
  public void setCandidateGroupsExpression(String candidateGroupsExpression) {
    this.candidateGroupsExpression = candidateGroupsExpression;
  }

  @EximeeBpmsQueryParam(value = "taskVariables", converter = VariableListConverter.class)
  public void setTaskVariables(List<VariableQueryParameterDto> taskVariables) {
    this.taskVariables = taskVariables;
  }

  @EximeeBpmsQueryParam(value = "processVariables", converter = VariableListConverter.class)
  public void setProcessVariables(List<VariableQueryParameterDto> processVariables) {
    this.processVariables = processVariables;
  }

  @EximeeBpmsQueryParam(value = "variableNamesIgnoreCase", converter = BooleanConverter.class)
  public void setVariableNamesIgnoreCase(Boolean variableNamesCaseInsensitive) {
    this.variableNamesIgnoreCase = variableNamesCaseInsensitive;
  }

  @EximeeBpmsQueryParam(value = "variableValuesIgnoreCase", converter = BooleanConverter.class)
  public void setVariableValuesIgnoreCase(Boolean variableValuesCaseInsensitive) {
    this.variableValuesIgnoreCase = variableValuesCaseInsensitive;
  }

  @EximeeBpmsQueryParam(value = "withCommentAttachmentInfo", converter = BooleanConverter.class)
  public void setWithCommentAttachmentInfo(Boolean withCommentAttachmentInfo) {
    this.withCommentAttachmentInfo = withCommentAttachmentInfo;
  }

  @EximeeBpmsQueryParam(value = "withTaskVariablesInReturn", converter = BooleanConverter.class)
  public void setWithTaskVariablesInReturn(Boolean withTaskVariablesInReturn) {
    this.withTaskVariablesInReturn = withTaskVariablesInReturn;
  }

  @EximeeBpmsQueryParam(value = "withTaskLocalVariablesInReturn", converter = BooleanConverter.class)
  public void setWithTaskLocalVariablesInReturn(Boolean withTaskLocalVariablesInReturn) {
    this.withTaskLocalVariablesInReturn = withTaskLocalVariablesInReturn;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected TaskQuery createNewQuery(ProcessEngine engine) {
    return engine.getTaskService().createTaskQuery();
  }

  @Override
  protected void applyFilters(TaskQuery query) {
    if (orQueries != null) {
      List<TaskQueryDto> nonEmptyOrQueries = new ArrayList<>();

      for (TaskQueryDto orQueryDto : orQueries) {
        if (!isEmptyOrQuery(orQueryDto)) {
          nonEmptyOrQueries.add(orQueryDto);
        }
      }

      if (!nonEmptyOrQueries.isEmpty()) {
        TaskQueryImpl orQuery = (TaskQueryImpl) query.or();

        for (TaskQueryDto orQueryDto : nonEmptyOrQueries) {
          orQueryDto.applyFilters(orQuery);
        }

        orQuery.endOr();
      }
    }
    if (processInstanceBusinessKey != null) {
      query.processInstanceBusinessKey(processInstanceBusinessKey);
    }
    if (processInstanceBusinessKeyExpression != null) {
      query.processInstanceBusinessKeyExpression(processInstanceBusinessKeyExpression);
    }
    if (processInstanceBusinessKeyIn != null && processInstanceBusinessKeyIn.length > 0) {
      query.processInstanceBusinessKeyIn(processInstanceBusinessKeyIn);
    }
    if (processInstanceBusinessKeyLike != null) {
      query.processInstanceBusinessKeyLike(processInstanceBusinessKeyLike);
    }
    if (processInstanceBusinessKeyLikeExpression != null) {
      query.processInstanceBusinessKeyLikeExpression(processInstanceBusinessKeyLikeExpression);
    }
    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }
    if (processDefinitionKeyIn != null && processDefinitionKeyIn.length > 0) {
      query.processDefinitionKeyIn(processDefinitionKeyIn);
    }
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (executionId != null) {
      query.executionId(executionId);
    }
    if (activityInstanceIdIn != null && activityInstanceIdIn.length > 0) {
      query.activityInstanceIdIn(activityInstanceIdIn);
    }
    if (tenantIdIn != null && tenantIdIn.length > 0) {
      query.tenantIdIn(tenantIdIn);
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (processDefinitionName != null) {
      query.processDefinitionName(processDefinitionName);
    }
    if (processDefinitionNameLike != null) {
      query.processDefinitionNameLike(processDefinitionNameLike);
    }
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (processInstanceIdIn != null && processInstanceIdIn.length > 0) {
      query.processInstanceIdIn(processInstanceIdIn);
    }
    if (assignee != null) {
      query.taskAssignee(assignee);
    }
    if (assigneeExpression != null) {
      query.taskAssigneeExpression(assigneeExpression);
    }
    if (assigneeLike != null) {
      query.taskAssigneeLike(assigneeLike);
    }
    if (assigneeLikeExpression != null) {
      query.taskAssigneeLikeExpression(assigneeLikeExpression);
    }
    if (assigneeIn != null && assigneeIn.length > 0) {
      query.taskAssigneeIn(assigneeIn);
    }
    if (assigneeNotIn != null && assigneeNotIn.length > 0) {
      query.taskAssigneeNotIn(assigneeNotIn);
    }
    if (candidateGroup != null) {
      query.taskCandidateGroup(candidateGroup);
    }
    if (candidateGroupExpression != null) {
      query.taskCandidateGroupExpression(candidateGroupExpression);
    }
    if (candidateGroupLike != null) {
      query.taskCandidateGroupLike(candidateGroupLike);
    }
    if (withCandidateGroups != null && withCandidateGroups) {
      query.withCandidateGroups();
    }
    if (withoutCandidateGroups != null && withoutCandidateGroups) {
      query.withoutCandidateGroups();
    }
    if (withCandidateUsers != null && withCandidateUsers) {
      query.withCandidateUsers();
    }
    if (withoutCandidateUsers != null && withoutCandidateUsers) {
      query.withoutCandidateUsers();
    }
    if (candidateUser != null) {
      query.taskCandidateUser(candidateUser);
    }
    if (candidateUserExpression != null) {
      query.taskCandidateUserExpression(candidateUserExpression);
    }
    if (taskIdIn != null && taskIdIn.length > 0) {
      query.taskIdIn(taskIdIn);
    }
    if (taskId != null) {
      query.taskId(taskId);
    }
    if (taskDefinitionKeyIn != null && taskDefinitionKeyIn.length > 0) {
      query.taskDefinitionKeyIn(taskDefinitionKeyIn);
    }
    if (taskDefinitionKeyNotIn != null && taskDefinitionKeyNotIn.length > 0) {
      query.taskDefinitionKeyNotIn(taskDefinitionKeyNotIn);
    }
    if (taskDefinitionKey != null) {
      query.taskDefinitionKey(taskDefinitionKey);
    }
    if (taskDefinitionKeyLike != null) {
      query.taskDefinitionKeyLike(taskDefinitionKeyLike);
    }
    if (description != null) {
      query.taskDescription(description);
    }
    if (descriptionLike != null) {
      query.taskDescriptionLike(descriptionLike);
    }
    if (involvedUser != null) {
      query.taskInvolvedUser(involvedUser);
    }
    if (involvedUserExpression != null) {
      query.taskInvolvedUserExpression(involvedUserExpression);
    }
    if (maxPriority != null) {
      query.taskMaxPriority(maxPriority);
    }
    if (minPriority != null) {
      query.taskMinPriority(minPriority);
    }
    if (name != null) {
      query.taskName(name);
    }
    if (nameNotEqual != null) {
      query.taskNameNotEqual(nameNotEqual);
    }
    if (nameLike != null) {
      query.taskNameLike(nameLike);
    }
    if (nameNotLike != null) {
      query.taskNameNotLike(nameNotLike);
    }
    if (owner != null) {
      query.taskOwner(owner);
    }
    if (ownerExpression != null) {
      query.taskOwnerExpression(ownerExpression);
    }
    if (priority != null) {
      query.taskPriority(priority);
    }
    if (parentTaskId != null) {
      query.taskParentTaskId(parentTaskId);
    }
    if (assigned != null && assigned) {
      query.taskAssigned();
    }
    if (unassigned != null && unassigned) {
      query.taskUnassigned();
    }
    if (dueAfter != null) {
      query.dueAfter(dueAfter);
    }
    if (dueAfterExpression != null) {
      query.dueAfterExpression(dueAfterExpression);
    }
    if (dueBefore != null) {
      query.dueBefore(dueBefore);
    }
    if (dueBeforeExpression != null) {
      query.dueBeforeExpression(dueBeforeExpression);
    }
    if (dueDate != null) {
      query.dueDate(dueDate);
    }
    if (dueDateExpression != null) {
      query.dueDateExpression(dueDateExpression);
    }
    if (TRUE.equals(withoutDueDate)) {
      query.withoutDueDate();
    }
    if (followUpAfter != null) {
      query.followUpAfter(followUpAfter);
    }
    if (followUpAfterExpression != null) {
      query.followUpAfterExpression(followUpAfterExpression);
    }
    if (followUpBefore != null) {
      query.followUpBefore(followUpBefore);
    }
    if (followUpBeforeExpression != null) {
      query.followUpBeforeExpression(followUpBeforeExpression);
    }
    if (followUpBeforeOrNotExistent != null) {
      query.followUpBeforeOrNotExistent(followUpBeforeOrNotExistent);
    }
    if (followUpBeforeOrNotExistentExpression != null) {
      query.followUpBeforeOrNotExistentExpression(followUpBeforeOrNotExistentExpression);
    }
    if (followUpDate != null) {
      query.followUpDate(followUpDate);
    }
    if (followUpDateExpression != null) {
      query.followUpDateExpression(followUpDateExpression);
    }
    if (createdAfter != null) {
      query.taskCreatedAfter(createdAfter);
    }
    if (createdAfterExpression != null) {
      query.taskCreatedAfterExpression(createdAfterExpression);
    }
    if (createdBefore != null) {
      query.taskCreatedBefore(createdBefore);
    }
    if (createdBeforeExpression != null) {
      query.taskCreatedBeforeExpression(createdBeforeExpression);
    }
    if (createdOn != null) {
      query.taskCreatedOn(createdOn);
    }
    if (createdOnExpression != null) {
      query.taskCreatedOnExpression(createdOnExpression);
    }
    if (updatedAfter != null) {
      query.taskUpdatedAfter(updatedAfter);
    }
    if (updatedAfterExpression != null) {
      query.taskUpdatedAfterExpression(updatedAfterExpression);
    }
    if (delegationState != null) {
      DelegationStateConverter converter = new DelegationStateConverter();
      DelegationState state = converter.convertQueryParameterToType(delegationState);
      query.taskDelegationState(state);
    }
    if (candidateGroups != null) {
      query.taskCandidateGroupIn(candidateGroups);
    }
    if (candidateGroupsExpression != null) {
      query.taskCandidateGroupInExpression(candidateGroupsExpression);
    }
    if (includeAssignedTasks != null && includeAssignedTasks) {
      query.includeAssignedTasks();
    }
    if (active != null && active) {
      query.active();
    }
    if (suspended != null && suspended) {
      query.suspended();
    }
    if (variableValuesIgnoreCase != null && variableValuesIgnoreCase) {
      query.matchVariableValuesIgnoreCase();
    }
    if (variableNamesIgnoreCase != null && variableNamesIgnoreCase) {
      query.matchVariableNamesIgnoreCase();
    }

    if (taskVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : taskVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.resolveValue(objectMapper);

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueNotEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
          query.taskVariableValueGreaterThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueGreaterThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
          query.taskVariableValueLessThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueLessThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LIKE_OPERATOR_NAME)) {
          query.taskVariableValueLike(variableName, String.valueOf(variableValue));
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid task variable comparator specified: " + op);
        }

      }
    }

    if (processVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : processVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.resolveValue(objectMapper);

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.processVariableValueEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME)) {
          query.processVariableValueNotEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
          query.processVariableValueGreaterThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.processVariableValueGreaterThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
          query.processVariableValueLessThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.processVariableValueLessThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LIKE_OPERATOR_NAME)) {
          query.processVariableValueLike(variableName, String.valueOf(variableValue));
        } else if (op.equals(VariableQueryParameterDto.NOT_LIKE_OPERATOR_NAME)) {
          query.processVariableValueNotLike(variableName, String.valueOf(variableValue));
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid process variable comparator specified: " + op);
        }

      }
    }

    if (withCommentAttachmentInfo != null && withCommentAttachmentInfo) {
      query.withCommentAttachmentInfo();
    }
  }

  @Override
  protected void applySortBy(TaskQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
      query.orderByProcessInstanceId();
    } else if (sortBy.equals(SORT_BY_DUE_DATE_VALUE)) {
      query.orderByDueDate();
    } else if (sortBy.equals(SORT_BY_FOLLOW_UP_VALUE)) {
      query.orderByFollowUpDate();
    } else if (sortBy.equals(SORT_BY_EXECUTION_ID_VALUE)) {
      query.orderByExecutionId();
    } else if (sortBy.equals(SORT_BY_ASSIGNEE_VALUE)) {
      query.orderByTaskAssignee();
    } else if (sortBy.equals(SORT_BY_CREATE_TIME_VALUE)) {
      query.orderByTaskCreateTime();
    } else if (sortBy.equals(SORT_BY_LAST_UPDATED_VALUE)) {
      query.orderByLastUpdated();
    } else if (sortBy.equals(SORT_BY_DESCRIPTION_VALUE)) {
      query.orderByTaskDescription();
    } else if (sortBy.equals(SORT_BY_ID_VALUE)) {
      query.orderByTaskId();
    } else if (sortBy.equals(SORT_BY_NAME_VALUE)) {
      query.orderByTaskName();
    } else if (sortBy.equals(SORT_BY_TENANT_ID_VALUE)) {
      query.orderByTenantId();
    } else if (sortBy.equals(SORT_BY_NAME_CASE_INSENSITIVE_VALUE)) {
      query.orderByTaskNameCaseInsensitive();
    } else if (sortBy.equals(SORT_BY_PRIORITY_VALUE)) {
      query.orderByTaskPriority();

    } else if (sortBy.equals(SORT_BY_PROCESS_VARIABLE)) {
      String variableName = getVariableName(parameters);
      String valueTypeName = getValueTypeName(parameters);
      query.orderByProcessVariable(variableName, getValueTypeByName(valueTypeName, engine));

    } else if (sortBy.equals(SORT_BY_EXECUTION_VARIABLE)) {
      String variableName = getVariableName(parameters);
      String valueTypeName = getValueTypeName(parameters);
      query.orderByExecutionVariable(variableName, getValueTypeByName(valueTypeName, engine));

    } else if (sortBy.equals(SORT_BY_TASK_VARIABLE)) {
      String variableName = getVariableName(parameters);
      String valueTypeName = getValueTypeName(parameters);
      query.orderByTaskVariable(variableName, getValueTypeByName(valueTypeName, engine));

    }
  }

  protected String getValueTypeName(Map<String, Object> parameters) {
    String valueTypeName = (String) getValue(parameters, SORT_PARAMETERS_VALUE_TYPE);
    if (valueTypeName != null) {
      valueTypeName = VariableValueDto.fromRestApiTypeName(valueTypeName);
    }
    return valueTypeName;
  }

  protected String getVariableName(Map<String, Object> parameters) {
    return (String) getValue(parameters, SORT_PARAMETERS_VARIABLE_NAME);
  }

  protected Object getValue(Map<String, Object> map, String key) {
    if (map != null) {
      return map.get(key);
    }
    return null;
  }

  protected ValueType getValueTypeByName(String name, ProcessEngine engine) {
    ValueTypeResolver valueTypeResolver = engine.getProcessEngineConfiguration().getValueTypeResolver();
    return valueTypeResolver.typeForName(name);
  }

  public static TaskQueryDto fromQuery(Query<?, ?> query) {
    return fromQuery(query, false);
  }

  public static TaskQueryDto fromQuery(Query<?, ?> query, boolean isOrQueryActive) {
    TaskQueryImpl taskQuery = (TaskQueryImpl) query;

    TaskQueryDto dto = new TaskQueryDto();

    if (!isOrQueryActive) {
      dto.orQueries = new ArrayList<>();
      for (TaskQueryImpl orQuery : taskQuery.getQueries()) {
        if (orQuery.isOrQueryActive()) {
          dto.orQueries.add(fromQuery(orQuery, true));
        }
      }
    }

    dto.activityInstanceIdIn = taskQuery.getActivityInstanceIdIn();

    dto.candidateUser = taskQuery.getCandidateUser();
    dto.candidateGroup = taskQuery.getCandidateGroup();
    dto.candidateGroupLike = taskQuery.getCandidateGroupLike();
    dto.candidateGroups = taskQuery.getCandidateGroupsInternal();
    dto.includeAssignedTasks = taskQuery.isIncludeAssignedTasksInternal();
    dto.withCandidateGroups = taskQuery.isWithCandidateGroups();
    dto.withoutCandidateGroups = taskQuery.isWithoutCandidateGroups();
    dto.withCandidateUsers = taskQuery.isWithCandidateUsers();
    dto.withoutCandidateUsers = taskQuery.isWithoutCandidateUsers();

    dto.processInstanceBusinessKey = taskQuery.getProcessInstanceBusinessKey();
    dto.processInstanceBusinessKeyLike = taskQuery.getProcessInstanceBusinessKeyLike();
    dto.processDefinitionKey = taskQuery.getProcessDefinitionKey();
    dto.processDefinitionKeyIn = taskQuery.getProcessDefinitionKeys();
    dto.processDefinitionId = taskQuery.getProcessDefinitionId();
    dto.executionId = taskQuery.getExecutionId();

    dto.processDefinitionName = taskQuery.getProcessDefinitionName();
    dto.processDefinitionNameLike = taskQuery.getProcessDefinitionNameLike();
    dto.processInstanceId = taskQuery.getProcessInstanceId();
    if (taskQuery.getProcessInstanceIdIn() != null) {
      dto.processInstanceIdIn = taskQuery.getProcessInstanceIdIn();
    }

    dto.assignee = taskQuery.getAssignee();

    if (taskQuery.getAssigneeIn() != null) {
      dto.assigneeIn = taskQuery.getAssigneeIn()
          .toArray(new String[taskQuery.getAssigneeIn().size()]);
    }

    dto.assigneeLike = taskQuery.getAssigneeLike();
    dto.taskDefinitionKey = taskQuery.getKey();
    dto.taskDefinitionKeyIn = taskQuery.getKeys();
    dto.taskDefinitionKeyNotIn = taskQuery.getKeyNotIn();
    dto.taskDefinitionKeyLike = taskQuery.getKeyLike();
    dto.description = taskQuery.getDescription();
    dto.descriptionLike = taskQuery.getDescriptionLike();
    dto.involvedUser = taskQuery.getInvolvedUser();
    dto.maxPriority = taskQuery.getMaxPriority();
    dto.minPriority = taskQuery.getMinPriority();
    dto.name = taskQuery.getName();
    dto.nameNotEqual = taskQuery.getNameNotEqual();
    dto.nameLike = taskQuery.getNameLike();
    dto.nameNotLike = taskQuery.getNameNotLike();
    dto.owner = taskQuery.getOwner();
    dto.priority = taskQuery.getPriority();
    dto.assigned = taskQuery.isAssignedInternal();
    dto.unassigned = taskQuery.isUnassignedInternal();
    dto.parentTaskId = taskQuery.getParentTaskId();

    dto.dueAfter = taskQuery.getDueAfter();
    dto.dueBefore = taskQuery.getDueBefore();
    dto.dueDate = taskQuery.getDueDate();
    if (taskQuery.isWithoutDueDate()) {
      dto.withoutDueDate = taskQuery.isWithoutDueDate();
    }

    dto.followUpAfter = taskQuery.getFollowUpAfter();

    dto.variableNamesIgnoreCase = taskQuery.isVariableNamesIgnoreCase();
    dto.variableValuesIgnoreCase = taskQuery.isVariableValuesIgnoreCase();

    if (taskQuery.isFollowUpNullAccepted()) {
      dto.followUpBeforeOrNotExistent = taskQuery.getFollowUpBefore();
    } else {
      dto.followUpBefore = taskQuery.getFollowUpBefore();
    }
    dto.followUpDate = taskQuery.getFollowUpDate();
    dto.createdAfter = taskQuery.getCreateTimeAfter();
    dto.createdBefore = taskQuery.getCreateTimeBefore();
    dto.createdOn = taskQuery.getCreateTime();

    dto.updatedAfter = taskQuery.getUpdatedAfter();

    if (taskQuery.getDelegationState() != null) {
      dto.delegationState = taskQuery.getDelegationState().toString();
    }

    if (taskQuery.isWithoutTenantId()) {
      if (taskQuery.getTenantIds() != null) {
        dto.tenantIdIn = taskQuery.getTenantIds();
      } else {
        dto.withoutTenantId = true;
      }
    }

    dto.processVariables = new ArrayList<>();
    dto.taskVariables = new ArrayList<>();
    for (TaskQueryVariableValue variableValue : taskQuery.getVariables()) {
      VariableQueryParameterDto variableValueDto = new VariableQueryParameterDto(variableValue);

      if (variableValue.isProcessInstanceVariable()) {
        dto.processVariables.add(variableValueDto);
      } else if (variableValue.isLocal()) {
        dto.taskVariables.add(variableValueDto);
      }
    }

    if (taskQuery.getSuspensionState() == SuspensionState.ACTIVE) {
      dto.active = true;
    }
    if (taskQuery.getSuspensionState() == SuspensionState.SUSPENDED) {
      dto.suspended = true;
    }

    // sorting
    List<QueryOrderingProperty> orderingProperties = taskQuery.getOrderingProperties();
    if (!orderingProperties.isEmpty()) {
      dto.setSorting(convertQueryOrderingPropertiesToSortingDtos(orderingProperties));
    }

    // expressions
    Map<String, String> expressions = taskQuery.getExpressions();
    if (expressions.containsKey("taskAssignee")) {
      dto.setAssigneeExpression(expressions.get("taskAssignee"));
    }
    if (expressions.containsKey("taskAssigneeLike")) {
      dto.setAssigneeLikeExpression(expressions.get("taskAssigneeLike"));
    }
    if (expressions.containsKey("taskOwner")) {
      dto.setOwnerExpression(expressions.get("taskOwner"));
    }
    if (expressions.containsKey("taskCandidateUser")) {
      dto.setCandidateUserExpression(expressions.get("taskCandidateUser"));
    }
    if (expressions.containsKey("taskInvolvedUser")) {
      dto.setInvolvedUserExpression(expressions.get("taskInvolvedUser"));
    }
    if (expressions.containsKey("taskCandidateGroup")) {
      dto.setCandidateGroupExpression(expressions.get("taskCandidateGroup"));
    }
    if (expressions.containsKey("taskCandidateGroupIn")) {
      dto.setCandidateGroupsExpression(expressions.get("taskCandidateGroupIn"));
    }
    if (expressions.containsKey("taskCreatedOn")) {
      dto.setCreatedOnExpression(expressions.get("taskCreatedOn"));
    }
    if (expressions.containsKey("taskCreatedBefore")) {
      dto.setCreatedBeforeExpression(expressions.get("taskCreatedBefore"));
    }
    if (expressions.containsKey("taskCreatedAfter")) {
      dto.setCreatedAfterExpression(expressions.get("taskCreatedAfter"));
    }
    if (expressions.containsKey("taskUpdatedAfter")) {
      dto.setUpdatedAfterExpression(expressions.get("taskUpdatedAfter"));
    }
    if (expressions.containsKey("dueDate")) {
      dto.setDueDateExpression(expressions.get("dueDate"));
    }
    if (expressions.containsKey("dueBefore")) {
      dto.setDueBeforeExpression(expressions.get("dueBefore"));
    }
    if (expressions.containsKey("dueAfter")) {
      dto.setDueAfterExpression(expressions.get("dueAfter"));
    }
    if (expressions.containsKey("followUpDate")) {
      dto.setFollowUpDateExpression(expressions.get("followUpDate"));
    }
    if (expressions.containsKey("followUpBefore")) {
      dto.setFollowUpBeforeExpression(expressions.get("followUpBefore"));
    }
    if (expressions.containsKey("followUpBeforeOrNotExistent")) {
      dto.setFollowUpBeforeOrNotExistentExpression(expressions.get("followUpBeforeOrNotExistent"));
    }
    if (expressions.containsKey("followUpAfter")) {
      dto.setFollowUpAfterExpression(expressions.get("followUpAfter"));
    }
    if (expressions.containsKey("processInstanceBusinessKey")) {
      dto.setProcessInstanceBusinessKeyExpression(expressions.get("processInstanceBusinessKey"));
    }
    if (expressions.containsKey("processInstanceBusinessKeyLike")) {
      dto.setProcessInstanceBusinessKeyLikeExpression(expressions.get("processInstanceBusinessKeyLike"));
    }

    return dto;
  }

  public static List<SortingDto> convertQueryOrderingPropertiesToSortingDtos(List<QueryOrderingProperty> orderingProperties) {
    List<SortingDto> sortingDtos = new ArrayList<>();
    for (QueryOrderingProperty orderingProperty : orderingProperties) {
      SortingDto sortingDto;
      if (orderingProperty instanceof VariableOrderProperty) {
        sortingDto = convertVariableOrderPropertyToSortingDto((VariableOrderProperty) orderingProperty);
      } else {
        sortingDto = convertQueryOrderingPropertyToSortingDto(orderingProperty);
      }
      sortingDtos.add(sortingDto);
    }
    return sortingDtos;
  }

  public static SortingDto convertVariableOrderPropertyToSortingDto(VariableOrderProperty variableOrderProperty) {
    SortingDto sortingDto = new SortingDto();
    sortingDto.setSortBy(sortByValueForVariableOrderProperty(variableOrderProperty));
    sortingDto.setSortOrder(sortOrderValueForDirection(variableOrderProperty.getDirection()));
    sortingDto.setParameters(sortParametersForVariableOrderProperty(variableOrderProperty));
    return sortingDto;
  }

  public static SortingDto convertQueryOrderingPropertyToSortingDto(QueryOrderingProperty orderingProperty) {
    SortingDto sortingDto = new SortingDto();
    sortingDto.setSortBy(sortByValueForQueryProperty(orderingProperty.getQueryProperty()));
    sortingDto.setSortOrder(sortOrderValueForDirection(orderingProperty.getDirection()));
    return sortingDto;
  }

  public static String sortByValueForQueryProperty(QueryProperty queryProperty) {
    if (TaskQueryProperty.ASSIGNEE.equals(queryProperty)) {
      return SORT_BY_ASSIGNEE_VALUE;
    } else if (TaskQueryProperty.CREATE_TIME.equals(queryProperty)) {
      return SORT_BY_CREATE_TIME_VALUE;
    } else if (TaskQueryProperty.LAST_UPDATED.equals(queryProperty)) {
      return SORT_BY_LAST_UPDATED_VALUE;
    } else if (TaskQueryProperty.DESCRIPTION.equals(queryProperty)) {
      return SORT_BY_DESCRIPTION_VALUE;
    } else if (TaskQueryProperty.DUE_DATE.equals(queryProperty)) {
      return SORT_BY_DUE_DATE_VALUE;
    } else if (TaskQueryProperty.EXECUTION_ID.equals(queryProperty)) {
      return SORT_BY_EXECUTION_ID_VALUE;
    } else if (TaskQueryProperty.FOLLOW_UP_DATE.equals(queryProperty)) {
      return SORT_BY_FOLLOW_UP_VALUE;
    } else if (TaskQueryProperty.NAME.equals(queryProperty)) {
      return SORT_BY_NAME_VALUE;
    } else if (TaskQueryProperty.NAME_CASE_INSENSITIVE.equals(queryProperty)) {
      return SORT_BY_NAME_CASE_INSENSITIVE_VALUE;
    } else if (TaskQueryProperty.PRIORITY.equals(queryProperty)) {
      return SORT_BY_PRIORITY_VALUE;
    } else if (TaskQueryProperty.PROCESS_INSTANCE_ID.equals(queryProperty)) {
      return SORT_BY_PROCESS_INSTANCE_ID_VALUE;
    } else if (TaskQueryProperty.TASK_ID.equals(queryProperty)) {
      return SORT_BY_ID_VALUE;
    } else if (TaskQueryProperty.TENANT_ID.equals(queryProperty)) {
      return SORT_BY_TENANT_ID_VALUE;
    } else {
      throw new RestException("Unknown query property for task query " + queryProperty);
    }
  }

  public static String sortByValueForVariableOrderProperty(VariableOrderProperty variableOrderProperty) {
    for (QueryEntityRelationCondition relationCondition : variableOrderProperty.getRelationConditions()) {
      if (relationCondition.isPropertyComparison()) {
        return sortByValueForQueryEntityRelationCondition(relationCondition);
      }
    }

    // if no property comparison was found throw an exception
    throw new RestException("Unknown variable order property for task query " + variableOrderProperty);
  }

  public static String sortByValueForQueryEntityRelationCondition(QueryEntityRelationCondition relationCondition) {
    QueryProperty property = relationCondition.getProperty();
    QueryProperty comparisonProperty = relationCondition.getComparisonProperty();
    if (VariableInstanceQueryProperty.EXECUTION_ID.equals(property) && TaskQueryProperty.PROCESS_INSTANCE_ID.equals(comparisonProperty)) {
      return SORT_BY_PROCESS_VARIABLE;
    } else if (VariableInstanceQueryProperty.EXECUTION_ID.equals(property) && TaskQueryProperty.EXECUTION_ID.equals(comparisonProperty)) {
      return SORT_BY_EXECUTION_VARIABLE;
    } else if (VariableInstanceQueryProperty.TASK_ID.equals(property) && TaskQueryProperty.TASK_ID.equals(comparisonProperty)) {
      return SORT_BY_TASK_VARIABLE;
    } else {
      throw new RestException("Unknown relation condition for task query  with query property " + property + " and comparison property " + comparisonProperty);
    }
  }

  public static Map<String, Object> sortParametersForVariableOrderProperty(VariableOrderProperty variableOrderProperty) {
    Map<String, Object> parameters = new HashMap<>();
    for (QueryEntityRelationCondition relationCondition : variableOrderProperty.getRelationConditions()) {
      QueryProperty property = relationCondition.getProperty();
      if (VariableInstanceQueryProperty.VARIABLE_NAME.equals(property)) {
        parameters.put(SORT_PARAMETERS_VARIABLE_NAME, relationCondition.getScalarValue());
      } else if (VariableInstanceQueryProperty.VARIABLE_TYPE.equals(property)) {
        String type = VariableValueDto.toRestApiTypeName((String) relationCondition.getScalarValue());
        parameters.put(SORT_PARAMETERS_VALUE_TYPE, type);
      }
    }
    return parameters;
  }

  private boolean isEmptyOrQuery(TaskQueryDto dto) {
    return dto == null || allTrue(
        dto.getProcessInstanceBusinessKey() == null,
        dto.getProcessInstanceBusinessKeyExpression() == null,
        isEmpty(dto.getProcessInstanceBusinessKeyIn()),
        dto.getProcessInstanceBusinessKeyLike() == null,
        dto.getProcessInstanceBusinessKeyLikeExpression() == null,
        dto.getProcessDefinitionKey() == null,
        isEmpty(dto.getProcessDefinitionKeyIn()),
        dto.getProcessDefinitionId() == null,
        dto.getExecutionId() == null,
        isEmpty(dto.getActivityInstanceIdIn()),
        dto.getProcessDefinitionName() == null,
        dto.getProcessDefinitionNameLike() == null,
        dto.getProcessInstanceId() == null,
        isEmpty(dto.getProcessInstanceIdIn()),
        dto.getAssignee() == null,
        dto.getAssigneeExpression() == null,
        dto.getAssigneeLike() == null,
        dto.getAssigneeLikeExpression() == null,
        isEmpty(dto.getAssigneeIn()),
        isEmpty(dto.getAssigneeNotIn()),
        dto.getCandidateGroup() == null,
        dto.getCandidateGroupExpression() == null,
        dto.getCandidateGroupLike() == null,
        dto.getCandidateUser() == null,
        dto.getCandidateUserExpression() == null,
        dto.getIncludeAssignedTasks() == null,
        dto.getTaskDefinitionKey() == null,
        isEmpty(dto.getTaskDefinitionKeyIn()),
        isEmpty(dto.getTaskDefinitionKeyNotIn()),
        dto.getTaskDefinitionKeyLike() == null,
        dto.getTaskId() == null,
        isEmpty(dto.getTaskIdIn()),
        dto.getDescription() == null,
        dto.getDescriptionLike() == null,
        dto.getInvolvedUser() == null,
        dto.getInvolvedUserExpression() == null,
        dto.getMaxPriority() == null,
        dto.getMinPriority() == null,
        dto.getName() == null,
        dto.getNameNotEqual() == null,
        dto.getNameLike() == null,
        dto.getNameNotLike() == null,
        dto.getOwner() == null,
        dto.getOwnerExpression() == null,
        dto.getPriority() == null,
        dto.getParentTaskId() == null,
        dto.getAssigned() == null,
        dto.getUnassigned() == null,
        dto.getActive() == null,
        dto.getSuspended() == null,
        dto.getDueAfter() == null,
        dto.getDueAfterExpression() == null,
        dto.getDueBefore() == null,
        dto.getDueBeforeExpression() == null,
        dto.getDueDate() == null,
        dto.getDueDateExpression() == null,
        dto.getWithoutDueDate() == null,
        dto.getFollowUpAfter() == null,
        dto.getFollowUpAfterExpression() == null,
        dto.getFollowUpBefore() == null,
        dto.getFollowUpBeforeExpression() == null,
        dto.getFollowUpBeforeOrNotExistent() == null,
        dto.getFollowUpBeforeOrNotExistentExpression() == null,
        dto.getFollowUpDate() == null,
        dto.getFollowUpDateExpression() == null,
        dto.getCreatedAfter() == null,
        dto.getCreatedAfterExpression() == null,
        dto.getCreatedBefore() == null,
        dto.getCreatedBeforeExpression() == null,
        dto.getCreatedOn() == null,
        dto.getCreatedOnExpression() == null,
        dto.getUpdatedAfter() == null,
        dto.getUpdatedAfterExpression() == null,
        dto.getDelegationState() == null,
        isEmpty(dto.getTenantIdIn()),
        dto.getWithoutTenantId() == null,
        isEmpty(dto.getCandidateGroups()),
        dto.getCandidateGroupsExpression() == null,
        dto.getWithCandidateGroups() == null,
        dto.getWithoutCandidateGroups() == null,
        dto.getWithCandidateUsers() == null,
        dto.getWithoutCandidateUsers() == null,
        isEmpty(dto.getTaskVariables()),
        isEmpty(dto.getProcessVariables()),
        dto.getVariableNamesIgnoreCase() == null,
        dto.getVariableValuesIgnoreCase() == null,
        dto.getWithCommentAttachmentInfo() == null
    );
  }

  private boolean allTrue(boolean... conditions) {
    for (boolean condition : conditions) {
      if (!condition) {
        return false;
      }
    }
    return true;
  }

  private boolean isEmpty(Object[] values) {
    return values == null || values.length == 0;
  }

  private boolean isEmpty(Collection<?> values) {
    return values == null || values.isEmpty();
  }
}
