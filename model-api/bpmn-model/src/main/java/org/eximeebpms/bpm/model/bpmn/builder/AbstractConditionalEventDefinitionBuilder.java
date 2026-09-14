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
import org.eximeebpms.bpm.model.bpmn.instance.Condition;
import org.eximeebpms.bpm.model.bpmn.instance.ConditionalEventDefinition;
import org.eximeebpms.bpm.model.bpmn.instance.Event;

/**
 * Represents the abstract conditional event definition builder.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 * @param <B>
 */
public class AbstractConditionalEventDefinitionBuilder<B extends AbstractConditionalEventDefinitionBuilder<B>> extends AbstractRootElementBuilder<B, ConditionalEventDefinition>{

  public AbstractConditionalEventDefinitionBuilder(BpmnModelInstance modelInstance, ConditionalEventDefinition element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Sets the condition of the conditional event definition.
   *
   * @param conditionText the condition which should be evaluate to true or false
   * @return the builder object
   */
  public B condition(String conditionText) {
    Condition condition = createInstance(Condition.class);
    condition.setTextContent(conditionText);
    element.setCondition(condition);
    return myself;
  }

  /**
   * Sets the camunda variable name attribute, that defines on
   * which variable the condition should be evaluated.
   *
   * @param variableName the variable on which the condition should be evaluated
   * @return the builder object
   */
  public B eximeeBpmsVariableName(String variableName) {
    element.setEximeeBpmsVariableName(variableName);
    return myself;
  }

  /**
   * Set the camunda variable events attribute, that defines the variable
   * event on which the condition should be evaluated.
   *
   * @param variableEvents the events on which the condition should be evaluated
   * @return the builder object
   */
  public B eximeeBpmsVariableEvents(String variableEvents) {
    element.setEximeeBpmsVariableEvents(variableEvents);
    return myself;
  }

  /**
   * Set the camunda variable events attribute, that defines the variable
   * event on which the condition should be evaluated.
   *
   * @param variableEvents the events on which the condition should be evaluated
   * @return the builder object
   */
  public B eximeeBpmsVariableEvents(List<String> variableEvents) {
    element.setEximeeBpmsVariableEventsList(variableEvents);
    return myself;
  }

  /**
   * Finishes the building of a conditional event definition.
   *
   * @param <T>
   * @return the parent event builder
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public <T extends AbstractFlowNodeBuilder> T conditionalEventDefinitionDone() {
    return (T) ((Event) element.getParentElement()).builder();
  }

  // Deprecated Camunda-named aliases, removed in 1.5.0 (BPMS-607).

  /**
   * @deprecated use {@link #eximeeBpmsVariableName(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaVariableName(String variableName) {
    return eximeeBpmsVariableName(variableName);
  }

  /**
   * @deprecated use {@link #eximeeBpmsVariableEvents(String)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaVariableEvents(String variableEvents) {
    return eximeeBpmsVariableEvents(variableEvents);
  }

  /**
   * @deprecated use {@link #eximeeBpmsVariableEvents(List<String>)} instead.
   */
  @Deprecated(since = "1.4.0", forRemoval = true)
  public B camundaVariableEvents(List<String> variableEvents) {
    return eximeeBpmsVariableEvents(variableEvents);
  }
}