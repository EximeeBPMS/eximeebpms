package org.eximeebpms.bpm.engine.impl.jobexecutor.scriptviolationcleanup;

import org.eximeebpms.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.eximeebpms.bpm.engine.impl.persistence.entity.EverLivingJobEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;

import java.util.Date;

/**
 * Job declaration for the script violation cleanup job.
 *
 * <p>Creates a single {@link EverLivingJobEntity} that is scheduled to run immediately on
 * engine bootstrap. After each run the handler reschedules itself roughly a day into the future.</p>
 */
public class ScriptViolationCleanupJobDeclaration extends JobDeclaration<Void, EverLivingJobEntity> {

  public ScriptViolationCleanupJobDeclaration() {
    super(ScriptViolationCleanupJobHandler.TYPE);
  }

  @Override
  protected ExecutionEntity resolveExecution(Void context) {
    return null;
  }

  @Override
  protected EverLivingJobEntity newJobInstance(Void context) {
    return new EverLivingJobEntity();
  }

  @Override
  protected void postInitialize(Void context, EverLivingJobEntity job) {
    // no additional initialization required
  }

  @Override
  public EverLivingJobEntity reconfigure(Void context, EverLivingJobEntity job) {
    return job;
  }

  @Override
  protected ScriptViolationCleanupJobHandlerConfiguration resolveJobHandlerConfiguration(Void context) {
    return new ScriptViolationCleanupJobHandlerConfiguration();
  }

  @Override
  public Date resolveDueDate(Void context) {
    // schedule immediately on creation so the first run happens right away
    return ClockUtil.getCurrentTime();
  }
}
