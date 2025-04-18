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
package org.eximeebpms.bpm.engine.impl.bpmn.parser;

import java.util.List;

import org.eximeebpms.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.pvm.process.ActivityImpl;
import org.eximeebpms.bpm.engine.impl.pvm.process.ScopeImpl;
import org.eximeebpms.bpm.engine.impl.pvm.process.TransitionImpl;
import org.eximeebpms.bpm.engine.impl.util.xml.Element;
import org.eximeebpms.bpm.engine.impl.variable.VariableDeclaration;

/**
 * Abstract base class for implementing a {@link BpmnParseListener} without being forced to implement
 * all methods provided, which makes the implementation more robust to future changes.
 *
 * @author ruecker
 */
public class AbstractBpmnParseListener implements BpmnParseListener {

  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
  }

  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
  }

  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
  }

  public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
  }

  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
  }

  public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
  }

  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
  }

  public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
  }

  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
  }

  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity) {
  }

  public void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
  }

  public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {
  }

  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
  }

  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
  }

  public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity) {
  }

  public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, ActivityImpl messageActivity) {
  }

  public void parseBoundaryEscalationEventDefinition(Element escalationEventDefinition, boolean interrupting, ActivityImpl boundaryEventActivity) {
  }

  public void parseBoundaryConditionalEventDefinition(Element element, boolean interrupting, ActivityImpl conditionalActivity) {
  }

  public void parseIntermediateConditionalEventDefinition(Element conditionalEventDefinition, ActivityImpl conditionalActivity) {
  }

  public void parseConditionalStartEventForEventSubprocess(Element element, ActivityImpl conditionalActivity, boolean interrupting) {
  }

  @Override
  public void parseIoMapping(Element extensionElements, ActivityImpl activity, IoMapping inputOutput) {
  }
}
