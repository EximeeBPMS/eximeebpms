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
package org.eximeebpms.bpm.model.bpmn.builder;

import org.eximeebpms.bpm.model.bpmn.BpmnModelInstance;
import org.eximeebpms.bpm.model.bpmn.instance.Task;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractTaskBuilder<B extends AbstractTaskBuilder<B, E>, E extends Task> extends AbstractActivityBuilder<B, E> {

  protected AbstractTaskBuilder(BpmnModelInstance modelInstance, E element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /** camunda extensions */

  /**
   * @deprecated use eximeeBpmsAsyncBefore() instead.
   *
   * Sets the camunda async attribute to true.
   *
   * @return the builder object
   */
  @Deprecated
  public B eximeeBpmsAsync() {
    element.setEximeeBpmsAsyncBefore(true);
    return myself;
  }

  /**
   * @deprecated use eximeeBpmsAsyncBefore(isCamundaAsyncBefore) instead.
   *
   * Sets the camunda async attribute.
   *
   * @param isCamundaAsync  the async state of the task
   * @return the builder object
   */
  @Deprecated
  public B eximeeBpmsAsync(boolean isCamundaAsync) {
    element.setEximeeBpmsAsyncBefore(isCamundaAsync);
    return myself;
  }

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #eximeeBpmsAsync()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaAsync() {
    return eximeeBpmsAsync();
  }

  /**
   * @deprecated use {@link #eximeeBpmsAsync(boolean)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaAsync(boolean isEximeeBpmsAsync) {
    return eximeeBpmsAsync(isEximeeBpmsAsync);
  }
}
