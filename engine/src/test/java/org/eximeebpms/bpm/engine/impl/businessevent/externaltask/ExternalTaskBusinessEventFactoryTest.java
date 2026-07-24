package org.eximeebpms.bpm.engine.impl.businessevent.externaltask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.eximeebpms.bpm.engine.history.ExternalTaskState;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalTaskBusinessEventFactoryTest {

  private final ExternalTaskBusinessEventFactory factory = new ExternalTaskBusinessEventFactory();

  @Mock
  private ExternalTaskEntity externalTask;

  @Mock
  private ExecutionEntity execution;

  @BeforeEach
  void setUp() {
    when(externalTask.getId()).thenReturn("external-task-id");
    when(externalTask.getTopicName()).thenReturn("topic-name");
    when(externalTask.getWorkerId()).thenReturn("worker-id");
    when(externalTask.getPriority()).thenReturn(5L);
    when(externalTask.getRetries()).thenReturn(3);
    when(externalTask.getActivityId()).thenReturn("activity-id");
    when(externalTask.getActivityInstanceId()).thenReturn("activity-instance-id");
    when(externalTask.getExecutionId()).thenReturn("execution-id");
    when(externalTask.getProcessInstanceId()).thenReturn("process-instance-id");
    when(externalTask.getTenantId()).thenReturn("tenant-id");
    when(externalTask.getExecution()).thenReturn(execution);
    when(execution.getRootProcessInstanceId()).thenReturn("root-process-instance-id");
  }

  @AfterEach
  void tearDown() {
    ClockUtil.reset();
  }

  @Test
  void shouldBuildExternalTaskCreatedEvent() {
    // given
    final Date createTime = new Date(1_000L);
    when(externalTask.getCreateTime()).thenReturn(createTime);

    // when
    final BusinessEvent businessEvent = factory.createCreatedEvent(externalTask);

    // then
    assertThat(businessEvent).isInstanceOf(ExternalTaskBusinessEvent.class);
    final ExternalTaskBusinessEvent event = (ExternalTaskBusinessEvent) businessEvent;
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.EXTERNAL_TASK_CREATE.getBusinessEventName());
    assertThat(event.getExternalTaskId()).isEqualTo("external-task-id");
    assertThat(event.getTopicName()).isEqualTo("topic-name");
    assertThat(event.getWorkerId()).isEqualTo("worker-id");
    assertThat(event.getPriority()).isEqualTo(5L);
    assertThat(event.getRetries()).isEqualTo(3);
    assertThat(event.getActivityId()).isEqualTo("activity-id");
    assertThat(event.getActivityInstanceId()).isEqualTo("activity-instance-id");
    assertThat(event.getExecutionId()).isEqualTo("execution-id");
    assertThat(event.getProcessInstanceId()).isEqualTo("process-instance-id");
    assertThat(event.getTenantId()).isEqualTo("tenant-id");
    assertThat(event.getRootProcessInstanceId()).isEqualTo("root-process-instance-id");
    assertThat(event.getTimestamp()).isEqualTo(createTime);
    assertThat(event.getState()).isEqualTo(ExternalTaskState.CREATED.getStateCode());
    assertThat(event.getErrorMessage()).isNull();
    assertThat(event.getErrorDetails()).isNull();
  }

  @Test
  void shouldBuildExternalTaskFailedEvent() {
    // given
    final Date now = new Date(2_000L);
    ClockUtil.setCurrentTime(now);
    when(externalTask.getErrorMessage()).thenReturn("error-message");
    when(externalTask.getErrorDetails()).thenReturn("error-details");

    // when
    final BusinessEvent businessEvent = factory.createFailedEvent(externalTask);

    // then
    assertThat(businessEvent).isInstanceOf(ExternalTaskBusinessEvent.class);
    final ExternalTaskBusinessEvent event = (ExternalTaskBusinessEvent) businessEvent;
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.EXTERNAL_TASK_FAIL.getBusinessEventName());
    assertThat(event.getExternalTaskId()).isEqualTo("external-task-id");
    assertThat(event.getTimestamp()).isEqualTo(now);
    assertThat(event.getState()).isEqualTo(ExternalTaskState.FAILED.getStateCode());
    assertThat(event.getErrorMessage()).isEqualTo("error-message");
    assertThat(event.getErrorDetails()).isEqualTo("error-details");
  }

  @Test
  void shouldBuildExternalTaskFailedEventWithoutErrorDetails() {
    // given
    final Date now = new Date(2_500L);
    ClockUtil.setCurrentTime(now);
    when(externalTask.getErrorMessage()).thenReturn("error-message");
    when(externalTask.getErrorDetails()).thenReturn(null);

    // when
    final BusinessEvent businessEvent = factory.createFailedEvent(externalTask);

    // then
    final ExternalTaskBusinessEvent event = (ExternalTaskBusinessEvent) businessEvent;
    assertThat(event.getErrorMessage()).isEqualTo("error-message");
    assertThat(event.getErrorDetails()).isNull();
  }

  @Test
  void shouldBuildExternalTaskSuccessfulEvent() {
    // given
    final Date now = new Date(3_000L);
    ClockUtil.setCurrentTime(now);

    // when
    final BusinessEvent businessEvent = factory.createSuccessfulEvent(externalTask);

    // then
    assertThat(businessEvent).isInstanceOf(ExternalTaskBusinessEvent.class);
    final ExternalTaskBusinessEvent event = (ExternalTaskBusinessEvent) businessEvent;
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.EXTERNAL_TASK_SUCCESS.getBusinessEventName());
    assertThat(event.getExternalTaskId()).isEqualTo("external-task-id");
    assertThat(event.getTimestamp()).isEqualTo(now);
    assertThat(event.getState()).isEqualTo(ExternalTaskState.SUCCESSFUL.getStateCode());
    assertThat(event.getErrorMessage()).isNull();
    assertThat(event.getErrorDetails()).isNull();
  }

  @Test
  void shouldBuildExternalTaskDeletedEvent() {
    // given
    final Date now = new Date(4_000L);
    ClockUtil.setCurrentTime(now);

    // when
    final BusinessEvent businessEvent = factory.createDeletedEvent(externalTask);

    // then
    assertThat(businessEvent).isInstanceOf(ExternalTaskBusinessEvent.class);
    final ExternalTaskBusinessEvent event = (ExternalTaskBusinessEvent) businessEvent;
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.EXTERNAL_TASK_DELETE.getBusinessEventName());
    assertThat(event.getExternalTaskId()).isEqualTo("external-task-id");
    assertThat(event.getTimestamp()).isEqualTo(now);
    assertThat(event.getState()).isEqualTo(ExternalTaskState.DELETED.getStateCode());
  }
}
