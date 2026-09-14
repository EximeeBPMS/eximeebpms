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

import java.util.Collection;
import java.util.List;

import org.eximeebpms.bpm.model.bpmn.builder.UserTaskBuilder;

/**
 * The BPMN userTask element
 *
 * @author Sebastian Menski
 */
public interface UserTask extends Task {

  UserTaskBuilder builder();

  String getImplementation();

  void setImplementation(String implementation);

  Collection<Rendering> getRenderings();

  /** camunda extensions */

  String getEximeeBpmsAssignee();

  void setEximeeBpmsAssignee(String camundaAssignee);

  String getEximeeBpmsCandidateGroups();

  void setEximeeBpmsCandidateGroups(String camundaCandidateGroups);

  List<String> getEximeeBpmsCandidateGroupsList();

  void setEximeeBpmsCandidateGroupsList(List<String> camundaCandidateGroupsList);

  String getEximeeBpmsCandidateUsers();

  void setEximeeBpmsCandidateUsers(String camundaCandidateUsers);

  List<String> getEximeeBpmsCandidateUsersList();

  void setEximeeBpmsCandidateUsersList(List<String> camundaCandidateUsersList);

  String getEximeeBpmsDueDate();

  void setEximeeBpmsDueDate(String camundaDueDate);

  String getEximeeBpmsFollowUpDate();

  void setEximeeBpmsFollowUpDate(String camundaFollowUpDate);

  String getEximeeBpmsFormHandlerClass();

  void setEximeeBpmsFormHandlerClass(String camundaFormHandlerClass);

  String getEximeeBpmsFormKey();

  void setEximeeBpmsFormKey(String camundaFormKey);

  String getEximeeBpmsFormRef();

  void setEximeeBpmsFormRef(String camundaFormRef);

  String getEximeeBpmsFormRefBinding();

  void setEximeeBpmsFormRefBinding(String camundaFormRefBinding);

  String getEximeeBpmsFormRefVersion();

  void setEximeeBpmsFormRefVersion(String camundaFormRefVersion);

  String getEximeeBpmsPriority();

  void setEximeeBpmsPriority(String camundaPriority);

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #getEximeeBpmsAssignee()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaAssignee() {
    return getEximeeBpmsAssignee();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsAssignee(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaAssignee(String eximeeBpmsAssignee) {
    setEximeeBpmsAssignee(eximeeBpmsAssignee);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCandidateGroups()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCandidateGroups() {
    return getEximeeBpmsCandidateGroups();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCandidateGroups(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCandidateGroups(String eximeeBpmsCandidateGroups) {
    setEximeeBpmsCandidateGroups(eximeeBpmsCandidateGroups);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCandidateGroupsList()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default List<String> getCamundaCandidateGroupsList() {
    return getEximeeBpmsCandidateGroupsList();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCandidateGroupsList(List<String>)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCandidateGroupsList(List<String> eximeeBpmsCandidateGroupsList) {
    setEximeeBpmsCandidateGroupsList(eximeeBpmsCandidateGroupsList);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCandidateUsers()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCandidateUsers() {
    return getEximeeBpmsCandidateUsers();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCandidateUsers(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCandidateUsers(String eximeeBpmsCandidateUsers) {
    setEximeeBpmsCandidateUsers(eximeeBpmsCandidateUsers);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsCandidateUsersList()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default List<String> getCamundaCandidateUsersList() {
    return getEximeeBpmsCandidateUsersList();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCandidateUsersList(List<String>)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCandidateUsersList(List<String> eximeeBpmsCandidateUsersList) {
    setEximeeBpmsCandidateUsersList(eximeeBpmsCandidateUsersList);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsDueDate()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaDueDate() {
    return getEximeeBpmsDueDate();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsDueDate(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaDueDate(String eximeeBpmsDueDate) {
    setEximeeBpmsDueDate(eximeeBpmsDueDate);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsFollowUpDate()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaFollowUpDate() {
    return getEximeeBpmsFollowUpDate();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsFollowUpDate(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaFollowUpDate(String eximeeBpmsFollowUpDate) {
    setEximeeBpmsFollowUpDate(eximeeBpmsFollowUpDate);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsFormHandlerClass()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaFormHandlerClass() {
    return getEximeeBpmsFormHandlerClass();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsFormHandlerClass(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaFormHandlerClass(String eximeeBpmsFormHandlerClass) {
    setEximeeBpmsFormHandlerClass(eximeeBpmsFormHandlerClass);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsFormKey()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaFormKey() {
    return getEximeeBpmsFormKey();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsFormKey(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaFormKey(String eximeeBpmsFormKey) {
    setEximeeBpmsFormKey(eximeeBpmsFormKey);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsPriority()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaPriority() {
    return getEximeeBpmsPriority();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsPriority(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaPriority(String eximeeBpmsPriority) {
    setEximeeBpmsPriority(eximeeBpmsPriority);
  }
}
