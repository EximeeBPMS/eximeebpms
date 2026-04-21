package org.eximeebpms.trustedcode.plugin;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum TrustedCodePolicyMode {
  OFF,
  AUDIT,
  ENFORCE;

  public static TrustedCodePolicyMode defaultMode() {
    return ENFORCE;
  }

  public static TrustedCodePolicyMode from(String value) {
    if (value == null || value.isBlank()) {
      return defaultMode();
    }

    String normalized = value.trim().toUpperCase(Locale.ROOT);

    try {
      return TrustedCodePolicyMode.valueOf(normalized);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid trusted code policy mode '%s'. Supported values: %s".formatted(value, supportedValues()), ex);
    }
  }

  private static String supportedValues() {
    return Arrays.stream(values())
        .map(Enum::name)
        .collect(Collectors.joining(", "));
  }
}
