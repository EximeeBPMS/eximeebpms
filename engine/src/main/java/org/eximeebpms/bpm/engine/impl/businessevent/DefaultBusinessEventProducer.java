package org.eximeebpms.bpm.engine.impl.businessevent;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.DelegateTask;
import org.eximeebpms.bpm.engine.delegate.VariableScope;
import org.eximeebpms.bpm.engine.impl.ProcessEngineLogger;
import org.eximeebpms.bpm.engine.impl.businessevent.identitylink.IdentityLinkBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.process.ProcessInstanceBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.script.ScriptViolationBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.task.TaskInstanceBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.variable.VariableInstanceBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.cfg.ConfigurationLogger;
import org.eximeebpms.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;

public class DefaultBusinessEventProducer implements BusinessEventProducer {

  protected VariableInstanceBusinessEventFactory variableEvtFactory = new VariableInstanceBusinessEventFactory();
  protected ProcessInstanceBusinessEventFactory processEvtFactory = new ProcessInstanceBusinessEventFactory();
  protected IdentityLinkBusinessEventFactory identityLinkEvtFactory = new IdentityLinkBusinessEventFactory();
  protected TaskInstanceBusinessEventFactory taskInstanceEvtFactory = new TaskInstanceBusinessEventFactory();
  protected ScriptViolationBusinessEventFactory scriptViolationEvtFactory = new ScriptViolationBusinessEventFactory();

  protected static final ConfigurationLogger LOG = ProcessEngineLogger.CONFIG_LOGGER;

  @Override
  public BusinessEvent createVariableCreateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope) {
    return variableEvtFactory.createCreateEvent(variableInstance, sourceVariableScope);
  }

  @Override
  public BusinessEvent createVariableDeleteEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope) {
    return variableEvtFactory.createDeleteEvent(variableInstance, sourceVariableScope);
  }

  @Override
  public BusinessEvent createVariableUpdateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope) {
    return variableEvtFactory.createUpdateEvent(variableInstance, sourceVariableScope);
  }

  @Override
  public BusinessEvent createVariableMigrateEvt(VariableInstanceEntity variableInstance) {
    return variableEvtFactory.createMigrateEvent(variableInstance);
  }

  @Override
  public BusinessEvent createProcessInstanceStartEvt(DelegateExecution execution) {
    return processEvtFactory.createStartEvent(execution);
  }

  @Override
  public BusinessEvent createProcessInstanceEndEvt(DelegateExecution execution) {
    return processEvtFactory.createEndEvent(execution);
  }

  @Override
  public BusinessEvent createProcessInstanceUpdateEvt(DelegateExecution execution) {
    return processEvtFactory.createUpdateEvent(execution);
  }

  @Override
  public BusinessEvent createIdentityLinkAddEvt(IdentityLinkEntity identityLinkEntity) {
    return identityLinkEvtFactory.createAddEvent(identityLinkEntity);
  }

  @Override
  public BusinessEvent createIdentityLinkDeleteEvt(IdentityLinkEntity identityLinkEntity) {
    return identityLinkEvtFactory.createDeleteEvent(identityLinkEntity);
  }

  @Override
  public BusinessEvent createTaskInstanceCreateEvt(DelegateTask task) {
    return taskInstanceEvtFactory.createCreateEvent(task);
  }

  @Override
  public BusinessEvent createTaskInstanceUpdateEvt(DelegateTask task) {
    return taskInstanceEvtFactory.createUpdateEvent(task);
  }

  @Override
  public BusinessEvent createTaskInstanceCompleteEvt(DelegateTask task) {
    return taskInstanceEvtFactory.createCompleteEvent(task);
  }

  @Override
  public BusinessEvent createTaskInstanceDeleteEvt(DelegateTask task) {
    return taskInstanceEvtFactory.createDeleteEvent(task);
  }

  @Override
  public BusinessEvent createScriptViolationEvt(ScriptViolationEvent violation) {
    return scriptViolationEvtFactory.createScriptViolationEvent(violation);
  }
}
