package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.Objects;
import org.eximeebpms.bpm.engine.impl.el.ExpressionManager;
import org.eximeebpms.bpm.engine.impl.el.JuelExpression;
import org.eximeebpms.bpm.engine.impl.el.JuelExpressionManager;

public class SecureExpressionManager extends JuelExpressionManager {

  private final ScriptSecurityPolicy scriptSecurityPolicy;

  public SecureExpressionManager(
      ExpressionManager delegate,
      ScriptSecurityPolicy scriptSecurityPolicy) {

    super(delegate.getBeans(), delegate.getFunctions(), delegate.getElProvider());
    this.scriptSecurityPolicy = scriptSecurityPolicy;
  }

  @Override
  public JuelExpression createExpression(String expression) {
    Objects.requireNonNull(expression, "expression must not be null");

    enforceSecurity(expression);

    return super.createExpression(expression);
  }

  private void enforceSecurity(String expression) {
    if (scriptSecurityPolicy == null) {
      return;
    }

    ScriptSecurityContext context = ScriptSecurityContext.builder("juel")
        .source(expression)
        .sourceType(ScriptSourceType.EXPRESSION) // upewnij się że enum ma wartość
        .build();

    ScriptSecurityDecision decision = scriptSecurityPolicy.evaluate(context);

    if (decision.isDenied()) {
      throw new ScriptSecurityException(
          "Expression blocked by script security policy: "
              + decision.getReason().orElse("unknown"));
    }
  }
}
