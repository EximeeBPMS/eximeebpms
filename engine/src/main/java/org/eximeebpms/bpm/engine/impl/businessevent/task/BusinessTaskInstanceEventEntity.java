package org.eximeebpms.bpm.engine.impl.businessevent.task;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessDetailEventEntity;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessTaskInstanceEventEntity extends BusinessDetailEventEntity {

  protected String assignee;
  protected String owner;
  protected String name;
  protected String description;
  protected Date dueDate;
  protected Date followUpDate;
  protected int priority;
  protected String parentTaskId;
  protected String taskDefinitionKey;

  protected Date startTime;
  protected Date endTime;
  protected Long durationInMillis;
  protected String deleteReason;

  protected String taskState;
}
