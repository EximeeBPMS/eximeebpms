package org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup;

import org.eximeebpms.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.eximeebpms.bpm.engine.impl.persistence.entity.EverLivingJobEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;

import java.util.Date;

/**
 * Job declaration for the business-event outbox cleanup job.
 *
 * <p>Creates a single {@link EverLivingJobEntity} that is scheduled to run immediately on
 * engine bootstrap. After each run the handler reschedules itself one hour into the future.</p>
 */
public class BusinessEventOutboxCleanupJobDeclaration extends JobDeclaration<Void, EverLivingJobEntity> {

  public BusinessEventOutboxCleanupJobDeclaration() {
    super(BusinessEventOutboxCleanupJobHandler.TYPE);
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
  protected BusinessEventOutboxCleanupJobHandlerConfiguration resolveJobHandlerConfiguration(Void context) {
    return new BusinessEventOutboxCleanupJobHandlerConfiguration();
  }

  @Override
  public Date resolveDueDate(Void context) {
    // schedule immediately on creation so the first run happens right away
    return ClockUtil.getCurrentTime();
  }
}

