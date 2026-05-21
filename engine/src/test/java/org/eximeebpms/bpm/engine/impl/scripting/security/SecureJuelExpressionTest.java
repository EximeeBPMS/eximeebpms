package org.eximeebpms.bpm.engine.impl.scripting.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import lombok.Getter;
import org.eximeebpms.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.eximeebpms.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.eximeebpms.bpm.engine.impl.core.variable.scope.VariableInstanceFactory;
import org.eximeebpms.bpm.engine.impl.core.variable.scope.VariableInstanceLifecycleListener;
import org.eximeebpms.bpm.engine.impl.core.variable.scope.VariableStore;
import org.eximeebpms.bpm.engine.impl.el.Expression;
import org.junit.Test;

public class SecureJuelExpressionTest {

  @Test
  public void shouldBlockForbiddenExpressionOnGetValue() {
    // given
    SecureJuelExpressionManager expressionManager =
        new SecureJuelExpressionManager(null, new DefaultScriptSecurityPolicy());

    Expression expression = expressionManager.createExpression("${System.getenv('HOME')}");

    // when / then
    assertThatThrownBy(() -> expression.getValue(new TestVariableScope("invoiceProcess:1:123")))
        .isInstanceOf(ScriptSecurityException.class)
        .hasMessageContaining("Expression blocked by script security policy")
        .hasMessageContaining("Access to environment variables is forbidden");
  }

  @Test
  public void shouldPassProcessDefinitionKeyToPolicy() {
    // given
    CapturingDenyPolicy policy = new CapturingDenyPolicy();

    SecureJuelExpressionManager expressionManager =
        new SecureJuelExpressionManager(null, policy);

    Expression expression = expressionManager.createExpression("${1 + 1}");

    // when / then
    assertThatThrownBy(() -> expression.getValue(new TestVariableScope("legacyInvoiceProcess:1:123")))
        .isInstanceOf(ScriptSecurityException.class);
    assertThat(policy.capturedContext).isNotNull();
    assertThat(policy.capturedContext.getProcessDefinitionKey())
        .contains("legacyInvoiceProcess");
  }

  private static class CapturingDenyPolicy implements ScriptSecurityPolicy {

    private ScriptSecurityContext capturedContext;

    @Override
    public ScriptSecurityDecision evaluate(ScriptSecurityContext context) {
      this.capturedContext = context;
      return ScriptSecurityDecision.deny("blocked for test", "TEST_BLOCKED");
    }
  }

  @Getter
  static class TestVariableScope extends AbstractVariableScope {

    private final String processDefinitionId;

    private TestVariableScope(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
    }

    @Override
    protected VariableStore<CoreVariableInstance> getVariableStore() {
      return null;
    }

    @Override
    protected VariableInstanceFactory<CoreVariableInstance> getVariableInstanceFactory() {
      return null;
    }

    @Override
    protected List<VariableInstanceLifecycleListener<CoreVariableInstance>> getVariableInstanceLifecycleListeners() {
      return List.of();
    }

    @Override
    public AbstractVariableScope getParentVariableScope() {
      return null;
    }
  }
}
