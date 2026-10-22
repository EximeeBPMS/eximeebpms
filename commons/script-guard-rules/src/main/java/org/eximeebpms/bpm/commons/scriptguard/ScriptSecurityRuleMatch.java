package org.eximeebpms.bpm.commons.scriptguard;

import java.util.Objects;

/**
 * A single rule match — same {@code ruleCode}/{@code reason} vocabulary the
 * EximeeBPMS engine's own {@code DefaultScriptSecurityPolicy} uses, so a
 * match reported here is directly comparable to one reported by the engine.
 */
public record ScriptSecurityRuleMatch(String ruleCode, String reason) {

  public ScriptSecurityRuleMatch {
    Objects.requireNonNull(ruleCode, "ruleCode must not be null");
    Objects.requireNonNull(reason, "reason must not be null");
  }
}
