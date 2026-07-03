package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NoOpScriptViolationStore implements ScriptViolationStore {

  public static final NoOpScriptViolationStore INSTANCE = new NoOpScriptViolationStore();

  @Override
  public void record(ScriptViolationEvent event) {
  }

  @Override
  public List<ScriptViolationEvent> getRecent(int limit) {
    return List.of();
  }

  @Override
  public long getTotalCount() {
    return 0;
  }
}
