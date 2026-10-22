package org.eximeebpms.bpm.engine.impl.businessevent;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Decides which business event types are published, from the {@code enabledEventTypes}
 * allowlist and the {@code disabledEventTypes} denylist of a {@link BusinessEventConfiguration}.
 *
 * <h3>Token syntax</h3>
 * A token names types by their {@code <entity>:<event>} pair — the same pair that forms the
 * fully-qualified type {@code <prefix>:<entity>:<event>} carried in an event's
 * {@code businessEventType} field. Accepted forms:
 *
 * <ul>
 *   <li>{@code *} — every type</li>
 *   <li>{@code <entity>} or {@code <entity>:*} — every event on that entity, e.g. {@code variable-instance:*}</li>
 *   <li>{@code <entity>:<event>} — a single type, e.g. {@code task-instance:complete}</li>
 *   <li>{@code <prefix>:<entity>:<event>} and {@code <prefix>:<entity>:*} — the same, written the way
 *       the manual and {@code metadata.type} spell it; the prefix must match the configured one</li>
 * </ul>
 *
 * <p>Tokens are matched case-insensitively and blank entries are ignored. A token that names an
 * unknown entity or an unknown event on a known entity is rejected with an
 * {@link InvalidBusinessEventTypeException} when the filter is built, so a typo fails engine
 * bootstrap instead of silently matching nothing.</p>
 *
 * <h3>Evaluation</h3>
 * A type is published when the allowlist matches it <em>and</em> the denylist does not — the
 * denylist always wins. An empty allowlist therefore disables every type, which is a valid (if
 * blunt) way to keep the outbox empty while leaving the feature enabled.
 *
 * <p>Instances are immutable and safe to share between threads.</p>
 */
public class BusinessEventTypeFilter {

  public static final String WILDCARD = "*";
  public static final String SEPARATOR = ":";

  /** Matches every type — the default of {@code enabledEventTypes}. */
  public static final String ALL = WILDCARD;

  protected final Set<String> enabledTokens;
  protected final Set<String> disabledTokens;

  /**
   * {@code true} when the allowlist is {@code *} and the denylist is empty, i.e. the filter
   * passes everything. Lets the common (default) case short-circuit to a single field read.
   */
  protected final boolean allEnabled;

  protected BusinessEventTypeFilter(Set<String> enabledTokens, Set<String> disabledTokens) {
    this.enabledTokens = Collections.unmodifiableSet(enabledTokens);
    this.disabledTokens = Collections.unmodifiableSet(disabledTokens);
    this.allEnabled = disabledTokens.isEmpty() && enabledTokens.contains(WILDCARD);
  }

  /**
   * Builds — and thereby validates — the filter described by {@code configuration}.
   *
   * @throws InvalidBusinessEventTypeException if any configured token is malformed, carries a
   *         prefix other than the configured one, or names no known business event type
   */
  public static BusinessEventTypeFilter of(BusinessEventConfiguration configuration) {
    if (configuration == null) {
      return new BusinessEventTypeFilter(new LinkedHashSet<>(Set.of(WILDCARD)), new LinkedHashSet<>());
    }

    String prefix = configuration.getPrefix();
    return new BusinessEventTypeFilter(
        parse(configuration.getEnabledEventTypes(), prefix, "enabledEventTypes"),
        parse(configuration.getDisabledEventTypes(), prefix, "disabledEventTypes"));
  }

  /**
   * Whether events of this type are published.
   */
  public boolean isEnabled(BusinessEventType type) {
    if (allEnabled) {
      return true;
    }

    if (type == null) {
      return false;
    }

    return isEnabled(type.getEntityType(), type.getEventName());
  }

  /**
   * Whether events of the type named by {@code entityType}/{@code eventName} are published.
   */
  public boolean isEnabled(String entityType, String eventName) {
    if (allEnabled) {
      return true;
    }

    if (entityType == null || eventName == null) {
      return false;
    }

    String entity = normalize(entityType);
    String event = normalize(eventName);

    return matches(enabledTokens, entity, event) && !matches(disabledTokens, entity, event);
  }

  /**
   * Whether events carrying this fully-qualified type — {@code <prefix>:<entity>:<event>}, as found
   * in {@link BusinessEvent#getBusinessEventType()} — are published. The name is read from the
   * right, so a prefix that itself contains a colon does not confuse the split.
   */
  public boolean isEnabled(String businessEventTypeName) {
    if (allEnabled) {
      return true;
    }

    if (businessEventTypeName == null) {
      return false;
    }

    int eventSeparator = businessEventTypeName.lastIndexOf(SEPARATOR);
    if (eventSeparator < 1) {
      return false;
    }

    int entitySeparator = businessEventTypeName.lastIndexOf(SEPARATOR, eventSeparator - 1);

    return isEnabled(
        businessEventTypeName.substring(entitySeparator + 1, eventSeparator),
        businessEventTypeName.substring(eventSeparator + 1));
  }

