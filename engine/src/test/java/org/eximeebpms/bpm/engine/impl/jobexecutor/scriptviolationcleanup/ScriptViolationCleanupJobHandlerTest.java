package org.eximeebpms.bpm.engine.impl.jobexecutor.scriptviolationcleanup;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobManager;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.scripting.security.NoOpScriptViolationStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScriptViolationCleanupJobHandlerTest {

  @Mock
  private ProcessEngineConfigurationImpl config;
  @Mock
  private CommandContext commandContext;
  @Mock
  private DbScriptViolationStore dbStore;
  @Mock
  private JobManager jobManager;
  @Mock
  private JobEntity currentJob;

  private final ScriptViolationCleanupJobHandler handler = new ScriptViolationCleanupJobHandler();

  @Test
  public void shouldCleanupAndRescheduleWhenRetentionPositiveAndStoreIsDbBacked() {
    // given
    when(commandContext.getProcessEngineConfiguration()).thenReturn(config);
    when(config.getScriptViolationRetentionDays()).thenReturn(30);
    when(config.getScriptViolationStore()).thenReturn(dbStore);
    when(commandContext.getJobManager()).thenReturn(jobManager);
    when(commandContext.getCurrentJob()).thenReturn(currentJob);

    // when
    handler.execute(new ScriptViolationCleanupJobHandlerConfiguration(), null, commandContext, null);

    // then
    verify(dbStore).cleanupOlderThan(30);
    verify(jobManager).reschedule(eq(currentJob), any(Date.class));
  }

  @Test
  public void shouldNotCleanupButStillRescheduleWhenRetentionIsZeroOrLess() {
    // given
    when(commandContext.getProcessEngineConfiguration()).thenReturn(config);
    when(config.getScriptViolationRetentionDays()).thenReturn(0);
    when(config.getScriptViolationStore()).thenReturn(dbStore);
    when(commandContext.getJobManager()).thenReturn(jobManager);
    when(commandContext.getCurrentJob()).thenReturn(currentJob);

    // when
    handler.execute(new ScriptViolationCleanupJobHandlerConfiguration(), null, commandContext, null);

    // then — retention disabled: no deletion, but the ever-living job keeps rescheduling itself
    // so a later increase to retentionDays takes effect without needing a restart
    verify(dbStore, never()).cleanupOlderThan(anyInt());
    verify(jobManager).reschedule(eq(currentJob), any(Date.class));
  }

  @Test
  public void shouldNotCleanupButStillRescheduleWhenStoreIsNotDbBacked() {
    // given — e.g. script security enabled but nothing ever wired a DbScriptViolationStore
    when(commandContext.getProcessEngineConfiguration()).thenReturn(config);
    when(config.getScriptViolationRetentionDays()).thenReturn(30);
    when(config.getScriptViolationStore()).thenReturn(NoOpScriptViolationStore.INSTANCE);
    when(commandContext.getJobManager()).thenReturn(jobManager);
    when(commandContext.getCurrentJob()).thenReturn(currentJob);

    // when
    handler.execute(new ScriptViolationCleanupJobHandlerConfiguration(), null, commandContext, null);

    // then
    verify(jobManager).reschedule(eq(currentJob), any(Date.class));
  }
}
