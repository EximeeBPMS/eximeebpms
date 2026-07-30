package org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup;

import com.google.gson.JsonObject;
import lombok.Setter;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.jobexecutor.JobHandler;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.eximeebpms.bpm.engine.impl.util.JsonUtil;

import java.util.Date;

/**
 * Job handler that periodically deletes processed
 * {@link org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity} entries.
 *
 * <p>After each execution the job reschedules itself one hour into the future so it
 * keeps running as an ever-living periodic job.</p>
 */
public class BusinessEventOutboxCleanupJobHandler implements JobHandler<BusinessEventOutboxCleanupJobHandlerConfiguration> {

  public static final String TYPE = "business-event-outbox-cleanup";

  /** Fixed interval between cleanup runs (1 hour in milliseconds). */
  public static final long DEFAULT_CLEANUP_INTERVAL_MILLIS = 60L * 60L * 1000L;
  /** Default retention time for processed outbox entries (7 days in milliseconds). */
  public static final long DEFAULT_BUSINESS_EVENT_OUTBOX_RETENTION_MS = 7L * 24L * 60L * 60L * 1000L;

  @Setter
  private long cleanupInterval = DEFAULT_CLEANUP_INTERVAL_MILLIS;

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void execute(BusinessEventOutboxCleanupJobHandlerConfiguration configuration,
                      ExecutionEntity execution,
                      CommandContext commandContext,
                      String tenantId) {

    ProcessEngineConfigurationImpl engineConfig = commandContext.getProcessEngineConfiguration();

    long retentionMs = engineConfig.getBusinessEventConfiguration().getOutboxRetentionMs();
    Date cutoffDate = new Date(ClockUtil.getCurrentTime().getTime() - retentionMs);

    commandContext.getBusinessEventManager().deleteProcessedOlderThan(cutoffDate);

    // Reschedule the job to run again in one hour
    long intervalMs = engineConfig.getBusinessEventConfiguration().getOutboxCleanupIntervalMs();
    JobEntity currentJob = commandContext.getCurrentJob();
    Date nextRun = new Date(ClockUtil.getCurrentTime().getTime() + intervalMs);
    commandContext.getJobManager().reschedule(currentJob, nextRun);
  }

  @Override
  public BusinessEventOutboxCleanupJobHandlerConfiguration newConfiguration(String canonicalString) {
    JsonObject jsonObject = JsonUtil.asObject(canonicalString);
    return BusinessEventOutboxCleanupJobHandlerConfiguration.fromJson(jsonObject);
  }

  @Override
  public void onDelete(BusinessEventOutboxCleanupJobHandlerConfiguration configuration, JobEntity jobEntity) {
    // nothing to do
  }
}

