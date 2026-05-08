package org.eximeebpms.bpm.engine.impl.scripting.env;

import java.util.Objects;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptOrigin;

public final class ResolvedScriptEnvScript {

  private final String source;
  private final ScriptOrigin origin;
  private final String provider;

  private ResolvedScriptEnvScript(
      String source,
      ScriptOrigin origin,
      String provider) {
    this.source = Objects.requireNonNull(source, "source must not be null");
    this.origin = Objects.requireNonNullElse(origin, ScriptOrigin.USER);
    this.provider = provider;
  }

  public static ResolvedScriptEnvScript platform(String source, String provider) {
    return new ResolvedScriptEnvScript(source, ScriptOrigin.PLATFORM, provider);
  }

  public static ResolvedScriptEnvScript user(String source, String provider) {
    return new ResolvedScriptEnvScript(source, ScriptOrigin.USER, provider);
  }

  public static ResolvedScriptEnvScript processApplication(String source, String provider) {
    return new ResolvedScriptEnvScript(source, ScriptOrigin.PROCESS_APPLICATION, provider);
  }

  /**
   * Kept for compatibility with existing trusted resolver implementations.
   * In the new model, trusted environment scripts are represented as PLATFORM origin scripts.
   */
  public static ResolvedScriptEnvScript trusted(String source, String provider) {
    return platform(source, provider);
  }

  public String getSource() {
    return source;
  }

  public ScriptOrigin getOrigin() {
    return origin;
  }

  public String getProvider() {
    return provider;
  }
}
