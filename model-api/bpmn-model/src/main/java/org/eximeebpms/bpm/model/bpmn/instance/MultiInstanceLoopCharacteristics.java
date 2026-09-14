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

import org.eximeebpms.bpm.model.bpmn.MultiInstanceFlowCondition;
import org.eximeebpms.bpm.model.bpmn.builder.MultiInstanceLoopCharacteristicsBuilder;

/**
 * The BPMN 2.0 multiInstanceLoopCharacteristics element type
 *
 * @author Filip Hrisafov
 *
 */
public interface MultiInstanceLoopCharacteristics extends LoopCharacteristics {

  LoopCardinality getLoopCardinality();

  void setLoopCardinality(LoopCardinality loopCardinality);

  DataInput getLoopDataInputRef();

  void setLoopDataInputRef(DataInput loopDataInputRef);

  DataOutput getLoopDataOutputRef();

  void setLoopDataOutputRef(DataOutput loopDataOutputRef);

  InputDataItem getInputDataItem();

  void setInputDataItem(InputDataItem inputDataItem);

  OutputDataItem getOutputDataItem();

  void setOutputDataItem(OutputDataItem outputDataItem);

  Collection<ComplexBehaviorDefinition> getComplexBehaviorDefinitions();

  CompletionCondition getCompletionCondition();

  void setCompletionCondition(CompletionCondition completionCondition);

  boolean isSequential();

  void setSequential(boolean sequential);

  MultiInstanceFlowCondition getBehavior();

  void setBehavior(MultiInstanceFlowCondition behavior);

  EventDefinition getOneBehaviorEventRef();

  void setOneBehaviorEventRef(EventDefinition oneBehaviorEventRef);

  EventDefinition getNoneBehaviorEventRef();

  void setNoneBehaviorEventRef(EventDefinition noneBehaviorEventRef);

  String getEximeeBpmsCollection();

  void setEximeeBpmsCollection(String expression);

  String getEximeeBpmsElementVariable();

  void setEximeeBpmsElementVariable(String variableName);

  boolean isEximeeBpmsAsyncBefore();

  void setEximeeBpmsAsyncBefore(boolean isCamundaAsyncBefore);

  boolean isEximeeBpmsAsyncAfter();

  void setEximeeBpmsAsyncAfter(boolean isCamundaAsyncAfter);

  boolean isEximeeBpmsExclusive();

  void setEximeeBpmsExclusive(boolean isCamundaExclusive);

  MultiInstanceLoopCharacteristicsBuilder builder();

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #getEximeeBpmsCollection()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaCollection() {
    return getEximeeBpmsCollection();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsCollection(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaCollection(String expression) {
    setEximeeBpmsCollection(expression);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsElementVariable()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaElementVariable() {
    return getEximeeBpmsElementVariable();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsElementVariable(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaElementVariable(String variableName) {
    setEximeeBpmsElementVariable(variableName);
  }

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
}
