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
package org.eximeebpms.bpm.engine.impl.businessevent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.eximeebpms.bpm.engine.impl.businessevent.form.BusinessFormPropertyEventEntity;
import org.eximeebpms.bpm.engine.impl.businessevent.task.BusinessTaskInstanceEventEntity;
import org.eximeebpms.bpm.engine.impl.businessevent.variable.BusinessVariableUpdateEventEntity;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifies that {@link DbBusinessEventHandler} only propagates {@code taskId} onto the
 * outbox row for event types whose leftover, undispatched outbox entries are meant to be
 * purged when a task is deleted ({@link BusinessEventManager#deleteByTaskId}). Task-instance
 * lifecycle events must keep {@code taskId} unset, otherwise a {@code TASK_INSTANCE_DELETE}
 * event would be deleted from the outbox right after being inserted, before it can be dispatched.
 */
@ExtendWith(MockitoExtension.class)
class DbBusinessEventHandlerTest {

  @Mock
  private CommandContext commandContext;
  @Mock
  private DbEntityManager dbEntityManager;

  private final DbBusinessEventHandler handler = new DbBusinessEventHandler();

  @Test
  void shouldSetTaskIdForVariableUpdateEvent() {
    BusinessVariableUpdateEventEntity event = BusinessVariableUpdateEventEntity.builder()
        .taskId("aTaskId")
        .build();

    assertThat(capturedOutboxTaskId(event)).isEqualTo("aTaskId");
  }

  @Test
  void shouldSetTaskIdForFormPropertyUpdateEvent() {
    BusinessFormPropertyEventEntity event = BusinessFormPropertyEventEntity.builder()
        .taskId("aTaskId")
        .build();

    assertThat(capturedOutboxTaskId(event)).isEqualTo("aTaskId");
  }

  @Test
  void shouldNotSetTaskIdForTaskInstanceLifecycleEvent() {
    BusinessTaskInstanceEventEntity event = BusinessTaskInstanceEventEntity.builder()
        .taskId("aTaskId")
        .build();

    assertThat(capturedOutboxTaskId(event)).isNull();
  }

  private String capturedOutboxTaskId(BusinessEvent event) {
    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);
      when(commandContext.getDbEntityManager()).thenReturn(dbEntityManager);
      doNothing().when(dbEntityManager).insertWithoutId(any());

      handler.handleEvent(event);

      ArgumentCaptor<BusinessEventOutboxEntity> captor = ArgumentCaptor.forClass(BusinessEventOutboxEntity.class);
      org.mockito.Mockito.verify(dbEntityManager).insertWithoutId(captor.capture());
      return captor.getValue().getTaskId();
    }
  }
}
