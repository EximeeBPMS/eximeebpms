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

  private static final Pattern SYSTEM_GETENV_PATTERN = Pattern.compile("(?s).*\\b(?:java\\.lang\\.)?System\\s*\\.\\s*getenv\\s*\\(.*");
  private static final Pattern SYSTEM_GET_PROPERTY_PATTERN = Pattern.compile("(?s).*\\b(?:java\\.lang\\.)?System\\s*\\.\\s*getProperty\\s*\\(.*");
  private static final Pattern JAVA_TYPE_PATTERN = Pattern.compile("\\bjava\\.type\\s*\\(", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern PACKAGES_PATTERN = Pattern.compile("\\bpackages\\.", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  private static final List<Rule> RULES = List.of(
      Rule.pattern("\\bload\\s*\\(", "Loading external scripts is forbidden", "SCRIPT_SECURITY_LOAD"),

      // Reflection / dynamic class loading first, because patterns like
      // Class.forName("java.lang.Runtime") also contain java.lang.Runtime.
      Rule.pattern("\\bclass\\s*\\.\\s*forname\\s*\\(", "Dynamic class loading is forbidden", "SCRIPT_SECURITY_CLASS_FOR_NAME"),
      Rule.pattern("\\bgetclassloader\\s*\\(", "Access to class loaders is forbidden", "SCRIPT_SECURITY_CLASS_LOADER"),
      Rule.pattern("\\bjava\\.lang\\.reflect\\.", "Reflection access is forbidden", "SCRIPT_SECURITY_REFLECTION"),
      Rule.pattern("\\bgetdeclaredmethod\\s*\\(", "Reflective method access is forbidden", "SCRIPT_SECURITY_REFLECTION_METHOD"),
      Rule.pattern("\\bgetdeclaredfield\\s*\\(", "Reflective field access is forbidden", "SCRIPT_SECURITY_REFLECTION_FIELD"),

      // Process execution
      Rule.pattern("\\bnew\\s+java\\.lang\\.processbuilder\\b", "Process execution via ProcessBuilder is forbidden", "SCRIPT_SECURITY_PROCESS_BUILDER"),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.lang\\.processbuilder['\"]\\s*\\)", "Process execution via ProcessBuilder is forbidden", "SCRIPT_SECURITY_PROCESS_BUILDER"),
      Rule.pattern("\\bprocessbuilder\\b", "Process execution via ProcessBuilder is forbidden", "SCRIPT_SECURITY_PROCESS_BUILDER"),

      // Runtime / System
      Rule.pattern("\\bjava\\.lang\\.runtime\\b", "Access to java.lang.Runtime is forbidden", "SCRIPT_SECURITY_RUNTIME"),
      Rule.pattern("\\bruntime\\s*\\.\\s*getruntime\\s*\\(", "Runtime process execution is forbidden", "SCRIPT_SECURITY_RUNTIME_EXEC"),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.lang\\.runtime['\"]\\s*\\)", "Access to java.lang.Runtime is forbidden", "SCRIPT_SECURITY_RUNTIME"),

      Rule.pattern("\\bjava\\.lang\\.system\\b", "Access to JVM system APIs is forbidden", "SCRIPT_SECURITY_JAVA_LANG_SYSTEM"),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.lang\\.system['\"]\\s*\\)", "Access to JVM system APIs is forbidden", "SCRIPT_SECURITY_JAVA_LANG_SYSTEM"),
      Rule.pattern("\\bsystem\\s*\\.\\s*exit\\s*\\(", "JVM shutdown is forbidden", "SCRIPT_SECURITY_SYSTEM_EXIT"),
      Rule.pattern("\\bsystem\\s*\\.\\s*getenv\\s*\\(", "Access to environment variables is forbidden", "SCRIPT_SECURITY_SYSTEM_GETENV"),
      Rule.pattern("\\bsystem\\s*\\.\\s*getproperty\\s*\\(", "Access to JVM system properties is forbidden", "SCRIPT_SECURITY_SYSTEM_GET_PROPERTY"),

      // Filesystem
      Rule.pattern("\\bnew\\s+java\\.io\\.", "File system access is forbidden", "SCRIPT_SECURITY_JAVA_IO"),
      Rule.pattern("\\bjava\\.io\\.", "File system access is forbidden", "SCRIPT_SECURITY_JAVA_IO"),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.io\\.", "File system access is forbidden", "SCRIPT_SECURITY_JAVA_IO"),

      // NIO filesystem
      Rule.pattern("\\bnew\\s+java\\.nio\\.file\\.", "NIO file system access is forbidden", "SCRIPT_SECURITY_JAVA_NIO_FILE"),
      Rule.pattern("\\bjava\\.nio\\.file\\.", "NIO file system access is forbidden", "SCRIPT_SECURITY_JAVA_NIO_FILE"),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.nio\\.file\\.", "NIO file system access is forbidden", "SCRIPT_SECURITY_JAVA_NIO_FILE"),

      // NIO file channels
      Rule.pattern("\\bjava\\.nio\\.channels\\.(?:asynchronousfilechannel|filechannel)\\b", "NIO file channel access is forbidden", "SCRIPT_SECURITY_JAVA_NIO_FILE_CHANNEL"),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.nio\\.channels\\.(?:asynchronousfilechannel|filechannel)['\"]\\s*\\)", "NIO file channel access is forbidden", "SCRIPT_SECURITY_JAVA_NIO_FILE_CHANNEL"),

      // NIO network channels
      Rule.pattern("\\bjava\\.nio\\.channels\\.(?:socketchannel|serversocketchannel|datagramchannel|asynchronoussocketchannel|asynchronousserversocketchannel)\\b", "NIO network channel access is forbidden", "SCRIPT_SECURITY_JAVA_NIO_NETWORK_CHANNEL"),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.nio\\.channels\\.(?:socketchannel|serversocketchannel|datagramchannel|asynchronoussocketchannel|asynchronousserversocketchannel)['\"]\\s*\\)", "NIO network channel access is forbidden", "SCRIPT_SECURITY_JAVA_NIO_NETWORK_CHANNEL"),

      // Network
      Rule.pattern("\\bnew\\s+java\\.net\\.", "Network access is forbidden", "SCRIPT_SECURITY_JAVA_NET"),
      Rule.pattern("\\bjava\\.net\\.", "Network access is forbidden", "SCRIPT_SECURITY_JAVA_NET"),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.net\\.", "Network access is forbidden", "SCRIPT_SECURITY_JAVA_NET"),
      Rule.pattern("\\burlconnection\\b", "Network access is forbidden", "SCRIPT_SECURITY_URL_CONNECTION"),
      Rule.pattern("\\bhttpclient\\b", "HTTP client access is forbidden", "SCRIPT_SECURITY_HTTP_CLIENT"),
      Rule.pattern("\\bnew\\s+socket\\s*\\(", "Socket access is forbidden", "SCRIPT_SECURITY_SOCKET"),
      Rule.pattern("\\bserversocket\\b", "Server socket access is forbidden", "SCRIPT_SECURITY_SERVER_SOCKET"),

      // Generic Java construction after specific Java rules.
      Rule.pattern("\\bnew\\s+java\\.", "Direct Java object creation is forbidden", "SCRIPT_SECURITY_NEW_JAVA"),

      // Groovy
      Rule.pattern("\\bgroovyshell\\b", "Dynamic Groovy shell execution is forbidden", "SCRIPT_SECURITY_GROOVY_SHELL"),
      Rule.pattern("\\bmetaclass\\b", "Groovy metaclass access is forbidden", "SCRIPT_SECURITY_GROOVY_METACLASS")
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

    ScriptSecurityDecision hostLookupDecision = evaluateHostClassLookupRules(context, normalizedSource);
    if (hostLookupDecision != null) {
      return hostLookupDecision;
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
        .orElse("");
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

  private record Rule(Pattern pattern, String reason, String code) {

    private Rule {
      Objects.requireNonNull(pattern, "pattern must not be null");
      Objects.requireNonNull(reason, "reason must not be null");
      Objects.requireNonNull(code, "code must not be null");
    }

    private static Rule pattern(String regex, String reason, String code) {
      return new Rule(Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL), reason, code);
    }

    private Optional<ScriptSecurityDecision> evaluate(String source) {
      if (pattern.matcher(source).find()) {
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

  private ScriptSecurityDecision evaluateHostClassLookupRules(
      ScriptSecurityContext context,
      String normalizedSource) {

    boolean usesJavaType = JAVA_TYPE_PATTERN.matcher(normalizedSource).find();
    boolean usesPackages = PACKAGES_PATTERN.matcher(normalizedSource).find();

    if (!usesJavaType && !usesPackages) {
      return null;
    }

    if (context.getOrigin() == ScriptOrigin.PLATFORM) {
      return null;
    }

    ScriptSecurityDecision decision = ScriptSecurityDecision.deny(
        usesPackages
            ? "Host class lookup via Packages is forbidden"
            : "Host class lookup is forbidden",
        "SCRIPT_SECURITY_JAVA_TYPE");

    logDenied(context, decision);
    return decision;
  }
}
