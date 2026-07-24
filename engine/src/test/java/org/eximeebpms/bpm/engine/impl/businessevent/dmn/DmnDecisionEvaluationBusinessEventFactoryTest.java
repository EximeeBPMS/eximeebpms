package org.eximeebpms.bpm.engine.impl.businessevent.dmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnDecisionEvaluationEvent;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnDecisionLogicEvaluationEvent;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnEvaluatedDecisionRule;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnEvaluatedInput;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnEvaluatedOutput;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.variable.Variables;
import org.eximeebpms.bpm.engine.variable.value.StringValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DmnDecisionEvaluationBusinessEventFactoryTest {

  private final DmnDecisionEvaluationBusinessEventFactory factory = new DmnDecisionEvaluationBusinessEventFactory();

  @Mock
  private DmnDecisionEvaluationEvent evaluationEvent;

  @Mock
  private DmnDecisionLogicEvaluationEvent rootDecisionResult;

  @Mock
  private DecisionDefinitionEntity rootDecision;

  @Mock
  private ExecutionEntity execution;

  @Mock
  private CommandContext commandContext;

  @Test
  void shouldBuildEventForProcessExecutionWithoutRequiredDecisions() {
    // given
    mockRootDecision();
    when(evaluationEvent.getRequiredDecisionResults()).thenReturn(Collections.emptyList());

    when(execution.getRootProcessInstanceId()).thenReturn("root-process-instance-id");
    when(execution.getProcessInstanceId()).thenReturn("process-instance-id");
    when(execution.getId()).thenReturn("execution-id");
    when(execution.getActivityId()).thenReturn("activity-id");
    when(execution.getActivityInstanceId()).thenReturn("activity-instance-id");
    when(execution.getTenantId()).thenReturn("tenant-id");

    // when
    DmnDecisionEvaluationBusinessEvent event = factory.createEvent(execution, evaluationEvent);

    // then
    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.DMN_DECISION_EVALUATE.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.DMN_DECISION_EVALUATE.getBusinessEventName());

    DmnDecisionInstanceEvaluation rootInstance = event.getRootDecisionInstance();
    assertThat(rootInstance.getDecisionDefinitionId()).isEqualTo("root-decision-id");
    assertThat(rootInstance.getDecisionDefinitionKey()).isEqualTo("root-decision-key");
    assertThat(rootInstance.getDecisionDefinitionName()).isEqualTo("Root Decision");
    assertThat(rootInstance.getDecisionRequirementsDefinitionId()).isEqualTo("drd-id");
    assertThat(rootInstance.getDecisionRequirementsDefinitionKey()).isEqualTo("drd-key");
    assertThat(rootInstance.getEvaluationTime()).isNotNull();

    assertThat(event.getRequiredDecisionInstances()).isEmpty();

    assertThat(event.getRootProcessInstanceId()).isEqualTo("root-process-instance-id");
    assertThat(event.getProcessInstanceId()).isEqualTo("process-instance-id");
    assertThat(event.getExecutionId()).isEqualTo("execution-id");
    assertThat(event.getActivityId()).isEqualTo("activity-id");
    assertThat(event.getActivityInstanceId()).isEqualTo("activity-instance-id");
    assertThat(event.getTenantId()).isEqualTo("tenant-id");
    assertThat(event.getUserId()).isNull();
  }

  @Test
  void shouldBuildEventForStandaloneEvaluation() {
    // given
    mockRootDecision();
    when(evaluationEvent.getRequiredDecisionResults()).thenReturn(Collections.emptyList());
    when(rootDecision.getTenantId()).thenReturn("decision-tenant-id");
    when(commandContext.getAuthenticatedUserId()).thenReturn("user-id");

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);

      // when
      DmnDecisionEvaluationBusinessEvent event = factory.createEvent(evaluationEvent);

      // then
      assertThat(event.getUserId()).isEqualTo("user-id");
      assertThat(event.getTenantId()).isEqualTo("decision-tenant-id");
      assertThat(event.getProcessInstanceId()).isNull();
      assertThat(event.getExecutionId()).isNull();
      assertThat(event.getActivityId()).isNull();
    }
  }

  @Test
  void shouldBuildEventWithRequiredDecisionInstances() {
    // given
    mockRootDecision();

    DmnDecisionLogicEvaluationEvent requiredDecisionResult = mock(DmnDecisionLogicEvaluationEvent.class);
    DecisionDefinitionEntity requiredDecision = mock(DecisionDefinitionEntity.class);
    when(requiredDecisionResult.getDecision()).thenReturn(requiredDecision);
    when(requiredDecision.getId()).thenReturn("required-decision-id");
    when(requiredDecision.getKey()).thenReturn("required-decision-key");
    when(requiredDecision.getName()).thenReturn("Required Decision");

    when(evaluationEvent.getRequiredDecisionResults()).thenReturn(Collections.singletonList(requiredDecisionResult));

    // when
    DmnDecisionEvaluationBusinessEvent event = factory.createEvent(evaluationEvent);

    // then
    assertThat(event.getRootDecisionInstance().getDecisionDefinitionKey()).isEqualTo("root-decision-key");

    assertThat(event.getRequiredDecisionInstances()).hasSize(1);
    DmnDecisionInstanceEvaluation requiredInstance = event.getRequiredDecisionInstances().get(0);
    assertThat(requiredInstance.getDecisionDefinitionId()).isEqualTo("required-decision-id");
    assertThat(requiredInstance.getDecisionDefinitionKey()).isEqualTo("required-decision-key");
    assertThat(requiredInstance.getDecisionDefinitionName()).isEqualTo("Required Decision");
    assertThat(requiredInstance.getEvaluationTime()).isNotNull();
  }

  @Test
  void shouldMapDecisionTableInputsAndOutputs() {
    // given
    DmnDecisionTableEvaluationEvent tableEvaluationEvent = mock(DmnDecisionTableEvaluationEvent.class);
    when(evaluationEvent.getDecisionResult()).thenReturn(tableEvaluationEvent);
    when(evaluationEvent.getRequiredDecisionResults()).thenReturn(Collections.emptyList());
    when(tableEvaluationEvent.getDecision()).thenReturn(rootDecision);

    StringValue inputValue = Variables.stringValue("foo");
    DmnEvaluatedInput input = mock(DmnEvaluatedInput.class);
    when(input.getId()).thenReturn("in");
    when(input.getName()).thenReturn("input label");
    when(input.getValue()).thenReturn(inputValue);
    when(tableEvaluationEvent.getInputs()).thenReturn(Collections.singletonList(input));

    StringValue outputValue = Variables.stringValue("foo");
    DmnEvaluatedOutput output = mock(DmnEvaluatedOutput.class);
    when(output.getId()).thenReturn("out");
    when(output.getName()).thenReturn("output label");
    when(output.getOutputName()).thenReturn("result");
    when(output.getValue()).thenReturn(outputValue);

    Map<String, DmnEvaluatedOutput> outputEntries = new LinkedHashMap<>();
    outputEntries.put("result", output);

    DmnEvaluatedDecisionRule rule = mock(DmnEvaluatedDecisionRule.class);
    when(rule.getId()).thenReturn("rule-1");
    when(rule.getOutputEntries()).thenReturn(outputEntries);
    when(tableEvaluationEvent.getMatchingRules()).thenReturn(Collections.singletonList(rule));

    // when
    DmnDecisionEvaluationBusinessEvent event = factory.createEvent(evaluationEvent);

    // then
    DmnDecisionInstanceEvaluation rootInstance = event.getRootDecisionInstance();

    assertThat(rootInstance.getInputs()).hasSize(1);
    DmnDecisionInputEvaluation mappedInput = rootInstance.getInputs().get(0);
    assertThat(mappedInput.getClauseId()).isEqualTo("in");
    assertThat(mappedInput.getClauseName()).isEqualTo("input label");
    assertThat(mappedInput.getValue()).isEqualTo("foo");
    assertThat(mappedInput.getTypeName()).isEqualTo(inputValue.getType().getName());

    assertThat(rootInstance.getOutputs()).hasSize(1);
    DmnDecisionOutputEvaluation mappedOutput = rootInstance.getOutputs().get(0);
    assertThat(mappedOutput.getClauseId()).isEqualTo("out");
    assertThat(mappedOutput.getClauseName()).isEqualTo("output label");
    assertThat(mappedOutput.getRuleId()).isEqualTo("rule-1");
    assertThat(mappedOutput.getRuleOrder()).isEqualTo(1);
    assertThat(mappedOutput.getVariableName()).isEqualTo("result");
    assertThat(mappedOutput.getValue()).isEqualTo("foo");
    assertThat(mappedOutput.getTypeName()).isEqualTo(outputValue.getType().getName());
  }

  private void mockRootDecision() {
    when(evaluationEvent.getDecisionResult()).thenReturn(rootDecisionResult);
    when(rootDecisionResult.getDecision()).thenReturn(rootDecision);
    when(rootDecision.getId()).thenReturn("root-decision-id");
    when(rootDecision.getKey()).thenReturn("root-decision-key");
    when(rootDecision.getName()).thenReturn("Root Decision");
    when(rootDecision.getDecisionRequirementsDefinitionId()).thenReturn("drd-id");
    when(rootDecision.getDecisionRequirementsDefinitionKey()).thenReturn("drd-key");
  }

}
