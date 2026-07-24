package org.eximeebpms.bpm.engine.impl.businessevent.dmn;

import org.eximeebpms.bpm.dmn.engine.delegate.DmnDecisionEvaluationEvent;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnDecisionLiteralExpressionEvaluationEvent;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnDecisionLogicEvaluationEvent;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnEvaluatedDecisionRule;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnEvaluatedInput;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnEvaluatedOutput;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.eximeebpms.bpm.engine.repository.DecisionDefinition;
import org.eximeebpms.bpm.engine.variable.value.TypedValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DmnDecisionEvaluationBusinessEventFactory extends BusinessEventFactorySupport {

  public DmnDecisionEvaluationBusinessEvent createEvent(DmnDecisionEvaluationEvent evaluationEvent) {
    return createEvent(null, evaluationEvent);
  }

  public DmnDecisionEvaluationBusinessEvent createEvent(ExecutionEntity execution, DmnDecisionEvaluationEvent evaluationEvent) {
    DecisionDefinition rootDecision = (DecisionDefinition) evaluationEvent.getDecisionResult().getDecision();

    DmnDecisionEvaluationBusinessEvent event = new DmnDecisionEvaluationBusinessEvent();
    event.setEventType(BusinessEventTypes.DMN_DECISION_EVALUATE.getEventName());
    event.setBusinessEventType(BusinessEventTypes.DMN_DECISION_EVALUATE.getBusinessEventName());

    event.setRootDecisionInstance(createDecisionInstance(evaluationEvent.getDecisionResult()));

    List<DmnDecisionInstanceEvaluation> requiredDecisionInstances = new ArrayList<>();
    for (DmnDecisionLogicEvaluationEvent requiredDecisionResult : evaluationEvent.getRequiredDecisionResults()) {
      requiredDecisionInstances.add(createDecisionInstance(requiredDecisionResult));
    }
    event.setRequiredDecisionInstances(requiredDecisionInstances);

    if (execution != null) {
      fillProcessDefinitionData(event, execution);
      event.setRootProcessInstanceId(execution.getRootProcessInstanceId());
      event.setProcessInstanceId(execution.getProcessInstanceId());
      event.setExecutionId(execution.getId());
      event.setActivityId(execution.getActivityId());
      event.setActivityInstanceId(execution.getActivityInstanceId());
      event.setTenantId(execution.getTenantId() != null ? execution.getTenantId() : rootDecision.getTenantId());
    } else {
      event.setUserId(Optional.ofNullable(Context.getCommandContext())
          .map(CommandContext::getAuthenticatedUserId)
          .orElse(null));
      event.setTenantId(rootDecision.getTenantId());
    }

    return event;
  }

  protected DmnDecisionInstanceEvaluation createDecisionInstance(DmnDecisionLogicEvaluationEvent decisionResult) {
    DecisionDefinition decision = (DecisionDefinition) decisionResult.getDecision();

    return DmnDecisionInstanceEvaluation.builder()
        .decisionDefinitionId(decision.getId())
        .decisionDefinitionKey(decision.getKey())
        .decisionDefinitionName(decision.getName())
        .decisionRequirementsDefinitionId(decision.getDecisionRequirementsDefinitionId())
        .decisionRequirementsDefinitionKey(decision.getDecisionRequirementsDefinitionKey())
        .evaluationTime(ClockUtil.getCurrentTime())
        .inputs(createInputs(decisionResult))
        .outputs(createOutputs(decisionResult))
        .build();
  }

  protected List<DmnDecisionInputEvaluation> createInputs(DmnDecisionLogicEvaluationEvent decisionResult) {
    if (decisionResult instanceof DmnDecisionTableEvaluationEvent tableEvaluationEvent) {
      List<DmnDecisionInputEvaluation> inputs = new ArrayList<>();
      for (DmnEvaluatedInput input : tableEvaluationEvent.getInputs()) {
        inputs.add(toInputEvaluation(input));
      }
      return inputs;
    }
    return Collections.emptyList();
  }

  protected DmnDecisionInputEvaluation toInputEvaluation(DmnEvaluatedInput input) {
    TypedValue typedValue = input.getValue();

    return DmnDecisionInputEvaluation.builder()
        .clauseId(input.getId())
        .clauseName(input.getName())
        .typeName(typeName(typedValue))
        .value(typedValue != null ? typedValue.getValue() : null)
        .build();
  }

  protected List<DmnDecisionOutputEvaluation> createOutputs(DmnDecisionLogicEvaluationEvent decisionResult) {
    if (decisionResult instanceof DmnDecisionTableEvaluationEvent tableEvaluationEvent) {
      List<DmnDecisionOutputEvaluation> outputs = new ArrayList<>();

      List<DmnEvaluatedDecisionRule> matchingRules = tableEvaluationEvent.getMatchingRules();
      for (int index = 0; index < matchingRules.size(); index++) {
        DmnEvaluatedDecisionRule rule = matchingRules.get(index);
        String ruleId = rule.getId();
        int ruleOrder = index + 1;

        for (DmnEvaluatedOutput output : rule.getOutputEntries().values()) {
          outputs.add(toOutputEvaluation(output, ruleId, ruleOrder));
        }
      }
      return outputs;

    } else if (decisionResult instanceof DmnDecisionLiteralExpressionEvaluationEvent literalExpressionEvent) {
      TypedValue typedValue = literalExpressionEvent.getOutputValue();

      DmnDecisionOutputEvaluation output = DmnDecisionOutputEvaluation.builder()
          .variableName(literalExpressionEvent.getOutputName())
          .typeName(typeName(typedValue))
          .value(typedValue != null ? typedValue.getValue() : null)
          .build();
      return Collections.singletonList(output);
    }

    return Collections.emptyList();
  }

  protected DmnDecisionOutputEvaluation toOutputEvaluation(DmnEvaluatedOutput output, String ruleId, int ruleOrder) {
    TypedValue typedValue = output.getValue();

    return DmnDecisionOutputEvaluation.builder()
        .clauseId(output.getId())
        .clauseName(output.getName())
        .ruleId(ruleId)
        .ruleOrder(ruleOrder)
        .variableName(output.getOutputName())
        .typeName(typeName(typedValue))
        .value(typedValue != null ? typedValue.getValue() : null)
        .build();
  }

  protected String typeName(TypedValue typedValue) {
    return typedValue != null && typedValue.getType() != null ? typedValue.getType().getName() : null;
  }

}
