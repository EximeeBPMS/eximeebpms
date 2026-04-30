package org.eximeebpms.bpm.events.engine;

import java.util.Date;
import lombok.Builder;
import lombok.Value;
import org.eximeebpms.bpm.events.EventTypes;

@Value
@Builder
public class ProcessInstanceStartedEvent {

  public static final String NAME = EventTypes.PREFIX + "process-instance:started";

  String businessKey;
  String startUserId;
  String superProcessInstanceId;
  String superCaseInstanceId;
  String deleteReason;
  String endActivityId;
  String startActivityId;
  String tenantId;
  String state;
  Long durationInMillis;
  Date startTime;
  Date endTime;
  String id;
  String rootProcessInstanceId;
  String processInstanceId;
  String executionId;
  String processDefinitionId;
  String processDefinitionKey;
  String processDefinitionName;
  Integer processDefinitionVersion;
  String caseInstanceId;
  String caseExecutionId;
  String caseDefinitionId;
  String caseDefinitionKey;
  String caseDefinitionName;
  String eventType;
  long sequenceCounter;
  Date removalTime;
}
