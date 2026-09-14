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
import org.eximeebpms.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.eximeebpms.bpm.model.bpmn.instance.ServiceTask;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsErrorEventDefinition;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractServiceTaskBuilder<B extends AbstractServiceTaskBuilder<B>> extends AbstractTaskBuilder<B, ServiceTask> {

  protected AbstractServiceTaskBuilder(BpmnModelInstance modelInstance, ServiceTask element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Sets the implementation of the build service task.
   *
   * @param implementation  the implementation to set
   * @return the builder object
   */
  public B implementation(String implementation) {
    element.setImplementation(implementation);
    return myself;
  }

  /** camunda extensions */

  /**
   * Sets the camunda class attribute.
   *
   * @param camundaClass  the class name to set
   * @return the builder object
   */
  @SuppressWarnings("rawtypes")
  public B eximeeBpmsClass(Class camundaClass) {
    return eximeeBpmsClass(camundaClass.getName());
  }

  /**
   * Sets the camunda class attribute.
   *
   * @param camundaClass  the class name to set
   * @return the builder object
   */
  public B eximeeBpmsClass(String fullQualifiedClassName) {
    element.setEximeeBpmsClass(fullQualifiedClassName);
    return myself;
  }

  /**
   * Sets the camunda delegateExpression attribute.
   *
   * @param camundaExpression  the delegateExpression to set
   * @return the builder object
   */
  public B eximeeBpmsDelegateExpression(String camundaExpression) {
    element.setEximeeBpmsDelegateExpression(camundaExpression);
    return myself;
  }

  /**
   * Sets the camunda expression attribute.
   *
   * @param camundaExpression  the expression to set
   * @return the builder object
   */
  public B eximeeBpmsExpression(String camundaExpression) {
    element.setEximeeBpmsExpression(camundaExpression);
    return myself;
  }

  /**
   * Sets the camunda resultVariable attribute.
   *
   * @param camundaResultVariable  the name of the process variable
   * @return the builder object
   */
  public B eximeeBpmsResultVariable(String camundaResultVariable) {
    element.setEximeeBpmsResultVariable(camundaResultVariable);
    return myself;
  }

  /**
   * Sets the camunda topic attribute. This is only meaningful when
   * the {@link #eximeeBpmsType(String)} attribute has the value <code>external</code>.
   *
   * @param camundaTopic the topic to set
   * @return the build object
   */
  public B eximeeBpmsTopic(String camundaTopic) {
    element.setEximeeBpmsTopic(camundaTopic);
    return myself;
  }

  /**
   * Sets the camunda type attribute.
   *
   * @param camundaType  the type of the service task
   * @return the builder object
   */
  public B eximeeBpmsType(String camundaType) {
    element.setEximeeBpmsType(camundaType);
    return myself;
  }

  /**
   * Sets the camunda topic attribute and the camunda type attribute to the
   * value <code>external</code. Reduces two calls to {@link #eximeeBpmsType(String)} and {@link #eximeeBpmsTopic(String)}.
   *
   * @param camundaTopic the topic to set
   * @return the build object
   */
  public B eximeeBpmsExternalTask(String camundaTopic) {
    this.eximeeBpmsType("external");
    this.eximeeBpmsTopic(camundaTopic);
    return myself;
  }

  /**
   * Sets the camunda task priority attribute. This is only meaningful when
   * the {@link #eximeeBpmsType(String)} attribute has the value <code>external</code>.
   *
   *
   * @param taskPriority the priority for the external task
   * @return the builder object
   */
  public B eximeeBpmsTaskPriority(String taskPriority) {
    element.setEximeeBpmsTaskPriority(taskPriority);
    return myself;
  }

  /**
   * Creates an error event definition for this service task and returns a builder for the error event definition.
   * This is only meaningful when the {@link #eximeeBpmsType(String)} attribute has the value <code>external</code>.
   *
   * @return the error event definition builder object
   */
  public EximeeBpmsErrorEventDefinitionBuilder eximeeBpmsErrorEventDefinition() {
    ErrorEventDefinition camundaErrorEventDefinition = createInstance(EximeeBpmsErrorEventDefinition.class);
    addExtensionElement(camundaErrorEventDefinition);
    return new EximeeBpmsErrorEventDefinitionBuilder(modelInstance, camundaErrorEventDefinition);
  }

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #eximeeBpmsClass(Class)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaClass(Class eximeeBpmsClass) {
    return eximeeBpmsClass(eximeeBpmsClass);
  }

  /**
   * @deprecated use {@link #eximeeBpmsClass(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaClass(String fullQualifiedClassName) {
    return eximeeBpmsClass(fullQualifiedClassName);
  }

  /**
   * @deprecated use {@link #eximeeBpmsDelegateExpression(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaDelegateExpression(String eximeeBpmsExpression) {
    return eximeeBpmsDelegateExpression(eximeeBpmsExpression);
  }

  /**
   * @deprecated use {@link #eximeeBpmsExpression(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaExpression(String eximeeBpmsExpression) {
    return eximeeBpmsExpression(eximeeBpmsExpression);
  }

  /**
   * @deprecated use {@link #eximeeBpmsResultVariable(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaResultVariable(String eximeeBpmsResultVariable) {
    return eximeeBpmsResultVariable(eximeeBpmsResultVariable);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTopic(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTopic(String eximeeBpmsTopic) {
    return eximeeBpmsTopic(eximeeBpmsTopic);
  }

  /**
   * @deprecated use {@link #eximeeBpmsType(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaType(String eximeeBpmsType) {
    return eximeeBpmsType(eximeeBpmsType);
  }

  /**
   * @deprecated use {@link #eximeeBpmsExternalTask(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaExternalTask(String eximeeBpmsTopic) {
    return eximeeBpmsExternalTask(eximeeBpmsTopic);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskPriority(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskPriority(String taskPriority) {
    return eximeeBpmsTaskPriority(taskPriority);
  }

  /**
   * @deprecated use {@link #eximeeBpmsErrorEventDefinition()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public EximeeBpmsErrorEventDefinitionBuilder camundaErrorEventDefinition() {
    return eximeeBpmsErrorEventDefinition();
  }
}
