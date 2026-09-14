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

  @Deprecated
  Integer getEximeeBpmsHistoryTimeToLive();

  @Deprecated
  void setEximeeBpmsHistoryTimeToLive(Integer historyTimeToLive);

  String getEximeeBpmsHistoryTimeToLiveString();

  void setEximeeBpmsHistoryTimeToLiveString(String historyTimeToLive);

  Boolean isEximeeBpmsStartableInTasklist();

  void setEximeeBpmsIsStartableInTasklist(Boolean isStartableInTasklist);

  String getEximeeBpmsVersionTag();

  void setEximeeBpmsVersionTag(String versionTag);

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #getEximeeBpmsCandidateStarterGroups()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCandidateStarterGroups() {
    return getEximeeBpmsCandidateStarterGroups();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCandidateStarterGroups(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCandidateStarterGroups(String eximeeBpmsCandidateStarterGroups) {
    setEximeeBpmsCandidateStarterGroups(eximeeBpmsCandidateStarterGroups);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCandidateStarterGroupsList()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default List<String> getCamundaCandidateStarterGroupsList() {
    return getEximeeBpmsCandidateStarterGroupsList();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCandidateStarterGroupsList(List<String>)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCandidateStarterGroupsList(List<String> eximeeBpmsCandidateStarterGroupsList) {
    setEximeeBpmsCandidateStarterGroupsList(eximeeBpmsCandidateStarterGroupsList);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCandidateStarterUsers()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCandidateStarterUsers() {
    return getEximeeBpmsCandidateStarterUsers();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCandidateStarterUsers(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCandidateStarterUsers(String eximeeBpmsCandidateStarterUsers) {
    setEximeeBpmsCandidateStarterUsers(eximeeBpmsCandidateStarterUsers);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCandidateStarterUsersList()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default List<String> getCamundaCandidateStarterUsersList() {
    return getEximeeBpmsCandidateStarterUsersList();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCandidateStarterUsersList(List<String>)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCandidateStarterUsersList(List<String> eximeeBpmsCandidateStarterUsersList) {
    setEximeeBpmsCandidateStarterUsersList(eximeeBpmsCandidateStarterUsersList);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsJobPriority()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaJobPriority() {
    return getEximeeBpmsJobPriority();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsJobPriority(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaJobPriority(String jobPriority) {
    setEximeeBpmsJobPriority(jobPriority);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsTaskPriority()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaTaskPriority() {
    return getEximeeBpmsTaskPriority();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsTaskPriority(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaTaskPriority(String taskPriority) {
    setEximeeBpmsTaskPriority(taskPriority);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsHistoryTimeToLive()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default Integer getCamundaHistoryTimeToLive() {
    return getEximeeBpmsHistoryTimeToLive();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsHistoryTimeToLive(Integer)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaHistoryTimeToLive(Integer historyTimeToLive) {
    setEximeeBpmsHistoryTimeToLive(historyTimeToLive);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsHistoryTimeToLiveString()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaHistoryTimeToLiveString() {
    return getEximeeBpmsHistoryTimeToLiveString();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsHistoryTimeToLiveString(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaHistoryTimeToLiveString(String historyTimeToLive) {
    setEximeeBpmsHistoryTimeToLiveString(historyTimeToLive);
  }

  /**
   * @deprecated use {@link #isEximeeBpmsStartableInTasklist()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default Boolean isCamundaStartableInTasklist() {
    return isEximeeBpmsStartableInTasklist();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsIsStartableInTasklist(Boolean)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaIsStartableInTasklist(Boolean isStartableInTasklist) {
    setEximeeBpmsIsStartableInTasklist(isStartableInTasklist);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsVersionTag()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaVersionTag() {
    return getEximeeBpmsVersionTag();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsVersionTag(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaVersionTag(String versionTag) {
    setEximeeBpmsVersionTag(versionTag);
  }
}
