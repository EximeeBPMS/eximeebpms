package org.eximeebpms.bpm.engine.impl.businessevent;

import java.util.List;
import org.eximeebpms.bpm.engine.delegate.TaskListener;
import org.eximeebpms.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.eximeebpms.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.eximeebpms.bpm.engine.impl.businessevent.activity.BusinessEventActivityInstanceExecutionListener;
import org.eximeebpms.bpm.engine.impl.businessevent.process.BusinessEventProcessInstanceExecutionListener;
import org.eximeebpms.bpm.engine.impl.businessevent.task.BusinessEventTaskInstanceTaskListener;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.pvm.PvmEvent;
import org.eximeebpms.bpm.engine.impl.pvm.process.ActivityImpl;
import org.eximeebpms.bpm.engine.impl.pvm.process.ScopeImpl;
import org.eximeebpms.bpm.engine.impl.task.TaskDefinition;
import org.eximeebpms.bpm.engine.impl.util.xml.Element;

public class BusinessEventParseListener extends AbstractBpmnParseListener {

  protected final BusinessEventProcessInstanceExecutionListener processInstanceListener = new BusinessEventProcessInstanceExecutionListener();
  protected final BusinessEventActivityInstanceExecutionListener activityInstanceListener = new BusinessEventActivityInstanceExecutionListener();
  protected final BusinessEventTaskInstanceTaskListener taskInstanceListener = new BusinessEventTaskInstanceTaskListener();

  @Override
  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      addProcessInstanceListeners(processDefinition);
    }
  }

  @Override
  public void parseUserTask(final Element userTaskElement, final ScopeImpl scope, final ActivityImpl activity) {
    addActivityInstanceListeners(activity);
    addTaskInstanceListeners(getTaskDefinition(activity));
  }

  @Override
  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  @Override
  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    // do not report link events as activity instances
    if (!"intermediateLinkCatch".equals(activity.getProperty("type"))) {
      addActivityInstanceListeners(activity);
    }
  }

  @Override
  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl activity) {
    addActivityInstanceListeners(activity);
  }

  protected TaskDefinition getTaskDefinition(final ActivityImpl activity) {
    return ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition();
  }

  protected void addProcessInstanceListeners(ProcessDefinitionEntity processDefinition) {
    processDefinition.addBuiltInListener(PvmEvent.EVENTNAME_START, processInstanceListener);
    processDefinition.addBuiltInListener(PvmEvent.EVENTNAME_END, processInstanceListener);
    processDefinition.addBuiltInListener("update", processInstanceListener);
  }

  protected void addActivityInstanceListeners(final ActivityImpl activity) {
    activity.addBuiltInListener(PvmEvent.EVENTNAME_START, activityInstanceListener, 0);
    activity.addBuiltInListener(PvmEvent.EVENTNAME_END, activityInstanceListener);
  }

  private void addTaskInstanceListeners(final TaskDefinition taskDefinition) {
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_CREATE, taskInstanceListener);
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, taskInstanceListener);
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_UPDATE, taskInstanceListener);
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_COMPLETE, taskInstanceListener);
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_DELETE, taskInstanceListener);
  }
}
