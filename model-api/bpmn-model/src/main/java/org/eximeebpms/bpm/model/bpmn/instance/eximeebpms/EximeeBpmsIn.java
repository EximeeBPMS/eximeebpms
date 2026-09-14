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
package org.eximeebpms.bpm.model.bpmn.instance.eximeebpms;

import org.eximeebpms.bpm.model.bpmn.instance.BpmnModelElementInstance;

/**
 * The BPMN in camunda extension element
 *
 * @author Sebastian Menski
 */
public interface EximeeBpmsIn extends BpmnModelElementInstance {

  String getEximeeBpmsSource();

  void setEximeeBpmsSource(String camundaSource);

  String getEximeeBpmsSourceExpression();

  void setEximeeBpmsSourceExpression(String camundaSourceExpression);

  String getEximeeBpmsVariables();

  void setEximeeBpmsVariables(String camundaVariables);

  String getEximeeBpmsTarget();

  void setEximeeBpmsTarget(String camundaTarget);

  String getEximeeBpmsBusinessKey();

  void setEximeeBpmsBusinessKey(String camundaBusinessKey);

  boolean getEximeeBpmsLocal();

  void setEximeeBpmsLocal(boolean camundaLocal);

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #getEximeeBpmsSource()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaSource() {
    return getEximeeBpmsSource();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsSource(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaSource(String eximeeBpmsSource) {
    setEximeeBpmsSource(eximeeBpmsSource);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsSourceExpression()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaSourceExpression() {
    return getEximeeBpmsSourceExpression();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsSourceExpression(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaSourceExpression(String eximeeBpmsSourceExpression) {
    setEximeeBpmsSourceExpression(eximeeBpmsSourceExpression);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsVariables()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaVariables() {
    return getEximeeBpmsVariables();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsVariables(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaVariables(String eximeeBpmsVariables) {
    setEximeeBpmsVariables(eximeeBpmsVariables);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsTarget()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaTarget() {
    return getEximeeBpmsTarget();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsTarget(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaTarget(String eximeeBpmsTarget) {
    setEximeeBpmsTarget(eximeeBpmsTarget);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsBusinessKey()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaBusinessKey() {
    return getEximeeBpmsBusinessKey();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsBusinessKey(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaBusinessKey(String eximeeBpmsBusinessKey) {
    setEximeeBpmsBusinessKey(eximeeBpmsBusinessKey);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsLocal()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default boolean getCamundaLocal() {
    return getEximeeBpmsLocal();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsLocal(boolean)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaLocal(boolean eximeeBpmsLocal) {
    setEximeeBpmsLocal(eximeeBpmsLocal);
  }
}
