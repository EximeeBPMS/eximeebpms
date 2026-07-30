package org.eximeebpms.bpm.engine.impl.businessevent;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.DelegateTask;
import org.eximeebpms.bpm.engine.delegate.VariableScope;
import org.eximeebpms.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;

/**
 * <p>The producer for business events. The business event producer is
 * responsible for extracting data from the runtime structures
 * (Executions, Tasks, ...) and adding the data to a {@link BusinessEvent}.
 *
 */
public interface BusinessEventProducer {

  /**
   * Creates the business event fired when a variable is <strong>created</strong>.
   *
   * @param variableInstance the runtime variable instance
   * @param the scope to which the variable is linked
   * @return the business event
   */
  BusinessEvent createVariableCreateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope);

  /**
   * Creates the business event fired when a variable is <strong>updated</strong>.
   *
   * @param variableInstance the runtime variable instance
   * @param the scope to which the variable is linked
   * @return the business event
   */
  BusinessEvent createVariableUpdateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope);

  /**
   * Creates the business event fired when a variable is <strong>migrated</strong>.
   *
   * @param variableInstance the runtime variable instance
   * @param the scope to which the variable is linked
   * @return the business event
   */
  BusinessEvent createVariableMigrateEvt(VariableInstanceEntity variableInstance);

  /**
   * Creates the business event fired when a variable is <strong>deleted</strong>.
   *
   * @param variableInstance
   * @param sourceVariableScope
   * @return the business event
   */
  BusinessEvent createVariableDeleteEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope);

  /**
   * Creates the business event fired when a process is <strong>started</strong>.
   *
   * @param execution
   * @return the business event
   */
  BusinessEvent createProcessInstanceStartEvt(DelegateExecution execution);
  /**
   * Creates the business event fired when a process is <strong>ended</strong>.
   *
   * @param execution
   * @return the business event
   */
  BusinessEvent createProcessInstanceEndEvt(DelegateExecution execution);
  /**
   * Creates the business event fired when a process is <strong>updated</strong>.
   *
   * @param execution
   * @return the business event
   */
  BusinessEvent createProcessInstanceUpdateEvt(DelegateExecution execution);

  /**
   * Creates the business event fired when an identity-link is <strong>added</strong>.
   *
   * @param identityLinkEntity
   * @return the business event
   */
  BusinessEvent createIdentityLinkAddEvt(IdentityLinkEntity identityLinkEntity);

  /**
   * Creates the business event fired when an identity-link is <strong>deleted</strong>.
   *
   * @param identityLinkEntity
   * @return the business event
   */
  BusinessEvent createIdentityLinkDeleteEvt(IdentityLinkEntity identityLinkEntity);

  /**
   * Creates the business event fired when an task-instance is <strong>created</strong>.
   *
   * @param task
   * @return the business event
   */
  BusinessEvent createTaskInstanceCreateEvt(DelegateTask task);

  /**
   * Creates the business event fired when an task-instance is <strong>updated</strong>.
   *
   * @param task
   * @return the business event
   */
  BusinessEvent createTaskInstanceUpdateEvt(DelegateTask task);

  /**
   * Creates the business event fired when an task-instance is <strong>completed</strong>.
   *
   * @param task
   * @return the business event
   */
  BusinessEvent createTaskInstanceCompleteEvt(DelegateTask task);

  /**
   * Creates the business event fired when an task-instance is <strong>deleted</strong>.
   *
   * @param task
   * @return the business event
   */
  BusinessEvent createTaskInstanceDeleteEvt(DelegateTask task);

  /**
   * Creates the business event fired when a script violation is <strong>detected</strong>.
   *
   * @param violation the recorded script violation event
   * @return the business event
   */
  BusinessEvent createScriptViolationEvt(ScriptViolationEvent violation);

}
