package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class DefaultScriptSecurityPolicy implements ScriptSecurityPolicy {

  private static final List<Rule> RULES = List.of(
      // Environment and JVM/system access
      Rule.contains("system.getenv", "Access to environment variables is forbidden", "SCRIPT_SECURITY_SYSTEM_GETENV"),
      Rule.contains("system.getproperty", "Access to JVM system properties is forbidden", "SCRIPT_SECURITY_SYSTEM_GET_PROPERTY"),
      Rule.contains("packages.java.lang.system", "Access to JVM system APIs is forbidden", "SCRIPT_SECURITY_PACKAGES_SYSTEM"),

      // Process execution
      Rule.contains("runtime.getruntime", "Runtime process execution is forbidden", "SCRIPT_SECURITY_RUNTIME_EXEC"),
      Rule.contains("java.lang.runtime", "Access to java.lang.Runtime is forbidden", "SCRIPT_SECURITY_RUNTIME"),
      Rule.contains("processbuilder", "Process execution via ProcessBuilder is forbidden", "SCRIPT_SECURITY_PROCESS_BUILDER"),

      // Dynamic class loading / class loader access
      Rule.contains("class.forname", "Dynamic class loading is forbidden", "SCRIPT_SECURITY_CLASS_FOR_NAME"),
      Rule.contains("getclassloader", "Access to class loaders is forbidden", "SCRIPT_SECURITY_CLASS_LOADER"),
      Rule.contains("packages.java.lang.class", "Dynamic class access is forbidden", "SCRIPT_SECURITY_PACKAGES_CLASS"),

      // Reflection
      Rule.contains("java.lang.reflect.", "Reflection access is forbidden", "SCRIPT_SECURITY_REFLECTION"),
      Rule.contains("getdeclaredmethod", "Reflective method access is forbidden", "SCRIPT_SECURITY_REFLECTION_METHOD"),
      Rule.contains("getdeclaredfield", "Reflective field access is forbidden", "SCRIPT_SECURITY_REFLECTION_FIELD"),

      // File system
      Rule.contains("java.io.", "File system access is forbidden", "SCRIPT_SECURITY_JAVA_IO"),
      Rule.contains("java.nio.", "NIO access is forbidden", "SCRIPT_SECURITY_JAVA_NIO"),
      Rule.contains("java.nio.file.", "File system access via NIO is forbidden", "SCRIPT_SECURITY_JAVA_NIO_FILE"),
      Rule.contains("packages.java.io.", "File system access is forbidden", "SCRIPT_SECURITY_PACKAGES_JAVA_IO"),
      Rule.contains("packages.java.nio.", "NIO access is forbidden", "SCRIPT_SECURITY_PACKAGES_JAVA_NIO"),

      // Network
      Rule.contains("java.net.", "Network access is forbidden", "SCRIPT_SECURITY_JAVA_NET"),
      Rule.contains("packages.java.net.", "Network access is forbidden", "SCRIPT_SECURITY_PACKAGES_JAVA_NET"),
      Rule.contains("urlconnection", "Network access is forbidden", "SCRIPT_SECURITY_URL_CONNECTION"),
      Rule.contains("httpclient", "HTTP client access is forbidden", "SCRIPT_SECURITY_HTTP_CLIENT"),
      Rule.contains("socket(", "Socket access is forbidden", "SCRIPT_SECURITY_SOCKET"),
      Rule.contains("serversocket", "Server socket access is forbidden", "SCRIPT_SECURITY_SERVER_SOCKET"),

      // Script-engine / Groovy-specific escalation points
      Rule.contains("groovyshell", "Dynamic Groovy shell execution is forbidden", "SCRIPT_SECURITY_GROOVY_SHELL"),
      Rule.contains("metaclass", "Groovy metaclass access is forbidden", "SCRIPT_SECURITY_GROOVY_METACLASS"),
      Rule.contains("evaluate(", "Dynamic script evaluation is forbidden", "SCRIPT_SECURITY_DYNAMIC_EVALUATE")
  );

  @Override
  public ScriptSecurityDecision evaluate(ScriptSecurityContext context) {
    Objects.requireNonNull(context, "context must not be null");

    String normalizedSource = normalize(context.getSource());
    if (normalizedSource.isEmpty()) {
      return ScriptSecurityDecision.allow();
    }

    return RULES.stream()
        .map(rule -> rule.evaluate(normalizedSource))
        .flatMap(Optional::stream)
        .findFirst()
        .orElseGet(ScriptSecurityDecision::allow);
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

  private record Rule(String token, String reason, String code) {

    private static Rule contains(String token, String reason, String code) {
      return new Rule(
          stripWhitespace(Objects.requireNonNull(token, "token must not be null").toLowerCase(Locale.ROOT)),
          Objects.requireNonNull(reason, "reason must not be null"),
          Objects.requireNonNull(code, "code must not be null"));
    }

    private Optional<ScriptSecurityDecision> evaluate(String normalizedSource) {
      if (normalizedSource.contains(token)) {
        return Optional.of(ScriptSecurityDecision.deny(reason, code));
      }
      return Optional.empty();
    }
  }
}
