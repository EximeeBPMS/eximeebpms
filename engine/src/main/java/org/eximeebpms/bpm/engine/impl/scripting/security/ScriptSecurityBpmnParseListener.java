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
package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.Objects;
import org.eximeebpms.bpm.engine.ProcessEngineException;
import org.eximeebpms.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.eximeebpms.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.pvm.process.ActivityImpl;
import org.eximeebpms.bpm.engine.impl.pvm.process.ScopeImpl;
import org.eximeebpms.bpm.engine.impl.pvm.process.TransitionImpl;
import org.eximeebpms.bpm.engine.impl.util.xml.Element;
import org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants;

public class ScriptSecurityBpmnParseListener extends AbstractBpmnParseListener {

  private static final String UNSPECIFIED_LANGUAGE = "unspecified";

  protected final ScriptSecurityPolicy scriptSecurityPolicy;

  public ScriptSecurityBpmnParseListener(ScriptSecurityPolicy scriptSecurityPolicy) {
    this.scriptSecurityPolicy = Objects.requireNonNull(scriptSecurityPolicy, "scriptSecurityPolicy must not be null");
  }

  @Override
  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
    String processKey = processDefinition != null ? processDefinition.getKey() : resolveProcessKey(processElement);
    String processName = processDefinition != null && processDefinition.getName() != null && !processDefinition.getName().isBlank()
        ? processDefinition.getName()
        : resolveProcessName(processElement);

