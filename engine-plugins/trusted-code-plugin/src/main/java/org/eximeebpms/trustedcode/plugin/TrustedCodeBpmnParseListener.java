package org.eximeebpms.trustedcode.plugin;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.pvm.process.ActivityImpl;
import org.eximeebpms.bpm.engine.impl.pvm.process.ScopeImpl;
import org.eximeebpms.bpm.engine.impl.util.xml.Element;

@Slf4j
@RequiredArgsConstructor
public class TrustedCodeBpmnParseListener extends AbstractBpmnParseListener {

  private static final String TAG_EXTENSION_ELEMENTS = "extensionElements";
  private static final String TAG_EXECUTION_LISTENER = "executionListener";
  private static final String TAG_TASK_LISTENER = "taskListener";
  private static final String TAG_SCRIPT = "script";

  private static final String ATTR_ID = "id";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_RESOURCE = "resource";

  private static final String RULE_SCRIPT_TASK_FORBIDDEN = "SCRIPT_TASK_FORBIDDEN";
  private static final String RULE_SCRIPT_EXECUTION_LISTENER_FORBIDDEN = "SCRIPT_EXECUTION_LISTENER_FORBIDDEN";
  private static final String RULE_SCRIPT_TASK_LISTENER_FORBIDDEN = "SCRIPT_TASK_LISTENER_FORBIDDEN";
  private static final String RULE_EXTERNAL_SCRIPT_RESOURCE_FORBIDDEN = "EXTERNAL_SCRIPT_RESOURCE_FORBIDDEN";

  private static final String ELEMENT_TYPE_SCRIPT_TASK = "bpmn:ScriptTask";
  private static final String ELEMENT_TYPE_EXECUTION_LISTENER = "camunda:ExecutionListener";
  private static final String ELEMENT_TYPE_TASK_LISTENER = "camunda:TaskListener";

  private static final String MESSAGE_SCRIPT_TASK =
      "Script Task is forbidden by trusted-code policy";
  private static final String MESSAGE_SCRIPT_EXECUTION_LISTENER =
      "Script execution listener is forbidden by trusted-code policy";
  private static final String MESSAGE_SCRIPT_TASK_LISTENER =
      "Script task listener is forbidden by trusted-code policy";
  private static final String MESSAGE_EXTERNAL_SCRIPT_RESOURCE =
      "External script resource is forbidden by trusted-code policy";

  private final TrustedCodePolicy policy;

  @Override
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    if (!policy.isEnabled()) {
      return;
    }

    List<TrustedCodeViolation> violations = new ArrayList<>();

    if (policy.isBlockScriptTasks()) {
      violations.add(buildViolation(
          RULE_SCRIPT_TASK_FORBIDDEN,
          resolveResourceName(),
          resolveProcessDefinitionKey(scope),
          scriptTaskElement,
          ELEMENT_TYPE_SCRIPT_TASK,
          MESSAGE_SCRIPT_TASK
      ));
    }

    collectExecutionListenerViolations(scriptTaskElement, scope, violations);
    collectTaskListenerViolations(scriptTaskElement, scope, violations);

