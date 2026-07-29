package org.eximeebpms.bpm.engine.impl.businessevent.variable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessDetailEventEntity;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessVariableUpdateEventEntity extends BusinessDetailEventEntity {

  protected int revision;

  protected String variableName;
  protected String variableInstanceId;
  protected String scopeActivityInstanceId;

  protected String serializerName;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;
  protected byte[] byteValue;

  protected String byteArrayId;

  protected Boolean isInitial = false;

  public void setInitial(Boolean initial) {
    isInitial = initial;
  }
}
