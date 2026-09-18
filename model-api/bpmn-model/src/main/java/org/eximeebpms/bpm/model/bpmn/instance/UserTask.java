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

}
