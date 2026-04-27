package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.Optional;

import org.eximeebpms.bpm.engine.ProcessEngineException;

public class ScriptSecurityException extends ProcessEngineException {

  private static final long serialVersionUID = 1L;

  private final String decisionCode;

  public ScriptSecurityException(String message) {
    this(message, null, null);
  }

  public ScriptSecurityException(String message, String decisionCode) {
    this(message, decisionCode, null);
  }

  public ScriptSecurityException(String message, Throwable cause) {
    this(message, null, cause);
  }

  public ScriptSecurityException(String message, String decisionCode, Throwable cause) {
    super(message, cause);
    this.decisionCode = normalize(decisionCode).orElse(null);
  }

  public ScriptSecurityException(String message, ScriptSecurityDecision decision) {
    this(message, decision.getCode().orElse(null), null);
  }

  public Optional<String> getDecisionCode() {
    return Optional.ofNullable(decisionCode);
  }

  private static Optional<String> normalize(String value) {
    return Optional.ofNullable(value)
        .map(String::trim)
        .filter(normalized -> !normalized.isEmpty());
  }
}
