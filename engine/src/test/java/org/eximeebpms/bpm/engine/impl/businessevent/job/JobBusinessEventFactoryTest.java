package org.eximeebpms.bpm.engine.impl.businessevent.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.job.JobState;
import org.eximeebpms.bpm.engine.management.JobDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobBusinessEventFactoryTest {

  private final JobBusinessEventFactory factory = new JobBusinessEventFactory();

  @Mock
  private JobEntity job;
  @Mock
  private ExecutionEntity execution;
  @Mock
  private JobDefinition jobDefinition;
  @Mock
  private CommandContext commandContext;
  @Mock
  private ProcessEngineConfigurationImpl processEngineConfiguration;
  @Mock
  private DbEntityManager dbEntityManager;
  @Mock
  private ProcessDefinitionEntity processDefinitionEntity;

  @Test
  void shouldBuildJobCreatedEventWithJobDefinitionData() {
    // given
    mockJobBase();
    mockJobDefinition();

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
      when(processEngineConfiguration.getHostname()).thenReturn("host-1");

      // when
      final BusinessEvent businessEvent = factory.createJobCreatedEvent(job);

      // then
      assertThat(businessEvent).isInstanceOf(JobLogBusinessEvent.class);

      final JobLogBusinessEvent event = (JobLogBusinessEvent) businessEvent;

      assertThat(event.getJobId()).isEqualTo("job-id");
      assertThat(event.getBatchId()).isEqualTo("batch-id");
      assertThat(event.getJobRetries()).isEqualTo(3);
      assertThat(event.getJobPriority()).isEqualTo(50L);
      assertThat(event.getHostname()).isEqualTo("host-1");
      assertThat(event.getJobDefinitionId()).isEqualTo("job-definition-id");
      assertThat(event.getJobDefinitionType()).isEqualTo("job-definition-type");
      assertThat(event.getJobDefinitionConfiguration()).isEqualTo("job-definition-configuration");
      assertThat(event.getActivityId()).isEqualTo("activity-id");
      assertThat(event.getFailedActivityId()).isEqualTo("failed-activity-id");
      assertThat(event.getExecutionId()).isEqualTo("execution-id");
      assertThat(event.getProcessInstanceId()).isEqualTo("process-instance-id");
      assertThat(event.getDeploymentId()).isEqualTo("deployment-id");
      assertThat(event.getTenantId()).isEqualTo("tenant-id");
      assertThat(event.getState()).isEqualTo(JobState.CREATED.getStateCode());
    }
  }

  @Test
  void shouldBuildJobDeletedEvent() {
    // given
    mockJobBase();
    mockJobDefinition();

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
      when(processEngineConfiguration.getHostname()).thenReturn("host-1");

      // when
      final BusinessEvent businessEvent = factory.createJobDeletedEvent(job);

      // then
      final JobLogBusinessEvent event = (JobLogBusinessEvent) businessEvent;

      assertThat(event.getState()).isEqualTo(JobState.DELETED.getStateCode());
      assertThat(event.getJobId()).isEqualTo("job-id");
    }
  }

  @Test
  void shouldUseJobHandlerTypeAsJobDefinitionTypeWhenNoJobDefinitionExists() {
    // given
    mockJobBase();
    when(job.getJobDefinition()).thenReturn(null);
    when(job.getJobHandlerType()).thenReturn("async-signal-handler");

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
      when(processEngineConfiguration.getHostname()).thenReturn("host-1");

      // when
      final BusinessEvent businessEvent = factory.createJobCreatedEvent(job);

      // then
      final JobLogBusinessEvent event = (JobLogBusinessEvent) businessEvent;

      assertThat(event.getJobDefinitionId()).isNull();
      assertThat(event.getJobDefinitionConfiguration()).isNull();
      assertThat(event.getJobDefinitionType()).isEqualTo("async-signal-handler");
    }
  }

  @Test
  void shouldNotSetRootProcessInstanceIdWhenExecutionIsNull() {
    // given
    mockJobBase();
    mockJobDefinition();
    when(job.getExecution()).thenReturn(null);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
      when(processEngineConfiguration.getHostname()).thenReturn("host-1");

      // when
      final BusinessEvent businessEvent = factory.createJobCreatedEvent(job);

      // then
      assertThat(businessEvent.getRootProcessInstanceId()).isNull();
    }
  }

  @Test
  void shouldSetRootProcessInstanceIdWhenExecutionExists() {
    // given
    mockJobBase();
    mockJobDefinition();
    when(job.getExecution()).thenReturn(execution);
    when(execution.getRootProcessInstanceId()).thenReturn("root-process-instance-id");

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
      when(processEngineConfiguration.getHostname()).thenReturn("host-1");

      // when
      final BusinessEvent businessEvent = factory.createJobCreatedEvent(job);

      // then
      assertThat(businessEvent.getRootProcessInstanceId()).isEqualTo("root-process-instance-id");
    }
  }

  @Test
  void shouldSetTimestampToCurrentTime() {
    // given
    mockJobBase();
    mockJobDefinition();
    final Date before = new Date();

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
      when(processEngineConfiguration.getHostname()).thenReturn("host-1");

      // when
      final BusinessEvent businessEvent = factory.createJobCreatedEvent(job);
      final Date after = new Date();

      // then
      final JobLogBusinessEvent event = (JobLogBusinessEvent) businessEvent;
      assertThat(event.getTimestamp()).isBetween(before, after, true, true);
    }
  }

  @Test
  void shouldBuildJobSuccessfulEvent() {
    // given
    mockJobBase();
    mockJobDefinition();

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
      when(processEngineConfiguration.getHostname()).thenReturn("host-1");

      // when
      final BusinessEvent businessEvent = factory.createJobSuccessfulEvent(job);

      // then
      final JobLogBusinessEvent event = (JobLogBusinessEvent) businessEvent;
      assertThat(event.getState()).isEqualTo(JobState.SUCCESSFUL.getStateCode());
      assertThat(event.getJobId()).isEqualTo("job-id");
      assertThat(event.getJobExceptionMessage()).isNull();
      assertThat(event.getExceptionStacktrace()).isNull();
    }
  }

  @Test
  void shouldBuildJobFailedEventWithExceptionDetails() {
    // given
    mockJobBase();
    mockJobDefinition();
    final Exception exception = new RuntimeException("boom");

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
      when(processEngineConfiguration.getHostname()).thenReturn("host-1");

      // when
      final BusinessEvent businessEvent = factory.createJobFailedEvent(job, exception);

      // then
      final JobLogBusinessEvent event = (JobLogBusinessEvent) businessEvent;
      assertThat(event.getState()).isEqualTo(JobState.FAILED.getStateCode());
      assertThat(event.getJobExceptionMessage()).isEqualTo("boom");
      assertThat(event.getExceptionStacktrace()).contains("RuntimeException");
    }
  }

  @Test
  void shouldBuildJobFailedEventWithoutException() {
    // given
    mockJobBase();
    mockJobDefinition();

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
      when(processEngineConfiguration.getHostname()).thenReturn("host-1");

      // when
      final BusinessEvent businessEvent = factory.createJobFailedEvent(job, null);

      // then
      final JobLogBusinessEvent event = (JobLogBusinessEvent) businessEvent;
      assertThat(event.getState()).isEqualTo(JobState.FAILED.getStateCode());
      assertThat(event.getJobExceptionMessage()).isNull();
      assertThat(event.getExceptionStacktrace()).isNull();
    }
  }

  @Test
  void shouldSetJobDueDate() {
    // given
    mockJobBase();
    mockJobDefinition();
    final Date dueDate = new Date();
    when(job.getDuedate()).thenReturn(dueDate);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
      when(processEngineConfiguration.getHostname()).thenReturn("host-1");

      // when
      final BusinessEvent businessEvent = factory.createJobCreatedEvent(job);

      // then
      final JobLogBusinessEvent event = (JobLogBusinessEvent) businessEvent;
      assertThat(event.getJobDueDate()).isEqualTo(dueDate);
    }
  }

  @Test
  void shouldSetSequenceCounterFromJob() {
    // given
    mockJobBase();
    mockJobDefinition();
    when(job.getSequenceCounter()).thenReturn(42L);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
      when(processEngineConfiguration.getHostname()).thenReturn("host-1");

      // when
      final BusinessEvent businessEvent = factory.createJobCreatedEvent(job);

      // then
      assertThat(businessEvent.getSequenceCounter()).isEqualTo(42L);
    }
  }

  @Test
  void shouldFillProcessDefinitionDataWhenProcessDefinitionExists() {
    // given
    mockJobBase();
    mockJobDefinition();
    when(job.getProcessDefinitionId()).thenReturn("process-definition-id");
    when(commandContext.getDbEntityManager()).thenReturn(dbEntityManager);
    when(dbEntityManager.selectById(ProcessDefinitionEntity.class, "process-definition-id"))
        .thenReturn(processDefinitionEntity);
    when(processDefinitionEntity.getId()).thenReturn("process-definition-id");
    when(processDefinitionEntity.getKey()).thenReturn("process-definition-key");
    when(processDefinitionEntity.getVersion()).thenReturn(3);
    when(processDefinitionEntity.getName()).thenReturn("process-definition-name");

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
      when(processEngineConfiguration.getHostname()).thenReturn("host-1");

      // when
      final BusinessEvent businessEvent = factory.createJobCreatedEvent(job);

      // then
      assertThat(businessEvent.getProcessDefinitionId()).isEqualTo("process-definition-id");
      assertThat(businessEvent.getProcessDefinitionKey()).isEqualTo("process-definition-key");
      assertThat(businessEvent.getProcessDefinitionVersion()).isEqualTo(3);
      assertThat(businessEvent.getProcessDefinitionName()).isEqualTo("process-definition-name");
    }
  }

  private void mockJobBase() {
    when(job.getId()).thenReturn("job-id");
    when(job.getBatchId()).thenReturn("batch-id");
    when(job.getDuedate()).thenReturn(new Date());
    when(job.getRetries()).thenReturn(3);
    when(job.getPriority()).thenReturn(50L);
    when(job.getActivityId()).thenReturn("activity-id");
    when(job.getFailedActivityId()).thenReturn("failed-activity-id");
    when(job.getExecutionId()).thenReturn("execution-id");
    when(job.getProcessInstanceId()).thenReturn("process-instance-id");
    when(job.getDeploymentId()).thenReturn("deployment-id");
    when(job.getTenantId()).thenReturn("tenant-id");
  }

  private void mockJobDefinition() {
    when(job.getJobDefinition()).thenReturn(jobDefinition);
    when(jobDefinition.getId()).thenReturn("job-definition-id");
    when(jobDefinition.getJobType()).thenReturn("job-definition-type");
    when(jobDefinition.getJobConfiguration()).thenReturn("job-definition-configuration");
  }
}

