package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.eximeebpms.bpm.engine.impl.el.Expression;
import org.eximeebpms.bpm.engine.impl.el.JuelExpressionManager;
import org.eximeebpms.bpm.impl.juel.jakarta.el.ValueExpression;

@Getter
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
    ensureInitialized();
    ValueExpression valueExpression = createValueExpression(expression);
    return new SecureJuelExpression(valueExpression, this, expression, this::getScriptSecurityPolicy);
  }
}
