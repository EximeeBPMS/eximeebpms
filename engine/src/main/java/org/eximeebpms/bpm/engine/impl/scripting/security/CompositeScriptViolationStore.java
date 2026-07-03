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

import java.util.List;

public record CompositeScriptViolationStore(List<ScriptViolationStore> delegates) implements ScriptViolationStore {

  public CompositeScriptViolationStore {
    if (delegates == null || delegates.isEmpty()) {
      throw new IllegalArgumentException("delegates must not be empty");
    }
    delegates = List.copyOf(delegates);
  }

  @Override
  public void record(ScriptViolationEvent event) {
    for (ScriptViolationStore delegate : delegates) {
      delegate.record(event);
    }
  }

  @Override
  public List<ScriptViolationEvent> getRecent(int limit) {
    return delegates.get(0).getRecent(limit);
  }

  @Override
  public long getTotalCount() {
    return delegates.get(0).getTotalCount();
  }
}
