package org.eximeebpms.bpm.engine.impl.businessevent;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessDetailEventEntity extends BusinessEvent {

    protected String activityInstanceId;
    protected String taskId;
    protected Date timestamp;
    protected String tenantId;
    protected String userOperationId;

}
