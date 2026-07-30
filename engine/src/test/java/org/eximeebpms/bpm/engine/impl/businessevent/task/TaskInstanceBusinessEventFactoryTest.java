package org.eximeebpms.bpm.engine.impl.businessevent.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.TaskEntity;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskInstanceBusinessEventFactoryTest {

  private final TaskInstanceBusinessEventFactory factory = new TaskInstanceBusinessEventFactory();

  @Mock
  private TaskEntity task;
  @Mock
  private ExecutionEntity execution;
  @Mock
  private CommandContext commandContext;
  @Mock
  private DbEntityManager dbEntityManager;
  @Mock
  private HistoricTaskInstanceEventEntity historicTask;


  @AfterEach
  void tearDown() {
    ClockUtil.reset();
  }

  @Test
  void shouldBuildTaskInstanceCompleteEventWithDurationWhenHistoricStartTimeExists() {
    // given
    final Date startTime = new Date(1_000L);
    final Date endTime = new Date(6_000L);

    mockTaskBase();

    when(historicTask.getStartTime()).thenReturn(startTime);
    when(dbEntityManager.selectById(HistoricTaskInstanceEventEntity.class, "task-id")).thenReturn(historicTask);
    when(commandContext.getDbEntityManager()).thenReturn(dbEntityManager);

    ClockUtil.setCurrentTime(endTime);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);

      // when
      final BusinessEvent businessEvent = factory.createCompleteEvent(task);

      // then
      assertThat(businessEvent).isInstanceOf(BusinessTaskInstanceEventEntity.class);

      final BusinessTaskInstanceEventEntity event = (BusinessTaskInstanceEventEntity) businessEvent;

      assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.TASK_INSTANCE_COMPLETE.getEventName());
      assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.TASK_INSTANCE_COMPLETE.getBusinessEventName());
      assertThat(event.getTaskId()).isEqualTo("task-id");
      assertThat(event.getStartTime()).isEqualTo(startTime);
      assertThat(event.getEndTime()).isEqualTo(endTime);
      assertThat(event.getDurationInMillis()).isEqualTo(5_000L);
      assertThat(event.getDeleteReason()).isNull();
    }
  }

  @Test
  void shouldBuildTaskInstanceDeleteEventWithDurationWhenHistoricStartTimeExists() {
    // given
    final Date startTime = new Date(1_000L);
    final Date endTime = new Date(6_000L);

    mockTaskBase();
    when(task.getDeleteReason()).thenReturn("deleted");
    when(historicTask.getStartTime()).thenReturn(startTime);
    when(dbEntityManager.selectById(HistoricTaskInstanceEventEntity.class, "task-id")).thenReturn(historicTask);
    when(commandContext.getDbEntityManager()).thenReturn(dbEntityManager);

    ClockUtil.setCurrentTime(endTime);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);

      // when
      final BusinessEvent businessEvent = factory.createDeleteEvent(task);

      // then
      assertThat(businessEvent).isInstanceOf(BusinessTaskInstanceEventEntity.class);

      final BusinessTaskInstanceEventEntity event = (BusinessTaskInstanceEventEntity) businessEvent;

      assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.TASK_INSTANCE_DELETE.getEventName());
      assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.TASK_INSTANCE_DELETE.getBusinessEventName());
      assertThat(event.getTaskId()).isEqualTo("task-id");
      assertThat(event.getStartTime()).isEqualTo(startTime);
      assertThat(event.getEndTime()).isEqualTo(endTime);
      assertThat(event.getDurationInMillis()).isEqualTo(5_000L);
      assertThat(event.getDeleteReason()).isEqualTo("deleted");
    }
  }

  @Test
  void shouldBuildTaskInstanceCompleteEventWithoutDurationWhenHistoricTaskDoesNotExist() {
    // given
    final Date endTime = new Date(6_000L);

    mockTaskBase();
    when(dbEntityManager.selectById(HistoricTaskInstanceEventEntity.class, "task-id")).thenReturn(null);
    when(commandContext.getDbEntityManager()).thenReturn(dbEntityManager);

    ClockUtil.setCurrentTime(endTime);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);

      // when
      final BusinessEvent businessEvent = factory.createCompleteEvent(task);

      // then
      final BusinessTaskInstanceEventEntity event = (BusinessTaskInstanceEventEntity) businessEvent;

      assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.TASK_INSTANCE_COMPLETE.getEventName());
      assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.TASK_INSTANCE_COMPLETE.getBusinessEventName());
      assertThat(event.getStartTime()).isNull();
      assertThat(event.getEndTime()).isEqualTo(endTime);
      assertThat(event.getDurationInMillis()).isNull();
    }
  }

  @Test
  void shouldFillExecutionDataWhenTaskHasExecution() {
    // given
    mockTaskWithExecution();

    // when
    final BusinessEvent businessEvent = factory.createUpdateEvent(task);

    // then
    assertThat(businessEvent).isInstanceOf(BusinessTaskInstanceEventEntity.class);

    final BusinessTaskInstanceEventEntity event = (BusinessTaskInstanceEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.TASK_INSTANCE_UPDATE.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.TASK_INSTANCE_UPDATE.getBusinessEventName());
    assertThat(event.getActivityInstanceId()).isEqualTo("activity-instance-id");
    assertThat(event.getRootProcessInstanceId()).isEqualTo("root-process-instance-id");
    assertThat(event.getSequenceCounter()).isEqualTo(7L);
  }

  private void mockTaskBase() {
    when(task.getId()).thenReturn("task-id");
    when(task.getProcessInstanceId()).thenReturn("process-instance-id");
    when(task.getExecutionId()).thenReturn("execution-id");
    when(task.getPriority()).thenReturn(50);
  }

  private void mockTaskWithExecution() {
    mockTaskBase();

    when(task.getExecution()).thenReturn(execution);
    when(execution.getActivityInstanceId()).thenReturn("activity-instance-id");
    when(execution.getRootProcessInstanceId()).thenReturn("root-process-instance-id");
    when(execution.getSequenceCounter()).thenReturn(7L);
  }
}
