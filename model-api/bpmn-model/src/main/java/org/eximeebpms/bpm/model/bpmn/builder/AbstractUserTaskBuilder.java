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

import java.util.List;

import org.eximeebpms.bpm.model.bpmn.BpmnModelInstance;
import org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants;
import org.eximeebpms.bpm.model.bpmn.instance.TimerEventDefinition;
import org.eximeebpms.bpm.model.bpmn.instance.UserTask;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsFormData;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsFormField;
import org.eximeebpms.bpm.model.bpmn.instance.eximeebpms.EximeeBpmsTaskListener;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractUserTaskBuilder<B extends AbstractUserTaskBuilder<B>> extends AbstractTaskBuilder<B, UserTask> {

  protected AbstractUserTaskBuilder(BpmnModelInstance modelInstance, UserTask element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Sets the implementation of the build user task.
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
   * Sets the camunda attribute assignee.
   *
   * @param camundaAssignee  the assignee to set
   * @return the builder object
   */
  public B eximeeBpmsAssignee(String camundaAssignee) {
    element.setEximeeBpmsAssignee(camundaAssignee);
    return myself;
  }

  /**
   * Sets the camunda candidate groups attribute.
   *
   * @param camundaCandidateGroups  the candidate groups to set
   * @return the builder object
   */
  public B eximeeBpmsCandidateGroups(String camundaCandidateGroups) {
    element.setEximeeBpmsCandidateGroups(camundaCandidateGroups);
    return myself;
  }

  /**
   * Sets the camunda candidate groups attribute.
   *
   * @param camundaCandidateGroups  the candidate groups to set
   * @return the builder object
   */
  public B eximeeBpmsCandidateGroups(List<String> camundaCandidateGroups) {
    element.setEximeeBpmsCandidateGroupsList(camundaCandidateGroups);
    return myself;
  }

  /**
   * Sets the camunda candidate users attribute.
   *
   * @param camundaCandidateUsers  the candidate users to set
   * @return the builder object
   */
  public B eximeeBpmsCandidateUsers(String camundaCandidateUsers) {
    element.setEximeeBpmsCandidateUsers(camundaCandidateUsers);
    return myself;
  }

  /**
   * Sets the camunda candidate users attribute.
   *
   * @param camundaCandidateUsers  the candidate users to set
   * @return the builder object
   */
  public B eximeeBpmsCandidateUsers(List<String> camundaCandidateUsers) {
    element.setEximeeBpmsCandidateUsersList(camundaCandidateUsers);
    return myself;
  }

  /**
   * Sets the camunda due date attribute.
   *
   * @param camundaDueDate  the due date of the user task
   * @return the builder object
   */
  public B eximeeBpmsDueDate(String camundaDueDate) {
    element.setEximeeBpmsDueDate(camundaDueDate);
    return myself;
  }

  /**
   * Sets the camunda follow up date attribute.
   *
   * @param camundaFollowUpDate  the follow up date of the user task
   * @return the builder object
   */
  public B eximeeBpmsFollowUpDate(String camundaFollowUpDate) {
    element.setEximeeBpmsFollowUpDate(camundaFollowUpDate);
    return myself;
  }

  /**
   * Sets the camunda form handler class attribute.
   *
   * @param camundaFormHandlerClass  the class name of the form handler
   * @return the builder object
   */
  @SuppressWarnings("rawtypes")
  public B eximeeBpmsFormHandlerClass(Class camundaFormHandlerClass) {
    return eximeeBpmsFormHandlerClass(camundaFormHandlerClass.getName());
  }

  /**
   * Sets the camunda form handler class attribute.
   *
   * @param camundaFormHandlerClass  the class name of the form handler
   * @return the builder object
   */
  public B eximeeBpmsFormHandlerClass(String fullQualifiedClassName) {
    element.setEximeeBpmsFormHandlerClass(fullQualifiedClassName);
    return myself;
  }

  /**
   * Sets the camunda form key attribute.
   *
   * @param camundaFormKey  the form key to set
   * @return the builder object
   */
  public B eximeeBpmsFormKey(String camundaFormKey) {
    element.setEximeeBpmsFormKey(camundaFormKey);
    return myself;
  }

  /**
   * Sets the camunda form ref attribute.
   *
   * @param camundaFormRef the form ref to set
   * @return the builder object
   */
  public B eximeeBpmsFormRef(String camundaFormRef) {
    element.setEximeeBpmsFormRef(camundaFormRef);
    return myself;
  }

  /**
   * Sets the camunda form ref binding attribute.
   *
   * @param camundaFormRef the form ref binding to set
   * @return the builder object
   */
  public B eximeeBpmsFormRefBinding(String camundaFormRefBinding) {
    element.setEximeeBpmsFormRefBinding(camundaFormRefBinding);
    return myself;
  }

  /**
   * Sets the camunda form ref version attribute.
   *
   * @param camundaFormRef the form ref version to set
   * @return the builder object
   */
  public B eximeeBpmsFormRefVersion(String camundaFormRefVersion) {
    element.setEximeeBpmsFormRefVersion(camundaFormRefVersion);
    return myself;
  }

  /**
   * Sets the camunda priority attribute.
   *
   * @param camundaPriority  the priority of the user task
   * @return the builder object
   */
  public B eximeeBpmsPriority(String camundaPriority) {
    element.setEximeeBpmsPriority(camundaPriority);
    return myself;
  }

  /**
   * Creates a new camunda form field extension element.
   *
   * @return the builder object
   */
  public EximeeBpmsUserTaskFormFieldBuilder eximeeBpmsFormField() {
    EximeeBpmsFormData camundaFormData = getCreateSingleExtensionElement(EximeeBpmsFormData.class);
    EximeeBpmsFormField camundaFormField = createChild(camundaFormData, EximeeBpmsFormField.class);
    return new EximeeBpmsUserTaskFormFieldBuilder(modelInstance, element, camundaFormField);
  }

  /**
   * Add a class based task listener with specified event name
   *
   * @param eventName - event names to listen to
   * @param fullQualifiedClassName - a string representing a class
   * @return the builder object
   */
  @SuppressWarnings("rawtypes")
  public B eximeeBpmsTaskListenerClass(String eventName, Class listenerClass) {
    return eximeeBpmsTaskListenerClass(eventName, listenerClass.getName());
  }

  /**
   * Add a class based task listener with specified event name
   *
   * @param eventName - event names to listen to
   * @param fullQualifiedClassName - a string representing a class
   * @return the builder object
   */
  public B eximeeBpmsTaskListenerClass(String eventName, String fullQualifiedClassName) {
    EximeeBpmsTaskListener executionListener = createInstance(EximeeBpmsTaskListener.class);
    executionListener.setEximeeBpmsEvent(eventName);
    executionListener.setEximeeBpmsClass(fullQualifiedClassName);

    addExtensionElement(executionListener);

    return myself;
  }

  public B eximeeBpmsTaskListenerExpression(String eventName, String expression) {
    EximeeBpmsTaskListener executionListener = createInstance(EximeeBpmsTaskListener.class);
    executionListener.setEximeeBpmsEvent(eventName);
    executionListener.setEximeeBpmsExpression(expression);

    addExtensionElement(executionListener);

    return myself;
  }

  public B eximeeBpmsTaskListenerDelegateExpression(String eventName, String delegateExpression) {
    EximeeBpmsTaskListener executionListener = createInstance(EximeeBpmsTaskListener.class);
    executionListener.setEximeeBpmsEvent(eventName);
    executionListener.setEximeeBpmsDelegateExpression(delegateExpression);

    addExtensionElement(executionListener);

    return myself;
  }

  @SuppressWarnings("rawtypes")
  public B eximeeBpmsTaskListenerClassTimeoutWithCycle(String id, Class listenerClass, String timerCycle) {
    return eximeeBpmsTaskListenerClassTimeoutWithCycle(id, listenerClass.getName(), timerCycle);
  }

  @SuppressWarnings("rawtypes")
  public B eximeeBpmsTaskListenerClassTimeoutWithDate(String id, Class listenerClass, String timerDate) {
    return eximeeBpmsTaskListenerClassTimeoutWithDate(id, listenerClass.getName(), timerDate);
  }

  @SuppressWarnings("rawtypes")
  public B eximeeBpmsTaskListenerClassTimeoutWithDuration(String id, Class listenerClass, String timerDuration) {
    return eximeeBpmsTaskListenerClassTimeoutWithDuration(id, listenerClass.getName(), timerDuration);
  }

  public B eximeeBpmsTaskListenerClassTimeoutWithCycle(String id, String fullQualifiedClassName, String timerCycle) {
    return createEximeeBpmsTaskListenerClassTimeout(id, fullQualifiedClassName, createTimeCycle(timerCycle));
  }

  public B eximeeBpmsTaskListenerClassTimeoutWithDate(String id, String fullQualifiedClassName, String timerDate) {
    return createEximeeBpmsTaskListenerClassTimeout(id, fullQualifiedClassName, createTimeDate(timerDate));
  }

  public B eximeeBpmsTaskListenerClassTimeoutWithDuration(String id, String fullQualifiedClassName, String timerDuration) {
    return createEximeeBpmsTaskListenerClassTimeout(id, fullQualifiedClassName, createTimeDuration(timerDuration));
  }

  public B eximeeBpmsTaskListenerExpressionTimeoutWithCycle(String id, String expression, String timerCycle) {
    return createEximeeBpmsTaskListenerExpressionTimeout(id, expression, createTimeCycle(timerCycle));
  }

  public B eximeeBpmsTaskListenerExpressionTimeoutWithDate(String id, String expression, String timerDate) {
    return createEximeeBpmsTaskListenerExpressionTimeout(id, expression, createTimeDate(timerDate));
  }

  public B eximeeBpmsTaskListenerExpressionTimeoutWithDuration(String id, String expression, String timerDuration) {
    return createEximeeBpmsTaskListenerExpressionTimeout(id, expression, createTimeDuration(timerDuration));
  }

  public B eximeeBpmsTaskListenerDelegateExpressionTimeoutWithCycle(String id, String delegateExpression, String timerCycle) {
    return createEximeeBpmsTaskListenerDelegateExpressionTimeout(id, delegateExpression, createTimeCycle(timerCycle));
  }

  public B eximeeBpmsTaskListenerDelegateExpressionTimeoutWithDate(String id, String delegateExpression, String timerDate) {
    return createEximeeBpmsTaskListenerDelegateExpressionTimeout(id, delegateExpression, createTimeDate(timerDate));
  }

  public B eximeeBpmsTaskListenerDelegateExpressionTimeoutWithDuration(String id, String delegateExpression, String timerDuration) {
    return createEximeeBpmsTaskListenerDelegateExpressionTimeout(id, delegateExpression, createTimeDuration(timerDuration));
  }

  protected B createEximeeBpmsTaskListenerClassTimeout(String id, String fullQualifiedClassName, TimerEventDefinition timerDefinition) {
    EximeeBpmsTaskListener executionListener = createEximeeBpmsTaskListenerTimeout(id, timerDefinition);
    executionListener.setEximeeBpmsClass(fullQualifiedClassName);
    return myself;
  }

  protected B createEximeeBpmsTaskListenerExpressionTimeout(String id, String expression, TimerEventDefinition timerDefinition) {
    EximeeBpmsTaskListener executionListener = createEximeeBpmsTaskListenerTimeout(id, timerDefinition);
    executionListener.setEximeeBpmsExpression(expression);
    return myself;
  }

  protected B createEximeeBpmsTaskListenerDelegateExpressionTimeout(String id, String delegateExpression, TimerEventDefinition timerDefinition) {
    EximeeBpmsTaskListener executionListener = createEximeeBpmsTaskListenerTimeout(id, timerDefinition);
    executionListener.setEximeeBpmsDelegateExpression(delegateExpression);
    return myself;
  }

  protected EximeeBpmsTaskListener createEximeeBpmsTaskListenerTimeout(String id, TimerEventDefinition timerDefinition) {
    EximeeBpmsTaskListener executionListener = createInstance(EximeeBpmsTaskListener.class);
    executionListener.setAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID, id, true);
    executionListener.setEximeeBpmsEvent("timeout");
    executionListener.addChildElement(timerDefinition);
    addExtensionElement(executionListener);
    return executionListener;
  }

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #eximeeBpmsAssignee(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaAssignee(String eximeeBpmsAssignee) {
    return eximeeBpmsAssignee(eximeeBpmsAssignee);
  }

  /**
   * @deprecated use {@link #eximeeBpmsCandidateGroups(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaCandidateGroups(String eximeeBpmsCandidateGroups) {
    return eximeeBpmsCandidateGroups(eximeeBpmsCandidateGroups);
  }

  /**
   * @deprecated use {@link #eximeeBpmsCandidateGroups(List<String>)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaCandidateGroups(List<String> eximeeBpmsCandidateGroups) {
    return eximeeBpmsCandidateGroups(eximeeBpmsCandidateGroups);
  }

  /**
   * @deprecated use {@link #eximeeBpmsCandidateUsers(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaCandidateUsers(String eximeeBpmsCandidateUsers) {
    return eximeeBpmsCandidateUsers(eximeeBpmsCandidateUsers);
  }

  /**
   * @deprecated use {@link #eximeeBpmsCandidateUsers(List<String>)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaCandidateUsers(List<String> eximeeBpmsCandidateUsers) {
    return eximeeBpmsCandidateUsers(eximeeBpmsCandidateUsers);
  }

  /**
   * @deprecated use {@link #eximeeBpmsDueDate(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaDueDate(String eximeeBpmsDueDate) {
    return eximeeBpmsDueDate(eximeeBpmsDueDate);
  }

  /**
   * @deprecated use {@link #eximeeBpmsFollowUpDate(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaFollowUpDate(String eximeeBpmsFollowUpDate) {
    return eximeeBpmsFollowUpDate(eximeeBpmsFollowUpDate);
  }

  /**
   * @deprecated use {@link #eximeeBpmsFormHandlerClass(Class)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaFormHandlerClass(Class eximeeBpmsFormHandlerClass) {
    return eximeeBpmsFormHandlerClass(eximeeBpmsFormHandlerClass);
  }

  /**
   * @deprecated use {@link #eximeeBpmsFormHandlerClass(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaFormHandlerClass(String fullQualifiedClassName) {
    return eximeeBpmsFormHandlerClass(fullQualifiedClassName);
  }

  /**
   * @deprecated use {@link #eximeeBpmsFormKey(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaFormKey(String eximeeBpmsFormKey) {
    return eximeeBpmsFormKey(eximeeBpmsFormKey);
  }

  /**
   * @deprecated use {@link #eximeeBpmsFormRef(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaFormRef(String eximeeBpmsFormRef) {
    return eximeeBpmsFormRef(eximeeBpmsFormRef);
  }

  /**
   * @deprecated use {@link #eximeeBpmsFormRefBinding(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaFormRefBinding(String eximeeBpmsFormRefBinding) {
    return eximeeBpmsFormRefBinding(eximeeBpmsFormRefBinding);
  }

  /**
   * @deprecated use {@link #eximeeBpmsFormRefVersion(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaFormRefVersion(String eximeeBpmsFormRefVersion) {
    return eximeeBpmsFormRefVersion(eximeeBpmsFormRefVersion);
  }

  /**
   * @deprecated use {@link #eximeeBpmsPriority(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaPriority(String eximeeBpmsPriority) {
    return eximeeBpmsPriority(eximeeBpmsPriority);
  }

  /**
   * @deprecated use {@link #eximeeBpmsFormField()} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public EximeeBpmsUserTaskFormFieldBuilder camundaFormField() {
    return eximeeBpmsFormField();
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerClass(String, Class)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerClass(String eventName, Class listenerClass) {
    return eximeeBpmsTaskListenerClass(eventName, listenerClass);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerClass(String, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerClass(String eventName, String fullQualifiedClassName) {
    return eximeeBpmsTaskListenerClass(eventName, fullQualifiedClassName);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerExpression(String, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerExpression(String eventName, String expression) {
    return eximeeBpmsTaskListenerExpression(eventName, expression);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerDelegateExpression(String, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerDelegateExpression(String eventName, String delegateExpression) {
    return eximeeBpmsTaskListenerDelegateExpression(eventName, delegateExpression);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerClassTimeoutWithCycle(String, Class, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerClassTimeoutWithCycle(String id, Class listenerClass, String timerCycle) {
    return eximeeBpmsTaskListenerClassTimeoutWithCycle(id, listenerClass, timerCycle);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerClassTimeoutWithDate(String, Class, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerClassTimeoutWithDate(String id, Class listenerClass, String timerDate) {
    return eximeeBpmsTaskListenerClassTimeoutWithDate(id, listenerClass, timerDate);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerClassTimeoutWithDuration(String, Class, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerClassTimeoutWithDuration(String id, Class listenerClass, String timerDuration) {
    return eximeeBpmsTaskListenerClassTimeoutWithDuration(id, listenerClass, timerDuration);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerClassTimeoutWithCycle(String, String, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerClassTimeoutWithCycle(String id, String fullQualifiedClassName, String timerCycle) {
    return eximeeBpmsTaskListenerClassTimeoutWithCycle(id, fullQualifiedClassName, timerCycle);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerClassTimeoutWithDate(String, String, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerClassTimeoutWithDate(String id, String fullQualifiedClassName, String timerDate) {
    return eximeeBpmsTaskListenerClassTimeoutWithDate(id, fullQualifiedClassName, timerDate);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerClassTimeoutWithDuration(String, String, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerClassTimeoutWithDuration(String id, String fullQualifiedClassName, String timerDuration) {
    return eximeeBpmsTaskListenerClassTimeoutWithDuration(id, fullQualifiedClassName, timerDuration);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerExpressionTimeoutWithCycle(String, String, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerExpressionTimeoutWithCycle(String id, String expression, String timerCycle) {
    return eximeeBpmsTaskListenerExpressionTimeoutWithCycle(id, expression, timerCycle);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerExpressionTimeoutWithDate(String, String, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerExpressionTimeoutWithDate(String id, String expression, String timerDate) {
    return eximeeBpmsTaskListenerExpressionTimeoutWithDate(id, expression, timerDate);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerExpressionTimeoutWithDuration(String, String, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerExpressionTimeoutWithDuration(String id, String expression, String timerDuration) {
    return eximeeBpmsTaskListenerExpressionTimeoutWithDuration(id, expression, timerDuration);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerDelegateExpressionTimeoutWithCycle(String, String, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerDelegateExpressionTimeoutWithCycle(String id, String delegateExpression, String timerCycle) {
    return eximeeBpmsTaskListenerDelegateExpressionTimeoutWithCycle(id, delegateExpression, timerCycle);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerDelegateExpressionTimeoutWithDate(String, String, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerDelegateExpressionTimeoutWithDate(String id, String delegateExpression, String timerDate) {
    return eximeeBpmsTaskListenerDelegateExpressionTimeoutWithDate(id, delegateExpression, timerDate);
  }

  /**
   * @deprecated use {@link #eximeeBpmsTaskListenerDelegateExpressionTimeoutWithDuration(String, String, String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaTaskListenerDelegateExpressionTimeoutWithDuration(String id, String delegateExpression, String timerDuration) {
    return eximeeBpmsTaskListenerDelegateExpressionTimeoutWithDuration(id, delegateExpression, timerDuration);
  }
}
