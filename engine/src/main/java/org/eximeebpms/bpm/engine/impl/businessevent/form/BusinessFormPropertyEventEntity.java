package org.eximeebpms.bpm.engine.impl.businessevent.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessDetailEventEntity;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessFormPropertyEventEntity extends BusinessDetailEventEntity {

  protected String propertyId;
  protected String propertyValue;

  @Override
  protected boolean canEqual(Object other) {
    return other instanceof BusinessFormPropertyEventEntity;
  }

}
