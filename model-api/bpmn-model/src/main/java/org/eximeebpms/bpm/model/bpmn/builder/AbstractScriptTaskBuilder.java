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
import org.eximeebpms.bpm.model.bpmn.instance.Script;
import org.eximeebpms.bpm.model.bpmn.instance.ScriptTask;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractScriptTaskBuilder<B extends AbstractScriptTaskBuilder<B>> extends AbstractTaskBuilder<B, ScriptTask> {

  protected AbstractScriptTaskBuilder(BpmnModelInstance modelInstance, ScriptTask element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Sets the script format of the build script task.
   *
   * @param scriptFormat  the script format to set
   * @return the builder object
   */
  public B scriptFormat(String scriptFormat) {
    element.setScriptFormat(scriptFormat);
    return myself;
  }

  /**
   * Sets the script of the build script task.
   *
   * @param script  the script to set
   * @return the builder object
   */
  public B script(Script script) {
    element.setScript(script);
    return myself;
  }

  public B scriptText(String scriptText) {
    Script script = createChild(Script.class);
    script.setTextContent(scriptText);
    return myself;
  }

  /** camunda extensions */

  /**
   * Sets the camunda result variable of the build script task.
   *
   * @param camundaResultVariable  the result variable to set
   * @return the builder object
   */
  public B eximeeBpmsResultVariable(String camundaResultVariable) {
    element.setEximeeBpmsResultVariable(camundaResultVariable);
    return myself;
  }

  /**
   * Sets the camunda resource of the build script task.
   *
   * @param camundaResource  the resource to set
   * @return the builder object
   */
  public B eximeeBpmsResource(String camundaResource) {
    element.setEximeeBpmsResource(camundaResource);
    return myself;
  }

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #eximeeBpmsResultVariable(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaResultVariable(String eximeeBpmsResultVariable) {
    return eximeeBpmsResultVariable(eximeeBpmsResultVariable);
  }

  /**
   * @deprecated use {@link #eximeeBpmsResource(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaResource(String eximeeBpmsResource) {
    return eximeeBpmsResource(eximeeBpmsResource);
  }
}
