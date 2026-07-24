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
package org.eximeebpms.bpm.engine.impl.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProcessor;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProducer;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifies that {@link JobManager} fires the correct {@link BusinessEvent}s for
 * job creation and deletion via {@link BusinessEventProcessor}.
 */
@ExtendWith(MockitoExtension.class)
class JobManagerBusinessEventTest {

  private final JobManager jobManager = new JobManager();

  @Mock
  private JobEntity job;
  @Mock
  private CommandContext commandContext;
  @Mock
  private DbEntityManager dbEntityManager;
  @Mock
  private HistoricJobLogManager historicJobLogManager;
  @Mock
  private BusinessEventProducer producer;
  @Mock
  private BusinessEvent expectedEvent;

  @Test
  void shouldFireJobCreatedBusinessEventOnInsert() {
    // given
    try (MockedStatic<Context> context = mockStatic(Context.class);
         MockedStatic<BusinessEventProcessor> processor = mockStatic(BusinessEventProcessor.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getSession(DbEntityManager.class)).thenReturn(dbEntityManager);
      when(commandContext.getSession(HistoricJobLogManager.class)).thenReturn(historicJobLogManager);

      // when
      jobManager.insertJob(job);

      // then
      ArgumentCaptor<BusinessEventProcessor.BusinessEventCreator> captor =
          ArgumentCaptor.forClass(BusinessEventProcessor.BusinessEventCreator.class);
      processor.verify(() -> BusinessEventProcessor.processBusinessEvents(captor.capture()));

      when(producer.createJobCreatedEvt(job)).thenReturn(expectedEvent);
      BusinessEvent createdEvent = captor.getValue().createBusinessEvent(producer);

      verify(producer).createJobCreatedEvt(job);
      assertThat(createdEvent).isSameAs(expectedEvent);
    }
  }

  @Test
  void shouldFireJobDeletedBusinessEventOnDeleteWhenFireDeleteEventIsTrue() {
    // given
    try (MockedStatic<Context> context = mockStatic(Context.class);
         MockedStatic<BusinessEventProcessor> processor = mockStatic(BusinessEventProcessor.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getSession(DbEntityManager.class)).thenReturn(dbEntityManager);
      when(commandContext.getSession(HistoricJobLogManager.class)).thenReturn(historicJobLogManager);

      // when
      jobManager.deleteJob(job, true);

      // then
      ArgumentCaptor<BusinessEventProcessor.BusinessEventCreator> captor =
          ArgumentCaptor.forClass(BusinessEventProcessor.BusinessEventCreator.class);
      processor.verify(() -> BusinessEventProcessor.processBusinessEvents(captor.capture()));

      when(producer.createJobDeletedEvt(job)).thenReturn(expectedEvent);
      BusinessEvent createdEvent = captor.getValue().createBusinessEvent(producer);

      verify(producer).createJobDeletedEvt(job);
      assertThat(createdEvent).isSameAs(expectedEvent);
    }
  }

  @Test
  void shouldNotFireJobDeletedBusinessEventWhenFireDeleteEventIsFalse() {
    // given
    try (MockedStatic<Context> context = mockStatic(Context.class);
         MockedStatic<BusinessEventProcessor> processor = mockStatic(BusinessEventProcessor.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getSession(DbEntityManager.class)).thenReturn(dbEntityManager);

      // when
      jobManager.deleteJob(job, false);

      // then
      processor.verify(() -> BusinessEventProcessor.processBusinessEvents(org.mockito.ArgumentMatchers.any()), never());
    }
  }
}
