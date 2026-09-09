package org.eximeebpms.bpm.engine.test.standalone.scripting;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.eximeebpms.bpm.engine.ManagementService;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;
import org.eximeebpms.bpm.engine.impl.jobexecutor.scriptviolationcleanup.ScriptViolationCleanupJobHandler;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptOrigin;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSourceType;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.eximeebpms.bpm.engine.runtime.Job;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * Integration test for {@link ScriptViolationCleanupJobHandler}'s bootstrap wiring via
 * {@code BootstrapEngineCommand} — the engine-native mechanism that replaces the old
 * Spring-only {@code @Scheduled} cleanup job, so it must be proven against a real engine/DB
 * independently of any Spring Boot machinery.
 */
public class ScriptViolationCleanupJobIT {

  private static final int RETENTION_DAYS = 30;

  private final ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(config -> {
    config.setScriptSecurityMode("AUDIT");
    config.setScriptViolationStore(new DbScriptViolationStore(config));
    config.setScriptViolationRetentionDays(RETENTION_DAYS);
  });

  private final ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule);

  private CommandExecutor commandExecutor;
  private ManagementService managementService;

  @Before
  public void setUp() {
    ProcessEngineConfigurationImpl configuration = engineRule.getProcessEngineConfiguration();
    commandExecutor = configuration.getCommandExecutorTxRequired();
    managementService = engineRule.getManagementService();
  }

  @After
  public void cleanUp() {
    deleteCleanupJob();
    deleteAllViolations();
    ClockUtil.reset();
  }

  @Test
  public void shouldBootstrapCleanupJobOnEngineStartWhenRetentionPositive() {
    List<Job> jobs = findCleanupJobs();

    assertThat(jobs)
        .as("exactly one script violation cleanup job should be bootstrapped when retention > 0")
        .hasSize(1);
  }

  @Test
  public void shouldDeleteViolationsOlderThanRetentionOnExecution() {
    Instant expired = Instant.now().minus(RETENTION_DAYS + 1, ChronoUnit.DAYS);
    Instant recent = Instant.now();
    insertViolation(expired, "EXPIRED");
    insertViolation(recent, "RECENT");

    assertThat(countAllViolations()).isEqualTo(2);

    executeCleanupJob();

    assertThat(countAllViolations())
        .as("only the violation older than the retention window should be deleted")
        .isEqualTo(1);
  }

  @Test
  public void shouldRescheduleJobAfterExecution() {
    Instant beforeExecution = ClockUtil.getCurrentTime().toInstant();

    executeCleanupJob();

    Job rescheduledJob = findCleanupJobs().get(0);
    long expectedMinDue = beforeExecution.toEpochMilli() + ScriptViolationCleanupJobHandler.DEFAULT_CLEANUP_INTERVAL_MILLIS;

    assertThat(rescheduledJob.getDuedate().getTime())
        .as("cleanup job should be rescheduled roughly a day after execution")
        .isGreaterThanOrEqualTo(expectedMinDue);
  }

  private void insertViolation(Instant timestamp, String ruleCode) {
    DbScriptViolationStore store = (DbScriptViolationStore) engineRule.getProcessEngineConfiguration().getScriptViolationStore();
    commandExecutor.execute(ctx -> {
      store.record(new ScriptViolationEvent(
          timestamp, "testProcess", null, "scriptTask",
          "javascript", ScriptSourceType.INLINE_SOURCE, ScriptOrigin.USER,
          ruleCode, "test violation"));
      return null;
    });
  }

  private void executeCleanupJob() {
    List<Job> jobs = findCleanupJobs();
    assertThat(jobs).as("cleanup job must exist before execution").isNotEmpty();
    managementService.executeJob(jobs.get(0).getId());
  }

  @SuppressWarnings("unchecked")
  private List<Job> findCleanupJobs() {
    return commandExecutor.execute(ctx ->
        (List<Job>) ctx.getJobManager().findJobsByHandlerType(ScriptViolationCleanupJobHandler.TYPE));
  }

  private void deleteCleanupJob() {
    commandExecutor.execute(ctx -> {
      ctx.getJobManager()
          .findJobsByHandlerType(ScriptViolationCleanupJobHandler.TYPE)
          .forEach(job -> {
            ctx.getJobManager().deleteJob((JobEntity) job);
            ctx.getHistoricJobLogManager().deleteHistoricJobLogByJobId(job.getId());
          });
      return null;
    });
  }

  private void deleteAllViolations() {
    DbScriptViolationStore store = (DbScriptViolationStore) engineRule.getProcessEngineConfiguration().getScriptViolationStore();
    commandExecutor.execute(ctx -> {
      store.cleanupOlderThan(0);
      return null;
    });
  }

  private long countAllViolations() {
    DbScriptViolationStore store = (DbScriptViolationStore) engineRule.getProcessEngineConfiguration().getScriptViolationStore();
    return commandExecutor.execute(ctx -> store.getTotalCount());
  }
}
