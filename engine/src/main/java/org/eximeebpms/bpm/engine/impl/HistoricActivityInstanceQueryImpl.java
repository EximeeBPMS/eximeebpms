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
package org.eximeebpms.bpm.engine.impl;

import static org.eximeebpms.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serial;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import org.eximeebpms.bpm.engine.ProcessEngineException;
import org.eximeebpms.bpm.engine.history.HistoricActivityInstance;
import org.eximeebpms.bpm.engine.history.HistoricActivityInstanceQuery;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;
import org.eximeebpms.bpm.engine.impl.pvm.runtime.ActivityInstanceState;
import org.eximeebpms.bpm.engine.impl.util.CompareUtil;

/**
 * @author Tom Baeyens
 */
public class HistoricActivityInstanceQueryImpl extends AbstractQuery<HistoricActivityInstanceQuery, HistoricActivityInstance>
    implements HistoricActivityInstanceQuery {

  @Serial
  private static final long serialVersionUID = 1L;
  @Getter
  protected String activityInstanceId;
  @Getter
  protected String processInstanceId;
  @Getter
  protected String executionId;
  @Getter
  protected String processDefinitionId;
  @Getter
  protected String activityId;
  @Getter
  protected String activityName;
  protected String activityNameLike;
  @Getter
  protected String activityType;
  @Getter
  protected String assignee;
  @Getter
  protected boolean finished;
  @Getter
  protected boolean unfinished;
  @Getter
  protected Date startedBefore;
  @Getter
  protected Date startedAfter;
  @Getter
  protected Date finishedBefore;
  @Getter
  protected Date finishedAfter;
  @Getter
  protected ActivityInstanceState activityInstanceState;
  protected String[] tenantIds;
  protected boolean isTenantIdSet;
  protected String activityInstanceIdAfter;

  public HistoricActivityInstanceQueryImpl() {
  }

  public HistoricActivityInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricActivityInstanceManager()
      .findHistoricActivityInstanceCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricActivityInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistoricActivityInstanceManager()
      .findHistoricActivityInstancesByQueryCriteria(this, page);
  }

  public HistoricActivityInstanceQueryImpl idAfter(String id) {
    this.activityInstanceIdAfter = id;
    return this;
  }

  public String getIdAfter() {
    return activityInstanceIdAfter;
  }

  public HistoricActivityInstanceQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricActivityInstanceQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public HistoricActivityInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public HistoricActivityInstanceQueryImpl activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  public HistoricActivityInstanceQueryImpl activityName(String activityName) {
    this.activityName = activityName;
    return this;
  }

  public HistoricActivityInstanceQueryImpl activityNameLike(String activityNameLike) {
    this.activityNameLike = activityNameLike;
    return this;
  }

  public HistoricActivityInstanceQueryImpl activityType(String activityType) {
    this.activityType = activityType;
    return this;
  }

  public HistoricActivityInstanceQueryImpl taskAssignee(String assignee) {
    this.assignee = assignee;
    return this;
  }

  public HistoricActivityInstanceQueryImpl finished() {
    this.finished = true;
    return this;
  }

  public HistoricActivityInstanceQueryImpl unfinished() {
    this.unfinished = true;
    return this;
  }

  public HistoricActivityInstanceQueryImpl completeScope() {
    if (activityInstanceState != null) {
      throw new ProcessEngineException("Already querying for activity instance state <" + activityInstanceState + ">");
    }

    this.activityInstanceState = ActivityInstanceState.SCOPE_COMPLETE;
    return this;
  }

  public HistoricActivityInstanceQueryImpl canceled() {
    if (activityInstanceState != null) {
      throw new ProcessEngineException("Already querying for activity instance state <" + activityInstanceState + ">");
    }
    this.activityInstanceState = ActivityInstanceState.CANCELED;
    return this;
  }

  public HistoricActivityInstanceQueryImpl startedAfter(Date date) {
    startedAfter = date;
    return this;
  }

  public HistoricActivityInstanceQueryImpl startedBefore(Date date) {
    startedBefore = date;
    return this;
  }

  public HistoricActivityInstanceQueryImpl finishedAfter(Date date) {
    finishedAfter = date;
    return this;
  }

  public HistoricActivityInstanceQueryImpl finishedBefore(Date date) {
    finishedBefore = date;
    return this;
  }

  public HistoricActivityInstanceQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    this.isTenantIdSet = true;
    return this;
  }

  @Override
  public HistoricActivityInstanceQuery withoutTenantId() {
    this.tenantIds = null;
    this.isTenantIdSet = true;
    return this;
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions()
      || CompareUtil.areNotInAscendingOrder(startedAfter, startedBefore)
      || CompareUtil.areNotInAscendingOrder(finishedAfter, finishedBefore);
  }

  // ordering /////////////////////////////////////////////////////////////////

  public HistoricActivityInstanceQueryImpl orderByHistoricActivityInstanceDuration() {
    orderBy(HistoricActivityInstanceQueryProperty.DURATION);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByHistoricActivityInstanceEndTime() {
    orderBy(HistoricActivityInstanceQueryProperty.END);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByExecutionId() {
    orderBy(HistoricActivityInstanceQueryProperty.EXECUTION_ID);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByHistoricActivityInstanceId() {
    orderBy(HistoricActivityInstanceQueryProperty.HISTORIC_ACTIVITY_INSTANCE_ID);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByProcessDefinitionId() {
    orderBy(HistoricActivityInstanceQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByProcessInstanceId() {
    orderBy(HistoricActivityInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByHistoricActivityInstanceStartTime() {
    orderBy(HistoricActivityInstanceQueryProperty.START);
    return this;
  }

  public HistoricActivityInstanceQuery orderByActivityId() {
    orderBy(HistoricActivityInstanceQueryProperty.ACTIVITY_ID);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByActivityName() {
    orderBy(HistoricActivityInstanceQueryProperty.ACTIVITY_NAME);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByActivityType() {
    orderBy(HistoricActivityInstanceQueryProperty.ACTIVITY_TYPE);
    return this;
  }

  public HistoricActivityInstanceQuery orderPartiallyByOccurrence() {
    orderBy(HistoricActivityInstanceQueryProperty.SEQUENCE_COUNTER);
    return this;
  }

  public HistoricActivityInstanceQuery orderByTenantId() {
    return orderBy(HistoricActivityInstanceQueryProperty.TENANT_ID);
  }

  public HistoricActivityInstanceQueryImpl activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  // getters and setters //////////////////////////////////////////////////////

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }
}
