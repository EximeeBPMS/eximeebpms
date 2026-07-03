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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ScriptViolationManager;

public class DbScriptViolationStore implements ScriptViolationStore {

  private final ProcessEngineConfigurationImpl config;
  private final AtomicLong totalCount = new AtomicLong(0);

  public DbScriptViolationStore(ProcessEngineConfigurationImpl config) {
    this.config = config;
  }

  @Override
  public void record(ScriptViolationEvent event) {
    totalCount.incrementAndGet();
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext != null) {
      commandContext.getSession(ScriptViolationManager.class).insert(event);
    } else {
      config.getCommandExecutorTxRequired().execute(ctx -> {
        ctx.getSession(ScriptViolationManager.class).insert(event);
        return null;
      });
    }
  }

  @Override
  public List<ScriptViolationEvent> getRecent(int limit) {
    return config.getCommandExecutorTxRequired().execute(
        ctx -> ctx.getSession(ScriptViolationManager.class).findRecent(limit));
  }

  @Override
  public long getTotalCount() {
    long count = totalCount.get();
    if (count > 0) {
      return count;
    }
    return config.getCommandExecutorTxRequired().execute(
        ctx -> ctx.getSession(ScriptViolationManager.class).countAll());
  }

  public void cleanupOlderThan(int retentionDays) {
    Date cutoff = Date.from(Instant.now().minus(retentionDays, ChronoUnit.DAYS));
    config.getCommandExecutorTxRequired().execute(ctx -> {
      ctx.getSession(ScriptViolationManager.class).deleteOlderThan(cutoff);
      return null;
    });
    totalCount.set(0);
  }

}
