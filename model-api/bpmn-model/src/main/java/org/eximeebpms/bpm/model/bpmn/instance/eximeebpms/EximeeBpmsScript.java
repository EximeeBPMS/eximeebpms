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
 * The BPMN script camunda extension element
 *
 * @author Sebastian Menski
 */
public interface EximeeBpmsScript extends BpmnModelElementInstance {

  String getEximeeBpmsScriptFormat();

  void setEximeeBpmsScriptFormat(String camundaScriptFormat);

  String getEximeeBpmsResource();

  void setEximeeBpmsResource(String camundaResource);

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #getEximeeBpmsResource()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default String getCamundaResource() {
    return getEximeeBpmsResource();
  }

  /**
   * @deprecated use {@link #setEximeeBpmsResource(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  default void setCamundaResource(String eximeeBpmsResource) {
    setEximeeBpmsResource(eximeeBpmsResource);
  }
}
