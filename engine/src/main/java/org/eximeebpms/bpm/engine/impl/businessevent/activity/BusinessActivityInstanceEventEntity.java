package org.eximeebpms.bpm.engine.impl.businessevent.activity;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessActivityInstanceEventEntity extends BusinessEvent {

  protected String activityId;
  protected String activityName;
  protected String activityType;
  protected String activityInstanceId;
  protected int activityInstanceState;
  protected String parentActivityInstanceId;
  protected String calledProcessInstanceId;
  protected String tenantId;

  protected Date startTime;
  protected Date endTime;
  protected Long durationInMillis;

}
