package org.eximeebpms.bpm.engine.impl.businessevent.dmn;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Fired when a decision is evaluated. Bundles the decision that was
 * directly evaluated ({@link #rootDecisionInstance}) together with any
 * required (sub-)decisions from the same decision requirements diagram
 * ({@link #requiredDecisionInstances}) into a single event, mirroring
 * {@code HistoricDecisionEvaluationEvent}.</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DmnDecisionEvaluationBusinessEvent extends BusinessEvent {

  protected DmnDecisionInstanceEvaluation rootDecisionInstance;

  @Builder.Default
  protected List<DmnDecisionInstanceEvaluation> requiredDecisionInstances = new ArrayList<>();

  protected String tenantId;
  protected String userId;

  protected String activityId;
  protected String activityInstanceId;

}
