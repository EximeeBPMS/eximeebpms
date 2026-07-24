package org.eximeebpms.bpm.engine.impl.businessevent.dmn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmnDecisionOutputEvaluation implements Serializable {

  private String clauseId;
  private String clauseName;

  private String ruleId;
  private Integer ruleOrder;

  private String variableName;

  private String typeName;
  private Object value;

}
