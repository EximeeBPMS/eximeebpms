package org.eximeebpms.bpm.engine.impl.scripting.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.eximeebpms.bpm.engine.impl.el.Expression;
import org.junit.Before;
import org.junit.Test;

public class SecureJuelExpressionManagerTest {

  private SecureJuelExpressionManager expressionManager;

  @Before
  public void setUp() {
    expressionManager = new SecureJuelExpressionManager(Map.of(), new DefaultScriptSecurityPolicy());
  }

  @Test
  public void shouldCreateSafeExpression() {
    // when
    Expression expression = expressionManager.createExpression("${1 + 1}");

    // then
    assertThat(expression).isNotNull();
  }

  @Test
  public void shouldBlockForbiddenExpression() {
    assertThatThrownBy(() -> expressionManager.createExpression("${System.getenv('HOME')}"))
        .isInstanceOf(ScriptSecurityException.class)
        .hasMessageContaining("Expression blocked by script security policy")
        .hasMessageContaining("Access to environment variables is forbidden");
  }
}
