package org.eximeebpms.bpm.engine.impl.businessevent;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.DelegateTask;
import org.eximeebpms.bpm.engine.delegate.VariableScope;
import org.eximeebpms.bpm.engine.impl.batch.BatchEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;
import org.eximeebpms.bpm.engine.runtime.Incident;

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
   * @param sourceVariableScope the scope to which the variable is linked
   * @return the business event
   */
  BusinessEvent createVariableCreateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope);

  /**
   * Creates the business event fired when a variable is <strong>updated</strong>.
   *
   * @param variableInstance the runtime variable instance
   * @param sourceVariableScope the scope to which the variable is linked
   * @return the business event
   */
  BusinessEvent createVariableUpdateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope);

  /**
   * Creates the business event fired when a variable is <strong>migrated</strong>.
   *
   * @param variableInstance the runtime variable instance
   * @return the business event
   */
  BusinessEvent createVariableMigrateEvt(VariableInstanceEntity variableInstance);

  /**
   * Creates the business event fired when a variable is <strong>deleted</strong>.
   *
   * @param variableInstance the runtime variable instance
   * @param sourceVariableScope the scope to which the variable is linked
   * @return the business event
   */
  BusinessEvent createVariableDeleteEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope);

  /**
   * Creates the business event fired when a process is <strong>started</strong>.
   *
   * @param execution the execution of the process instance that was started
   * @return the business event
   */
  BusinessEvent createProcessInstanceStartEvt(DelegateExecution execution);
  /**
   * Creates the business event fired when a process is <strong>ended</strong>.
   *
   * @param execution the execution of the process instance that ended
   * @return the business event
   */
  BusinessEvent createProcessInstanceEndEvt(DelegateExecution execution);
  /**
   * Creates the business event fired when a process is <strong>updated</strong>.
   *
   * @param execution the execution of the process instance that was updated
   * @return the business event
   */
  BusinessEvent createProcessInstanceUpdateEvt(DelegateExecution execution);

  /**
   * Creates the business event fired when an activity instance is <strong>started</strong>.
   *
   * @param execution
   * @return the business event
   */
  BusinessEvent createActivityInstanceStartEvt(DelegateExecution execution);

  /**
   * Creates the business event fired when an activity instance is <strong>ended</strong>.
   *
   * @param execution
   * @return the business event
   */
  BusinessEvent createActivityInstanceEndEvt(DelegateExecution execution);

  /**
   * Creates the business event fired when a process instance is <strong>migrated</strong>.
   *
   * @param execution
   * @return the business event
   */
  BusinessEvent createProcessInstanceMigrateEvt(DelegateExecution execution);

  /**
   * Creates the business event fired when an identity-link is <strong>added</strong>.
   *
   * @param identityLinkEntity the identity-link entity that was added
   * @return the business event
   */
  BusinessEvent createIdentityLinkAddEvt(IdentityLinkEntity identityLinkEntity);

  /**
   * Creates the business event fired when an identity-link is <strong>deleted</strong>.
   *
   * @param identityLinkEntity the identity-link entity that was deleted
   * @return the business event
   */
  BusinessEvent createIdentityLinkDeleteEvt(IdentityLinkEntity identityLinkEntity);

  /**
   * Creates the business event fired when an task-instance is <strong>created</strong>.
   *
   * @param task the task that was created
   * @return the business event
   */
  BusinessEvent createTaskInstanceCreateEvt(DelegateTask task);

  /**
   * Creates the business event fired when an task-instance is <strong>updated</strong>.
   *
   * @param task the task that was updated
   * @return the business event
   */
  BusinessEvent createTaskInstanceUpdateEvt(DelegateTask task);

  /**
   * Creates the business event fired when a task-instance is <strong>migrated</strong>.
   *
   * @param task
   * @return the business event
   */
  BusinessEvent createTaskInstanceMigrateEvt(DelegateTask task);

  /**
   * Creates the business event fired when an task-instance is <strong>completed</strong>.
   *
   * @param task the task that was completed
   * @return the business event
   */
  BusinessEvent createTaskInstanceCompleteEvt(DelegateTask task);

  /**
   * Creates the business event fired when an task-instance is <strong>deleted</strong>.
   *
   * @param task the task that was deleted
   * @return the business event
   */
  BusinessEvent createTaskInstanceDeleteEvt(DelegateTask task);

  /**
   * Creates the business event fired when a job is <strong>created</strong>.
   *
   * @param job the job entity
   * @return the business event
   */
  BusinessEvent createJobCreatedEvt(JobEntity job);

  /**
   * Creates the business event fired when a job is <strong>deleted</strong>.
   *
   * @param job the job entity
   * @return the business event
   */
  BusinessEvent createJobDeletedEvt(JobEntity job);

  /**
   * Creates the business event fired when a job has <strong>failed</strong>.
   *
   * @param job     the job entity
   * @param failure the exception that caused the job to fail
   * @return the business event
   */
  BusinessEvent createJobFailedEvt(JobEntity job, Throwable failure);

  /**
   * Creates the business event fired when a job is <strong>successful</strong>.
   *
   * @param job the job entity
   * @return the business event
   */
  BusinessEvent createJobSuccessfulEvt(JobEntity job);

  /**
   * Creates the business event fired when a script violation is <strong>detected</strong>.
   *
   * @param violation the recorded script violation event
   * @return the business event
   */
  BusinessEvent createScriptViolationEvt(ScriptViolationEvent violation);

  /**
   * Creates the business event fired when an incident is <strong>created</strong>.
   *
   * @param incident the incident that was created
   * @return the business event
   */
  BusinessEvent createBusinessIncidentCreateEvt(Incident incident);

  /**
   * Creates the business event fired when an incident is <strong>resolved</strong>.
   *
   * @param incident the incident that was resolved
   * @return the business event
   */
  BusinessEvent createBusinessIncidentResolveEvt(Incident incident);

  /**
   * Creates the business event fired when an incident is <strong>deleted</strong>.
   *
   * @param incident the incident that was deleted
   * @return the business event
   */
  BusinessEvent createBusinessIncidentDeleteEvt(Incident incident);

  /**
   * Creates the business event fired when an incident is <strong>migrated</strong>.
   *
   * @param incident the incident that was migrated
   * @return the business event
   */
  BusinessEvent createBusinessIncidentMigrateEvt(Incident incident);

  /**
   * Creates the business event fired when an incident is <strong>updated</strong>.
   *
   * @param incident the incident that was updated
   * @return the business event
   */
  BusinessEvent createBusinessIncidentUpdateEvt(Incident incident);

    /**
     * Creates the business event fired when a batch is <strong>started</strong>.
     *
     * @param batchEntity the batch entity
     * @return the business event
     */
    BusinessEvent createBatchStartBusinessEvent(BatchEntity batchEntity);

    /**
     * Creates the business event fired when a batch is <strong>ended</strong>.
     *
     * @param batchEntity the batch entity
     * @return the business event
     */
    BusinessEvent createBatchEndBusinessEvent(BatchEntity batchEntity);

    /**
     * Creates the business event fired when a batch is <strong>updated</strong>.
     *
     * @param batchEntity the batch entity
     * @return the business event
     */
    BusinessEvent createBatchUpdateBusinessEvent(BatchEntity batchEntity);

    /**
     * Creates the business event fired when an external task is <strong>created</strong>.
     *
     * @param externalTaskEntity the external task entity
     * @return the business event
     */
    BusinessEvent createExternalTaskCreatedBusinessEvent(ExternalTaskEntity externalTaskEntity);

    /**
     * Creates the business event fired when an external task <strong>fails</strong>.
     *
     * @param externalTaskEntity the external task entity
     * @return the business event
     */
    BusinessEvent createExternalTaskFailedBusinessEvent(ExternalTaskEntity externalTaskEntity);

    /**
     * Creates the business event fired when an external task completes <strong>successfully</strong>.
     *
     * @param externalTaskEntity the external task entity
     * @return the business event
     */
    BusinessEvent createExternalTaskSuccessfulBusinessEvent(ExternalTaskEntity externalTaskEntity);

    /**
     * Creates the business event fired when an external task is <strong>deleted</strong>.
     *
     * @param externalTaskEntity the external task entity
     * @return the business event
     */
    BusinessEvent createExternalTaskDeletedBusinessEvent(ExternalTaskEntity externalTaskEntity);
}
