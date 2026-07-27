package org.eximeebpms.bpm.engine.impl.businessevent.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FormPropertyBusinessEventFactoryTest {

  private final FormPropertyBusinessEventFactory factory = new FormPropertyBusinessEventFactory();

  @Mock
  private ExecutionEntity execution;
  @Mock
  private CommandContext commandContext;

  @AfterEach
  void tearDown() {
    ClockUtil.reset();
  }

  @Test
  void shouldBuildFormPropertyUpdateEventMatchingHistoryFields() {
    // given
    final Date now = new Date(10_000L);
    ClockUtil.setCurrentTime(now);

    mockExecutionBase();
    when(execution.isProcessInstanceStarting()).thenReturn(false);
    when(execution.getActivityInstanceId()).thenReturn("activity-instance-id");
    when(execution.getSequenceCounter()).thenReturn(7L);
    when(commandContext.getOperationId()).thenReturn("operation-id");

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);

      // when
      final BusinessEvent businessEvent = factory.createUpdateEvent(execution, "propertyId", "propertyValue", "task-id");

      // then
      assertThat(businessEvent).isInstanceOf(BusinessFormPropertyEventEntity.class);

      final BusinessFormPropertyEventEntity event = (BusinessFormPropertyEventEntity) businessEvent;

      assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.FORM_PROPERTY_UPDATE.getEventName());
      assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.FORM_PROPERTY_UPDATE.getBusinessEventName());
      assertThat(event.getTimestamp()).isEqualTo(now);
      assertThat(event.getExecutionId()).isEqualTo("execution-id");
      assertThat(event.getProcessInstanceId()).isEqualTo("process-instance-id");
      assertThat(event.getPropertyId()).isEqualTo("propertyId");
      assertThat(event.getPropertyValue()).isEqualTo("propertyValue");
      assertThat(event.getTaskId()).isEqualTo("task-id");
      assertThat(event.getTenantId()).isEqualTo("tenant-id");
      assertThat(event.getUserOperationId()).isEqualTo("operation-id");
      assertThat(event.getRootProcessInstanceId()).isEqualTo("root-process-instance-id");
      assertThat(event.getSequenceCounter()).isEqualTo(7L);
      assertThat(event.getActivityInstanceId()).isEqualTo("activity-instance-id");
    }
  }

  @Test
  void shouldUseProcessInstanceIdAsActivityInstanceIdWhenProcessInstanceStarting() {
    // given
    mockExecutionBase();
    when(execution.isProcessInstanceStarting()).thenReturn(true);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);

      // when
      final BusinessEvent businessEvent = factory.createUpdateEvent(execution, "propertyId", "propertyValue", null);

      // then
      final BusinessFormPropertyEventEntity event = (BusinessFormPropertyEventEntity) businessEvent;
      assertThat(event.getActivityInstanceId()).isEqualTo("process-instance-id");
      assertThat(event.getTaskId()).isNull();
    }
  }

  private void mockExecutionBase() {
    when(execution.getId()).thenReturn("execution-id");
    when(execution.getProcessInstanceId()).thenReturn("process-instance-id");
    when(execution.getTenantId()).thenReturn("tenant-id");
    when(execution.getRootProcessInstanceId()).thenReturn("root-process-instance-id");
    when(execution.getProcessDefinitionId()).thenReturn(null);
  }
}
