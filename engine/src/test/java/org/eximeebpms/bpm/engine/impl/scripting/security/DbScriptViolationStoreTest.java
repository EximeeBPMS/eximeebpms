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
package org.eximeebpms.bpm.engine.impl.scripting.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.interceptor.Command;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ScriptViolationManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DbScriptViolationStoreTest {

  @Mock
  private ProcessEngineConfigurationImpl config;
  @Mock
  private CommandExecutor executor;
  @Mock
  private CommandContext ctx;
  @Mock
  private ScriptViolationManager manager;

  @Test
  public void shouldDelegateGetRecentToManagerViaCommandExecutor() {
    // given
    when(config.getCommandExecutorTxRequired()).thenReturn(executor);

    ScriptViolationEvent event = sampleEvent();
    when(executor.execute(any(Command.class))).thenAnswer(inv -> {
      Command<?> cmd = inv.getArgument(0);
      return cmd.execute(mockContextWith(event));
    });

    DbScriptViolationStore store = new DbScriptViolationStore(config);

    // when
    List<ScriptViolationEvent> result = store.getRecent(10);

    // then
    assertThat(result).containsExactly(event);
  }

  @Test
  public void shouldDelegateCountToManagerViaCommandExecutor() {
    // given
    when(config.getCommandExecutorTxRequired()).thenReturn(executor);

    when(executor.execute(any(Command.class))).thenAnswer(inv -> {
      Command<?> cmd = inv.getArgument(0);
      when(ctx.getSession(ScriptViolationManager.class)).thenReturn(manager);
      when(manager.countAll()).thenReturn(42L);
      return cmd.execute(ctx);
    });

    DbScriptViolationStore store = new DbScriptViolationStore(config);

    // when
    long count = store.getTotalCount();

    // then
    assertThat(count).isEqualTo(42L);
  }

  @Test
  public void shouldReturnInMemoryCounterWhenPositive() {
    // given
    when(config.getCommandExecutorTxRequired()).thenReturn(executor);

    when(executor.execute(any(Command.class))).thenAnswer(inv -> {
      Command<?> cmd = inv.getArgument(0);
      when(ctx.getSession(ScriptViolationManager.class)).thenReturn(manager);
      org.mockito.Mockito.doNothing().when(manager).insert(any(ScriptViolationEvent.class));
      return cmd.execute(ctx);
    });

    DbScriptViolationStore store = new DbScriptViolationStore(config);

    // when
    store.record(sampleEvent());

    // then — in-memory counter is used instead of a DB round-trip
    assertThat(store.getTotalCount()).isEqualTo(1L);
  }

  @Test
  public void shouldCleanupOlderThanAndResetInMemoryCounter() {
    // given
    when(config.getCommandExecutorTxRequired()).thenReturn(executor);
    when(executor.execute(any(Command.class))).thenAnswer(inv -> {
      Command<?> cmd = inv.getArgument(0);
      when(ctx.getSession(ScriptViolationManager.class)).thenReturn(manager);
      return cmd.execute(ctx);
    });
    DbScriptViolationStore store = new DbScriptViolationStore(config);
    store.record(sampleEvent()); // counter = 1

    // when
    store.cleanupOlderThan(30);

    // then — in-memory counter is reset; getTotalCount falls back to DB (returns 0 by default)
    assertThat(store.getTotalCount()).isEqualTo(0L);
  }

  private CommandContext mockContextWith(ScriptViolationEvent event) {
    when(ctx.getSession(ScriptViolationManager.class)).thenReturn(manager);
    when(manager.findRecent(10)).thenReturn(List.of(event));
    return ctx;
  }

  private ScriptViolationEvent sampleEvent() {
    return new ScriptViolationEvent(
        Instant.now(), "proc", null, "task", "javascript",
        ScriptSourceType.INLINE_SOURCE, ScriptOrigin.USER,
        "RULE_CODE", "reason");
  }
}
