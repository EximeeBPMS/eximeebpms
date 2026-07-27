package org.eximeebpms.bpm.engine.impl.businessevent.useroperationlog;

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
public class UserOperationLogBusinessEvent extends BusinessEvent {

  protected String operationId;
  protected String operationType;
  protected String entityType;

  protected String property;
  protected String orgValue;
  protected String newValue;

  protected String userId;

  protected String taskId;
  protected String jobId;
  protected String jobDefinitionId;

  protected String deploymentId;
  protected String batchId;
  protected String externalTaskId;

  protected String category;
  protected String annotation;
  protected String tenantId;

  protected Date timestamp;

}
