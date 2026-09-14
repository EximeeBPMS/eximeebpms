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

import java.util.Collection;

/**
 * The BPMN formField camunda extension element
 *
 * @author Sebastian Menski
 */
public interface EximeeBpmsFormField extends BpmnModelElementInstance {

  String getEximeeBpmsId();

  void setEximeeBpmsId(String camundaId);

  String getEximeeBpmsLabel();

  void setEximeeBpmsLabel(String camundaLabel);

  String getEximeeBpmsType();

  void setEximeeBpmsType(String camundaType);

  String getEximeeBpmsDatePattern();

  void setEximeeBpmsDatePattern(String camundaDatePattern);

  String getEximeeBpmsDefaultValue();

  void setEximeeBpmsDefaultValue(String camundaDefaultValue);

  EximeeBpmsProperties getEximeeBpmsProperties();

  void setEximeeBpmsProperties(EximeeBpmsProperties camundaProperties);

  EximeeBpmsValidation getEximeeBpmsValidation();

  void setEximeeBpmsValidation(EximeeBpmsValidation camundaValidation);

  Collection<EximeeBpmsValue> getEximeeBpmsValues();

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #getEximeeBpmsId()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaId() {
    return getEximeeBpmsId();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsId(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaId(String eximeeBpmsId) {
    setEximeeBpmsId(eximeeBpmsId);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsLabel()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaLabel() {
    return getEximeeBpmsLabel();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsLabel(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaLabel(String eximeeBpmsLabel) {
    setEximeeBpmsLabel(eximeeBpmsLabel);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsType()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaType() {
    return getEximeeBpmsType();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsType(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaType(String eximeeBpmsType) {
    setEximeeBpmsType(eximeeBpmsType);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsDatePattern()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaDatePattern() {
    return getEximeeBpmsDatePattern();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsDatePattern(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaDatePattern(String eximeeBpmsDatePattern) {
    setEximeeBpmsDatePattern(eximeeBpmsDatePattern);
  }

  /**
   * @deprecated use {@link #getEximeeBpmsDefaultValue()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaDefaultValue() {
    return getEximeeBpmsDefaultValue();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsDefaultValue(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaDefaultValue(String eximeeBpmsDefaultValue) {
    setEximeeBpmsDefaultValue(eximeeBpmsDefaultValue);
  }
}
