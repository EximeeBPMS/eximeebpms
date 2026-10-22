package org.eximeebpms.bpm.commons.scriptguard;

/**
 * Mirrors {@code org.eximeebpms.bpm.engine.impl.scripting.security.ScriptOrigin} —
 * kept as a separate type here so this module has no dependency on the
 * process-engine runtime. Callers on the engine side map their own enum to
 * this one at the call boundary.
 */
public enum ScriptOrigin {
  USER,
  PROCESS_APPLICATION,
  PLATFORM,
  UNKNOWN
}
