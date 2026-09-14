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
package org.eximeebpms.bpm.model.dmn.instance;

import java.util.Collection;

public interface Decision extends DrgElement {

  Question getQuestion();

  void setQuestion(Question question);

  AllowedAnswers getAllowedAnswers();

  void setAllowedAnswers(AllowedAnswers allowedAnswers);

  Variable getVariable();

  void setVariable(Variable variable);

  Collection<InformationRequirement> getInformationRequirements();

  Collection<KnowledgeRequirement> getKnowledgeRequirements();

  Collection<AuthorityRequirement> getAuthorityRequirements();

  Collection<SupportedObjectiveReference> getSupportedObjectiveReferences();

  Collection<PerformanceIndicator> getImpactedPerformanceIndicators();

  Collection<OrganizationUnit> getDecisionMakers();

  Collection<OrganizationUnit> getDecisionOwners();

  Collection<UsingProcessReference> getUsingProcessReferences();

  Collection<UsingTaskReference> getUsingTaskReferences();

  Expression getExpression();

  void setExpression(Expression expression);

  // eximeebpms extensions

  /**
   * @deprecated use {@link #getEximeeBpmsHistoryTimeToLiveString()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  Integer getEximeeBpmsHistoryTimeToLive();

  /**
   * @deprecated use {@link #setEximeeBpmsHistoryTimeToLiveString(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  void setEximeeBpmsHistoryTimeToLive(Integer historyTimeToLive);

  String getEximeeBpmsHistoryTimeToLiveString();

  void setEximeeBpmsHistoryTimeToLiveString(String historyTimeToLive);

  /**
   * @deprecated use {@link #getEximeeBpmsHistoryTimeToLiveString()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default Integer getCamundaHistoryTimeToLive() {
    return getEximeeBpmsHistoryTimeToLive();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsHistoryTimeToLiveString(String)} instead.
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


  String getVersionTag();

  void setVersionTag(String inputValue);
}
