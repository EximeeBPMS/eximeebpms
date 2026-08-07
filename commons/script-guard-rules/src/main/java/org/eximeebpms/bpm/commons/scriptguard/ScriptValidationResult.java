package org.eximeebpms.bpm.commons.scriptguard;

import java.util.List;

/**
 * Result of validating a script/expression against a {@link ScriptSecurityRuleSet}.
 * Reports rule matches only — never an {@code ALLOW}/{@code AUDIT}/{@code DENY}
 * outcome, since that tri-state depends on a target engine instance's
 * configured enforcement mode, which this module does not know.
 */
public final class ScriptValidationResult {

  private static final ScriptValidationResult CLEAN = new ScriptValidationResult(List.of());

  private final List<ScriptSecurityRuleMatch> matches;

  private ScriptValidationResult(List<ScriptSecurityRuleMatch> matches) {
    this.matches = List.copyOf(matches);
  }

  public static ScriptValidationResult clean() {
    return CLEAN;
  }

  public static ScriptValidationResult matched(ScriptSecurityRuleMatch match) {
    return new ScriptValidationResult(List.of(match));
  }

  public List<ScriptSecurityRuleMatch> getMatches() {
    return matches;
  }

  public boolean isClean() {
    return matches.isEmpty();
  }

  @Override
  public String toString() {
    return "ScriptValidationResult{matches=" + matches + '}';
  }
}
