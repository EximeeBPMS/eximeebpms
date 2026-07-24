package org.eximeebpms.bpm.engine.impl.businessevent.externaltask;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExternalTaskBusinessEvent extends BusinessEvent {

    protected Date timestamp;

    protected String externalTaskId;

    protected String topicName;
    protected String workerId;
    protected long priority;
    protected Integer retries;

    protected String errorMessage;

    protected String errorDetails;
    protected String activityId;

    protected String activityInstanceId;
    protected String tenantId;

    protected int state;
}