    handleViolations(violations);
  }

  @Override
  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
    if (!policy.isEnabled()) {
      return;
    }

    List<TrustedCodeViolation> violations = new ArrayList<>();
    collectExecutionListenerViolations(processElement, processDefinition, violations);
    handleViolations(violations);
  }

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
    collectGenericElementViolations(startEventElement, scope);
  }

  @Override
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    collectGenericElementViolations(endEventElement, scope);
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    collectGenericElementViolations(userTaskElement, scope);
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    collectGenericElementViolations(serviceTaskElement, scope);
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    collectGenericElementViolations(businessRuleTaskElement, scope);
  }

  @Override
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    collectGenericElementViolations(taskElement, scope);
  }

  @Override
  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    collectGenericElementViolations(manualTaskElement, scope);
  }

  @Override
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    collectGenericElementViolations(sendTaskElement, scope);
  }

  @Override
  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    collectGenericElementViolations(receiveTaskElement, scope);
  }

  @Override
  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    collectGenericElementViolations(subProcessElement, scope);
  }

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    collectGenericElementViolations(callActivityElement, scope);
  }

  @Override
  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
    collectGenericElementViolations(boundaryEventElement, scopeElement);
  }

  @Override
  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    collectGenericElementViolations(intermediateEventElement, scope);
  }

  @Override
  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    collectGenericElementViolations(intermediateEventElement, scope);
  }

  private void collectGenericElementViolations(Element element, ScopeImpl scope) {
    if (!policy.isEnabled()) {
      return;
    }

    List<TrustedCodeViolation> violations = new ArrayList<>();
    collectExecutionListenerViolations(element, scope, violations);
    collectTaskListenerViolations(element, scope, violations);
    handleViolations(violations);
  }

  private void collectExecutionListenerViolations(
      Element parentElement,
      ScopeImpl scope,
      List<TrustedCodeViolation> violations) {

    if (!policy.isBlockScriptExecutionListeners() && !policy.isBlockExternalScriptResources()) {
      return;
    }

    Element extensionElements = parentElement.element(TAG_EXTENSION_ELEMENTS);
    if (extensionElements == null) {
      return;
    }

    for (Element executionListener : extensionElements.elements(TAG_EXECUTION_LISTENER)) {
      Element script = executionListener.element(TAG_SCRIPT);
      if (script == null) {
        continue;
      }

      if (policy.isBlockScriptExecutionListeners()) {
        violations.add(buildViolation(
            RULE_SCRIPT_EXECUTION_LISTENER_FORBIDDEN,
            resolveResourceName(),
            resolveProcessDefinitionKey(scope),
            parentElement,
            ELEMENT_TYPE_EXECUTION_LISTENER,
            MESSAGE_SCRIPT_EXECUTION_LISTENER
        ));
      }

      if (policy.isBlockExternalScriptResources() && hasExternalScriptResource(script)) {
        violations.add(buildViolation(
            RULE_EXTERNAL_SCRIPT_RESOURCE_FORBIDDEN,
            resolveResourceName(),
            resolveProcessDefinitionKey(scope),
            parentElement,
            ELEMENT_TYPE_EXECUTION_LISTENER,
            MESSAGE_EXTERNAL_SCRIPT_RESOURCE
        ));
      }
    }
  }

  private void collectTaskListenerViolations(
      Element parentElement,
      ScopeImpl scope,
      List<TrustedCodeViolation> violations) {

    if (!policy.isBlockScriptTaskListeners() && !policy.isBlockExternalScriptResources()) {
      return;
    }

    Element extensionElements = parentElement.element(TAG_EXTENSION_ELEMENTS);
    if (extensionElements == null) {
      return;
    }

    for (Element taskListener : extensionElements.elements(TAG_TASK_LISTENER)) {
      Element script = taskListener.element(TAG_SCRIPT);
      if (script == null) {
        continue;
      }

      if (policy.isBlockScriptTaskListeners()) {
        violations.add(buildViolation(
            RULE_SCRIPT_TASK_LISTENER_FORBIDDEN,
            resolveResourceName(),
            resolveProcessDefinitionKey(scope),
            parentElement,
            ELEMENT_TYPE_TASK_LISTENER,
            MESSAGE_SCRIPT_TASK_LISTENER
        ));
      }

      if (policy.isBlockExternalScriptResources() && hasExternalScriptResource(script)) {
        violations.add(buildViolation(
            RULE_EXTERNAL_SCRIPT_RESOURCE_FORBIDDEN,
            resolveResourceName(),
            resolveProcessDefinitionKey(scope),
            parentElement,
            ELEMENT_TYPE_TASK_LISTENER,
            MESSAGE_EXTERNAL_SCRIPT_RESOURCE
        ));
      }
    }
  }

  private boolean hasExternalScriptResource(Element scriptElement) {
    String resource = scriptElement.attribute(ATTR_RESOURCE);
    return resource != null && !resource.isBlank();
  }

  private void handleViolations(List<TrustedCodeViolation> violations) {
    if (violations == null || violations.isEmpty()) {
      return;
    }

    if (policy.isEnforceMode()) {
      throw new TrustedCodePolicyException(violations);
    }

    if (policy.isAuditMode()) {
      violations.forEach(violation -> log.warn("Trusted code policy violation detected: {}", violation));
    }
  }

  private String resolveProcessDefinitionKey(ScopeImpl scope) {
    if (scope instanceof ProcessDefinitionEntity processDefinitionEntity) {
      return processDefinitionEntity.getKey();
    }
    return null;
  }

  private TrustedCodeViolation buildViolation(
      String ruleCode,
      String resourceName,
      String processDefinitionKey,
      Element parentElement,
      String elementType,
      String message) {

    return TrustedCodeViolation.builder()
        .ruleCode(ruleCode)
        .resourceName(resourceName)
        .processDefinitionKey(processDefinitionKey)
        .elementId(parentElement.attribute(ATTR_ID))
        .elementName(parentElement.attribute(ATTR_NAME))
        .elementType(elementType)
        .message(message)
        .build();
  }

  private String resolveResourceName() {
    return null;
  }
}
