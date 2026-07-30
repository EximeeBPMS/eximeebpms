package org.eximeebpms.bpm.spring.boot.starter.configuration.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BusinessEventPublisherPropertiesResolver {

  private static final String PROPERTY_PREFIX = "eximeebpms.bpm.business-events.publisher-properties.";
  private static final String ENV_PREFIX = "EXIMEEBPMS_BPM_BUSINESS_EVENTS_PUBLISHER_PROPERTIES_";

  public static Map<String, String> resolve(Map<String, String> configuredPublisherProperties, Environment environment) {
    final Map<String, String> resolvedProperties = new HashMap<>();

    if (configuredPublisherProperties != null) {
      resolvedProperties.putAll(configuredPublisherProperties);
    }

    if (!(environment instanceof ConfigurableEnvironment configurableEnvironment)) {
      return Map.copyOf(resolvedProperties);
    }

    for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
      Object source = propertySource.getSource();

      if (!(source instanceof Map<?, ?> sourceProperties)) {
        continue;
      }

      addMatchingProperties(resolvedProperties, sourceProperties);
    }

    return Map.copyOf(resolvedProperties);
  }

  private static void addMatchingProperties(Map<String, String> resolvedProperties, Map<?, ?> sourceProperties) {
    for (Map.Entry<?, ?> entry : sourceProperties.entrySet()) {
      if (entry.getKey() == null || entry.getValue() == null) {
        continue;
      }

      final String rawKey = entry.getKey().toString();
      final String value = entry.getValue().toString();

      if (rawKey.startsWith(ENV_PREFIX)) {
        resolvedProperties.put(
            normalizeEnvironmentVariableSuffix(rawKey.substring(ENV_PREFIX.length())),
            value
        );
        continue;
      }

      final String canonicalKey = rawKey.toLowerCase(Locale.ROOT);
      if (canonicalKey.startsWith(PROPERTY_PREFIX)) {
        resolvedProperties.put(
            normalizePropertySuffix(canonicalKey.substring(PROPERTY_PREFIX.length())),
            value
        );
      }
    }
  }

  private static String normalizePropertySuffix(String suffix) {
    return suffix.toLowerCase(Locale.ROOT);
  }

  private static String normalizeEnvironmentVariableSuffix(String suffix) {
    final String normalized = suffix.toLowerCase(Locale.ROOT);

    final int publisherSeparatorIndex = normalized.indexOf('_');
    if (publisherSeparatorIndex < 0) {
      return normalized;
    }

    final String publisher = normalized.substring(0, publisherSeparatorIndex);
    final String property = normalized.substring(publisherSeparatorIndex + 1)
        .replace("__", ".")
        .replace("_", "-");

    return publisher + "." + property;
  }
}
