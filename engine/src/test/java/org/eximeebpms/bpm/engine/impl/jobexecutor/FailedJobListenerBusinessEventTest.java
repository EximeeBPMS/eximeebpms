/*
 * Copyright EximeeBPMS contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eximeebpms.bpm.engine.impl.jobexecutor;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProcessor;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProducer;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.interceptor.Command;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;
import org.eximeebpms.bpm.engine.impl.persistence.entity.HistoricJobLogManager;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifies that {@link FailedJobListener} fires the {@code JOB_FAIL} business event via
 * {@link BusinessEventProcessor}, carrying the collected failure, using a dedicated
 * {@code TX_REQUIRES_NEW} command executor (passed into the constructor, see
 * {@code ExecuteJobHelper#createFailedJobListener}) so the event survives even if the
 * retry-decrement command that runs afterwards fails or is rolled back - and only once, when the
 * job's retries are actually exhausted.
 */
@ExtendWith(MockitoExtension.class)
class FailedJobListenerBusinessEventTest {

  @Mock
  private CommandExecutor commandExecutor;
  @Mock
  private CommandExecutor commandExecutorTxRequiresNew;
  @Mock
  private CommandContext commandContext;
  @Mock
  private JobManager jobManager;
  @Mock
  private JobEntity job;
  @Mock
  private BusinessEventProducer producer;
  @Mock
  private BusinessEvent expectedEvent;
  @Mock
  private Command<Object> delegateCommand;
  @Mock
  private HistoricJobLogManager historicJobLogManager;

  @Test
  void shouldFireJobFailedBusinessEventInNewTransactionWhenJobExists() {
    // given
    RuntimeException failure = new RuntimeException("boom");
    JobFailureCollector jobFailureCollector = new JobFailureCollector("job-id");
    jobFailureCollector.setFailure(failure);

    FailedJobListener listener = new FailedJobListener(commandExecutor, commandExecutorTxRequiresNew, jobFailureCollector);

    when(commandContext.getJobManager()).thenReturn(jobManager);
    when(jobManager.findJobById("job-id")).thenReturn(job);

    ArgumentCaptor<Command<Void>> commandCaptor = ArgumentCaptor.forClass(Command.class);

    try (MockedStatic<BusinessEventProcessor> processor = mockStatic(BusinessEventProcessor.class)) {
      // when
      listener.fireJobFailedBusinessEventInNewTransaction();

      // then
      verify(commandExecutorTxRequiresNew).execute(commandCaptor.capture());
      commandCaptor.getValue().execute(commandContext);

      ArgumentCaptor<BusinessEventProcessor.BusinessEventCreator> creatorCaptor =
          ArgumentCaptor.forClass(BusinessEventProcessor.BusinessEventCreator.class);
      processor.verify(() -> BusinessEventProcessor.processBusinessEvents(creatorCaptor.capture()));

      when(producer.createJobFailedEvt(job, failure)).thenReturn(expectedEvent);
      creatorCaptor.getValue().createBusinessEvent(producer);

      verify(producer).createJobFailedEvt(job, failure);
      verify(job).setFailedActivityId(jobFailureCollector.getFailedActivityId());
    }
  }

  @Test
  void shouldNotFireJobFailedBusinessEventWhenJobIsNull() {
    // given
    RuntimeException failure = new RuntimeException("boom");
    JobFailureCollector jobFailureCollector = new JobFailureCollector("job-id");
    jobFailureCollector.setFailure(failure);

    FailedJobListener listener = new FailedJobListener(commandExecutor, commandExecutorTxRequiresNew, jobFailureCollector);

    when(commandContext.getJobManager()).thenReturn(jobManager);
    when(jobManager.findJobById("job-id")).thenReturn(null);

    ArgumentCaptor<Command<Void>> commandCaptor = ArgumentCaptor.forClass(Command.class);

    try (MockedStatic<BusinessEventProcessor> processor = mockStatic(BusinessEventProcessor.class)) {
      // when
      listener.fireJobFailedBusinessEventInNewTransaction();

      verify(commandExecutorTxRequiresNew).execute(commandCaptor.capture());
      commandCaptor.getValue().execute(commandContext);

      // then
      processor.verify(() -> BusinessEventProcessor.processBusinessEvents(org.mockito.ArgumentMatchers.any()), never());
    }
  }

  @Test
  void shouldFireHistoricJobFailedEventAndDelegateWithoutFiringBusinessEventAgain() {
    // given: FailedJobListenerCmd no longer fires the business event itself,
    // that responsibility moved to FailedJobListener#fireJobFailedBusinessEventInNewTransaction.
    RuntimeException failure = new RuntimeException("boom");
    JobFailureCollector jobFailureCollector = new JobFailureCollector("job-id");
    jobFailureCollector.setFailure(failure);

    FailedJobListener listener = new FailedJobListener(commandExecutor, commandExecutorTxRequiresNew, jobFailureCollector);
    FailedJobListener.FailedJobListenerCmd cmd =
        listener.new FailedJobListenerCmd("job-id", delegateCommand);

    when(commandContext.getJobManager()).thenReturn(jobManager);
    when(jobManager.findJobById("job-id")).thenReturn(job);
    when(commandContext.getHistoricJobLogManager()).thenReturn(historicJobLogManager);
    when(job.getRetries()).thenReturn(2);

    try (MockedStatic<Context> context = mockStatic(Context.class);
         MockedStatic<BusinessEventProcessor> processor = mockStatic(BusinessEventProcessor.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);

      // when
      Boolean retriesExhausted = cmd.execute(commandContext);

      // then
      verify(historicJobLogManager).fireJobFailedEvent(job, failure);
      verify(delegateCommand).execute(commandContext);
      processor.verifyNoInteractions();
      org.junit.jupiter.api.Assertions.assertEquals(false, retriesExhausted);
    }
  }

  @Test
  void shouldReportRetriesExhaustedWhenJobHasNoRetriesLeft() {
    // given
    RuntimeException failure = new RuntimeException("boom");
    JobFailureCollector jobFailureCollector = new JobFailureCollector("job-id");
    jobFailureCollector.setFailure(failure);

    FailedJobListener listener = new FailedJobListener(commandExecutor, commandExecutorTxRequiresNew, jobFailureCollector);
    FailedJobListener.FailedJobListenerCmd cmd =
        listener.new FailedJobListenerCmd("job-id", delegateCommand);

    when(commandContext.getJobManager()).thenReturn(jobManager);
    when(jobManager.findJobById("job-id")).thenReturn(job);
    when(commandContext.getHistoricJobLogManager()).thenReturn(historicJobLogManager);
    when(job.getRetries()).thenReturn(0);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);

      // when
      Boolean retriesExhausted = cmd.execute(commandContext);

      // then
      org.junit.jupiter.api.Assertions.assertEquals(true, retriesExhausted);
    }
  }
}
