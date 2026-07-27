package org.eximeebpms.bpm.engine.test.history.useroperationlog.businessevent;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import java.util.List;
import java.util.stream.Collectors;
import org.eximeebpms.bpm.engine.HistoryService;
import org.eximeebpms.bpm.engine.IdentityService;
import org.eximeebpms.bpm.engine.ProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.TaskService;
import org.eximeebpms.bpm.engine.businessevent.BusinessEventDispatcher;
import org.eximeebpms.bpm.engine.history.UserOperationLogEntry;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventConfiguration;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.businessevent.useroperationlog.UserOperationLogBusinessEvent;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;
import org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup.BusinessEventOutboxCleanupJobHandler;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.engine.test.Deployment;
import org.eximeebpms.bpm.engine.test.RequiredHistoryLevel;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class UserOperationLogBusinessEventTest {

  private static final String ONE_TASK_PROCESS = "org/eximeebpms/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml";

  protected BusinessEventConfiguration businessEventConfiguration = BusinessEventConfiguration.builder()
      .enabled(true)
      .build();

  protected ProcessEngineBootstrapRule bootstrapRule =
      new ProcessEngineBootstrapRule(config -> config.setBusinessEventConfiguration(businessEventConfiguration));

  protected ProvidedProcessEngineRule rule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(rule);

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected CommandExecutor commandExecutor;
  protected Gson gson = new Gson();

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
    historyService = rule.getHistoryService();
    identityService = rule.getIdentityService();
    commandExecutor = rule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    identityService.setAuthenticatedUserId("test-user");
    stopAutoDispatcher();
  }

  @After
  public void cleanUp() {
    identityService.clearAuthentication();
    // deleting the cleanup job itself fires a job:delete business event, so it must run
    // before clearing the outbox, not after
    deleteCleanupJob();
    deleteBusinessEventOutboxEntities();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = ONE_TASK_PROCESS)
  public void shouldPublishUserOperationLogBusinessEventForStartProcessInstance() {
    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // then the history reflects the operation
    UserOperationLogEntry historicEntry = historyService.createUserOperationLogQuery().singleResult();
    assertThat(historicEntry).isNotNull();
    assertThat(historicEntry.getOperationType()).isEqualTo(UserOperationLogEntry.OPERATION_TYPE_CREATE);
    assertThat(historicEntry.getProcessInstanceId()).isEqualTo(processInstance.getId());

    // and a matching business event was published to the outbox
    List<UserOperationLogBusinessEvent> events = findUserOperationLogEvents();
    assertThat(events).isNotEmpty();

    UserOperationLogBusinessEvent event = events.getFirst();
    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.USER_OPERATION_LOG.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.USER_OPERATION_LOG.getBusinessEventName());
    assertThat(event.getOperationId()).isEqualTo(historicEntry.getOperationId());
    assertThat(event.getOperationType()).isEqualTo(historicEntry.getOperationType());
    assertThat(event.getEntityType()).isEqualTo(historicEntry.getEntityType());
    assertThat(event.getUserId()).isEqualTo(historicEntry.getUserId());
    assertThat(event.getProcessInstanceId()).isEqualTo(historicEntry.getProcessInstanceId());

    // complete the task so no running instance remains for deployment cleanup to cascade-delete
    // (which would otherwise fire an extra, untracked business event after this method returns)
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
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

  private List<UserOperationLogBusinessEvent> findUserOperationLogEvents() {
    return rule.getProcessEngine().getBusinessEventService()
        .createBusinessEventOutboxQuery()
        .eventType(BusinessEventTypes.USER_OPERATION_LOG.getBusinessEventName())
        .list()
        .stream()
        .map(BusinessEventOutboxEntity.class::cast)
        .map(entry -> gson.fromJson(entry.getBusinessEvent(), UserOperationLogBusinessEvent.class))
        .toList();
  }

}
