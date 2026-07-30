package org.eximeebpms.bpm.engine.impl.businessevent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublisher;
import org.eximeebpms.bpm.engine.ProcessEngineException;

/**
 * Resolves business event publisher implementations by symbolic name.
 *
 * <p>The engine always contributes the built-in noop publisher. Additional
 * publishers, for example REST or Kafka, are discovered through Java {@link ServiceLoader}.
 *
 * <p>Resolution is intentionally fail-fast. If business events are enabled and
 * configuration points to a publisher that is not available on the process
 * engine classpath, engine startup should fail instead of silently falling back
 * to noop.</p>
 */
@Slf4j
public class BusinessEventPublisherResolver {

  protected final List<BusinessEventPublisher> publishers;

  public BusinessEventPublisherResolver() {
    this(loadPublishers());
  }

  public BusinessEventPublisherResolver(List<BusinessEventPublisher> publishers) {
    this.publishers = List.copyOf(publishers);
  }

  /**
   * Resolves a publisher by name and initializes it with publisher-specific
   * properties.
   *
   * @param publisherName symbolic publisher name, for example {@code noop} or {@code kafka}
   * @param properties raw publisher-specific properties
   * @return initialized publisher
   */
  public BusinessEventPublisher resolve(String publisherName, Map<String, String> properties) {
    String effectivePublisherName = normalizePublisherName(publisherName);

    BusinessEventPublisher publisher = publishers.stream()
        .filter(candidate -> effectivePublisherName.equalsIgnoreCase(candidate.getName()))
        .findFirst()
        .orElseThrow(() -> new ProcessEngineException(
            "Business event publisher '" + effectivePublisherName + "' was not found. "
                + "Make sure the corresponding provider is available on the process engine classpath "
                + "and registered under META-INF/services/"
                + BusinessEventPublisher.class.getName()
        ));

    Map<String, String> safeProperties = properties == null ? Map.of() : Map.copyOf(properties);

    log.debug("Initializing business event publisher [publisher={}]", publisher.getName());
    publisher.init(safeProperties);

    return publisher;
  }

  protected static List<BusinessEventPublisher> loadPublishers() {
    List<BusinessEventPublisher> loaded = new ArrayList<>();

    loaded.add(new NoopBusinessEventPublisher());

    ServiceLoader.load(BusinessEventPublisher.class)
        .forEach(loaded::add);

    return loaded;
  }

  protected static String normalizePublisherName(String publisherName) {
    if (publisherName == null || publisherName.isBlank()) {
      return NoopBusinessEventPublisher.NAME;
    }

    return publisherName.trim();
  }
}
