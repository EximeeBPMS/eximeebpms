package org.eximeebpms.bpm.engine.test.api.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.Map;
import java.util.Set;

import org.eximeebpms.bpm.engine.HistoryService;
import org.eximeebpms.bpm.engine.ExternalTaskService;
import org.eximeebpms.bpm.engine.ManagementService;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.TaskService;
import org.eximeebpms.bpm.engine.externaltask.LockedExternalTask;
import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.engine.test.api.runtime.FailingDelegate;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineTestRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.eximeebpms.bpm.model.bpmn.Bpmn;
import org.eximeebpms.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * End-to-end coverage for {@code historyExcludedProcessDefinitionKeys}: a real process engine
 * (H2, full command interceptor stack) is configured with one process definition key excluded
 * from history, and another one not excluded. Runs both processes to completion and asserts
 * directly against the history tables via {@link HistoryService} — no history at all for the
 * excluded process definition, normal history for the non-excluded one.
 */
public class HistoryExcludedProcessDefinitionKeysTest {

  protected static final String EXCLUDED_PROCESS_KEY = "excludedProcess";
  protected static final String INCLUDED_PROCESS_KEY = "includedProcess";

  // Deliberately does not call configuration.setHistory(...): the shared test database already
  // has a history level baked into its schema (see HistoryLevelSetupCommand /
  // "Note that when using the default history backend, the history level is stored in the
  // database and cannot be changed later" in history-configuration.md) — this test only needs
  // history to be enabled at all, which any ambient non-none level already satisfies.
  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setHistoryExcludedProcessDefinitionKeys(Set.of(EXCLUDED_PROCESS_KEY)));

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ManagementService managementService;
  protected ExternalTaskService externalTaskService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
    externalTaskService = engineRule.getExternalTaskService();

    testRule.deploy(oneTaskProcess(EXCLUDED_PROCESS_KEY));
    testRule.deploy(oneTaskProcess(INCLUDED_PROCESS_KEY));
  }

  protected static BpmnModelInstance oneTaskProcess(String processDefinitionKey) {
    return Bpmn.createExecutableProcess(processDefinitionKey)
        .startEvent()
        .userTask("userTask")
        .endEvent()
        .done();
  }

  protected static BpmnModelInstance failingAsyncServiceTaskProcess(String processDefinitionKey) {
    return Bpmn.createExecutableProcess(processDefinitionKey)
        .startEvent()
        .serviceTask("failingTask")
          .camundaAsyncBefore()
          .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
  }

  protected static BpmnModelInstance externalTaskProcess(String processDefinitionKey) {
    return Bpmn.createExecutableProcess(processDefinitionKey)
        .startEvent()
        .serviceTask("externalTask").camundaExternalTask("topic")
        .endEvent()
        .done();
  }

  /** Robust against a configured table prefix. */
  protected long byteArrayCount() {
    return managementService.getTableCount().entrySet().stream()
        .filter(entry -> entry.getKey().endsWith("ACT_GE_BYTEARRAY"))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("ACT_GE_BYTEARRAY not reported by getTableCount()"));
  }

  protected String runToCompletion(String processDefinitionKey) {
    String processInstanceId = runtimeService.startProcessInstanceByKey(processDefinitionKey).getId();
    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    taskService.complete(task.getId());
    return processInstanceId;
  }

  @Test
  public void shouldNotRecordAnyHistoryForExcludedProcessDefinition() {
    // when
    String processInstanceId = runToCompletion(EXCLUDED_PROCESS_KEY);

    // then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery()
        .processDefinitionKey(EXCLUDED_PROCESS_KEY).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery()
        .processInstanceId(processInstanceId).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery()
        .processDefinitionKey(EXCLUDED_PROCESS_KEY).count());
  }

  @Test
  public void shouldRecordHistoryForNonExcludedProcessDefinition() {
    // when
    String processInstanceId = runToCompletion(INCLUDED_PROCESS_KEY);

    // then
    assertEquals(1, historyService.createHistoricProcessInstanceQuery()
        .processDefinitionKey(INCLUDED_PROCESS_KEY).finished().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery()
        .processDefinitionKey(INCLUDED_PROCESS_KEY).finished().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery()
        .processInstanceId(processInstanceId).activityId("userTask").count());
  }

  /**
   * Regression guard for BPMS-662: a job failure on an excluded process definition used to
   * leave an unreferenced {@code job.exceptionByteArray} row behind, because the producer
   * inserted it before this handler chain decided not to persist the event. Only the runtime
   * job's own stacktrace byte array — the one referenced from {@code ACT_RU_JOB} — may appear.
   */
  @Test
  public void shouldNotLeaveOrphanedExceptionByteArrayWhenJobLogIsExcluded() {
    // given
    testRule.deploy(failingAsyncServiceTaskProcess(EXCLUDED_PROCESS_KEY));
    long byteArraysBefore = byteArrayCount();
    runtimeService.startProcessInstanceByKey(EXCLUDED_PROCESS_KEY);
    String jobId = managementService.createJobQuery().singleResult().getId();

    // when
    assertThrows(RuntimeException.class, () -> managementService.executeJob(jobId));

    // then — exactly one new byte array, the runtime one; no history byte array, no job log
    assertEquals(byteArraysBefore + 1, byteArrayCount());
    assertEquals(0, historyService.createHistoricJobLogQuery().count());
  }

  /**
   * Same defect as {@link #shouldNotLeaveOrphanedExceptionByteArrayWhenJobLogIsExcluded},
   * for the other producer-side insert: external-task error details (BPMS-662). Only the
   * runtime external task's own byte array may appear.
   */
  @Test
  public void shouldNotLeaveOrphanedErrorDetailsByteArrayWhenExternalTaskLogIsExcluded() {
    // given
    testRule.deploy(externalTaskProcess(EXCLUDED_PROCESS_KEY));
    runtimeService.startProcessInstanceByKey(EXCLUDED_PROCESS_KEY);
    LockedExternalTask externalTask = externalTaskService.fetchAndLock(1, "worker")
        .topic("topic", 10000L).execute().get(0);
    long byteArraysBefore = byteArrayCount();

    // when
    externalTaskService.handleFailure(externalTask.getId(), "worker", "errorMessage", "errorDetails", 0, 0L);

    // then
    assertEquals(byteArraysBefore + 1, byteArrayCount());
    assertEquals(0, historyService.createHistoricExternalTaskLogQuery().count());
  }

  @Test
  public void shouldRecordHistoryOnlyForNonExcludedProcessDefinitionWhenBothRun() {
    // when
    runToCompletion(EXCLUDED_PROCESS_KEY);
    runToCompletion(INCLUDED_PROCESS_KEY);

    // then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery()
        .processDefinitionKey(EXCLUDED_PROCESS_KEY).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery()
        .processDefinitionKey(INCLUDED_PROCESS_KEY).finished().count());
  }

}
