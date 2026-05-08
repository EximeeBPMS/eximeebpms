package org.eximeebpms.bpm.engine.impl.scripting.env;

public interface TrustedScriptEnvResolver extends ScriptEnvResolver {

  ResolvedScriptEnvScript[] resolveTrusted(String language);

  @Override
  default String[] resolve(String language) {
    ResolvedScriptEnvScript[] resolvedScripts = resolveTrusted(language);

    if (resolvedScripts == null) {
      return null;
    }

    String[] sources = new String[resolvedScripts.length];

    for (int i = 0; i < resolvedScripts.length; i++) {
      sources[i] = resolvedScripts[i].getSource();
    }

    return sources;
  }
}
