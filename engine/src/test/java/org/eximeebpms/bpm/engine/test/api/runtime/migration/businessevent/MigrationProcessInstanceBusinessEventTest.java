package org.eximeebpms.bpm.engine.test.api.runtime.migration.businessevent;

import static org.eximeebpms.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import java.util.Collections;
import java.util.List;
import org.eximeebpms.bpm.engine.HistoryService;
import org.eximeebpms.bpm.engine.ProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.businessevent.BusinessEventDispatcher;
import org.eximeebpms.bpm.engine.history.HistoricProcessInstance;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventConfiguration;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.businessevent.process.BusinessProcessInstanceEventEntity;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;
import org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup.BusinessEventOutboxCleanupJobHandler;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.migration.MigrationPlan;
import org.eximeebpms.bpm.engine.repository.ProcessDefinition;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.test.RequiredHistoryLevel;
import org.eximeebpms.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.eximeebpms.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MigrationProcessInstanceBusinessEventTest {

  protected BusinessEventConfiguration businessEventConfiguration = BusinessEventConfiguration.builder()
      .enabled(true)
      .build();

  protected ProcessEngineBootstrapRule bootstrapRule =
      new ProcessEngineBootstrapRule(config -> config.setBusinessEventConfiguration(businessEventConfiguration));

  protected ProvidedProcessEngineRule rule = new ProvidedProcessEngineRule(bootstrapRule);
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule)
      .around(rule)
      .around(testHelper);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected CommandExecutor commandExecutor;
  protected Gson gson = new Gson();

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
    historyService = rule.getHistoryService();
    commandExecutor = rule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    stopAutoDispatcher();
  }

  @After
  public void cleanUp() {
    deleteBusinessEventOutboxEntities();
    deleteCleanupJob();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void shouldPublishProcessInstanceMigrateBusinessEventMatchingHistory() {
    // given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.ONE_TASK_PROCESS)
        .changeElementId(ProcessModels.PROCESS_KEY, "new" + ProcessModels.PROCESS_KEY));

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceDefinition.getId());

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", "userTask")
        .build();

    // when
    runtimeService.newMigration(migrationPlan)
        .processInstanceIds(Collections.singletonList(processInstance.getId()))
        .execute();

    // then the history reflects the migrated state
    HistoricProcessInstance migratedHistoricInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();
    assertThat(migratedHistoricInstance).isNotNull();
    assertThat(migratedHistoricInstance.getProcessDefinitionKey()).isEqualTo(targetDefinition.getKey());
    assertThat(migratedHistoricInstance.getProcessDefinitionId()).isEqualTo(targetDefinition.getId());

    // and a matching business event was published to the outbox
    BusinessEventOutboxEntity outboxEntry = findSingleOutboxEntry(processInstance.getId());
    assertThat(outboxEntry).isNotNull();
    assertThat(outboxEntry.getEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_MIGRATE.getBusinessEventName());
    assertThat(outboxEntry.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(outboxEntry.getProcessDefinitionKey()).isEqualTo(targetDefinition.getKey());

    BusinessProcessInstanceEventEntity businessEvent = gson.fromJson(outboxEntry.getBusinessEvent(), BusinessProcessInstanceEventEntity.class);
    assertThat(businessEvent.getEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_MIGRATE.getEventName());
    assertThat(businessEvent.getBusinessEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_MIGRATE.getBusinessEventName());

    // the business event fields must be identical to the history event's fields
    assertThat(businessEvent.getProcessInstanceId()).isEqualTo(migratedHistoricInstance.getId());
    assertThat(businessEvent.getProcessDefinitionId()).isEqualTo(migratedHistoricInstance.getProcessDefinitionId());
    assertThat(businessEvent.getProcessDefinitionKey()).isEqualTo(migratedHistoricInstance.getProcessDefinitionKey());
    assertThat(businessEvent.getProcessDefinitionVersion()).isEqualTo(migratedHistoricInstance.getProcessDefinitionVersion());
    assertThat(businessEvent.getProcessDefinitionName()).isEqualTo(migratedHistoricInstance.getProcessDefinitionName());
    assertThat(businessEvent.getBusinessKey()).isEqualTo(migratedHistoricInstance.getBusinessKey());
    assertThat(businessEvent.getTenantId()).isEqualTo(migratedHistoricInstance.getTenantId());
    assertThat(businessEvent.getSuperProcessInstanceId()).isEqualTo(migratedHistoricInstance.getSuperProcessInstanceId());
    assertThat(businessEvent.getStartTime()).isEqualTo(migratedHistoricInstance.getStartTime());
    assertThat(businessEvent.getState()).isEqualTo(migratedHistoricInstance.getState());
  }

  private void stopAutoDispatcher() {
    BusinessEventDispatcher dispatcher = rule.getProcessEngineConfiguration().getBusinessEventDispatcher();
    if (dispatcher != null && dispatcher.isRunning()) {
      dispatcher.stop();
    }
  }

  private void deleteBusinessEventOutboxEntities() {
    commandExecutor.execute(ctx -> {
      ctx.getDbEntityManager().delete(BusinessEventOutboxEntity.class, "deleteAllBusinessEventOutbox", null);
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

  @SuppressWarnings("unchecked")
  private BusinessEventOutboxEntity findSingleOutboxEntry(String processInstanceId) {
    List<BusinessEventOutboxEntity> results = commandExecutor.execute(ctx ->
        ctx.getDbEntityManager().selectList("selectBusinessEventOutboxByProcInstId", processInstanceId));
    return results.stream()
        .filter(entry -> BusinessEventTypes.PROCESS_INSTANCE_MIGRATE.getBusinessEventName().equals(entry.getEventType()))
        .findFirst()
        .orElse(null);
  }
}
