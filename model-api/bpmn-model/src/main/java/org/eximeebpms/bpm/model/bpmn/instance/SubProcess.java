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

import org.eximeebpms.bpm.model.bpmn.builder.SubProcessBuilder;

import java.util.Collection;

/**
 * The BPMN subProcess element
 *
 * @author Sebastian Menski
 */
public interface SubProcess extends Activity {

  SubProcessBuilder builder();

  boolean triggeredByEvent();

  void setTriggeredByEvent(boolean triggeredByEvent);

  Collection<LaneSet> getLaneSets();

  Collection<FlowElement> getFlowElements();

  Collection<Artifact> getArtifacts();

  /** camunda extensions */

  /**
   * @deprecated use isEximeeBpmsAsyncBefore() instead.
   */
  @Deprecated
  boolean isEximeeBpmsAsync();

  /**
   * @deprecated use setEximeeBpmsAsyncBefore(isCamundaAsyncBefore) instead.
   */
  @Deprecated
  void setEximeeBpmsAsync(boolean isCamundaAsync);

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #isEximeeBpmsAsync()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default boolean isCamundaAsync() {
    return isEximeeBpmsAsync();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsAsync(boolean)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaAsync(boolean isEximeeBpmsAsync) {
    setEximeeBpmsAsync(isEximeeBpmsAsync);
  }
}
