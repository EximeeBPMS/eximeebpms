package org.eximeebpms.bpm.engine.impl.jobexecutor.scriptviolationcleanup;

import com.google.gson.JsonObject;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.jobexecutor.JobHandler;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.eximeebpms.bpm.engine.impl.util.JsonUtil;

import java.util.Date;

/**
 * Job handler that periodically deletes script violation records older than the configured
 * retention period from {@code ACT_RU_SCRIPT_VIOLATION}.
 *
 * <p>Engine-native and deployment-model-agnostic — the same job runs identically whether the
 * engine is started via the Spring Boot starter or a plain {@code bpm-platform.xml}/Tomcat
 * deployment, since retention is read from {@link ProcessEngineConfigurationImpl#getScriptViolationRetentionDays()}
 * rather than any Spring-specific configuration.</p>
 *
 * <p>After each execution the job reschedules itself roughly a day into the future so it keeps
 * running as an ever-living periodic job — mirrors
 * {@link org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup.BusinessEventOutboxCleanupJobHandler}.</p>
 */
public class ScriptViolationCleanupJobHandler implements JobHandler<ScriptViolationCleanupJobHandlerConfiguration> {

  public static final String TYPE = "script-violation-cleanup";

  /** Fixed interval between cleanup runs (24 hours in milliseconds). */
  public static final long DEFAULT_CLEANUP_INTERVAL_MILLIS = 24L * 60L * 60L * 1000L;

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void execute(ScriptViolationCleanupJobHandlerConfiguration configuration,
                      ExecutionEntity execution,
                      CommandContext commandContext,
                      String tenantId) {

    ProcessEngineConfigurationImpl engineConfig = commandContext.getProcessEngineConfiguration();

    int retentionDays = engineConfig.getScriptViolationRetentionDays();
    ScriptViolationStore store = engineConfig.getScriptViolationStore();

    if (retentionDays > 0 && store instanceof DbScriptViolationStore dbStore) {
      dbStore.cleanupOlderThan(retentionDays);
    }

    // Reschedule the job to run again roughly a day from now, regardless of whether this run
    // did any work — retention/store can change at runtime without a restart.
    JobEntity currentJob = commandContext.getCurrentJob();
    Date nextRun = new Date(ClockUtil.getCurrentTime().getTime() + DEFAULT_CLEANUP_INTERVAL_MILLIS);
    commandContext.getJobManager().reschedule(currentJob, nextRun);
  }

  @Override
  public ScriptViolationCleanupJobHandlerConfiguration newConfiguration(String canonicalString) {
    JsonObject jsonObject = JsonUtil.asObject(canonicalString);
    return ScriptViolationCleanupJobHandlerConfiguration.fromJson(jsonObject);
  }

  @Override
  public void onDelete(ScriptViolationCleanupJobHandlerConfiguration configuration, JobEntity jobEntity) {
    // nothing to do
  }
}
