package org.eximeebpms.bpm.engine.impl.businessevent;

import java.util.List;
import org.eximeebpms.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.eximeebpms.bpm.engine.impl.businessevent.activity.BusinessEventActivityInstanceExecutionListener;
import org.eximeebpms.bpm.engine.impl.businessevent.process.BusinessEventProcessInstanceExecutionListener;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.pvm.PvmEvent;
import org.eximeebpms.bpm.engine.impl.pvm.process.ActivityImpl;
import org.eximeebpms.bpm.engine.impl.pvm.process.ScopeImpl;
import org.eximeebpms.bpm.engine.impl.util.xml.Element;

public class BusinessEventParseListener extends AbstractBpmnParseListener {

  protected final BusinessEventProcessInstanceExecutionListener processInstanceListener = new BusinessEventProcessInstanceExecutionListener();
  protected final BusinessEventActivityInstanceExecutionListener activityInstanceListener = new BusinessEventActivityInstanceExecutionListener();

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
    return isProcessInstanceParsingNeeded() || isActivityInstanceParsingNeeded();
  }

  protected boolean isProcessInstanceParsingNeeded() {
    // process-instance:start and process-instance-update are not parsed listeners: they are
    // emitted by ExecutionEntity#fireBusinessProcessStartEvent and
    // #fireBusinessProcessInstanceUpdate at the points history emits its own
    return typeFilter.isEnabled(BusinessEventTypes.PROCESS_INSTANCE_END);
  }

  protected boolean isActivityInstanceParsingNeeded() {
    return typeFilter.isEnabled(BusinessEventTypes.ACTIVITY_INSTANCE_START)
        || typeFilter.isEnabled(BusinessEventTypes.ACTIVITY_INSTANCE_END);
  }

  @Override
  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      addProcessInstanceListeners(processDefinition);
    }
  }

  @Override
  public void parseUserTask(final Element userTaskElement, final ScopeImpl scope, final ActivityImpl activity) {
    // Deliberately no activity-instance:update on task create/assignment, unlike history's
    // ActivityInstanceUpdateListener: that only denormalises taskId/assignee onto ACT_HI_ACTINST,
    // and task-instance:create/update already carry taskId, assignee and activityInstanceId at the
    // same moment. See docs/specs/business-events.md in eximeebpms-factory, "Why user tasks produce
    // no activity-instance:update" (BPMS-782).
    addActivityInstanceListeners(activity);
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

  protected void addProcessInstanceListeners(ProcessDefinitionEntity processDefinition) {
    if (typeFilter.isEnabled(BusinessEventTypes.PROCESS_INSTANCE_END)) {
      processDefinition.addBuiltInListener(PvmEvent.EVENTNAME_END, processInstanceListener);
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
}
