package org.eximeebpms.bpm.engine.impl.businessevent.job;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eximeebpms.bpm.engine.job.JobState;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JobLogBusinessEvent extends BusinessEvent {


    protected Date timestamp;

    protected String jobId;

    protected Date jobDueDate;

    protected int jobRetries;

    protected long jobPriority;

    protected String jobExceptionMessage;

    protected String exceptionStacktrace;

    protected String jobDefinitionId;

    protected String jobDefinitionType;

    protected String jobDefinitionConfiguration;

    protected String activityId;

    protected String failedActivityId;

    protected String deploymentId;

    protected int state;

    protected String tenantId;

    protected String hostname;

    protected String batchId;

    public boolean isCreationLog() {
        return state == JobState.CREATED.getStateCode();
    }

    public boolean isFailureLog() {
        return state == JobState.FAILED.getStateCode();
    }

    public boolean isSuccessLog() {
        return state == JobState.SUCCESSFUL.getStateCode();
    }

    public boolean isDeletionLog() {
        return state == JobState.DELETED.getStateCode();
    }

}
