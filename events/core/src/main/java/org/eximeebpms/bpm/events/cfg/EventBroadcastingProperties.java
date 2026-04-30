package org.eximeebpms.bpm.events.cfg;

import lombok.Builder;
import lombok.Value;

/**
 * Runtime configuration for event broadcasting.
 *
 * <p>This class is framework-neutral. Spring Boot, Tomcat and WildFly
 * integrations should map their native configuration mechanisms to this model.</p>
 */
@Value
@Builder(toBuilder = true)
public class EventBroadcastingProperties {

  /**
   * Master switch for event broadcasting.
   */
  @Builder.Default
  boolean enabled = false;

  /**
   * Whether publishing failures should break engine execution.
   *
   * <p>Production default is false. Event publishing should not make BPMN
   * processing unavailable unless explicitly requested.</p>
   */
  @Builder.Default
  boolean failOnPublishError = false;

  /**
   * Whether events should be published asynchronously by the broadcasting layer.
   *
   * <p>Transport-specific publishers may still implement their own async behavior.</p>
   */
  @Builder.Default
  boolean async = true;

  /**
   * Event type switches.
   *
   * <p>All event families are enabled by default once broadcasting itself is enabled.</p>
   */
  @Builder.Default
  EventTypeProperties eventTypes = EventTypeProperties.defaults();

  public static EventBroadcastingProperties disabled() {
    return EventBroadcastingProperties.builder().enabled(false).build();
  }

  public static EventBroadcastingProperties enabledWithDefaults() {
    return EventBroadcastingProperties.builder().enabled(true).build();
  }
}
