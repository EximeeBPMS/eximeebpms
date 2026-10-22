package org.eximeebpms.bpm.commons.scriptguard;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The shipped Script Guard rule set — extracted verbatim from the engine's
 * {@code DefaultScriptSecurityPolicy} so this module and the engine agree on
 * every rule match, by construction (one rule set, two callers), not by a
 * maintenance promise between separately maintained copies.
 *
 * <p>Rule precedence is preserved exactly as in the engine: the two
 * case-sensitive {@code System.getenv}/{@code System.getProperty} checks are
 * evaluated against the raw (non-normalized) source <em>before</em> the main
 * rule list, exactly as {@code DefaultScriptSecurityPolicy.evaluate} does —
 * this changes which rule code is reported when a script matches more than
 * one rule, so the order is not incidental.</p>
 */
public final class DefaultScriptSecurityRuleSet implements ScriptSecurityRuleSet {

  public static final DefaultScriptSecurityRuleSet INSTANCE = new DefaultScriptSecurityRuleSet();

  private DefaultScriptSecurityRuleSet() {
  }

  private static final Pattern SYSTEM_GETENV_PATTERN = Pattern.compile("(?s).*\\b(?:java\\.lang\\.)?System\\s*\\.\\s*getenv\\s*\\(.*");
  private static final Pattern SYSTEM_GET_PROPERTY_PATTERN = Pattern.compile("(?s).*\\b(?:java\\.lang\\.)?System\\s*\\.\\s*getProperty\\s*\\(.*");
  private static final Pattern JAVA_TYPE_PATTERN = Pattern.compile("\\bjava\\.type\\s*\\(", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern PACKAGES_PATTERN = Pattern.compile("\\bpackages\\.", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  // Each of these is shared by several regex variants of the same logical rule below -
  // named constants instead of repeated literals so the variants can't silently drift
  // apart (a typo in just one copy) as the pattern list grows.
  private static final String CODE_PROCESS_BUILDER = "SCRIPT_SECURITY_PROCESS_BUILDER";
  private static final String MSG_PROCESS_BUILDER = "Process execution via ProcessBuilder is forbidden";
  private static final String CODE_JAVA_IO = "SCRIPT_SECURITY_JAVA_IO";
  private static final String MSG_JAVA_IO = "File system access is forbidden";
  private static final String CODE_JAVA_NIO_FILE = "SCRIPT_SECURITY_JAVA_NIO_FILE";
  private static final String MSG_JAVA_NIO_FILE = "NIO file system access is forbidden";
  private static final String CODE_JAVA_NET = "SCRIPT_SECURITY_JAVA_NET";
  private static final String MSG_JAVA_NET = "Network access is forbidden";

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
      Rule.pattern("\\bnew\\s+java\\.lang\\.processbuilder\\b", MSG_PROCESS_BUILDER, CODE_PROCESS_BUILDER),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.lang\\.processbuilder['\"]\\s*\\)", MSG_PROCESS_BUILDER, CODE_PROCESS_BUILDER),
      Rule.pattern("\\bprocessbuilder\\b", MSG_PROCESS_BUILDER, CODE_PROCESS_BUILDER),

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
      Rule.pattern("\\bnew\\s+java\\.io\\.", MSG_JAVA_IO, CODE_JAVA_IO),
      Rule.pattern("\\bjava\\.io\\.", MSG_JAVA_IO, CODE_JAVA_IO),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.io\\.", MSG_JAVA_IO, CODE_JAVA_IO),

      // NIO filesystem
      Rule.pattern("\\bnew\\s+java\\.nio\\.file\\.", MSG_JAVA_NIO_FILE, CODE_JAVA_NIO_FILE),
      Rule.pattern("\\bjava\\.nio\\.file\\.", MSG_JAVA_NIO_FILE, CODE_JAVA_NIO_FILE),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.nio\\.file\\.", MSG_JAVA_NIO_FILE, CODE_JAVA_NIO_FILE),

      // NIO file channels
      Rule.pattern("\\bjava\\.nio\\.channels\\.(?:asynchronousfilechannel|filechannel)\\b", "NIO file channel access is forbidden", "SCRIPT_SECURITY_JAVA_NIO_FILE_CHANNEL"),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.nio\\.channels\\.(?:asynchronousfilechannel|filechannel)['\"]\\s*\\)", "NIO file channel access is forbidden", "SCRIPT_SECURITY_JAVA_NIO_FILE_CHANNEL"),

      // NIO network channels
      Rule.pattern("\\bjava\\.nio\\.channels\\.(?:socketchannel|serversocketchannel|datagramchannel|asynchronoussocketchannel|asynchronousserversocketchannel)\\b", "NIO network channel access is forbidden", "SCRIPT_SECURITY_JAVA_NIO_NETWORK_CHANNEL"),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.nio\\.channels\\.(?:socketchannel|serversocketchannel|datagramchannel|asynchronoussocketchannel|asynchronousserversocketchannel)['\"]\\s*\\)", "NIO network channel access is forbidden", "SCRIPT_SECURITY_JAVA_NIO_NETWORK_CHANNEL"),

      // Network
      Rule.pattern("\\bnew\\s+java\\.net\\.", MSG_JAVA_NET, CODE_JAVA_NET),
      Rule.pattern("\\bjava\\.net\\.", MSG_JAVA_NET, CODE_JAVA_NET),
      Rule.pattern("\\bjava\\.type\\s*\\(\\s*['\"]java\\.net\\.", MSG_JAVA_NET, CODE_JAVA_NET),
      Rule.pattern("\\burlconnection\\b", MSG_JAVA_NET, "SCRIPT_SECURITY_URL_CONNECTION"),
      Rule.pattern("\\bhttpclient\\b", "HTTP client access is forbidden", "SCRIPT_SECURITY_HTTP_CLIENT"),
      Rule.pattern("\\bnew\\s+socket\\s*\\(", "Socket access is forbidden", "SCRIPT_SECURITY_SOCKET"),
      Rule.pattern("\\bserversocket\\b", "Server socket access is forbidden", "SCRIPT_SECURITY_SERVER_SOCKET"),

      // Generic Java construction after specific Java rules.
      Rule.pattern("\\bnew\\s+java\\.", "Direct Java object creation is forbidden", "SCRIPT_SECURITY_NEW_JAVA"),

      // Groovy
      Rule.pattern("\\bgroovyshell\\b", "Dynamic Groovy shell execution is forbidden", "SCRIPT_SECURITY_GROOVY_SHELL"),
      Rule.pattern("\\bmetaclass\\b", "Groovy metaclass access is forbidden", "SCRIPT_SECURITY_GROOVY_METACLASS")
  );

  @Override
  public ScriptValidationResult validate(String source, ScriptOrigin origin) {
    Objects.requireNonNull(origin, "origin must not be null");

    String normalizedSource = normalize(source);
    if (normalizedSource.isEmpty()) {
      return ScriptValidationResult.clean();
    }

    Optional<ScriptSecurityRuleMatch> match = matchRegexRules(source)
        .or(() -> matchDenyRules(normalizedSource))
        .or(() -> matchHostClassLookupRules(normalizedSource, origin));

    return match.map(ScriptValidationResult::matched).orElseGet(ScriptValidationResult::clean);
  }

  private static Optional<ScriptSecurityRuleMatch> matchRegexRules(String source) {
    if (SYSTEM_GETENV_PATTERN.matcher(source).matches()) {
      return Optional.of(new ScriptSecurityRuleMatch("SCRIPT_SECURITY_SYSTEM_GETENV", "Access to environment variables is forbidden"));
    }

    if (SYSTEM_GET_PROPERTY_PATTERN.matcher(source).matches()) {
      return Optional.of(new ScriptSecurityRuleMatch("SCRIPT_SECURITY_SYSTEM_GET_PROPERTY", "Access to JVM system properties is forbidden"));
    }

    return Optional.empty();
  }

  private static Optional<ScriptSecurityRuleMatch> matchDenyRules(String normalizedSource) {
    return RULES.stream()
        .map(rule -> rule.evaluate(normalizedSource))
        .flatMap(Optional::stream)
        .findFirst();
  }

  private static Optional<ScriptSecurityRuleMatch> matchHostClassLookupRules(String normalizedSource, ScriptOrigin origin) {
    boolean usesJavaType = JAVA_TYPE_PATTERN.matcher(normalizedSource).find();
    boolean usesPackages = PACKAGES_PATTERN.matcher(normalizedSource).find();

    if (!usesJavaType && !usesPackages) {
      return Optional.empty();
    }

    if (origin == ScriptOrigin.PLATFORM) {
      return Optional.empty();
    }

    return Optional.of(new ScriptSecurityRuleMatch(
        "SCRIPT_SECURITY_JAVA_TYPE",
        usesPackages ? "Host class lookup via Packages is forbidden" : "Host class lookup is forbidden"));
  }

  private static String normalize(String source) {
    return Optional.ofNullable(source)
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

    private Optional<ScriptSecurityRuleMatch> evaluate(String source) {
      if (pattern.matcher(source).find()) {
        return Optional.of(new ScriptSecurityRuleMatch(code, reason));
      }
      return Optional.empty();
    }
  }
}
