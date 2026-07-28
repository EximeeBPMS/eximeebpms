package org.eximeebpms.bpm.engine.impl.businessevent;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEnginePlugin;

@Getter
@Setter
public class BusinessEventConfigurationPlugin implements ProcessEnginePlugin {

  private boolean enabled = false;
  private long outboxRetentionMs;
  private long outboxCleanupIntervalMs;
  private long dispatchIntervalMs;
  private int dispatcherBatchSize;
  private String prefix;
  private String publisher;
  private String publisherProperties;
  private String enabledEventTypes;

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    BusinessEventConfiguration.BusinessEventConfigurationBuilder builder =
        BusinessEventConfiguration.builder()
            .enabled(enabled)
            .publisherProperties(parsePublisherProperties(publisherProperties));

    if (outboxRetentionMs > 0) {
      builder.outboxRetentionMs(outboxRetentionMs);
    }

    if (outboxCleanupIntervalMs > 0) {
      builder.outboxCleanupIntervalMs(outboxCleanupIntervalMs);
    }

    if (dispatchIntervalMs > 0) {
      builder.dispatchIntervalMs(dispatchIntervalMs);
    }

    if (dispatcherBatchSize > 0) {
      builder.dispatcherBatchSize(dispatcherBatchSize);
    }

    if (prefix != null && !prefix.isBlank()) {
      builder.prefix(prefix.trim());
    }

    if (publisher != null && !publisher.isBlank()) {
      builder.publisher(publisher.trim());
    }

    if (enabledEventTypes != null && !enabledEventTypes.isBlank()) {
      builder.enabledEventTypes(enabledEventTypes.trim());
    }

    processEngineConfiguration.setBusinessEventConfiguration(builder.build());
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    // no-op
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    // no-op
  }

  private static Map<String, String> parsePublisherProperties(String rawProperties) {
    if (rawProperties == null || rawProperties.isBlank()) {
      return Map.of();
    }

    final Map<String, String> parsedProperties = new LinkedHashMap<>();
    final String[] lines = rawProperties.split("\\R");
    Arrays.stream(lines).forEach(line -> {
      if (line.trim().isEmpty() || line.trim().startsWith("#")) {
        return;
      }

      final int separatorIndex = line.indexOf('=');
      if (separatorIndex <= 0) {
        throw new IllegalArgumentException("Invalid business event publisher property entry: '" + line + "'. Expected format: key=value");
      }

      final String key = line.substring(0, separatorIndex).trim();
      final String value = line.substring(separatorIndex + 1).trim();

      if (key.isEmpty()) {
        throw new IllegalArgumentException("Invalid business event publisher property entry: '" + line + "'. Property key must not be empty");
      }

      parsedProperties.put(key, value);
    });

    return Map.copyOf(parsedProperties);
  }
}
