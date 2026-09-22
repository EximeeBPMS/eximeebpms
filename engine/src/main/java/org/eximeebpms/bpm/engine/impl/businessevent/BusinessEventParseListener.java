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

  /**
   * Decides which built-in listeners are attached at all. Types excluded by the configured
   * {@code enabledEventTypes}/{@code disabledEventTypes} filter get no listener on the process
   * definition, so a disabled type costs nothing at runtime rather than being produced and then
   * dropped.
   */
  protected final BusinessEventTypeFilter typeFilter;

  public BusinessEventParseListener() {
    this(BusinessEventTypeFilter.of(null));
  }

  public BusinessEventParseListener(BusinessEventTypeFilter typeFilter) {
    this.typeFilter = typeFilter == null ? BusinessEventTypeFilter.of(null) : typeFilter;
  }

  /**
   * Whether this listener would attach anything at all. When it would not, the engine leaves it
   * out of the BPMN parse listener chain entirely.
   *
   * <p>Covers only the types this listener is responsible for — the {@code migrate} events of the
   * same entities are produced by the migration code, not by parsed listeners, and are filtered
   * there.</p>
   */
  public boolean isActive() {
    return isProcessInstanceParsingNeeded() || isActivityInstanceParsingNeeded() || isTaskInstanceParsingNeeded();
  }

  protected boolean isProcessInstanceParsingNeeded() {
    return typeFilter.isEnabled(BusinessEventTypes.PROCESS_INSTANCE_START)
        || typeFilter.isEnabled(BusinessEventTypes.PROCESS_INSTANCE_END)
        || typeFilter.isEnabled(BusinessEventTypes.PROCESS_INSTANCE_UPDATE);
  }

  protected boolean isActivityInstanceParsingNeeded() {
    return typeFilter.isEnabled(BusinessEventTypes.ACTIVITY_INSTANCE_START)
        || typeFilter.isEnabled(BusinessEventTypes.ACTIVITY_INSTANCE_END);
  }

  protected boolean isTaskInstanceParsingNeeded() {
    return typeFilter.isEnabled(BusinessEventTypes.TASK_INSTANCE_CREATE)
        || typeFilter.isEnabled(BusinessEventTypes.TASK_INSTANCE_UPDATE)
        || typeFilter.isEnabled(BusinessEventTypes.TASK_INSTANCE_COMPLETE)
        || typeFilter.isEnabled(BusinessEventTypes.TASK_INSTANCE_DELETE);
  }

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
    if (typeFilter.isEnabled(BusinessEventTypes.PROCESS_INSTANCE_START)) {
      processDefinition.addBuiltInListener(PvmEvent.EVENTNAME_START, processInstanceListener);
    }

    if (typeFilter.isEnabled(BusinessEventTypes.PROCESS_INSTANCE_END)) {
      processDefinition.addBuiltInListener(PvmEvent.EVENTNAME_END, processInstanceListener);
    }

    if (typeFilter.isEnabled(BusinessEventTypes.PROCESS_INSTANCE_UPDATE)) {
      processDefinition.addBuiltInListener("update", processInstanceListener);
    }
  }

  protected void addActivityInstanceListeners(final ActivityImpl activity) {
    if (typeFilter.isEnabled(BusinessEventTypes.ACTIVITY_INSTANCE_START)) {
      activity.addBuiltInListener(PvmEvent.EVENTNAME_START, activityInstanceListener, 0);
    }

    if (typeFilter.isEnabled(BusinessEventTypes.ACTIVITY_INSTANCE_END)) {
      activity.addBuiltInListener(PvmEvent.EVENTNAME_END, activityInstanceListener);
    }
  }

  private void addTaskInstanceListeners(final TaskDefinition taskDefinition) {
    if (typeFilter.isEnabled(BusinessEventTypes.TASK_INSTANCE_CREATE)) {
      taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_CREATE, taskInstanceListener);
    }

    if (typeFilter.isEnabled(BusinessEventTypes.TASK_INSTANCE_UPDATE)) {
      // both BPMN events map to the single task-instance:update business event
      taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, taskInstanceListener);
      taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_UPDATE, taskInstanceListener);
    }

    if (typeFilter.isEnabled(BusinessEventTypes.TASK_INSTANCE_COMPLETE)) {
      taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_COMPLETE, taskInstanceListener);
    }

    if (typeFilter.isEnabled(BusinessEventTypes.TASK_INSTANCE_DELETE)) {
      taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_DELETE, taskInstanceListener);
    }
  }
}
