package org.eximeebpms.bpm.engine.impl.businessevent.dmn;

import org.eximeebpms.bpm.dmn.engine.DmnDecision;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnDecisionEvaluationEvent;
import org.eximeebpms.bpm.dmn.engine.delegate.DmnDecisionEvaluationListener;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProcessor;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProducer;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.context.CoreExecutionContext;
import org.eximeebpms.bpm.engine.impl.core.instance.CoreExecution;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.repository.DecisionDefinition;

public class BusinessEventDecisionEvaluationListener implements DmnDecisionEvaluationListener {

  @Override
  public void notify(final DmnDecisionEvaluationEvent evaluationEvent) {
    BusinessEventProcessor.processBusinessEvents(new BusinessEventProcessor.BusinessEventCreator() {

      @Override
      public BusinessEvent createBusinessEvent(BusinessEventProducer producer) {
        if (!isDeployedDecisionTable(evaluationEvent.getDecisionResult().getDecision())) {
          return null;
        }

        ExecutionEntity execution = resolveExecution();

        return execution != null
            ? producer.createDecisionEvaluationEvt(execution, evaluationEvent)
            : producer.createDecisionEvaluationEvt(evaluationEvent);
      }
    });
  }

  protected ExecutionEntity resolveExecution() {
    CoreExecutionContext<? extends CoreExecution> executionContext = Context.getCoreExecutionContext();
    if (executionContext != null) {
      CoreExecution coreExecution = executionContext.getExecution();
      if (coreExecution instanceof ExecutionEntity) {
        return (ExecutionEntity) coreExecution;
      }
    }
    return null;
  }

  protected boolean isDeployedDecisionTable(DmnDecision decision) {
    if (decision instanceof DecisionDefinition) {
      return ((DecisionDefinition) decision).getId() != null;
    } else {
      return false;
    }
  }

}
