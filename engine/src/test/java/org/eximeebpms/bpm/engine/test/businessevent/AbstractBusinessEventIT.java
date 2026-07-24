package org.eximeebpms.bpm.engine.test.businessevent;

import org.eximeebpms.bpm.engine.businessevent.BusinessEventDispatcher;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventConfiguration;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;
import org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup.BusinessEventOutboxCleanupJobHandler;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineTestRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.RuleChain;

public abstract class AbstractBusinessEventIT {
    protected BusinessEventConfiguration businessEventConfiguration = BusinessEventConfiguration.builder()
            .enabled(true)
                        .build();
    protected ProcessEngineBootstrapRule bootstrapRule =
            new ProcessEngineBootstrapRule(config -> {
                config.setBusinessEventConfiguration(businessEventConfiguration);
            });

    protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
    protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

    @Rule
    public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule)
            .around(engineRule)
            .around(testRule);

    protected CommandExecutor commandExecutor;

    @Before
    public void setUp() {
        commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
        stopAutoDispatcher();
    }

    @After
    public void cleanUp() {
        deleteCleanupJob();
        deleteBusinessEventOutboxEntities();
        ClockUtil.reset();
    }

    /**
     * Stops the engine-managed background dispatcher so it does not race with the
     * manually-created dispatchers used in sub-class tests.
     * Called before any outbox records are inserted, so the final-drain inside
     * {@link BusinessEventDispatcher#stop()} finds an empty table and is harmless.
     */
    private void stopAutoDispatcher() {
        BusinessEventDispatcher dispatcher =
                engineRule.getProcessEngineConfiguration().getBusinessEventDispatcher();
        if (dispatcher != null && dispatcher.isRunning()) {
            dispatcher.stop();
        }
    }

    private void deleteBusinessEventOutboxEntities() {
        commandExecutor.execute(ctx -> {
            ctx.getDbEntityManager().delete(
                    BusinessEventOutboxEntity.class, "deleteAllBusinessEventOutbox", null);
            return null;
        });
    }

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
}
