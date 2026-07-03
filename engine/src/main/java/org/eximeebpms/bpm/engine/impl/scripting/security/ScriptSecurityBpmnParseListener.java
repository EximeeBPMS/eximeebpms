package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.Objects;
import org.eximeebpms.bpm.engine.impl.ProcessEngineLogger;
import org.eximeebpms.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.eximeebpms.bpm.engine.impl.scripting.ScriptLogger;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.pvm.process.ActivityImpl;
import org.eximeebpms.bpm.engine.impl.pvm.process.ScopeImpl;
import org.eximeebpms.bpm.engine.impl.pvm.process.TransitionImpl;
import org.eximeebpms.bpm.engine.impl.util.xml.Element;
import org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants;

public class ScriptSecurityBpmnParseListener extends AbstractBpmnParseListener {

  private static final ScriptLogger LOG = ProcessEngineLogger.SCRIPT_LOGGER;
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

    String language = resolveConditionExpressionLanguage(conditionExpression);

    if (language == null || language.isBlank()) {
      return;
    }

    validateDeploymentScript(
        language,
        conditionExpression.getText(),
        ScriptSourceType.EXPRESSION,
        resolveTransitionId(sequenceFlowElement, transition),
        resolveProcessKey(scope),
        resolveProcessName(scope));
  }

  @Override
  public void parseIoMapping(Element extensionElements, ActivityImpl activity, IoMapping inputOutput) {
    final Element inputOutputElement = resolveInputOutputElement(extensionElements);
    if (inputOutputElement == null) {
      return;
    }

    validateScriptParameters(inputOutputElement, BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_PARAMETER, activity);
    validateScriptParameters(inputOutputElement, BpmnModelConstants.CAMUNDA_ELEMENT_OUTPUT_PARAMETER, activity);
  }

  protected String resolveTransitionId(Element sequenceFlowElement, TransitionImpl transition) {
    if (transition != null && transition.getId() != null) {
      return transition.getId();
    }

    return sequenceFlowElement.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_ID);
  }

  protected void validateExtensionListenerScripts(
      Element element,
      String processDefinitionKey,
      String processDefinitionName,
      String activityId) {

    final Element extensionElements = resolveExtensionElements(element);
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
    if (element == null) {
      return null;
    }

    Element extensionElements = element.element(BpmnModelConstants.BPMN_ELEMENT_EXTENSION_ELEMENTS);
    if (extensionElements != null) {
      return extensionElements;
    }

    for (Element childElement : element.elements()) {
      if (isTargetElement(childElement, BpmnModelConstants.BPMN_ELEMENT_EXTENSION_ELEMENTS)) {
        return childElement;
      }
    }

    return null;
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
        final Element scriptElement = findScriptElement(listenerElement);
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
    String activityId = resolveActivityId(activity);
    String processKey = resolveProcessKey(activity != null ? activity.getProcessDefinition() : null);
    String processName = resolveProcessName(activity != null ? activity.getProcessDefinition() : null);

    for (Element parameterElement : inputOutputElement.elements()) {
      if (!isTargetElement(parameterElement, parameterElementName)) {
        continue;
      }

      Element scriptElement = findScriptElement(parameterElement);
      if (scriptElement == null) {
        continue;
      }

      validateDeploymentScript(
          scriptElement.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_SCRIPT_FORMAT),
          scriptElement.getText(),
          activityId,
          processKey,
          processName);
    }
  }

  protected boolean isTargetElement(Element element, String expectedElementName) {
    if (element == null || expectedElementName == null) {
      return false;
    }

    String tagName = element.getTagName();
    return expectedElementName.equals(tagName) || tagName.endsWith(":" + expectedElementName);
  }

  protected Element findScriptElement(Element parentElement) {
    if (parentElement == null) {
      return null;
    }

    for (Element childElement : parentElement.elements()) {
      if (isTargetElement(childElement, BpmnModelConstants.BPMN_ELEMENT_SCRIPT)
          || isTargetElement(childElement, BpmnModelConstants.CAMUNDA_ELEMENT_SCRIPT)) {
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

    validateDeploymentScript(
        language,
        source,
        ScriptSourceType.INLINE_SOURCE,
        activityId,
        processDefinitionKey,
        processDefinitionName);
  }

  protected void validateDeploymentScript(
      String language,
      String source,
      ScriptSourceType sourceType,
      String activityId,
      String processDefinitionKey,
      String processDefinitionName) {

    ScriptSecurityPolicy policy = resolveScriptSecurityPolicy();

    if (policy == null || source == null || source.isBlank()) {
      return;
    }

    ScriptSecurityContext context = ScriptSecurityContext.builder(resolveLanguage(language))
        .source(source)
        .sourceType(sourceType)
        .activityId(activityId)
        .processDefinitionKey(processDefinitionKey)
        .processDefinitionName(processDefinitionName)
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    if (decision.isAudit()) {
      LOG.warnScriptDeploymentAllowedByAuditMode(activityId, processDefinitionKey,
          decision.getCode().orElse(null), decision.getReason().orElse(null));
    } else if (decision.isDenied()) {
      throw new ScriptSecurityException(
          buildDeploymentSecurityExceptionMessage(activityId, processDefinitionKey, decision),
          decision.getCode().orElse(null));
    }
  }

  protected ScriptSecurityPolicy resolveScriptSecurityPolicy() {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();

    if (configuration == null) {
      return scriptSecurityPolicy;
    }

    if (!configuration.isScriptSecurityEnabled()) {
      return null;
    }

    if (configuration.getScriptSecurityPolicy() != null) {
      return configuration.getScriptSecurityPolicy();
    }

    return scriptSecurityPolicy;
  }

  protected String resolveLanguage(String language) {
    return language == null || language.isBlank()
        ? UNSPECIFIED_LANGUAGE
        : language;
  }

  protected String resolveScriptSource(Element element) {
    Element scriptElement = element.element(BpmnModelConstants.BPMN_ELEMENT_SCRIPT);

    if (scriptElement == null) {
      return null;
    }

    return scriptElement.getText();
  }

  protected String resolveActivityId(Element activityElement, ActivityImpl activity) {
    final String activityId = activityElement.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_ID);
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
    final ProcessDefinitionEntity processDefinition = resolveProcessDefinition(scope);
    if (processDefinition == null) {
      return null;
    }

    final String name = processDefinition.getName();
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
    final ProcessDefinitionEntity processDefinition = resolveProcessDefinition(scope);
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

  protected String resolveConditionExpressionLanguage(Element conditionExpression) {
    String language = conditionExpression.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_LANGUAGE);

    if (language != null && !language.isBlank()) {
      return language;
    }

    return conditionExpression.attribute(BpmnModelConstants.BPMN_ATTRIBUTE_SCRIPT_FORMAT);
  }

  protected String buildDeploymentSecurityExceptionMessage(
      String activityId,
      String processDefinitionKey,
      ScriptSecurityDecision decision) {

    StringBuilder message = new StringBuilder("Process deployment blocked by script security policy");

    decision.getReason().ifPresent(reason -> message.append(": ").append(reason));

    if (activityId != null && !activityId.isBlank()) {
      message.append(" while parsing activity '").append(activityId).append("'");
    }

    if (processDefinitionKey != null && !processDefinitionKey.isBlank()) {
      message.append(" in process definition '").append(processDefinitionKey).append("'");
    }

    return message.toString();
  }
}
