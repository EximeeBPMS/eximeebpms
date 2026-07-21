package org.eximeebpms.bpm.engine.impl.businessevent.batch;

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
public class BatchBusinessEvent extends BusinessEvent {

  protected String type;

  protected int totalJobs;
  protected int batchJobsPerSeed;
  protected int invocationsPerBatchJob;

  protected String seedJobDefinitionId;
  protected String monitorJobDefinitionId;
  protected String batchJobDefinitionId;

  protected String tenantId;
  protected String createUserId;

  protected Date startTime;
  protected Date endTime;
  protected Date executionStartTime;

}
