package org.eximeebpms.bpm.engine.impl.scripting.env;

import java.util.Objects;
import org.eximeebpms.bpm.engine.impl.scripting.ExecutableScript;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptOrigin;

public record ResolvedExecutableEnvScript(
    ExecutableScript executableScript,
    ScriptOrigin origin,
    String provider) {

  public ResolvedExecutableEnvScript {
    Objects.requireNonNull(executableScript, "executableScript must not be null");
    origin = Objects.requireNonNullElse(origin, ScriptOrigin.USER);
  }
}
