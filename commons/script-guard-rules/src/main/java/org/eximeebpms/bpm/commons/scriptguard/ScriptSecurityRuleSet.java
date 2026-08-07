package org.eximeebpms.bpm.commons.scriptguard;

/**
 * Validates a script/expression's source against the Script Guard rule set,
 * independently of any running process engine — no dependency on the
 * process-engine runtime. See {@link DefaultScriptSecurityRuleSet} for the
 * shipped rule set, which is the same one the EximeeBPMS engine's own
 * {@code DefaultScriptSecurityPolicy} enforces.
 */
public interface ScriptSecurityRuleSet {

  ScriptValidationResult validate(String source, ScriptOrigin origin);
}
