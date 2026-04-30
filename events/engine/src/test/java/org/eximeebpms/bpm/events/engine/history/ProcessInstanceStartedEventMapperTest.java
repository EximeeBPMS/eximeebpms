package org.eximeebpms.bpm.events.engine.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.EventSource;
import org.eximeebpms.bpm.events.EximeeBPMSEventTypes;
import org.eximeebpms.bpm.events.engine.mapper.ProcessInstanceStartedEventMapper;
import org.eximeebpms.bpm.events.payload.ProcessInstanceStartedEvent;
import org.junit.jupiter.api.Test;

class ProcessInstanceStartedEventMapperTest {

  private final ProcessInstanceStartedEventMapper mapper = new ProcessInstanceStartedEventMapper();

  @Test
  void shouldMapProcessInstanceStartedEvent() {
    // given
    final Date startTime = new Date();
    final HistoricProcessInstanceEventEntity historyEvent = getEntity(startTime);

    // when
    EventEnvelope envelope = mapper.map(historyEvent);

    // then
    assertThat(envelope.getId()).isNotBlank();
    assertThat(envelope.getType()).isEqualTo(EximeeBPMSEventTypes.PROCESS_INSTANCE_STARTED);
    assertThat(envelope.getSource()).isEqualTo(EventSource.HISTORY.value());
    assertThat(envelope.getOccurredAt()).isEqualTo(startTime.toInstant());

    assertThat(envelope.getTenantId()).isEqualTo("tenant-1");
    assertThat(envelope.getRootProcessInstanceId()).isEqualTo("root-process-instance-id");
    assertThat(envelope.getProcessInstanceId()).isEqualTo("process-instance-id");
    assertThat(envelope.getExecutionId()).isEqualTo("execution-id");
    assertThat(envelope.getProcessDefinitionId()).isEqualTo("process-definition-id");
    assertThat(envelope.getProcessDefinitionKey()).isEqualTo("process-definition-key");

    assertThat(envelope.getHeaders())
        .containsEntry("origin", "bpms")
        .containsEntry("processKey", "root-process-instance-id")
        .containsEntry("processName", "process-definition-key")
        .containsEntry("noProcessContext", "false");

    assertThat(envelope.getPayload())
        .isInstanceOf(ProcessInstanceStartedEvent.class);

    ProcessInstanceStartedEvent payload = (ProcessInstanceStartedEvent) envelope.getPayload();

    assertThat(payload.getBusinessKey()).isEqualTo("business-key-1");
    assertThat(payload.getStartUserId()).isEqualTo("demo");
    assertThat(payload.getStartActivityId()).isEqualTo("StartEvent_1");
    assertThat(payload.getTenantId()).isEqualTo("tenant-1");
    assertThat(payload.getStartTime()).isEqualTo(startTime);
    assertThat(payload.getRootProcessInstanceId()).isEqualTo("root-process-instance-id");
    assertThat(payload.getProcessInstanceId()).isEqualTo("process-instance-id");
    assertThat(payload.getProcessDefinitionKey()).isEqualTo("process-definition-key");
    assertThat(payload.getProcessDefinitionVersion()).isEqualTo(3);
    assertThat(payload.getEventType()).isEqualTo("create");
    assertThat(payload.getSequenceCounter()).isEqualTo(42L);
  }

  @Test
  void shouldSupportProcessInstanceStartedEvent() {
    // given
    final HistoricProcessInstanceEventEntity historyEvent = new HistoricProcessInstanceEventEntity();
    historyEvent.setStartTime(new Date());
    historyEvent.setEndTime(null);

    // when / then
    assertThat(mapper.supports(historyEvent)).isTrue();
  }

  @Test
  void shouldNotSupportEndedProcessInstanceEvent() {
    // given
    final HistoricProcessInstanceEventEntity historyEvent = new HistoricProcessInstanceEventEntity();
    historyEvent.setStartTime(new Date());
    historyEvent.setEndTime(new Date());

    // when / then
    assertThat(mapper.supports(historyEvent)).isFalse();
  }

  private static HistoricProcessInstanceEventEntity getEntity(final Date startTime) {
    final HistoricProcessInstanceEventEntity historyEvent = new HistoricProcessInstanceEventEntity();
    historyEvent.setId("history-event-id");
    historyEvent.setBusinessKey("business-key-1");
    historyEvent.setStartUserId("demo");
    historyEvent.setStartActivityId("StartEvent_1");
    historyEvent.setTenantId("tenant-1");
    historyEvent.setStartTime(startTime);
    historyEvent.setRootProcessInstanceId("root-process-instance-id");
    historyEvent.setProcessInstanceId("process-instance-id");
    historyEvent.setExecutionId("execution-id");
    historyEvent.setProcessDefinitionId("process-definition-id");
    historyEvent.setProcessDefinitionKey("process-definition-key");
    historyEvent.setProcessDefinitionName("Process definition name");
    historyEvent.setProcessDefinitionVersion(3);
    historyEvent.setEventType("create");
    historyEvent.setSequenceCounter(42L);
    return historyEvent;
  }
}