  /**
   * Whether this filter passes every type, i.e. whether it can be skipped entirely.
   */
  public boolean isAllEnabled() {
    return allEnabled;
  }

  public Set<String> getEnabledTokens() {
    return enabledTokens;
  }

  public Set<String> getDisabledTokens() {
    return disabledTokens;
  }

  /**
   * The built-in types this filter publishes, as {@code <entity>:<event>} tokens. Intended for
   * logging the effective configuration at startup; matching itself never goes through this set,
   * so that custom {@link BusinessEventType} implementations keep working.
   */
  public Set<String> getEffectiveTokens() {
    Set<String> effective = new LinkedHashSet<>();
    for (String token : BusinessEventTypes.getTokens()) {
      int separator = token.indexOf(SEPARATOR);
      if (isEnabled(token.substring(0, separator), token.substring(separator + 1))) {
        effective.add(token);
      }
    }

    return Collections.unmodifiableSet(effective);
  }

  protected static boolean matches(Set<String> tokens, String entity, String event) {
    if (tokens.isEmpty()) {
      return false;
    }

    return tokens.contains(WILDCARD)
        || tokens.contains(entity + SEPARATOR + WILDCARD)
        || tokens.contains(entity + SEPARATOR + event);
  }

  protected static Set<String> parse(Collection<String> rawTokens, String prefix, String propertyName) {
    Set<String> tokens = new LinkedHashSet<>();
    if (rawTokens == null) {
      return tokens;
    }

    for (String rawToken : rawTokens) {
      if (rawToken == null || rawToken.isBlank()) {
        continue;
      }

      tokens.add(parseToken(rawToken, prefix, propertyName));
    }

    return tokens;
  }

  protected static String parseToken(String rawToken, String prefix, String propertyName) {
    String token = normalize(rawToken);

    if (WILDCARD.equals(token)) {
      return WILDCARD;
    }

    String[] segments = token.split(SEPARATOR, -1);

    if (segments.length == 3) {
      String configuredPrefix = normalize(prefix == null ? BusinessEventType.BUSINESS_EVENT_PREFIX : prefix);
      if (!configuredPrefix.equals(segments[0])) {
        throw invalid(rawToken, propertyName,
            "its prefix '" + segments[0] + "' is not the configured business event prefix '" + configuredPrefix + "'");
      }

      segments = new String[] { segments[1], segments[2] };
    }

    if (segments.length == 1) {
      // bare entity, e.g. 'variable-instance' — same meaning as 'variable-instance:*'
      segments = new String[] { segments[0], WILDCARD };
    }

    if (segments.length != 2) {
      throw invalid(rawToken, propertyName,
          "expected '*', '<entity>', '<entity>:<event>' or '<prefix>:<entity>:<event>'");
    }

    String entity = segments[0];
    String event = segments[1];

    if (entity.isEmpty() || event.isEmpty()) {
      throw invalid(rawToken, propertyName,
          "expected '*', '<entity>', '<entity>:<event>' or '<prefix>:<entity>:<event>'");
    }

    if (WILDCARD.equals(entity)) {
      throw invalid(rawToken, propertyName, "the entity segment must not be a wildcard — use '*' to match every type");
    }

    if (!BusinessEventTypes.isKnownEntityType(entity)) {
      throw invalid(rawToken, propertyName, "'" + entity + "' is not a known business event entity");
    }

    if (!WILDCARD.equals(event) && !BusinessEventTypes.isKnownToken(entity, event)) {
      throw invalid(rawToken, propertyName, "'" + entity + "' has no '" + event + "' event");
    }

    return entity + SEPARATOR + event;
  }

  protected static InvalidBusinessEventTypeException invalid(String rawToken, String propertyName, String reason) {
    return new InvalidBusinessEventTypeException(
        "Invalid business event type '" + rawToken + "' in " + propertyName + ": " + reason
            + ". Known types: " + String.join(", ", BusinessEventTypes.getTokens()));
  }

  protected static String normalize(String value) {
    return value.trim().toLowerCase(Locale.ROOT);
  }

  @Override
  public String toString() {
    return "BusinessEventTypeFilter[enabled=" + enabledTokens + ", disabled=" + disabledTokens + "]";
  }

}
