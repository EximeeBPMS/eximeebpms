package org.eximeebpms.bpm.engine.impl.businessevent.process;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessDetailEventEntity;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BusinessProcessInstanceEventEntity extends BusinessDetailEventEntity {

  protected String businessKey;
  protected String startUserId;
  protected String superProcessInstanceId;
  protected String deleteReason;
  protected String endActivityId;
  protected String startActivityId;
  protected String state;
  protected Long durationInMillis;
  private Date startTime;
  private Date endTime;
}
