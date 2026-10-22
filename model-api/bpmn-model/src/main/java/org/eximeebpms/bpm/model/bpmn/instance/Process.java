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
package org.eximeebpms.bpm.model.bpmn.instance;

import org.eximeebpms.bpm.model.bpmn.ProcessType;
import org.eximeebpms.bpm.model.bpmn.builder.ProcessBuilder;

import java.util.Collection;
import java.util.List;

/**
 * The BPMN process element
 *
 * @author Daniel Meyer
 * @author Sebastian Menski
 */
public interface Process extends CallableElement {

  ProcessBuilder builder();

  ProcessType getProcessType();

  void setProcessType(ProcessType processType);

  boolean isClosed();

  void setClosed(boolean closed);

  boolean isExecutable();

  void setExecutable(boolean executable);

  // TODO: collaboration ref

  Auditing getAuditing();

  void setAuditing(Auditing auditing);

  Monitoring getMonitoring();

  void setMonitoring(Monitoring monitoring);

  Collection<Property> getProperties();

  Collection<LaneSet> getLaneSets();

  Collection<FlowElement> getFlowElements();

  Collection<Artifact> getArtifacts();

  Collection<CorrelationSubscription> getCorrelationSubscriptions();

  Collection<ResourceRole> getResourceRoles();

  Collection<Process> getSupports();

  /** camunda extensions */

  String getEximeeBpmsCandidateStarterGroups();

  void setEximeeBpmsCandidateStarterGroups(String camundaCandidateStarterGroups);

  List<String> getEximeeBpmsCandidateStarterGroupsList();

  void setEximeeBpmsCandidateStarterGroupsList(List<String> camundaCandidateStarterGroupsList);

  String getEximeeBpmsCandidateStarterUsers();

  void setEximeeBpmsCandidateStarterUsers(String camundaCandidateStarterUsers);

  List<String> getEximeeBpmsCandidateStarterUsersList();

  void setEximeeBpmsCandidateStarterUsersList(List<String> camundaCandidateStarterUsersList);

  String getEximeeBpmsJobPriority();

  void setEximeeBpmsJobPriority(String jobPriority);

  String getEximeeBpmsTaskPriority();

  void setEximeeBpmsTaskPriority(String taskPriority);

  String getEximeeBpmsHistoryTimeToLiveString();

  void setEximeeBpmsHistoryTimeToLiveString(String historyTimeToLive);

  Boolean isEximeeBpmsStartableInTasklist();

  void setEximeeBpmsIsStartableInTasklist(Boolean isStartableInTasklist);

  String getEximeeBpmsVersionTag();

  void setEximeeBpmsVersionTag(String versionTag);

}
