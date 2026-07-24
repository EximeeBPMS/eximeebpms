package org.eximeebpms.bpm.engine.impl.businessevent.job;

import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.eximeebpms.bpm.engine.job.JobState;
import org.eximeebpms.bpm.engine.management.JobDefinition;

import java.util.Date;

import static org.eximeebpms.bpm.engine.impl.util.ExceptionUtil.getExceptionStacktrace;

public class JobBusinessEventFactory extends BusinessEventFactorySupport {

    public BusinessEvent createJobCreatedEvent(JobEntity job) {
        return createBusinessJobLogEvt(job, BusinessEventTypes.JOB_CREATE);
    }

    public BusinessEvent createJobDeletedEvent(JobEntity job) {
        return createBusinessJobLogEvt(job, BusinessEventTypes.JOB_DELETE);
    }

    public BusinessEvent createJobSuccessfulEvent(JobEntity job) {
        return createBusinessJobLogEvt(job, BusinessEventTypes.JOB_SUCCESS);
    }

    public BusinessEvent createJobFailedEvent(JobEntity job, Throwable exception) {
        JobLogBusinessEvent event = (JobLogBusinessEvent) createBusinessJobLogEvt(job, BusinessEventTypes.JOB_FAIL);
        if (exception != null) {
            event.setJobExceptionMessage(exception.getMessage());
            event.setExceptionStacktrace(getExceptionStacktrace(exception));
        }
        return event;
    }

    protected BusinessEvent createBusinessJobLogEvt(JobEntity job, BusinessEventTypes eventType) {
        JobLogBusinessEvent event = new JobLogBusinessEvent();
        initBusinessJobLogEvent(event, job, eventType);
        return event;
    }

    protected void initBusinessJobLogEvent(JobLogBusinessEvent evt, JobEntity jobEntity, BusinessEventTypes eventType) {
        Date currentTime = ClockUtil.getCurrentTime();
        evt.setTimestamp(currentTime);
        evt.setEventType(eventType.getEventName());
        evt.setBusinessEventType(eventType.getBusinessEventName());

        evt.setJobId(jobEntity.getId());
        evt.setBatchId(jobEntity.getBatchId());
        evt.setJobDueDate(jobEntity.getDuedate());
        evt.setJobRetries(jobEntity.getRetries());
        evt.setJobPriority(jobEntity.getPriority());

        String hostName = Context.getCommandContext().getProcessEngineConfiguration().getHostname();
        evt.setHostname(hostName);

        JobDefinition jobDefinition = jobEntity.getJobDefinition();
        if (jobDefinition != null) {
            evt.setJobDefinitionId(jobDefinition.getId());
            evt.setJobDefinitionType(jobDefinition.getJobType());
            evt.setJobDefinitionConfiguration(jobDefinition.getJobConfiguration());
        }
        else {
            // in case of async signal there does not exist a job definition
            // but we use the jobHandlerType as jobDefinitionType
            evt.setJobDefinitionType(jobEntity.getJobHandlerType());
        }

        fillProcessDefinitionData(evt, jobEntity);

        evt.setActivityId(jobEntity.getActivityId());
        evt.setFailedActivityId(jobEntity.getFailedActivityId());
        evt.setExecutionId(jobEntity.getExecutionId());
        evt.setProcessInstanceId(jobEntity.getProcessInstanceId());
        evt.setDeploymentId(jobEntity.getDeploymentId());
        evt.setTenantId(jobEntity.getTenantId());

        ExecutionEntity execution = jobEntity.getExecution();
        if (execution != null) {
            evt.setRootProcessInstanceId(execution.getRootProcessInstanceId());
        }

        // initialize sequence counter
        initSequenceCounter(jobEntity.getSequenceCounter(), evt);

        JobState state = switch (eventType) {
            case JOB_CREATE -> JobState.CREATED;
            case JOB_FAIL -> JobState.FAILED;
            case JOB_SUCCESS -> JobState.SUCCESSFUL;
            case JOB_DELETE -> JobState.DELETED;
            default ->
                    throw new IllegalArgumentException("Unsupported business event type for job state mapping: " + eventType);
        };
        evt.setState(state.getStateCode());
    }
}
