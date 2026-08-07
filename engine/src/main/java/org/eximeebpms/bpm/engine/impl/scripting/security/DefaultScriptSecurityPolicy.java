package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.commons.scriptguard.DefaultScriptSecurityRuleSet;
import org.eximeebpms.bpm.commons.scriptguard.ScriptSecurityRuleMatch;
import org.eximeebpms.bpm.commons.scriptguard.ScriptSecurityRuleSet;
import org.eximeebpms.bpm.commons.scriptguard.ScriptValidationResult;

@Slf4j
public final class DefaultScriptSecurityPolicy implements ScriptSecurityPolicy {

  private final ScriptSecurityRuleSet ruleSet;
  private final Set<String> allowlistedProcessDefinitionKeys;
  private final boolean auditMode;
  private final ScriptViolationStore violationStore;
  private final List<ScriptViolationListener> listeners;

  public DefaultScriptSecurityPolicy() {
    this(Set.of(), false, NoOpScriptViolationStore.INSTANCE, List.of());
  }

  public DefaultScriptSecurityPolicy(Set<String> allowlistedProcessDefinitionKeys) {
    this(allowlistedProcessDefinitionKeys, false, NoOpScriptViolationStore.INSTANCE, List.of());
  }

  public DefaultScriptSecurityPolicy(Set<String> allowlistedProcessDefinitionKeys, boolean auditMode) {
    this(allowlistedProcessDefinitionKeys, auditMode, NoOpScriptViolationStore.INSTANCE, List.of());
  }

  public DefaultScriptSecurityPolicy(
      Set<String> allowlistedProcessDefinitionKeys,
      boolean auditMode,
      ScriptViolationStore violationStore) {
    this(allowlistedProcessDefinitionKeys, auditMode, violationStore, List.of());
  }

  public DefaultScriptSecurityPolicy(
      Set<String> allowlistedProcessDefinitionKeys,
      boolean auditMode,
      ScriptViolationStore violationStore,
      List<ScriptViolationListener> listeners) {
    this.ruleSet = DefaultScriptSecurityRuleSet.INSTANCE;
    this.allowlistedProcessDefinitionKeys = normalizeProcessDefinitionKeys(allowlistedProcessDefinitionKeys);
    this.auditMode = auditMode;
    this.violationStore = Objects.requireNonNull(violationStore, "violationStore must not be null");
    this.listeners = List.copyOf(Objects.requireNonNull(listeners, "listeners must not be null"));
  }

  @Override
  public ScriptSecurityDecision evaluate(ScriptSecurityContext context) {
    Objects.requireNonNull(context, "context must not be null");

    if (isAllowlistedProcess(context)) {
      logAllowlisted(context);
      return ScriptSecurityDecision.allow();
    }

    ScriptValidationResult result = ruleSet.validate(context.getSource(), toRuleSetOrigin(context.getOrigin()));

    if (result.isClean()) {
      logAllowed(context);
      return ScriptSecurityDecision.allow();
    }

    ScriptSecurityRuleMatch match = result.getMatches().get(0);
    ScriptSecurityDecision decision = makeDecision(match.reason(), match.ruleCode());
    logDenied(context, decision);
    return decision;
  }

  private boolean isAllowlistedProcess(ScriptSecurityContext context) {
    if (allowlistedProcessDefinitionKeys.isEmpty()) {
      return false;
    }

    return context.getProcessDefinitionKey()
        .map(DefaultScriptSecurityPolicy::normalizeProcessDefinitionKey)
        .filter(allowlistedProcessDefinitionKeys::contains)
        .isPresent();
  }

  private static org.eximeebpms.bpm.commons.scriptguard.ScriptOrigin toRuleSetOrigin(ScriptOrigin origin) {
    return switch (origin) {
      case USER -> org.eximeebpms.bpm.commons.scriptguard.ScriptOrigin.USER;
      case PROCESS_APPLICATION -> org.eximeebpms.bpm.commons.scriptguard.ScriptOrigin.PROCESS_APPLICATION;
      case PLATFORM -> org.eximeebpms.bpm.commons.scriptguard.ScriptOrigin.PLATFORM;
      case UNKNOWN -> org.eximeebpms.bpm.commons.scriptguard.ScriptOrigin.UNKNOWN;
    };
  }

  private static Set<String> normalizeProcessDefinitionKeys(Set<String> processDefinitionKeys) {
    if (processDefinitionKeys == null || processDefinitionKeys.isEmpty()) {
      return Set.of();
    }

    return processDefinitionKeys.stream()
        .map(DefaultScriptSecurityPolicy::normalizeProcessDefinitionKey)
        .filter(key -> !key.isBlank())
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
  }

  private static String normalizeProcessDefinitionKey(String processDefinitionKey) {
    return Optional.ofNullable(processDefinitionKey)
        .map(String::trim)
        .map(value -> value.toLowerCase(Locale.ROOT))
        .orElse("");
  }

  private void logAllowlisted(ScriptSecurityContext context) {
    log.info(
        "Script security policy skipped for allowlisted processDefinitionKey. sourceType={}, language={}, processDefinitionKey={}",
        context.getSourceType(),
        context.getLanguage(),
        context.getProcessDefinitionKey().orElse(null));
  }

  private void logAllowed(ScriptSecurityContext context) {
    if (log.isDebugEnabled()) {
      log.debug(
          "Script security policy allowed execution. sourceType={}, language={}, origin={}, provider={}, processDefinitionKey={}",
          context.getSourceType(),
          context.getLanguage(),
          context.getOrigin(),
          context.getProvider().orElse(null),
          context.getProcessDefinitionKey().orElse(null));
    }
  }

  private void logDenied(ScriptSecurityContext context, ScriptSecurityDecision decision) {
    final String action = decision.isAudit() ? "audit (execution allowed)" : "denied execution";
    log.warn("Script security policy {}. sourceType={}, language={}, origin={}, provider={}, processDefinitionKey={}, ruleCode={}, reason={}",
        action,
        context.getSourceType(),
        context.getLanguage(),
        context.getOrigin(),
        context.getProvider().orElse(null),
        context.getProcessDefinitionKey().orElse(null),
        decision.getCode().orElse(null),
        decision.getReason().orElse(null));
    final ScriptViolationEvent violation = buildEvent(context, decision);
    violationStore.record(violation);
    listeners.forEach(listener -> {
      try {
        listener.onViolation(violation);
      } catch (Exception e) {
        log.warn("ScriptViolationListener threw an exception, skipping: {}", e.getMessage());
      }
    });
  }

  private ScriptSecurityDecision makeDecision(String reason, String code) {
    return auditMode
        ? ScriptSecurityDecision.audit(reason, code)
        : ScriptSecurityDecision.deny(reason, code);
  }

  private ScriptViolationEvent buildEvent(ScriptSecurityContext context, ScriptSecurityDecision decision) {
    return new ScriptViolationEvent(
        java.time.Instant.now(),
        context.getProcessDefinitionKey().orElse(null),
        context.getProcessDefinitionId().orElse(null),
        context.getActivityId().orElse(null),
        context.getLanguage(),
        context.getSourceType(),
        context.getOrigin(),
        decision.getCode().orElse(null),
        decision.getReason().orElse(null));
  }
}
