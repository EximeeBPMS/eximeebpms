package org.eximeebpms.bpm.engine.impl.businessevent;

import org.eximeebpms.bpm.dmn.engine.delegate.DmnDecisionEvaluationEvent;
import java.util.List;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.DelegateTask;
import org.eximeebpms.bpm.engine.delegate.VariableScope;
import org.eximeebpms.bpm.engine.impl.ProcessEngineLogger;
import org.eximeebpms.bpm.engine.impl.batch.BatchEntity;
import org.eximeebpms.bpm.engine.impl.businessevent.activity.ActivityInstanceBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.batch.BatchBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.externaltask.ExternalTaskBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.dmn.DmnDecisionEvaluationBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.identitylink.IdentityLinkBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.incident.IncidentBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.job.JobBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.process.ProcessInstanceBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.script.ScriptViolationBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.task.TaskInstanceBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.useroperationlog.UserOperationLogBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.businessevent.variable.VariableInstanceBusinessEventFactory;
import org.eximeebpms.bpm.engine.impl.cfg.ConfigurationLogger;
import org.eximeebpms.bpm.engine.impl.oplog.UserOperationLogContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;
import org.eximeebpms.bpm.engine.runtime.Incident;

public class DefaultBusinessEventProducer implements BusinessEventProducer {

  protected VariableInstanceBusinessEventFactory variableEvtFactory = new VariableInstanceBusinessEventFactory();
  protected ProcessInstanceBusinessEventFactory processEvtFactory = new ProcessInstanceBusinessEventFactory();
  protected ActivityInstanceBusinessEventFactory activityInstanceEvtFactory = new ActivityInstanceBusinessEventFactory();
  protected IdentityLinkBusinessEventFactory identityLinkEvtFactory = new IdentityLinkBusinessEventFactory();
  protected TaskInstanceBusinessEventFactory taskInstanceEvtFactory = new TaskInstanceBusinessEventFactory();
  protected JobBusinessEventFactory jobEvtFactory = new JobBusinessEventFactory();
  protected ScriptViolationBusinessEventFactory scriptViolationEvtFactory = new ScriptViolationBusinessEventFactory();
  protected IncidentBusinessEventFactory incidentBusinessEventFactory = new IncidentBusinessEventFactory();
  protected BatchBusinessEventFactory batchEvtFactory = new BatchBusinessEventFactory();
  protected ExternalTaskBusinessEventFactory externalTaskEvtFactory = new ExternalTaskBusinessEventFactory();
  protected DmnDecisionEvaluationBusinessEventFactory dmnDecisionEvaluationEvtFactory = new DmnDecisionEvaluationBusinessEventFactory();
  protected UserOperationLogBusinessEventFactory userOperationLogEvtFactory = new UserOperationLogBusinessEventFactory();

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
  public BusinessEvent createActivityInstanceStartEvt(DelegateExecution execution) {
    return activityInstanceEvtFactory.createStartEvent(execution);
  }

  @Override
  public BusinessEvent createActivityInstanceEndEvt(DelegateExecution execution) {
    return activityInstanceEvtFactory.createEndEvent(execution);
  }

  @Override
  public BusinessEvent createProcessInstanceMigrateEvt(DelegateExecution execution) {
    return processEvtFactory.createMigrateEvent(execution);
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
  public BusinessEvent createTaskInstanceMigrateEvt(DelegateTask task) {
    return taskInstanceEvtFactory.createMigrateEvent(task);
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
  public BusinessEvent createJobCreatedEvt(JobEntity job) {
    return jobEvtFactory.createJobCreatedEvent(job);
  }

  @Override
  public BusinessEvent createJobDeletedEvt(JobEntity job) {
    return jobEvtFactory.createJobDeletedEvent(job);
  }

  @Override
  public BusinessEvent createJobSuccessfulEvt(JobEntity job) {
    return jobEvtFactory.createJobSuccessfulEvent(job);
  }

  @Override
  public BusinessEvent createJobFailedEvt(JobEntity job, Throwable failure) {
    return jobEvtFactory.createJobFailedEvent(job, failure);
  }

  @Override
  public BusinessEvent createScriptViolationEvt(ScriptViolationEvent violation) {
    return scriptViolationEvtFactory.createScriptViolationEvent(violation);
  }

  @Override
  public BusinessEvent createBusinessIncidentCreateEvt(Incident incident) {
    return incidentBusinessEventFactory.createBusinessIncidentEvt(incident, BusinessEventTypes.INCIDENT_CREATE);
  }

  @Override
  public BusinessEvent createBusinessIncidentResolveEvt(Incident incident) {
    return incidentBusinessEventFactory.createBusinessIncidentEvt(incident, BusinessEventTypes.INCIDENT_RESOLVE);
  }

  @Override
  public BusinessEvent createBusinessIncidentDeleteEvt(Incident incident) {
    return incidentBusinessEventFactory.createBusinessIncidentEvt(incident, BusinessEventTypes.INCIDENT_DELETE);
  }

  @Override
  public BusinessEvent createBusinessIncidentMigrateEvt(Incident incident) {
    return incidentBusinessEventFactory.createBusinessIncidentEvt(incident, BusinessEventTypes.INCIDENT_MIGRATE);
  }

  @Override
  public BusinessEvent createBusinessIncidentUpdateEvt(Incident incident) {
    return incidentBusinessEventFactory.createBusinessIncidentEvt(incident, BusinessEventTypes.INCIDENT_UPDATE);
  }

  @Override
  public BusinessEvent createBatchStartBusinessEvent(BatchEntity batchEntity) {
    return batchEvtFactory.createStartEvent(batchEntity);
  }

  @Override
  public BusinessEvent createBatchEndBusinessEvent(BatchEntity batchEntity) {
    return batchEvtFactory.createEndEvent(batchEntity);
  }

  @Override
  public BusinessEvent createBatchUpdateBusinessEvent(BatchEntity batchEntity) {
    return batchEvtFactory.createUpdateEvent(batchEntity);
  }

  @Override
  public BusinessEvent createExternalTaskCreatedBusinessEvent(ExternalTaskEntity externalTaskEntity) {
    return externalTaskEvtFactory.createCreatedEvent(externalTaskEntity);
  }

  @Override
  public BusinessEvent createExternalTaskFailedBusinessEvent(ExternalTaskEntity externalTaskEntity) {
    return externalTaskEvtFactory.createFailedEvent(externalTaskEntity);
  }

  @Override
  public BusinessEvent createExternalTaskSuccessfulBusinessEvent(ExternalTaskEntity externalTaskEntity) {
    return externalTaskEvtFactory.createSuccessfulEvent(externalTaskEntity);
  }

  @Override
  public BusinessEvent createExternalTaskDeletedBusinessEvent(ExternalTaskEntity externalTaskEntity) {
    return externalTaskEvtFactory.createDeletedEvent(externalTaskEntity);
  }

  @Override
  public BusinessEvent createDecisionEvaluationEvt(ExecutionEntity execution, DmnDecisionEvaluationEvent evaluationEvent) {
    return dmnDecisionEvaluationEvtFactory.createEvent(execution, evaluationEvent);
  }

  @Override
  public BusinessEvent createDecisionEvaluationEvt(DmnDecisionEvaluationEvent evaluationEvent) {
    return dmnDecisionEvaluationEvtFactory.createEvent(evaluationEvent);
  }

  @Override
  public List<BusinessEvent> createUserOperationLogEvents(UserOperationLogContext context) {
    return userOperationLogEvtFactory.createEvents(context);
  }
}
