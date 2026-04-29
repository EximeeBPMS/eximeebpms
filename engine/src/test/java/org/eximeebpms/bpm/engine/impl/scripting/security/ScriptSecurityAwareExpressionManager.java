package org.eximeebpms.bpm.engine.impl.scripting.security;

import lombok.Getter;
import org.eximeebpms.bpm.engine.impl.el.JuelExpressionManager;

@Getter
public class ScriptSecurityAwareExpressionManager extends JuelExpressionManager implements ScriptSecurityAware {

  private ScriptSecurityPolicy scriptSecurityPolicy;

  @Override
  public void setScriptSecurityPolicy(ScriptSecurityPolicy scriptSecurityPolicy) {
    this.scriptSecurityPolicy = scriptSecurityPolicy;
  }

}
