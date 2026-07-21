package org.eximeebpms.bpm.engine.impl.businessevent.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.eximeebpms.bpm.engine.impl.batch.BatchEntity;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BatchBusinessEventFactoryTest {

  private final BatchBusinessEventFactory factory = new BatchBusinessEventFactory();

  @Mock
  private BatchEntity batch;

  @Mock
  private CommandContext commandContext;

  @BeforeEach
  void setUp() {
    when(batch.getId()).thenReturn("batch-id");
    when(batch.getType()).thenReturn("batch-type");
    when(batch.getTotalJobs()).thenReturn(10);
    when(batch.getBatchJobsPerSeed()).thenReturn(5);
    when(batch.getInvocationsPerBatchJob()).thenReturn(2);
    when(batch.getSeedJobDefinitionId()).thenReturn("seed-job-definition-id");
    when(batch.getMonitorJobDefinitionId()).thenReturn("monitor-job-definition-id");
    when(batch.getBatchJobDefinitionId()).thenReturn("batch-job-definition-id");
    when(batch.getTenantId()).thenReturn("tenant-id");
  }

  @AfterEach
  void tearDown() {
    ClockUtil.reset();
  }

  @Test
  void shouldBuildBatchStartEvent() {
    // given
    final Date startTime = new Date(1_000L);
    when(batch.getStartTime()).thenReturn(startTime);

    when(commandContext.getAuthenticatedUserId()).thenReturn("user-id");

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);

      // when
      final BatchBusinessEvent event = factory.createStartEvent(batch);

      // then
      assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.BATCH_START.getEventName());
      assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.BATCH_START.getBusinessEventName());
      assertThat(event.getId()).isEqualTo("batch-id");
      assertThat(event.getType()).isEqualTo("batch-type");
      assertThat(event.getTotalJobs()).isEqualTo(10);
      assertThat(event.getBatchJobsPerSeed()).isEqualTo(5);
      assertThat(event.getInvocationsPerBatchJob()).isEqualTo(2);
      assertThat(event.getSeedJobDefinitionId()).isEqualTo("seed-job-definition-id");
      assertThat(event.getMonitorJobDefinitionId()).isEqualTo("monitor-job-definition-id");
      assertThat(event.getBatchJobDefinitionId()).isEqualTo("batch-job-definition-id");
      assertThat(event.getTenantId()).isEqualTo("tenant-id");
      assertThat(event.getStartTime()).isEqualTo(startTime);
      assertThat(event.getCreateUserId()).isEqualTo("user-id");
      assertThat(event.getEndTime()).isNull();
      assertThat(event.getExecutionStartTime()).isNull();
    }
  }

  @Test
  void shouldBuildBatchEndEvent() {
    // given
    final Date endTime = new Date(6_000L);
    ClockUtil.setCurrentTime(endTime);

    // when
    final BatchBusinessEvent event = factory.createEndEvent(batch);

    // then
    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.BATCH_END.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.BATCH_END.getBusinessEventName());
    assertThat(event.getId()).isEqualTo("batch-id");
    assertThat(event.getEndTime()).isEqualTo(endTime);
    assertThat(event.getStartTime()).isNull();
    assertThat(event.getExecutionStartTime()).isNull();
    assertThat(event.getCreateUserId()).isNull();
  }

  @Test
  void shouldBuildBatchUpdateEvent() {
    // given
    final Date executionStartTime = new Date(3_000L);
    when(batch.getExecutionStartTime()).thenReturn(executionStartTime);

    // when
    final BatchBusinessEvent event = factory.createUpdateEvent(batch);

    // then
    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.BATCH_UPDATE.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.BATCH_UPDATE.getBusinessEventName());
    assertThat(event.getId()).isEqualTo("batch-id");
    assertThat(event.getExecutionStartTime()).isEqualTo(executionStartTime);
    assertThat(event.getStartTime()).isNull();
    assertThat(event.getEndTime()).isNull();
    assertThat(event.getCreateUserId()).isNull();
  }
}
