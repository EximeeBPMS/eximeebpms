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

import org.eximeebpms.bpm.model.bpmn.Query;
import org.eximeebpms.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;

import java.util.Collection;

/**
 * The BPMN flowNode element
 *
 * @author Sebastian Menski
 */
public interface FlowNode extends FlowElement {

  @SuppressWarnings("rawtypes")
  AbstractFlowNodeBuilder builder();

  Collection<SequenceFlow> getIncoming();

  Collection<SequenceFlow> getOutgoing();

  Query<FlowNode> getPreviousNodes();

  Query<FlowNode> getSucceedingNodes();

  boolean isEximeeBpmsAsyncBefore();

  void setEximeeBpmsAsyncBefore(boolean isCamundaAsyncBefore);

  boolean isEximeeBpmsAsyncAfter();

  void setEximeeBpmsAsyncAfter(boolean isCamundaAsyncAfter);

  boolean isEximeeBpmsExclusive();

  void setEximeeBpmsExclusive(boolean isCamundaExclusive);

  String getEximeeBpmsJobPriority();

  void setEximeeBpmsJobPriority(String jobPriority);

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #isEximeeBpmsAsyncBefore()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default boolean isCamundaAsyncBefore() {
    return isEximeeBpmsAsyncBefore();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsAsyncBefore(boolean)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaAsyncBefore(boolean isEximeeBpmsAsyncBefore) {
    setEximeeBpmsAsyncBefore(isEximeeBpmsAsyncBefore);
  }

  /**
   * @deprecated use {@link #isEximeeBpmsAsyncAfter()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default boolean isCamundaAsyncAfter() {
    return isEximeeBpmsAsyncAfter();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsAsyncAfter(boolean)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaAsyncAfter(boolean isEximeeBpmsAsyncAfter) {
    setEximeeBpmsAsyncAfter(isEximeeBpmsAsyncAfter);
  }

  /**
   * @deprecated use {@link #isEximeeBpmsExclusive()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default boolean isCamundaExclusive() {
    return isEximeeBpmsExclusive();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsExclusive(boolean)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaExclusive(boolean isEximeeBpmsExclusive) {
    setEximeeBpmsExclusive(isEximeeBpmsExclusive);
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
}
