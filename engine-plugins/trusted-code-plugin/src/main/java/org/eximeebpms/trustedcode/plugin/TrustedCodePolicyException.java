package org.eximeebpms.trustedcode.plugin;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.eximeebpms.bpm.engine.ProcessEngineException;

@Getter
public class TrustedCodePolicyException extends ProcessEngineException {

  private static final int MESSAGE_VIOLATION_LIMIT = 5;

  private static final String UNKNOWN_RULE = "UNKNOWN_RULE";
  private static final String UNKNOWN_ELEMENT = "unknown element";
  private static final String UNKNOWN = "unknown";

  private static final String VIOLATION_MESSAGE_FORMAT = "[%s] %s (%s)";
  private static final String VIOLATION_DETAILS_MESSAGE_FORMAT = "%s; ... and %d more";

  private final List<TrustedCodeViolation> violations;

  public TrustedCodePolicyException(List<TrustedCodeViolation> violations) {
    super(buildMessage(violations));
    this.violations = !isViolationsEmpty(violations) ? List.copyOf(violations) : List.of();
  }

  public TrustedCodePolicyException(String message, List<TrustedCodeViolation> violations) {
    super(message);
    this.violations = !isViolationsEmpty(violations) ? List.copyOf(violations) : List.of();
  }

  private static String buildMessage(List<TrustedCodeViolation> violations) {
    if (isViolationsEmpty(violations)) {
      return "Deployment rejected by trusted-code policy";
    }

    String details = violations.stream()
        .limit(MESSAGE_VIOLATION_LIMIT)
        .map(TrustedCodePolicyException::formatViolation)
        .collect(Collectors.joining("; "));

    if (violations.size() > MESSAGE_VIOLATION_LIMIT) {
      details = VIOLATION_DETAILS_MESSAGE_FORMAT.formatted(details, violations.size() - MESSAGE_VIOLATION_LIMIT);
    }

    return "Deployment rejected by trusted-code policy: %s".formatted(details);
  }

  private static boolean isViolationsEmpty(List<TrustedCodeViolation> violations) {
    return violations == null || violations.isEmpty();
  }

  private static String formatViolation(TrustedCodeViolation violation) {
    String ruleCode = valueOrFallback(violation.getRuleCode(), UNKNOWN_RULE);
    String elementType = valueOrFallback(violation.getElementType(), UNKNOWN_ELEMENT);
    String elementId = valueOrFallback(violation.getElementId(), UNKNOWN);

    return VIOLATION_MESSAGE_FORMAT.formatted(ruleCode, elementType, elementId);
  }

  private static String valueOrFallback(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }
}
