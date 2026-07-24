/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eximeebpms.bpm.engine.impl.jobexecutor;

import org.eximeebpms.bpm.engine.impl.ProcessEngineLogger;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProcessor;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProducer;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.interceptor.Command;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.management.Metrics;

import java.util.Date;

/**
 * @author Frederik Heremans
 * @author Bernd Ruecker
 */
public class FailedJobListener implements Command<Void> {

    private final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

    protected CommandExecutor commandExecutor;
    // Dedicated REQUIRES_NEW executor used only for firing the JOB_FAIL business event in its own,
    // independent transaction (see fireJobFailedBusinessEventInNewTransaction). The regular
    // commandExecutor above must stay a plain (TX_REQUIRED) executor: it is reused for the nested
    // FailedJobListenerCmd execution below, which needs to join the ambient transaction opened by
    // the outer commandExecutor.execute(failedJobListener) call rather than opening a second,
    // separate transaction - otherwise the retry decrement and the job handler command would no
    // longer commit atomically.
    protected CommandExecutor commandExecutorTxRequiresNew;
    protected JobFailureCollector jobFailureCollector;
    protected int countRetries = 0;
    protected int totalRetries = ProcessEngineConfigurationImpl.DEFAULT_FAILED_JOB_LISTENER_MAX_RETRIES;
    // guards against firing the JOB_FAIL business event more than once when
    // the retry-decrement command below gets retried (e.g. on OptimisticLockingException),
    // since this listener instance is reused across those retries.
    protected boolean jobFailedBusinessEventFired = false;

    public FailedJobListener(CommandExecutor commandExecutor, CommandExecutor commandExecutorTxRequiresNew,
            JobFailureCollector jobFailureCollector) {
        this.commandExecutor = commandExecutor;
        this.commandExecutorTxRequiresNew = commandExecutorTxRequiresNew;
        this.jobFailureCollector = jobFailureCollector;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        if (isJobReacquired(commandContext)) {
            // skip failed listener if job has been already re-acquired
            LOG.debugFailedJobListenerSkipped(jobFailureCollector.getJobId());
            return null;
        }

        initTotalRetries(commandContext);

        logJobFailure(commandContext);

        FailedJobCommandFactory failedJobCommandFactory = commandContext.getFailedJobCommandFactory();
        String jobId = jobFailureCollector.getJobId();
        Command<Object> cmd = failedJobCommandFactory.getCommand(jobId, jobFailureCollector.getFailure());
        boolean retriesExhausted = commandExecutor.execute(new FailedJobListenerCmd(jobId, cmd));

        // Persist the JOB_FAIL business event only once, on the attempt that actually exhausts
        // the job's retries (i.e. the job permanently fails), not on every individual retry
        // attempt - a new FailedJobListener instance is created per execution attempt, so
        // firing unconditionally here would produce one event per retry. Fired in its own,
        // independent transaction so it survives regardless of whether the outer transaction of
        // this attempt ultimately commits or rolls back.
        if (!jobFailedBusinessEventFired && retriesExhausted) {
            fireJobFailedBusinessEventInNewTransaction();
            jobFailedBusinessEventFired = true;
        }

        return null;
    }

    protected boolean isJobReacquired(CommandContext commandContext) {
        // if persisted job's lockExpirationTime differs from the snapshot taken before this job was
        // executed, then it's been already re-acquired by another thread. We compare against the
        // snapshot (not job.getLockExpirationTime()) because the job handler may have mutated the
        // JobEntity's lock fields as a side effect (e.g. rescheduling an ever-living job), and such an
        // in-memory mutation is not undone even if the surrounding transaction rolls back afterwards.
        // Note: lockExpirationTimeBeforeExecution may legitimately be null (e.g. the job was executed
        // directly, without having been locked by a prior job acquisition). In that case, a non-null
        // persisted lock expiration time means the job WAS acquired/re-acquired concurrently, so we
        // must not fall through to treating it as "not reacquired" - doing so would let this listener
        // redundantly decrement retries on a job another thread already committed, corrupting its
        // revision and breaking that other thread's own commit.
        JobEntity persistedJob = commandContext.getJobManager().findJobById(jobFailureCollector.getJobId());
        Date lockExpirationTimeBeforeExecution = jobFailureCollector.getLockExpirationTime();

        if (persistedJob == null || persistedJob.getLockExpirationTime() == null) {
            return false;
        }
        return !persistedJob.getLockExpirationTime().equals(lockExpirationTimeBeforeExecution);
    }

