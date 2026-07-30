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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.time.Instant;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProcessor;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptOrigin;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSourceType;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class BusinessEventScriptViolationListenerTest {

  private final BusinessEventScriptViolationListener listener = new BusinessEventScriptViolationListener();

  @Test
  void shouldDoNothingWhenProcessEngineConfigurationIsNull() {
    // given
    ScriptViolationEvent event = buildViolation();

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getProcessEngineConfiguration).thenReturn(null);

      // when / then — no exception, early return
      assertThatCode(() -> listener.onViolation(event)).doesNotThrowAnyException();
    }
  }

  @Test
  void shouldCallBusinessEventProcessorWhenContextIsAvailable() {
    // given
    ScriptViolationEvent event = buildViolation();
    ProcessEngineConfigurationImpl config = mock(ProcessEngineConfigurationImpl.class);

    try (MockedStatic<Context> context = mockStatic(Context.class);
         MockedStatic<BusinessEventProcessor> processor = mockStatic(BusinessEventProcessor.class)) {
      context.when(Context::getProcessEngineConfiguration).thenReturn(config);

      // when
      listener.onViolation(event);

      // then
      processor.verify(() -> BusinessEventProcessor.processBusinessEvents(any()));
    }
  }

  @Test
  void shouldNotThrowWhenBusinessEventProcessorThrows() {
    // given
    ScriptViolationEvent event = buildViolation();
    ProcessEngineConfigurationImpl config = mock(ProcessEngineConfigurationImpl.class);

    try (MockedStatic<Context> context = mockStatic(Context.class);
         MockedStatic<BusinessEventProcessor> processor = mockStatic(BusinessEventProcessor.class)) {
      context.when(Context::getProcessEngineConfiguration).thenReturn(config);
      processor.when(() -> BusinessEventProcessor.processBusinessEvents(any()))
          .thenThrow(new RuntimeException("processor failure"));

      // when / then — exception from processor propagates (listener does not swallow it;
      // the caller in DefaultScriptSecurityPolicy catches it)
      assertThatCode(() -> listener.onViolation(event))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("processor failure");
    }
  }

  private ScriptViolationEvent buildViolation() {
    return new ScriptViolationEvent(
        Instant.now(), "testProcess", null, "task1",
        "javascript", ScriptSourceType.INLINE_SOURCE, ScriptOrigin.USER,
        "RULE_CODE", "test reason");
  }
}
