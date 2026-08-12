package org.eximeebpms.bpm.engine.test.api.history;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.eximeebpms.bpm.engine.HistoryService;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.TaskService;
import org.eximeebpms.bpm.engine.task.Task;
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

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    historyService = engineRule.getHistoryService();

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
