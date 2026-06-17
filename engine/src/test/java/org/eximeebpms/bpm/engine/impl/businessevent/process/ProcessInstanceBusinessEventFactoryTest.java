package org.eximeebpms.bpm.engine.impl.businessevent.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.pvm.process.ActivityImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessInstanceBusinessEventFactoryTest {

  private final ProcessInstanceBusinessEventFactory factory = new ProcessInstanceBusinessEventFactory();

  @Mock
  private ExecutionEntity execution;

  @Mock
  private DelegateExecution delegateExecution;

  @Mock
  private ActivityImpl activity;

  @Test
  void shouldBuildProcessInstanceStartEvent() {
    // given
    mockBaseExecution();
    when(execution.getActivityId()).thenReturn("start-event");

    // when
    final BusinessEvent businessEvent = factory.createStartEvent(execution);

    // then
    assertThat(businessEvent).isInstanceOf(BusinessProcessInstanceEventEntity.class);

    final BusinessProcessInstanceEventEntity event = (BusinessProcessInstanceEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_START.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_START.getBusinessEventName());
    assertThat(event.getId()).isEqualTo("process-instance-id");
    assertThat(event.getProcessInstanceId()).isEqualTo("process-instance-id");
    assertThat(event.getExecutionId()).isEqualTo("execution-id");
    assertThat(event.getRootProcessInstanceId()).isEqualTo("root-process-instance-id");
    assertThat(event.getBusinessKey()).isEqualTo("business-key");
    assertThat(event.getTenantId()).isEqualTo("tenant-id");
    assertThat(event.getStartActivityId()).isEqualTo("start-event");
    assertThat(event.getState()).isEqualTo(BusinessProcessInstanceState.ACTIVE.getValue());
    assertThat(event.getSequenceCounter()).isEqualTo(7L);

    assertThat(event.getTimestamp()).isNotNull();
    assertThat(event.getStartTime()).isNotNull();

    assertThat(event.getEndTime()).isNull();
    assertThat(event.getDeleteReason()).isNull();
    assertThat(event.getEndActivityId()).isNull();
    assertThat(event.getDurationInMillis()).isNull();
  }

  @Test
  void shouldBuildCompletedProcessInstanceEndEventWhenActivityExists() {
    // given
    mockBaseExecution();
    when(execution.getActivityId()).thenReturn("end-event");
    when(execution.getActivity()).thenReturn(activity);
    when(execution.getDeleteReason()).thenReturn(null);

    // when
    final BusinessEvent businessEvent = factory.createEndEvent(execution);

    // then
    assertThat(businessEvent).isInstanceOf(BusinessProcessInstanceEventEntity.class);

    final BusinessProcessInstanceEventEntity event = (BusinessProcessInstanceEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_END.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_END.getBusinessEventName());
    assertThat(event.getId()).isEqualTo("process-instance-id");
    assertThat(event.getProcessInstanceId()).isEqualTo("process-instance-id");
    assertThat(event.getExecutionId()).isEqualTo("execution-id");
    assertThat(event.getEndActivityId()).isEqualTo("end-event");
    assertThat(event.getState()).isEqualTo(BusinessProcessInstanceState.COMPLETED.getValue());

    assertThat(event.getDeleteReason()).isNull();
    assertThat(event.getTimestamp()).isNotNull();
    assertThat(event.getEndTime()).isNotNull();

    // startTime jest uzupełniane tylko jeśli istnieje historic process instance w DB.
    assertThat(event.getStartTime()).isNull();
    assertThat(event.getDurationInMillis()).isNull();
  }

  @Test
  void shouldBuildInternallyTerminatedProcessInstanceEndEventWhenActivityDoesNotExistAndExecutionIsNotExternallyTerminated() {
    // given
    mockBaseExecution();
    when(execution.getActivityId()).thenReturn("end-event");
    when(execution.getActivity()).thenReturn(null);
    when(execution.isExternallyTerminated()).thenReturn(false);
    when(execution.getDeleteReason()).thenReturn("deleted by user");

    // when
    final BusinessEvent businessEvent = factory.createEndEvent(execution);

    // then
    assertThat(businessEvent).isInstanceOf(BusinessProcessInstanceEventEntity.class);

    final BusinessProcessInstanceEventEntity event = (BusinessProcessInstanceEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_END.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_END.getBusinessEventName());
    assertThat(event.getState()).isEqualTo(BusinessProcessInstanceState.INTERNALLY_TERMINATED.getValue());
    assertThat(event.getDeleteReason()).isEqualTo("deleted by user");
    assertThat(event.getEndActivityId()).isEqualTo("end-event");
    assertThat(event.getEndTime()).isNotNull();
  }

  @Test
  void shouldBuildExternallyTerminatedProcessInstanceEndEventWhenExecutionIsExternallyTerminated() {
    // given
    mockBaseExecution();
    when(execution.getActivityId()).thenReturn("end-event");
    when(execution.getActivity()).thenReturn(null);
    when(execution.isExternallyTerminated()).thenReturn(true);
    when(execution.getDeleteReason()).thenReturn("externally deleted");

    // when
    final BusinessEvent businessEvent = factory.createEndEvent(execution);

    // then
    assertThat(businessEvent).isInstanceOf(BusinessProcessInstanceEventEntity.class);

    final BusinessProcessInstanceEventEntity event = (BusinessProcessInstanceEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_END.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_END.getBusinessEventName());
    assertThat(event.getState()).isEqualTo(BusinessProcessInstanceState.EXTERNALLY_TERMINATED.getValue());
    assertThat(event.getDeleteReason()).isEqualTo("externally deleted");
    assertThat(event.getEndActivityId()).isEqualTo("end-event");
    assertThat(event.getEndTime()).isNotNull();
  }

  @Test
  void shouldBuildActiveProcessInstanceUpdateEvent() {
    // given
    mockBaseExecution();
    when(execution.isSuspended()).thenReturn(false);

    // when
    final BusinessEvent businessEvent = factory.createUpdateEvent(execution);

    // then
    assertThat(businessEvent).isInstanceOf(BusinessProcessInstanceEventEntity.class);

    final BusinessProcessInstanceEventEntity event = (BusinessProcessInstanceEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_UPDATE.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_UPDATE.getBusinessEventName());
    assertThat(event.getId()).isEqualTo("process-instance-id");
    assertThat(event.getProcessInstanceId()).isEqualTo("process-instance-id");
    assertThat(event.getExecutionId()).isEqualTo("execution-id");
    assertThat(event.getState()).isEqualTo(BusinessProcessInstanceState.ACTIVE.getValue());

    assertThat(event.getTimestamp()).isNotNull();
    assertThat(event.getDeleteReason()).isNull();
    assertThat(event.getStartTime()).isNull();
    assertThat(event.getEndTime()).isNull();
  }

  @Test
  void shouldBuildSuspendedProcessInstanceUpdateEvent() {
    // given
    mockBaseExecution();
    when(execution.isSuspended()).thenReturn(true);

    // when
    final BusinessEvent businessEvent = factory.createUpdateEvent(execution);

    // then
    assertThat(businessEvent).isInstanceOf(BusinessProcessInstanceEventEntity.class);

    final BusinessProcessInstanceEventEntity event = (BusinessProcessInstanceEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_UPDATE.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.PROCESS_INSTANCE_UPDATE.getBusinessEventName());
    assertThat(event.getState()).isEqualTo(BusinessProcessInstanceState.SUSPENDED.getValue());
  }

  @Test
  void shouldReturnNullWhenDelegateExecutionIsNotExecutionEntity() {
    // given // when // then
    assertThat(factory.createStartEvent(delegateExecution)).isNull();
    assertThat(factory.createEndEvent(delegateExecution)).isNull();
    assertThat(factory.createUpdateEvent(delegateExecution)).isNull();
  }

  private void mockBaseExecution() {
    when(execution.getId()).thenReturn("execution-id");
    when(execution.getProcessInstanceId()).thenReturn("process-instance-id");
    when(execution.getRootProcessInstanceId()).thenReturn("root-process-instance-id");
    when(execution.getProcessBusinessKey()).thenReturn("business-key");
    when(execution.getTenantId()).thenReturn("tenant-id");
    when(execution.getSequenceCounter()).thenReturn(7L);
    when(execution.getSuperExecution()).thenReturn(null);
  }
}
