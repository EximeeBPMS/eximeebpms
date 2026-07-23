package org.eximeebpms.bpm.engine.impl.businessevent.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.eximeebpms.bpm.engine.impl.history.HistoryLevel;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.eximeebpms.bpm.engine.impl.pvm.process.ActivityImpl;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityInstanceBusinessEventFactoryTest {

  private final ActivityInstanceBusinessEventFactory factory = new ActivityInstanceBusinessEventFactory();

  @Mock
  private ExecutionEntity execution;
  @Mock
  private ActivityImpl activity;
  @Mock
  private CommandContext commandContext;
  @Mock
  private DbEntityManager dbEntityManager;
  @Mock
  private HistoricActivityInstanceEntity historicActivityInstance;
  @Mock
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @AfterEach
  void tearDown() {
    ClockUtil.reset();
  }

  @Test
  void shouldBuildActivityInstanceStartEvent() {
    // given
    final Date now = new Date(10_000L);
    ClockUtil.setCurrentTime(now);
    mockExecutionBase();

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      context.when(Context::getProcessEngineConfiguration).thenReturn(null);

      // when
      final BusinessEvent businessEvent = factory.createStartEvent(execution);

      // then
      assertThat(businessEvent).isInstanceOf(BusinessActivityInstanceEventEntity.class);

      final BusinessActivityInstanceEventEntity event = (BusinessActivityInstanceEventEntity) businessEvent;

      assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.ACTIVITY_INSTANCE_START.getEventName());
      assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.ACTIVITY_INSTANCE_START.getBusinessEventName());
      assertThat(event.getId()).isEqualTo("activity-instance-id");
      assertThat(event.getActivityInstanceId()).isEqualTo("activity-instance-id");
      assertThat(event.getParentActivityInstanceId()).isEqualTo("parent-activity-instance-id");
      assertThat(event.getProcessInstanceId()).isEqualTo("process-instance-id");
      assertThat(event.getExecutionId()).isEqualTo("execution-id");
      assertThat(event.getTenantId()).isEqualTo("tenant-id");
      assertThat(event.getRootProcessInstanceId()).isEqualTo("root-process-instance-id");
      assertThat(event.getActivityId()).isEqualTo("activity-id");
      assertThat(event.getActivityName()).isEqualTo("Activity Name");
      assertThat(event.getActivityType()).isEqualTo("userTask");
      assertThat(event.getSequenceCounter()).isEqualTo(7L);
      assertThat(event.getStartTime()).isEqualTo(now);
      assertThat(event.getEndTime()).isNull();
      assertThat(event.getDurationInMillis()).isNull();
    }
  }

  @Test
  void shouldBuildActivityInstanceEndEventWithDurationWhenHistoricStartTimeExists() {
    // given
    final Date startTime = new Date(1_000L);
    final Date endTime = new Date(6_000L);

    mockExecutionBase();
    when(execution.getActivityInstanceState()).thenReturn(1);
    when(historicActivityInstance.getStartTime()).thenReturn(startTime);
    when(dbEntityManager.selectById(HistoricActivityInstanceEntity.class, "activity-instance-id")).thenReturn(historicActivityInstance);
    when(commandContext.getDbEntityManager()).thenReturn(dbEntityManager);
    when(processEngineConfiguration.getHistoryLevel()).thenReturn(HistoryLevel.HISTORY_LEVEL_FULL);

    ClockUtil.setCurrentTime(endTime);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      context.when(Context::getProcessEngineConfiguration).thenReturn(processEngineConfiguration);

      // when
      final BusinessEvent businessEvent = factory.createEndEvent(execution);

      // then
      final BusinessActivityInstanceEventEntity event = (BusinessActivityInstanceEventEntity) businessEvent;

      assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.ACTIVITY_INSTANCE_END.getEventName());
      assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.ACTIVITY_INSTANCE_END.getBusinessEventName());
      assertThat(event.getActivityInstanceState()).isEqualTo(1);
      assertThat(event.getStartTime()).isEqualTo(startTime);
      assertThat(event.getEndTime()).isEqualTo(endTime);
      assertThat(event.getDurationInMillis()).isEqualTo(5_000L);
    }
  }

  @Test
  void shouldBuildActivityInstanceEndEventWithoutDurationWhenHistoricActivityInstanceDoesNotExist() {
    // given
    final Date endTime = new Date(6_000L);

    mockExecutionBase();
    when(dbEntityManager.selectById(HistoricActivityInstanceEntity.class, "activity-instance-id")).thenReturn(null);
    when(commandContext.getDbEntityManager()).thenReturn(dbEntityManager);
    when(processEngineConfiguration.getHistoryLevel()).thenReturn(HistoryLevel.HISTORY_LEVEL_FULL);

    ClockUtil.setCurrentTime(endTime);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      context.when(Context::getProcessEngineConfiguration).thenReturn(processEngineConfiguration);

      // when
      final BusinessEvent businessEvent = factory.createEndEvent(execution);

      // then
      final BusinessActivityInstanceEventEntity event = (BusinessActivityInstanceEventEntity) businessEvent;

      assertThat(event.getStartTime()).isNull();
      assertThat(event.getEndTime()).isEqualTo(endTime);
      assertThat(event.getDurationInMillis()).isNull();
    }
  }

  @Test
  void shouldNotQueryHistoricStartTimeWhenHistoryIsDisabled() {
    // given
    final Date endTime = new Date(6_000L);

    mockExecutionBase();
    when(processEngineConfiguration.getHistoryLevel()).thenReturn(HistoryLevel.HISTORY_LEVEL_NONE);

    ClockUtil.setCurrentTime(endTime);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      context.when(Context::getProcessEngineConfiguration).thenReturn(processEngineConfiguration);

      // when
      final BusinessEvent businessEvent = factory.createEndEvent(execution);

      // then
      final BusinessActivityInstanceEventEntity event = (BusinessActivityInstanceEventEntity) businessEvent;

      assertThat(event.getStartTime()).isNull();
      assertThat(event.getEndTime()).isEqualTo(endTime);
      assertThat(event.getDurationInMillis()).isNull();
      verify(commandContext, never()).getDbEntityManager();
    }
  }

  @Test
  void shouldReturnNullWhenExecutionIsNotAnExecutionEntity() {
    assertThat(factory.createStartEvent(null)).isNull();
  }

  private void mockExecutionBase() {
    when(execution.getActivity()).thenReturn(activity);
    when(activity.getId()).thenReturn("activity-id");
    when(activity.getProperty("name")).thenReturn("Activity Name");
    when(activity.getProperty("type")).thenReturn("userTask");

    when(execution.getActivityInstanceId()).thenReturn("activity-instance-id");
    when(execution.getParentActivityInstanceId()).thenReturn("parent-activity-instance-id");
    when(execution.getProcessInstanceId()).thenReturn("process-instance-id");
    when(execution.getId()).thenReturn("execution-id");
    when(execution.getTenantId()).thenReturn("tenant-id");
    when(execution.getRootProcessInstanceId()).thenReturn("root-process-instance-id");
    when(execution.getProcessDefinitionId()).thenReturn(null);
    when(execution.getSequenceCounter()).thenReturn(7L);
  }
}
