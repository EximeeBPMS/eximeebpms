package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DefaultScriptSecurityPolicy implements ScriptSecurityPolicy {

  private static final Pattern SYSTEM_GETENV_PATTERN =
      Pattern.compile("(?s).*\\b(?:java\\.lang\\.)?System\\s*\\.\\s*getenv\\s*\\(.*");

  private static final Pattern SYSTEM_GET_PROPERTY_PATTERN =
      Pattern.compile("(?s).*\\b(?:java\\.lang\\.)?System\\s*\\.\\s*getProperty\\s*\\(.*");

  private static final List<Rule> RULES = List.of(
      Rule.contains("load(", "Loading external scripts is forbidden", "SCRIPT_SECURITY_LOAD"),

      Rule.contains("newjava.net.", "Network access is forbidden", "SCRIPT_SECURITY_JAVA_NET"),
      Rule.contains("newjava.io.", "File system access is forbidden", "SCRIPT_SECURITY_JAVA_IO"),
      Rule.contains("newjava.nio.", "NIO access is forbidden", "SCRIPT_SECURITY_JAVA_NIO"),
      Rule.contains("newjava.lang.reflect.", "Reflection access is forbidden", "SCRIPT_SECURITY_REFLECTION"),
      Rule.contains("newjava.lang.processbuilder", "Process execution via ProcessBuilder is forbidden", "SCRIPT_SECURITY_PROCESS_BUILDER"),
      Rule.contains("newjava.lang.runtime", "Access to java.lang.Runtime is forbidden", "SCRIPT_SECURITY_RUNTIME"),
      Rule.contains("newjava.lang.system", "Access to JVM system APIs is forbidden", "SCRIPT_SECURITY_JAVA_LANG_SYSTEM"),
      Rule.contains("newjava.", "Direct Java object creation is forbidden", "SCRIPT_SECURITY_NEW_JAVA"),

      Rule.contains("java.type('java.lang.system'", "Access to JVM system APIs is forbidden", "SCRIPT_SECURITY_JAVA_LANG_SYSTEM"),
      Rule.contains("java.type(\"java.lang.system\"", "Access to JVM system APIs is forbidden", "SCRIPT_SECURITY_JAVA_LANG_SYSTEM"),
      Rule.contains("java.type('java.lang.runtime'", "Access to java.lang.Runtime is forbidden", "SCRIPT_SECURITY_RUNTIME"),
      Rule.contains("java.type(\"java.lang.runtime\"", "Access to java.lang.Runtime is forbidden", "SCRIPT_SECURITY_RUNTIME"),
      Rule.contains("java.type('java.lang.processbuilder'", "Process execution via ProcessBuilder is forbidden", "SCRIPT_SECURITY_PROCESS_BUILDER"),
      Rule.contains("java.type(\"java.lang.processbuilder\"", "Process execution via ProcessBuilder is forbidden", "SCRIPT_SECURITY_PROCESS_BUILDER"),
      Rule.contains("java.type('java.net.", "Network access is forbidden", "SCRIPT_SECURITY_JAVA_NET"),
      Rule.contains("java.type(\"java.net.", "Network access is forbidden", "SCRIPT_SECURITY_JAVA_NET"),
      Rule.contains("java.type('java.io.", "File system access is forbidden", "SCRIPT_SECURITY_JAVA_IO"),
      Rule.contains("java.type(\"java.io.", "File system access is forbidden", "SCRIPT_SECURITY_JAVA_IO"),
      Rule.contains("java.type('java.nio.", "NIO access is forbidden", "SCRIPT_SECURITY_JAVA_NIO"),
      Rule.contains("java.type(\"java.nio.", "NIO access is forbidden", "SCRIPT_SECURITY_JAVA_NIO"),

      Rule.contains("system.exit", "JVM shutdown is forbidden", "SCRIPT_SECURITY_SYSTEM_EXIT"),
      Rule.contains("system.getenv", "Access to environment variables is forbidden", "SCRIPT_SECURITY_SYSTEM_GETENV"),
      Rule.contains("system.getproperty", "Access to JVM system properties is forbidden", "SCRIPT_SECURITY_SYSTEM_GET_PROPERTY"),
      Rule.contains("java.lang.system.getenv", "Access to environment variables is forbidden", "SCRIPT_SECURITY_SYSTEM_GETENV"),
      Rule.contains("java.lang.system.getproperty", "Access to JVM system properties is forbidden", "SCRIPT_SECURITY_SYSTEM_GET_PROPERTY"),
      Rule.contains("packages.java.lang.system", "Access to JVM system APIs is forbidden", "SCRIPT_SECURITY_JAVA_LANG_SYSTEM"),

      Rule.contains("runtime.getruntime", "Runtime process execution is forbidden", "SCRIPT_SECURITY_RUNTIME_EXEC"),
      Rule.contains("java.lang.runtime", "Access to java.lang.Runtime is forbidden", "SCRIPT_SECURITY_RUNTIME"),
      Rule.contains("processbuilder", "Process execution via ProcessBuilder is forbidden", "SCRIPT_SECURITY_PROCESS_BUILDER"),

      Rule.contains("class.forname", "Dynamic class loading is forbidden", "SCRIPT_SECURITY_CLASS_FOR_NAME"),
      Rule.contains("getclassloader", "Access to class loaders is forbidden", "SCRIPT_SECURITY_CLASS_LOADER"),
      Rule.contains("packages.java.lang.class", "Dynamic class access is forbidden", "SCRIPT_SECURITY_PACKAGES_CLASS"),

      Rule.contains("java.lang.reflect.", "Reflection access is forbidden", "SCRIPT_SECURITY_REFLECTION"),
      Rule.contains("getdeclaredmethod", "Reflective method access is forbidden", "SCRIPT_SECURITY_REFLECTION_METHOD"),
      Rule.contains("getdeclaredfield", "Reflective field access is forbidden", "SCRIPT_SECURITY_REFLECTION_FIELD"),

      Rule.contains("java.io.", "File system access is forbidden", "SCRIPT_SECURITY_JAVA_IO"),
      Rule.contains("java.nio.", "NIO access is forbidden", "SCRIPT_SECURITY_JAVA_NIO"),
      Rule.contains("java.nio.file.", "File system access via NIO is forbidden", "SCRIPT_SECURITY_JAVA_NIO_FILE"),
      Rule.contains("packages.java.io.", "File system access is forbidden", "SCRIPT_SECURITY_JAVA_IO"),
      Rule.contains("packages.java.nio.", "NIO access is forbidden", "SCRIPT_SECURITY_JAVA_NIO"),

      Rule.contains("java.net.", "Network access is forbidden", "SCRIPT_SECURITY_JAVA_NET"),
      Rule.contains("packages.java.net.", "Network access is forbidden", "SCRIPT_SECURITY_JAVA_NET"),
      Rule.contains("urlconnection", "Network access is forbidden", "SCRIPT_SECURITY_URL_CONNECTION"),
      Rule.contains("httpclient", "HTTP client access is forbidden", "SCRIPT_SECURITY_HTTP_CLIENT"),
      Rule.contains("newsocket(", "Socket access is forbidden", "SCRIPT_SECURITY_SOCKET"),
      Rule.contains("serversocket", "Server socket access is forbidden", "SCRIPT_SECURITY_SERVER_SOCKET"),

      Rule.contains("groovyshell", "Dynamic Groovy shell execution is forbidden", "SCRIPT_SECURITY_GROOVY_SHELL"),
      Rule.contains("metaclass", "Groovy metaclass access is forbidden", "SCRIPT_SECURITY_GROOVY_METACLASS")
  );

  private final List<Rule> denyRules;
  private final Set<String> allowlistedProcessDefinitionKeys;

  public DefaultScriptSecurityPolicy() {
    this(RULES, Set.of());
  }

  public DefaultScriptSecurityPolicy(Set<String> allowlistedProcessDefinitionKeys) {
    this(RULES, allowlistedProcessDefinitionKeys);
  }

  DefaultScriptSecurityPolicy(List<Rule> denyRules, Set<String> allowlistedProcessDefinitionKeys) {
    this.denyRules = List.copyOf(Objects.requireNonNull(denyRules, "denyRules must not be null"));
    this.allowlistedProcessDefinitionKeys = normalizeProcessDefinitionKeys(allowlistedProcessDefinitionKeys);
  }

  @Override
  public ScriptSecurityDecision evaluate(ScriptSecurityContext context) {
    Objects.requireNonNull(context, "context must not be null");

    if (isAllowlistedProcess(context)) {
      logAllowlisted(context);
      return ScriptSecurityDecision.allow();
    }

    String source = context.getSource();
    String normalizedSource = normalize(source);

    if (normalizedSource.isEmpty()) {
      logAllowed(context);
      return ScriptSecurityDecision.allow();
    }

    ScriptSecurityDecision regexDecision = evaluateRegexRules(context, source);
    if (regexDecision != null) {
      return regexDecision;
    }

    ScriptSecurityDecision ruleDecision = evaluateDenyRules(context, normalizedSource);
    if (ruleDecision != null) {
      return ruleDecision;
    }

    if (containsHostClassLookup(normalizedSource)
        && !isAllowedPlatformJavaTypeLookup(context, normalizedSource)) {
      ScriptSecurityDecision decision = ScriptSecurityDecision.deny(
          "Host class lookup is forbidden",
          "SCRIPT_SECURITY_JAVA_TYPE");
      logDenied(context, decision);
      return decision;
    }

    logAllowed(context);
    return ScriptSecurityDecision.allow();
  }

  private ScriptSecurityDecision evaluateRegexRules(ScriptSecurityContext context, String source) {
    if (SYSTEM_GETENV_PATTERN.matcher(source).matches()) {
      ScriptSecurityDecision decision = ScriptSecurityDecision.deny(
          "Access to environment variables is forbidden",
          "SCRIPT_SECURITY_SYSTEM_GETENV");
      logDenied(context, decision);
      return decision;
    }

    if (SYSTEM_GET_PROPERTY_PATTERN.matcher(source).matches()) {
      ScriptSecurityDecision decision = ScriptSecurityDecision.deny(
          "Access to JVM system properties is forbidden",
          "SCRIPT_SECURITY_SYSTEM_GET_PROPERTY");
      logDenied(context, decision);
      return decision;
    }

    return null;
  }

  private ScriptSecurityDecision evaluateDenyRules(
      ScriptSecurityContext context,
      String normalizedSource) {

    ScriptSecurityDecision decision = denyRules.stream()
        .map(rule -> rule.evaluate(normalizedSource))
        .flatMap(Optional::stream)
        .findFirst()
        .orElse(null);

    if (decision != null) {
      logDenied(context, decision);
    }

    return decision;
  }

  private boolean containsHostClassLookup(String normalizedSource) {
    return normalizedSource.contains("java.type(")
        || normalizedSource.contains("packages.");
  }

  private boolean isAllowedPlatformJavaTypeLookup(
      ScriptSecurityContext context,
      String normalizedSource) {

    if (!context.isPlatformOrigin()) {
      return false;
    }

    return normalizedSource.contains("java.type('org.eximeebpms.spin.spin'")
        || normalizedSource.contains("java.type(\"org.eximeebpms.spin.spin\"");
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

  private static String normalize(String source) {
    return Optional.ofNullable(source)
        .map(value -> value.toLowerCase(Locale.ROOT))
        .map(DefaultScriptSecurityPolicy::stripWhitespace)
        .orElse("");
  }

  private static String stripWhitespace(String value) {
    StringBuilder normalized = new StringBuilder(value.length());

    for (int index = 0; index < value.length(); index++) {
      char current = value.charAt(index);
      if (!Character.isWhitespace(current)) {
        normalized.append(current);
      }
    }

    return normalized.toString();
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

  private record Rule(String token, String reason, String code) {

    private Rule {
      token = stripWhitespace(Objects.requireNonNull(token, "token must not be null").toLowerCase(Locale.ROOT));
      Objects.requireNonNull(reason, "reason must not be null");
      Objects.requireNonNull(code, "code must not be null");
    }

    private static Rule contains(String token, String reason, String code) {
      return new Rule(token, reason, code);
    }

    private Optional<ScriptSecurityDecision> evaluate(String normalizedSource) {
      if (normalizedSource.contains(token)) {
        return Optional.of(ScriptSecurityDecision.deny(reason, code));
      }
      return Optional.empty();
    }
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
    log.warn(
        "Script security policy denied execution. sourceType={}, language={}, origin={}, provider={}, processDefinitionKey={}, ruleCode={}, reason={}",
        context.getSourceType(),
        context.getLanguage(),
        context.getOrigin(),
        context.getProvider().orElse(null),
        context.getProcessDefinitionKey().orElse(null),
        decision.getCode().orElse(null),
        decision.getReason().orElse(null));
  }
}
