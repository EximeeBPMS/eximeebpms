package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.List;

public interface ScriptViolationStore {

  void record(ScriptViolationEvent event);

  List<ScriptViolationEvent> getRecent(int limit);

  long getTotalCount();
}
