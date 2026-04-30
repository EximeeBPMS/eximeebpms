package org.eximeebpms.bpm.events.engine.mapper;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoryEvent;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.EventSource;
import org.eximeebpms.bpm.events.EximeeBPMSEventTypes;
import org.eximeebpms.bpm.events.payload.ProcessInstanceStartedEvent;

public class ProcessInstanceStartedEventMapper implements HistoryEventMapper {

  @Override
  public boolean supports(HistoryEvent event) {
    return event instanceof HistoricProcessInstanceEventEntity processEvent
        && processEvent.getStartTime() != null
        && processEvent.getEndTime() == null;
  }

  @Override
  public EventEnvelope map(HistoryEvent event) {
    final HistoricProcessInstanceEventEntity processEvent =
        (HistoricProcessInstanceEventEntity) event;

    final ProcessInstanceStartedEvent payload =
        ProcessInstanceStartedEvent.builder()
            .businessKey(processEvent.getBusinessKey())
            .startUserId(processEvent.getStartUserId())
            .superProcessInstanceId(processEvent.getSuperProcessInstanceId())
            .superCaseInstanceId(processEvent.getSuperCaseInstanceId())
            .deleteReason(processEvent.getDeleteReason())
            .endActivityId(processEvent.getEndActivityId())
            .startActivityId(processEvent.getStartActivityId())
            .tenantId(processEvent.getTenantId())
            .state(processEvent.getState())
            .durationInMillis(processEvent.getDurationInMillis())
            .startTime(processEvent.getStartTime())
            .endTime(processEvent.getEndTime())
            .id(processEvent.getId())
            .rootProcessInstanceId(processEvent.getRootProcessInstanceId())
            .processInstanceId(processEvent.getProcessInstanceId())
            .executionId(processEvent.getExecutionId())
            .processDefinitionId(processEvent.getProcessDefinitionId())
            .processDefinitionKey(processEvent.getProcessDefinitionKey())
            .processDefinitionName(processEvent.getProcessDefinitionName())
            .processDefinitionVersion(processEvent.getProcessDefinitionVersion())
            .caseInstanceId(processEvent.getCaseInstanceId())
            .caseExecutionId(processEvent.getCaseExecutionId())
            .caseDefinitionId(processEvent.getCaseDefinitionId())
            .caseDefinitionKey(processEvent.getCaseDefinitionKey())
            .caseDefinitionName(processEvent.getCaseDefinitionName())
            .eventType(processEvent.getEventType())
            .sequenceCounter(processEvent.getSequenceCounter())
            .removalTime(processEvent.getRemovalTime())
            .build();

    return EventEnvelope.builder()
        .id(UUID.randomUUID().toString())
        .type(EximeeBPMSEventTypes.PROCESS_INSTANCE_STARTED)
        .source(EventSource.HISTORY.value())
        .occurredAt(toInstant(processEvent.getStartTime()))
        .tenantId(processEvent.getTenantId())
        .rootProcessInstanceId(processEvent.getRootProcessInstanceId())
        .processInstanceId(processEvent.getProcessInstanceId())
        .executionId(processEvent.getExecutionId())
        .processDefinitionId(processEvent.getProcessDefinitionId())
        .processDefinitionKey(processEvent.getProcessDefinitionKey())
        .payload(payload)
        .header("origin", "bpms")
        .header("processKey", processKey(processEvent))
        .header("processName", processEvent.getProcessDefinitionKey())
        .header("noProcessContext", Boolean.toString(noProcessContext(processEvent)))
        .build();
  }

  private Instant toInstant(Date date) {
    return date == null ? Instant.now() : date.toInstant();
  }

  private String processKey(HistoricProcessInstanceEventEntity event) {
    return event.getRootProcessInstanceId() != null
        ? event.getRootProcessInstanceId()
        : event.getProcessInstanceId();
  }

  private boolean noProcessContext(HistoricProcessInstanceEventEntity event) {
    return event.getProcessInstanceId() == null
        && event.getRootProcessInstanceId() == null;
  }
}
