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

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProcessor;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProducer;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifies that {@link JobEntity#postExecute(CommandContext)} fires the
 * {@code JOB_SUCCESS} business event via {@link BusinessEventProcessor} after a
 * job has been executed successfully.
 */
@ExtendWith(MockitoExtension.class)
class JobEntityBusinessEventTest {

  @Mock
  private CommandContext commandContext;
  @Mock
  private HistoricJobLogManager historicJobLogManager;
  @Mock
  private BusinessEventProducer producer;
  @Mock
  private BusinessEvent expectedEvent;

  @Test
  void shouldFireJobSuccessfulBusinessEventOnPostExecute() {
    // given
    MessageEntity job = spy(new MessageEntity());
    doNothing().when(job).delete(anyBoolean());
    when(commandContext.getHistoricJobLogManager()).thenReturn(historicJobLogManager);

    try (MockedStatic<BusinessEventProcessor> processor = mockStatic(BusinessEventProcessor.class)) {
      // when
      job.postExecute(commandContext);

      // then
      ArgumentCaptor<BusinessEventProcessor.BusinessEventCreator> captor =
          ArgumentCaptor.forClass(BusinessEventProcessor.BusinessEventCreator.class);
      processor.verify(() -> BusinessEventProcessor.processBusinessEvents(captor.capture()));

      when(producer.createJobSuccessfulEvt(job)).thenReturn(expectedEvent);
      BusinessEvent createdEvent = captor.getValue().createBusinessEvent(producer);

      verify(producer).createJobSuccessfulEvt(job);
      verify(historicJobLogManager).fireJobSuccessfulEvent(job);
      org.assertj.core.api.Assertions.assertThat(createdEvent).isSameAs(expectedEvent);
    }
  }
}