    validateExtensionListenerScripts(
        processElement,
        processKey,
        processName,
        null);
  }

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
    validateExtensionListenerScripts(
        startEventElement,
        resolveProcessKey(scope),
        resolveProcessName(scope),
        resolveActivityId(startEventElement, startEventActivity));
  }

  @Override
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    validateDeploymentScript(
        scriptTaskElement.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_SCRIPT_FORMAT),
        resolveScriptSource(scriptTaskElement),
        resolveActivityId(scriptTaskElement, activity),
        resolveProcessKey(scope),
        resolveProcessName(scope));

    validateExtensionListenerScripts(
        scriptTaskElement,
        resolveProcessKey(scope),
        resolveProcessName(scope),
        resolveActivityId(scriptTaskElement, activity));
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    validateExtensionListenerScripts(
        serviceTaskElement,
        resolveProcessKey(scope),
        resolveProcessName(scope),
        resolveActivityId(serviceTaskElement, activity));
  }

  @Override
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    validateExtensionListenerScripts(
        taskElement,
        resolveProcessKey(scope),
        resolveProcessName(scope),
        resolveActivityId(taskElement, activity));
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    validateExtensionListenerScripts(
        userTaskElement,
        resolveProcessKey(scope),
        resolveProcessName(scope),
        resolveActivityId(userTaskElement, activity));
  }

  @Override
  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    validateExtensionListenerScripts(
        subProcessElement,
        resolveProcessKey(scope),
        resolveProcessName(scope),
        resolveActivityId(subProcessElement, activity));
  }

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    validateExtensionListenerScripts(
        callActivityElement,
        resolveProcessKey(scope),
        resolveProcessName(scope),
        resolveActivityId(callActivityElement, activity));
  }

  @Override
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    validateExtensionListenerScripts(
        endEventElement,
        resolveProcessKey(scope),
        resolveProcessName(scope),
        resolveActivityId(endEventElement, activity));
  }

  @Override
  public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scope, TransitionImpl transition) {
    Element conditionExpression = sequenceFlowElement.element(BpmnModelConstants.BPMN_ELEMENT_CONDITION_EXPRESSION);
    if (conditionExpression == null) {
      return;
    }

    validateDeploymentScript(
        conditionExpression.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_LANGUAGE),
        conditionExpression.getText(),
        sequenceFlowElement.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_ID),
        resolveProcessKey(scope),
        resolveProcessName(scope));
  }

  @Override
  public void parseIoMapping(Element extensionElements, ActivityImpl activity, IoMapping inputOutput) {
    Element inputOutputElement = resolveInputOutputElement(extensionElements);
    if (inputOutputElement == null) {
      return;
    }

    validateScriptParameters(inputOutputElement, BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_PARAMETER, activity);
    validateScriptParameters(inputOutputElement, BpmnModelConstants.CAMUNDA_ELEMENT_OUTPUT_PARAMETER, activity);
  }

  protected void validateExtensionListenerScripts(
      Element element,
      String processDefinitionKey,
      String processDefinitionName,
      String activityId) {

    Element extensionElements = resolveExtensionElements(element);
    if (extensionElements == null) {
      return;
    }

    validateListenerScriptElements(
        extensionElements,
        BpmnModelConstants.CAMUNDA_ELEMENT_EXECUTION_LISTENER,
        processDefinitionKey,
        processDefinitionName,
        activityId);

    validateListenerScriptElements(
        extensionElements,
        BpmnModelConstants.CAMUNDA_ELEMENT_TASK_LISTENER,
        processDefinitionKey,
        processDefinitionName,
        activityId);
  }

  protected Element resolveExtensionElements(Element element) {
    return element != null
        ? element.element(BpmnModelConstants.BPMN_ELEMENT_EXTENSION_ELEMENTS)
        : null;
  }

  protected Element resolveInputOutputElement(Element extensionElements) {
    if (extensionElements == null) {
      return null;
    }

    for (Element childElement : extensionElements.elements()) {
      if (isTargetElement(childElement, BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_OUTPUT)) {
        return childElement;
      }
    }

    return null;
  }

  protected void validateListenerScriptElements(
      Element extensionElements,
      String listenerElementName,
      String processDefinitionKey,
      String processDefinitionName,
      String activityId) {

    for (Element listenerElement : extensionElements.elements()) {
      if (isTargetElement(listenerElement, listenerElementName)) {
        Element scriptElement = findScriptElement(listenerElement);
        if (scriptElement != null) {
          validateDeploymentScript(
              scriptElement.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_SCRIPT_FORMAT),
              scriptElement.getText(),
              activityId,
              processDefinitionKey,
              processDefinitionName);
        }
      }
    }
  }

  protected void validateScriptParameters(Element inputOutputElement, String parameterElementName, ActivityImpl activity) {
    for (Element parameterElement : inputOutputElement.elements()) {
      if (isTargetElement(parameterElement, parameterElementName)) {
        Element scriptElement = findScriptElement(parameterElement);
        if (scriptElement != null) {
          validateDeploymentScript(
              scriptElement.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_SCRIPT_FORMAT),
              scriptElement.getText(),
              resolveActivityId(activity),
              resolveProcessKey(activity != null ? activity.getProcessDefinition() : null),
              resolveProcessName(activity != null ? activity.getProcessDefinition() : null));
        }
      }
    }
  }

  protected boolean isTargetElement(Element element, String expectedElementName) {
    return expectedElementName.equals(element.getTagName());
  }

  protected Element findScriptElement(Element parentElement) {
    for (Element childElement : parentElement.elements()) {
      if (BpmnModelConstants.BPMN_ELEMENT_SCRIPT.equals(childElement.getTagName())
          || BpmnModelConstants.CAMUNDA_ELEMENT_SCRIPT.equals(childElement.getTagName())) {
        return childElement;
      }
    }
    return null;
  }

  protected void validateDeploymentScript(
      String language,
      String source,
      String activityId,
      String processDefinitionKey,
      String processDefinitionName) {

    if (source == null || source.isBlank()) {
      return;
    }

    ScriptSecurityContext context = ScriptSecurityContext.builder(resolveLanguage(language))
        .source(source)
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .activityId(activityId)
        .processDefinitionKey(processDefinitionKey)
        .processDefinitionName(processDefinitionName)
        .build();

    ScriptSecurityDecision decision = scriptSecurityPolicy.evaluate(context);
    if (decision.isDenied()) {
      throw new ProcessEngineException(buildDeploymentExceptionMessage(context, decision));
    }
  }

  protected String resolveLanguage(String language) {
    return language != null && !language.isBlank()
        ? language
        : UNSPECIFIED_LANGUAGE;
  }

  protected String resolveScriptSource(Element scriptTaskElement) {
    Element scriptElement = scriptTaskElement.element(BpmnModelConstants.BPMN_ELEMENT_SCRIPT);
    return scriptElement != null ? scriptElement.getText() : "";
  }

  protected String resolveActivityId(Element activityElement, ActivityImpl activity) {
    String activityId = activityElement.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_ID);
    if (activityId != null && !activityId.isBlank()) {
      return activityId;
    }
    return resolveActivityId(activity);
  }

  protected String resolveActivityId(ActivityImpl activity) {
    return activity != null ? activity.getId() : null;
  }

  protected String resolveProcessName(Element processElement) {
    if (processElement == null) {
      return null;
    }

    String processName = processElement.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_NAME);
    if (processName != null && !processName.isBlank()) {
      return processName;
    }

    return processElement.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_ID);
  }

  protected String resolveProcessName(ScopeImpl scope) {
    ProcessDefinitionEntity processDefinition = resolveProcessDefinition(scope);
    if (processDefinition == null) {
      return null;
    }

    String name = processDefinition.getName();
    if (name != null && !name.isBlank()) {
      return name;
    }

    return processDefinition.getKey();
  }

  protected String resolveProcessKey(Element processElement) {
    if (processElement == null) {
      return null;
    }

    return processElement.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_ID);
  }

  protected String resolveProcessKey(ScopeImpl scope) {
    ProcessDefinitionEntity processDefinition = resolveProcessDefinition(scope);
    return processDefinition != null ? processDefinition.getKey() : null;
  }

  protected ProcessDefinitionEntity resolveProcessDefinition(ScopeImpl scope) {
    if (scope == null) {
      return null;
    }

    if (scope instanceof ProcessDefinitionEntity processDefinitionEntity) {
      return processDefinitionEntity;
    }

    ScopeImpl current = scope;
    while (current != null) {
      if (current instanceof ProcessDefinitionEntity processDefinitionEntity) {
        return processDefinitionEntity;
      }
      current = current.getFlowScope();
    }

    return null;
  }

  protected String buildDeploymentExceptionMessage(ScriptSecurityContext context, ScriptSecurityDecision decision) {
    StringBuilder message = new StringBuilder("Process deployment blocked by script security policy");

    context.getProcessDefinitionKey().ifPresent(processDefinitionKey -> message
        .append(" for process '")
        .append(processDefinitionKey)
        .append("'"));

    context.getProcessDefinitionName()
        .filter(name -> !name.isBlank())
        .ifPresent(name -> message
            .append(" (name='")
            .append(name)
            .append("')"));

    context.getActivityId()
        .filter(activityId -> !activityId.isBlank())
        .ifPresent(activityId -> message
            .append(" at activity '")
            .append(activityId)
            .append("'"));

    decision.getReason()
        .ifPresent(reason -> message
            .append(": ")
            .append(reason));

    return message.toString();
  }
}
