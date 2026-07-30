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
package org.eximeebpms.bpm.engine.impl.businessevent.script;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptOrigin;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSourceType;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScriptViolationBusinessEventFactoryTest {

  private final ScriptViolationBusinessEventFactory factory = new ScriptViolationBusinessEventFactory();

  @Mock
  private CommandContext commandContext;
  @Mock
  private DbEntityManager dbEntityManager;
  @Mock
  private ProcessDefinitionEntity processDefinitionEntity;

  @Test
  void shouldSetCorrectEventTypesOnCreatedEvent() {
    // given
    ScriptViolationEvent violation = buildViolation(null);

    // when
    BusinessEvent result = factory.createScriptViolationEvent(violation);

    // then
    assertThat(result).isInstanceOf(BusinessScriptViolationEventEntity.class);
    BusinessScriptViolationEventEntity event = (BusinessScriptViolationEventEntity) result;
    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.SCRIPT_VIOLATION_CREATE.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.SCRIPT_VIOLATION_CREATE.getBusinessEventName());
  }

  @Test
  void shouldMapAllFieldsFromViolationWhenProcessDefinitionIdIsNull() {
    // given
    Instant timestamp = Instant.parse("2026-06-26T10:00:00Z");
    ScriptViolationEvent violation = new ScriptViolationEvent(
        timestamp, "myProcess", null, "serviceTask1",
        "javascript", ScriptSourceType.INLINE_SOURCE, ScriptOrigin.USER,
        "RULE_CODE", "reason text");

    // when
    BusinessEvent result = factory.createScriptViolationEvent(violation);

    // then
    BusinessScriptViolationEventEntity event = (BusinessScriptViolationEventEntity) result;
    assertThat(event.getTimestamp()).isEqualTo(Date.from(timestamp));
    assertThat(event.getProcessDefinitionKey()).isEqualTo("myProcess");
    assertThat(event.getProcessDefinitionId()).isNull();
    assertThat(event.getActivityId()).isEqualTo("serviceTask1");
    assertThat(event.getLanguage()).isEqualTo("javascript");
    assertThat(event.getSourceType()).isEqualTo("INLINE_SOURCE");
    assertThat(event.getOrigin()).isEqualTo("USER");
    assertThat(event.getRuleCode()).isEqualTo("RULE_CODE");
    assertThat(event.getReason()).isEqualTo("reason text");
  }

  @Test
  void shouldResolveProcessDefinitionDataFromDbWhenProcessDefinitionIdIsPresent() {
    // given
    ScriptViolationEvent violation = new ScriptViolationEvent(
        Instant.now(), "myProcess", "myProcess:1:abc", "serviceTask1",
        "javascript", ScriptSourceType.INLINE_SOURCE, ScriptOrigin.USER,
        "RULE_CODE", "reason");

    when(dbEntityManager.selectById(ProcessDefinitionEntity.class, "myProcess:1:abc"))
        .thenReturn(processDefinitionEntity);
    when(processDefinitionEntity.getId()).thenReturn("myProcess:1:abc");
    when(processDefinitionEntity.getKey()).thenReturn("myProcess");
    when(processDefinitionEntity.getVersion()).thenReturn(1);
    when(processDefinitionEntity.getName()).thenReturn("My Process");
    when(commandContext.getDbEntityManager()).thenReturn(dbEntityManager);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);

      // when
      BusinessEvent result = factory.createScriptViolationEvent(violation);

      // then
      BusinessScriptViolationEventEntity event = (BusinessScriptViolationEventEntity) result;
      assertThat(event.getProcessDefinitionId()).isEqualTo("myProcess:1:abc");
      assertThat(event.getProcessDefinitionKey()).isEqualTo("myProcess");
      assertThat(event.getProcessDefinitionVersion()).isEqualTo(1);
      assertThat(event.getProcessDefinitionName()).isEqualTo("My Process");
    }
  }

  @Test
  void shouldHandleNullOptionalFields() {
    // given
    ScriptViolationEvent violation = new ScriptViolationEvent(
        Instant.now(), null, null, null,
        "groovy", null, null, null, null);

    // when
    BusinessEvent result = factory.createScriptViolationEvent(violation);

    // then
    BusinessScriptViolationEventEntity event = (BusinessScriptViolationEventEntity) result;
    assertThat(event.getActivityId()).isNull();
    assertThat(event.getSourceType()).isNull();
    assertThat(event.getOrigin()).isNull();
    assertThat(event.getRuleCode()).isNull();
    assertThat(event.getReason()).isNull();
    assertThat(event.getProcessDefinitionKey()).isNull();
    assertThat(event.getProcessDefinitionId()).isNull();
  }

  @Test
  void shouldNotQueryDbWhenProcessDefinitionIdIsNull() {
    // given
    ScriptViolationEvent violation = buildViolation(null);

    // when / then — no Context access, no DB query
    assertThatCode(() -> factory.createScriptViolationEvent(violation)).doesNotThrowAnyException();
  }

  @Test
  void shouldFallbackToNullProcessDefinitionFieldsWhenProcessDefinitionEntityNotFoundInDb() {
    // given
    ScriptViolationEvent violation = buildViolation("unknownId:1:abc");

    when(dbEntityManager.selectById(ProcessDefinitionEntity.class, "unknownId:1:abc"))
        .thenReturn(null);
    when(commandContext.getDbEntityManager()).thenReturn(dbEntityManager);

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);

      // when
      BusinessEvent result = factory.createScriptViolationEvent(violation);

      // then
      BusinessScriptViolationEventEntity event = (BusinessScriptViolationEventEntity) result;
      assertThat(event.getProcessDefinitionId()).isNull();
      assertThat(event.getProcessDefinitionKey()).isNull();
    }
  }

  private ScriptViolationEvent buildViolation(String processDefinitionId) {
    return new ScriptViolationEvent(
        Instant.now(), "testProcess", processDefinitionId, "task1",
        "javascript", ScriptSourceType.INLINE_SOURCE, ScriptOrigin.USER,
        "RULE_CODE", "test reason");
  }
}
