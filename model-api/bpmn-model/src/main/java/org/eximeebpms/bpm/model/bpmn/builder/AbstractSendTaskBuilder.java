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
import org.eximeebpms.bpm.model.bpmn.instance.Message;
import org.eximeebpms.bpm.model.bpmn.instance.Operation;
import org.eximeebpms.bpm.model.bpmn.instance.SendTask;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractSendTaskBuilder<B extends AbstractSendTaskBuilder<B>> extends AbstractTaskBuilder<B, SendTask> {

  protected AbstractSendTaskBuilder(BpmnModelInstance modelInstance, SendTask element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Sets the implementation of the send task.
   *
   * @param implementation  the implementation to set
   * @return the builder object
   */
  public B implementation(String implementation) {
    element.setImplementation(implementation);
    return myself;
  }

  /**
   * Sets the message of the send task.
   * @param message  the message to set
   * @return the builder object
   */
  public B message(Message message) {
    element.setMessage(message);
    return myself;
  }

  /**
   * Sets the message with the given message name. If already a message
   * with this name exists it will be used, otherwise a new message is created.
   *
   * @param messageName the name of the message
   * @return the builder object
   */
  public B message(String messageName) {
    Message message = findMessageForName(messageName);
    return message(message);
  }

  /**
   * Sets the operation of the send task.
   *
   * @param operation  the operation to set
   * @return the builder object
   */
  public B operation(Operation operation) {
    element.setOperation(operation);
    return myself;
  }

  /** camunda extensions */

  /**
   * Sets the camunda class attribute.
   *
   * @param camundaClass  the class name to set
   * @return the builder object
   */
  public B eximeeBpmsClass(Class delegateClass) {
    return eximeeBpmsClass(delegateClass.getName());
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
   * Sets the camunda topic attribute.
   *
   * @param camundaTopic  the topic to set
   * @return the builder object
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
   * Set the camunda task priority attribute.
   * The priority is only used for service tasks which have as type value
   * <code>external</code>
   * 
   * @param taskPriority the task priority which should used for the external tasks
   * @return the builder object
   */
  public B eximeeBpmsTaskPriority(String taskPriority) {
    element.setEximeeBpmsTaskPriority(taskPriority);
    return myself;
  }

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #eximeeBpmsClass(Class)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaClass(Class delegateClass) {
    return eximeeBpmsClass(delegateClass);
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
   * @deprecated use {@link #eximeeBpmsTaskPriority(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskPriority(String taskPriority) {
    return eximeeBpmsTaskPriority(taskPriority);
  }
}
