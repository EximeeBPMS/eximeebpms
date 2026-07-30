package org.eximeebpms.bpm.engine.test.businessevent;

import org.eximeebpms.bpm.engine.ManagementService;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup.BusinessEventOutboxCleanupJobHandler;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.eximeebpms.bpm.engine.runtime.Job;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link BusinessEventOutboxCleanupJobHandler}.
 *
 * <p>Tests run against an in-memory H2 database and validate that:
 * <ul>
 *   <li>the cleanup job is bootstrapped automatically when {@code businessEventUsed=true}</li>
 *   <li>processed entries older than the configured retention are deleted</li>
 *   <li>processed entries newer than the retention are kept</li>
 *   <li>unprocessed entries are never deleted regardless of age</li>
 *   <li>the job reschedules itself one hour after each execution</li>
 * </ul>
 */
public class BusinessEventOutboxCleanupJobIT extends AbstractBusinessEventIT {

    /**
     * Short retention (1 second) to make "expired" records easy to create in tests.
     */
    private static final long RETENTION_MS = 1_000L;

    private ManagementService managementService;

    /**
     * Returns a {@link Date} that is {@code millisAgo} milliseconds in the past.
     */
    private static Date pastDate(long millisAgo) {
        return new Date(System.currentTimeMillis() - millisAgo);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Before
    public void before() {
        ProcessEngineConfigurationImpl configuration = engineRule.getProcessEngineConfiguration();
        configuration.getBusinessEventConfiguration().setOutboxRetentionMs(RETENTION_MS);
        commandExecutor = configuration.getCommandExecutorTxRequired();
        managementService = engineRule.getManagementService();
    }

    /**
     * GIVEN engine started with {@code businessEventUsed=true}
     * WHEN  startup completes
     * THEN  exactly one job with handler type {@value BusinessEventOutboxCleanupJobHandler#TYPE}
     * must exist in the job table
     */
    @Test
    public void shouldBootstrapCleanupJobOnEngineStart() {
        List<Job> cleanupJobs = findCleanupJobs();

        assertThat(cleanupJobs)
                .as("exactly one business-event outbox cleanup job should be bootstrapped")
                .hasSize(1);
    }

    /**
     * GIVEN 3 processed outbox records whose {@code processedDate} is older than the retention window
     * AND 2 processed outbox records processed within the retention window
     * WHEN  the cleanup job is executed
     * THEN  the 3 expired records are deleted
     * AND the 2 recent records remain in the table
     */
    @Test
    public void shouldDeleteProcessedEntriesOlderThanRetention() {
        // Insert 3 processed records that are already "expired"
        Date expiredDate = pastDate(RETENTION_MS + 500);
        insertProcessedRecord("EVENT_EXPIRED_1", expiredDate);
        insertProcessedRecord("EVENT_EXPIRED_2", expiredDate);
        insertProcessedRecord("EVENT_EXPIRED_3", expiredDate);

        // Insert 2 processed records within the retention window (processedDate = now)
        Date recentDate = new Date();
        insertProcessedRecord("EVENT_RECENT_1", recentDate);
        insertProcessedRecord("EVENT_RECENT_2", recentDate);

        assertThat(countAllOutboxRecords()).isEqualTo(5);

        // Execute the cleanup job
        executeCleanupJob();

        // Only the 2 recent records should remain
        assertThat(countAllOutboxRecords())
                .as("only records within the retention window should remain")
                .isEqualTo(2);

        List<BusinessEventOutboxEntity> remaining = findAllOutboxRecords();
        assertThat(remaining)
                .extracting(BusinessEventOutboxEntity::getEventType)
                .as("recent processed records should NOT be deleted")
                .containsExactlyInAnyOrder("EVENT_RECENT_1", "EVENT_RECENT_2");
    }

    /**
     * GIVEN unprocessed outbox records that are very old (older than the retention window)
     * WHEN  the cleanup job is executed
     * THEN  unprocessed records must NOT be deleted (they still need to be relayed)
     */
    @Test
    public void shouldNotDeleteUnprocessedEntries() {
        Date veryOldDate = pastDate(7L * 24 * 60 * 60 * 1000); // 7 days ago
        insertUnprocessedRecord("EVENT_UNPROCESSED_OLD_1", veryOldDate);
        insertUnprocessedRecord("EVENT_UNPROCESSED_OLD_2", veryOldDate);

        assertThat(countAllOutboxRecords()).isEqualTo(2);

        executeCleanupJob();

        assertThat(countAllOutboxRecords())
                .as("unprocessed records must never be deleted by the cleanup job")
                .isEqualTo(2);
    }

    /**
     * GIVEN   no outbox records
     * WHEN    the cleanup job is executed
     * THEN    no error is thrown and the job reschedules itself ~1 hour into the future
     */
    @Test
    public void shouldRescheduleJobOneHourAfterExecution() {
        Date beforeExecution = ClockUtil.getCurrentTime();

        executeCleanupJob();

        Job rescheduledJob = findCleanupJobs().get(0);
        Date dueDate = rescheduledJob.getDuedate();

        long expectedMinDue = beforeExecution.getTime() + BusinessEventOutboxCleanupJobHandler.DEFAULT_CLEANUP_INTERVAL_MILLIS;
        assertThat(dueDate.getTime())
                .as("cleanup job should be rescheduled approximately one hour after execution")
                .isGreaterThanOrEqualTo(expectedMinDue);
    }

    // -------------------------------------------------------------------------
    // Helpers – data setup
    // -------------------------------------------------------------------------

    /**
     * GIVEN  a mix of expired-processed, recent-processed and unprocessed records
     * WHEN   the cleanup job is executed
     * THEN   only expired-processed records are removed; all others remain intact
     */
    @Test
    public void shouldDeleteOnlyExpiredProcessedRecords_mixedScenario() {
        Date expiredDate = pastDate(RETENTION_MS + 500);
        Date recentDate = new Date();

        insertProcessedRecord("EXPIRED_PROC_1", expiredDate);
        insertProcessedRecord("EXPIRED_PROC_2", expiredDate);
        insertProcessedRecord("RECENT_PROC_1", recentDate);
        insertUnprocessedRecord("UNPROC_OLD_1", expiredDate);  // old but unprocessed – must stay
        insertUnprocessedRecord("UNPROC_NEW_1", recentDate);   // new and unprocessed – must stay

        assertThat(countAllOutboxRecords()).isEqualTo(5);

        executeCleanupJob();

        List<BusinessEventOutboxEntity> remaining = findAllOutboxRecords();
        assertThat(remaining)
                .extracting(BusinessEventOutboxEntity::getEventType)
                .as("only expired processed records should be removed")
                .containsExactlyInAnyOrder("RECENT_PROC_1", "UNPROC_OLD_1", "UNPROC_NEW_1");
    }

    /**
     * Inserts an outbox record that is already marked as processed with the given {@code processedDate}.
     */
    private void insertProcessedRecord(String eventType, Date processedDate) {
        commandExecutor.execute(ctx -> {
            BusinessEventOutboxEntity entity = BusinessEventOutboxEntity.builder()
                    .createdDate(processedDate)
                    .eventType(eventType)
                    .businessEvent("{\"eventType\":\"" + eventType + "\"}")
                    .processed(true)
                    .processedDate(processedDate)
                    .build();
            ctx.getDbEntityManager().insertWithoutId(entity);
            return null;
        });
    }

    // -------------------------------------------------------------------------
    // Helpers – job management
    // -------------------------------------------------------------------------

    /**
     * Inserts an unprocessed outbox record with the given {@code createdDate}.
     */
    private void insertUnprocessedRecord(String eventType, Date createdDate) {
        commandExecutor.execute(ctx -> {
            BusinessEventOutboxEntity entity = BusinessEventOutboxEntity.builder()
                    .createdDate(createdDate)
                    .eventType(eventType)
                    .businessEvent("{\"eventType\":\"" + eventType + "\"}")
                    .processed(false)
                    .build();
            ctx.getDbEntityManager().insertWithoutId(entity);
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
                (List<Job>) ctx.getJobManager()
                        .findJobsByHandlerType(BusinessEventOutboxCleanupJobHandler.TYPE)
        );
    }

    // -------------------------------------------------------------------------
    // Helpers – DB queries
    // -------------------------------------------------------------------------

    private void deleteCleanupJob() {
        commandExecutor.execute(ctx -> {
            ctx.getJobManager()
                    .findJobsByHandlerType(BusinessEventOutboxCleanupJobHandler.TYPE)
                    .forEach(job -> {
                        ctx.getJobManager().deleteJob((JobEntity) job);
                        ctx.getHistoricJobLogManager().deleteHistoricJobLogByJobId(job.getId());
                    });
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private List<BusinessEventOutboxEntity> findAllOutboxRecords() {
        return commandExecutor.execute(ctx -> {
            org.eximeebpms.bpm.engine.impl.BusinessEventQueryImpl query =
                    new org.eximeebpms.bpm.engine.impl.BusinessEventQueryImpl(commandExecutor);
            return (List<BusinessEventOutboxEntity>) ctx.getDbEntityManager().selectList(
                    "selectBusinessEventOutboxEntityByQueryCriteria",
                    query,
                    new org.eximeebpms.bpm.engine.impl.Page(0, Integer.MAX_VALUE)
            );
        });
    }

    private int countAllOutboxRecords() {
        return findAllOutboxRecords().size();
    }
}








