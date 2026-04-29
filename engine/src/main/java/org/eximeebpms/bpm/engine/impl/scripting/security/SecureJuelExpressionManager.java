package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.Map;
import java.util.Objects;
import org.eximeebpms.bpm.engine.impl.el.Expression;
import org.eximeebpms.bpm.engine.impl.el.JuelExpressionManager;

public class SecureJuelExpressionManager extends JuelExpressionManager implements ScriptSecurityAware {

  private ScriptSecurityPolicy scriptSecurityPolicy;

  public SecureJuelExpressionManager(Map<Object, Object> beans, ScriptSecurityPolicy scriptSecurityPolicy) {

    super(beans);
    this.scriptSecurityPolicy = Objects.requireNonNull(scriptSecurityPolicy, "scriptSecurityPolicy must not be null");
  }

  @Override
  public void setScriptSecurityPolicy(ScriptSecurityPolicy scriptSecurityPolicy) {
    this.scriptSecurityPolicy = scriptSecurityPolicy;
  }

  @Override
  public Expression createExpression(String expression) {
    enforceExpressionSecurity(expression);
    return super.createExpression(expression);
  }

  private void enforceExpressionSecurity(String expression) {
    if (scriptSecurityPolicy == null) {
      return;
    }

    ScriptSecurityContext context = ScriptSecurityContext.builder("juel")
        .source(expression)
        .sourceType(ScriptSourceType.EXPRESSION)
        .build();

    ScriptSecurityDecision decision = scriptSecurityPolicy.evaluate(context);

    if (decision.isDenied()) {
      throw new ScriptSecurityException("Expression blocked by script security policy: " + decision.getReason().orElse("unknown"));
    }
  }
}
