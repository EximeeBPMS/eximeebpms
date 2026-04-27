package org.eximeebpms.bpm.engine.impl.scripting.security;

@FunctionalInterface
public interface ScriptSecurityPolicy {

  ScriptSecurityDecision evaluate(ScriptSecurityContext context);
}
