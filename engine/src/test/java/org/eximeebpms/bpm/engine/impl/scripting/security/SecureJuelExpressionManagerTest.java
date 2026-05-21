package org.eximeebpms.bpm.engine.impl.scripting.security;

import static org.assertj.core.api.Assertions.assertThat;

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
}
