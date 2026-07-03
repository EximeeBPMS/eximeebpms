package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.Optional;
import java.util.function.Supplier;
import org.eximeebpms.bpm.engine.delegate.BaseDelegateExecution;
import org.eximeebpms.bpm.engine.impl.ProcessEngineLogger;
import org.eximeebpms.bpm.engine.impl.scripting.ScriptLogger;
import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.VariableScope;
import org.eximeebpms.bpm.engine.impl.el.JuelExpression;
import org.eximeebpms.bpm.engine.impl.el.JuelExpressionManager;
import org.eximeebpms.bpm.engine.impl.persistence.entity.TaskEntity;
import org.eximeebpms.bpm.impl.juel.jakarta.el.ValueExpression;

public class SecureJuelExpression extends JuelExpression {

  private static final ScriptLogger LOG = ProcessEngineLogger.SCRIPT_LOGGER;

  private final Supplier<ScriptSecurityPolicy> scriptSecurityPolicySupplier;

  public SecureJuelExpression(
      ValueExpression valueExpression,
      JuelExpressionManager expressionManager,
      String expressionText,
      Supplier<ScriptSecurityPolicy> scriptSecurityPolicySupplier) {

    super(valueExpression, expressionManager, expressionText);
    this.scriptSecurityPolicySupplier = scriptSecurityPolicySupplier;
  }

  @Override
  public Object getValue(VariableScope variableScope, BaseDelegateExecution contextExecution) {
    enforceExpressionSecurity(variableScope, contextExecution);
    return super.getValue(variableScope, contextExecution);
  }

  @Override
  public void setValue(Object value, VariableScope variableScope, BaseDelegateExecution contextExecution) {
    enforceExpressionSecurity(variableScope, contextExecution);
    super.setValue(value, variableScope, contextExecution);
  }

  private void enforceExpressionSecurity(VariableScope variableScope, BaseDelegateExecution contextExecution) {
    ScriptSecurityPolicy scriptSecurityPolicy = scriptSecurityPolicySupplier.get();

    if (scriptSecurityPolicy == null) {
      return;
    }

    ScriptSecurityDecision decision = scriptSecurityPolicy.evaluate(
        ScriptSecurityContext.builder("juel")
            .source(expressionText)
            .sourceType(ScriptSourceType.EXPRESSION)
            .processDefinitionKey(resolveProcessDefinitionKey(variableScope, contextExecution).orElse(null))
            .build());

    if (decision.isAudit()) {
      LOG.warnExpressionAllowedByAuditMode(decision.getReason().orElse("unknown"));
    } else if (decision.isDenied()) {
      throw new ScriptSecurityException(
          "Expression blocked by script security policy: " + decision.getReason().orElse("unknown"),
          decision.getCode().orElse(null)
      );
    }
  }

  private Optional<String> resolveProcessDefinitionKey(
      VariableScope variableScope,
      BaseDelegateExecution contextExecution) {

    if (contextExecution instanceof DelegateExecution execution) {
      return Optional.ofNullable(execution.getProcessDefinitionId())
          .map(this::extractProcessDefinitionKey);
    }

    if (variableScope instanceof DelegateExecution execution) {
      return Optional.ofNullable(execution.getProcessDefinitionId())
          .map(this::extractProcessDefinitionKey);
    }

    if (variableScope instanceof TaskEntity task) {
      return Optional.ofNullable(task.getProcessDefinitionId())
          .map(this::extractProcessDefinitionKey);
    }

    return resolveProcessDefinitionKeyReflectively(variableScope);
  }

  private Optional<String> resolveProcessDefinitionKeyReflectively(VariableScope variableScope) {
    if (variableScope == null) {
      return Optional.empty();
    }

    try {
      Object processDefinitionId = variableScope.getClass()
          .getMethod("getProcessDefinitionId")
          .invoke(variableScope);

      return Optional.ofNullable(processDefinitionId)
          .map(String::valueOf)
          .map(this::extractProcessDefinitionKey);
    } catch (ReflectiveOperationException ignored) {
      return Optional.empty();
    }
  }

  private String extractProcessDefinitionKey(String processDefinitionId) {
    int separatorIndex = processDefinitionId.indexOf(':');
    if (separatorIndex < 0) {
      return processDefinitionId;
    }
    return processDefinitionId.substring(0, separatorIndex);
  }
}
