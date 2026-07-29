package org.eximeebpms.bpm.engine.impl.businessevent.identitylink;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessIdentityLinkEventEntity extends BusinessEvent {

  protected Date time;

  protected String type;
  protected String userId;
  protected String groupId;
  protected String taskId;

  protected String operationType;
  protected String assignerId;

  protected String tenantId;
}
