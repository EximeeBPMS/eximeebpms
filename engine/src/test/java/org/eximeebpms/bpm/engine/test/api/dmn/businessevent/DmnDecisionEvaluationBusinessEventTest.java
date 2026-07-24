package org.eximeebpms.bpm.engine.test.api.dmn.businessevent;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.GsonBuilder;
import org.eximeebpms.bpm.engine.DecisionService;
import org.eximeebpms.bpm.engine.IdentityService;
import org.eximeebpms.bpm.engine.RuntimeService;
import org.eximeebpms.bpm.engine.businessevent.BusinessEventDispatcher;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventConfiguration;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.businessevent.dmn.DmnDecisionEvaluationBusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.dmn.DmnDecisionInputEvaluation;
import org.eximeebpms.bpm.engine.impl.businessevent.dmn.DmnDecisionInstanceEvaluation;
import org.eximeebpms.bpm.engine.impl.businessevent.dmn.DmnDecisionOutputEvaluation;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;
import org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup.BusinessEventOutboxCleanupJobHandler;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.test.Deployment;
import org.eximeebpms.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.eximeebpms.bpm.engine.test.util.ResetDmnConfigUtil;
import org.eximeebpms.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class DmnDecisionEvaluationBusinessEventTest {

  private static final String DECISION_PROCESS =
      "org/eximeebpms/bpm/engine/test/history/HistoricDecisionInstanceTest.processWithBusinessRuleTask.bpmn20.xml";
  private static final String DECISION_SINGLE_OUTPUT_DMN =
      "org/eximeebpms/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml";
  private static final String DRG_DMN =
      "org/eximeebpms/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  private static final String DECISION_DEFINITION_KEY = "testDecision";

  protected BusinessEventConfiguration businessEventConfiguration = BusinessEventConfiguration.builder()
      .enabled(true)
      .build();

  protected ProcessEngineBootstrapRule bootstrapRule =
      new ProcessEngineBootstrapRule(config -> config.setBusinessEventConfiguration(businessEventConfiguration));

  protected ProvidedProcessEngineRule rule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(rule);

  protected RuntimeService runtimeService;
  protected DecisionService decisionService;
  protected IdentityService identityService;
  protected CommandExecutor commandExecutor;
  protected Gson gson = new GsonBuilder()
          .setDateFormat(org.eximeebpms.bpm.engine.impl.businessevent.DbBusinessEventHandler.ISO_DATE_TIME)
          .create();

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
    decisionService = rule.getDecisionService();
    identityService = rule.getIdentityService();
    commandExecutor = rule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    stopAutoDispatcher();

    ResetDmnConfigUtil.reset(rule.getProcessEngineConfiguration().getDmnEngineConfiguration())
        .enableFeelLegacyBehavior(true)
        .init();
  }

  @After
  public void cleanUp() {
    ResetDmnConfigUtil.reset(rule.getProcessEngineConfiguration().getDmnEngineConfiguration())
        .enableFeelLegacyBehavior(false)
        .init();

    deleteCleanupJob();
    deleteBusinessEventOutboxEntities();
  }

  @Test
  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void shouldPublishDecisionEvaluateBusinessEventForBusinessRuleTaskInProcess() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("input1", "foo");

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", variables);

    // then
    List<DmnDecisionEvaluationBusinessEvent> events = findDecisionEvaluationEvents();
    assertThat(events).hasSize(1);

    DmnDecisionEvaluationBusinessEvent event = events.get(0);
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.DMN_DECISION_EVALUATE.getBusinessEventName());
    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.DMN_DECISION_EVALUATE.getEventName());
    DmnDecisionInstanceEvaluation rootInstance = event.getRootDecisionInstance();
    assertThat(rootInstance.getDecisionDefinitionKey()).isEqualTo(DECISION_DEFINITION_KEY);
    assertThat(event.getRequiredDecisionInstances()).isEmpty();
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isNotNull();
    assertThat(event.getActivityId()).isEqualTo("task");
    assertThat(event.getUserId()).isNull();

    assertThat(rootInstance.getInputs()).hasSize(1);
    DmnDecisionInputEvaluation input = rootInstance.getInputs().get(0);
    assertThat(input.getValue()).isEqualTo("foo");

    assertThat(rootInstance.getOutputs()).hasSize(1);
    DmnDecisionOutputEvaluation output = rootInstance.getOutputs().get(0);
    assertThat(output.getVariableName()).isEqualTo("result");
    assertThat(output.getValue()).isEqualTo("foo");
    assertThat(output.getRuleOrder()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = DECISION_SINGLE_OUTPUT_DMN)
  public void shouldPublishDecisionEvaluateBusinessEventForStandaloneEvaluation() {
    // given
    identityService.setAuthenticatedUserId("test-user");

    try {
      // when
      decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
          .variables(Variables.createVariables().putValue("input1", "bar"))
          .evaluate();
    } finally {
      identityService.clearAuthentication();
    }

    // then
    List<DmnDecisionEvaluationBusinessEvent> events = findDecisionEvaluationEvents();
    assertThat(events).hasSize(1);

    DmnDecisionEvaluationBusinessEvent event = events.get(0);
    DmnDecisionInstanceEvaluation rootInstance = event.getRootDecisionInstance();
    assertThat(rootInstance.getDecisionDefinitionKey()).isEqualTo(DECISION_DEFINITION_KEY);
    assertThat(event.getRequiredDecisionInstances()).isEmpty();
    assertThat(event.getProcessInstanceId()).isNull();
    assertThat(event.getExecutionId()).isNull();
    assertThat(event.getUserId()).isEqualTo("test-user");

    assertThat(rootInstance.getInputs()).hasSize(1);
    assertThat(rootInstance.getInputs().get(0).getValue()).isEqualTo("bar");

    assertThat(rootInstance.getOutputs()).hasSize(1);
    assertThat(rootInstance.getOutputs().get(0).getValue()).isEqualTo("bar");
  }

  @Test
  @Deployment(resources = DRG_DMN)
  public void shouldPublishSingleDecisionEvaluateBusinessEventWithRootAndRequiredDecisions() {
    // when
    decisionService.evaluateDecisionTableByKey("dish-decision")
        .variables(Variables.createVariables().putValue("temperature", 21).putValue("dayType", "Weekend"))
        .evaluate();

    // then
    List<DmnDecisionEvaluationBusinessEvent> events = findDecisionEvaluationEvents();
    assertThat(events).hasSize(1);

    DmnDecisionEvaluationBusinessEvent event = events.get(0);
    DmnDecisionInstanceEvaluation rootInstance = event.getRootDecisionInstance();
    assertThat(rootInstance.getDecisionDefinitionKey()).isEqualTo("dish-decision");
    assertThat(rootInstance.getInputs()).isNotEmpty();
    assertThat(rootInstance.getOutputs()).isNotEmpty();

    assertThat(event.getRequiredDecisionInstances())
        .extracting(DmnDecisionInstanceEvaluation::getDecisionDefinitionKey)
        .containsExactlyInAnyOrder("season", "guestCount");
    assertThat(event.getRequiredDecisionInstances())
        .allSatisfy(instance -> assertThat(instance.getOutputs()).isNotEmpty());
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

  private List<DmnDecisionEvaluationBusinessEvent> findDecisionEvaluationEvents() {
    return rule.getProcessEngine().getBusinessEventService()
        .createBusinessEventOutboxQuery()
        .eventType(BusinessEventTypes.DMN_DECISION_EVALUATE.getBusinessEventName())
        .list()
        .stream()
        .map(BusinessEventOutboxEntity.class::cast)
        .map(entry -> gson.fromJson(entry.getBusinessEvent(), DmnDecisionEvaluationBusinessEvent.class))
        .collect(Collectors.toList());
  }

}
