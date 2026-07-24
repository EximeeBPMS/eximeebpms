package org.eximeebpms.bpm.engine.impl.businessevent.dmn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmnDecisionInstanceEvaluation implements Serializable {

  private String decisionDefinitionId;
  private String decisionDefinitionKey;
  private String decisionDefinitionName;

  private String decisionRequirementsDefinitionId;
  private String decisionRequirementsDefinitionKey;

  private Date evaluationTime;

  @Builder.Default
  private List<DmnDecisionInputEvaluation> inputs = new ArrayList<>();

  @Builder.Default
  private List<DmnDecisionOutputEvaluation> outputs = new ArrayList<>();

}