    private void initTotalRetries(CommandContext commandContext) {
        totalRetries = commandContext.getProcessEngineConfiguration().getFailedJobListenerMaxRetries();
    }

    protected void fireHistoricJobFailedEvt(JobEntity job) {
        CommandContext commandContext = Context.getCommandContext();
        commandContext
                .getHistoricJobLogManager()
                .fireJobFailedEvent(job, jobFailureCollector.getFailure());
    }

    protected void fireJobFailedBusinessEventInNewTransaction() {
        // Run in a genuinely new, independent transaction with a fresh JobEntity load. The
        // outer commandContext may already have a stale, cached JobEntity (e.g. loaded by
        // isJobReacquired before the retries were decremented in their own nested transaction),
        // so re-using it here could report an outdated retries count on the event.
        commandExecutorTxRequiresNew.execute(new Command<Void>() {
            @Override
            public Void execute(CommandContext commandContext) {
                JobEntity job = commandContext.getJobManager().findJobById(jobFailureCollector.getJobId());
                if (job != null) {
                    job.setFailedActivityId(jobFailureCollector.getFailedActivityId());
                    fireJobFailedBusinessEvent(job);
                } else {
                    LOG.debugFailedJobNotFound(jobFailureCollector.getJobId());
                }
                return null;
            }
        });
    }

    protected void fireJobFailedBusinessEvent(JobEntity job) {
        BusinessEventProcessor.processBusinessEvents(new BusinessEventProcessor.BusinessEventCreator() {
            @Override
            public BusinessEvent createBusinessEvent(BusinessEventProducer producer) {
                return producer.createJobFailedEvt(job, jobFailureCollector.getFailure());
            }
        });
    }

    protected void logJobFailure(CommandContext commandContext) {
        if (commandContext.getProcessEngineConfiguration().isMetricsEnabled()) {
            commandContext.getProcessEngineConfiguration()
                    .getMetricsRegistry()
                    .markOccurrence(Metrics.JOB_FAILED);
        }
    }

    public void incrementCountRetries() {
        this.countRetries++;
    }

    public int getRetriesLeft() {
        return Math.max(0, totalRetries - countRetries);
    }

    protected class FailedJobListenerCmd implements Command<Boolean> {

        protected String jobId;
        protected Command<Object> cmd;

        public FailedJobListenerCmd(String jobId, Command<Object> cmd) {
            this.jobId = jobId;
            this.cmd = cmd;
        }

        /**
         * @return {@code true} if, after running the retry-decrement command, the job has no
         * retries left (i.e. it has permanently failed), {@code false} otherwise (including when
         * the job could no longer be found).
         */
        @Override
        public Boolean execute(CommandContext commandContext) {
            JobEntity job = commandContext
                    .getJobManager()
                    .findJobById(jobId);

            if (job != null) {
                job.setFailedActivityId(jobFailureCollector.getFailedActivityId());
                // the given job failed and a rollback happened,
                // that's why we have to increment the job
                // sequence counter once again
                job.incrementSequenceCounter();
                fireHistoricJobFailedEvt(job);
                cmd.execute(commandContext);
                return job.getRetries() <= 0;
            } else {
                LOG.debugFailedJobNotFound(jobId);
                return false;
            }
        }
    }

}
