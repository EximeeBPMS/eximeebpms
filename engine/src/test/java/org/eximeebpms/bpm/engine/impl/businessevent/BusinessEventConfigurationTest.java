package org.eximeebpms.bpm.engine.impl.businessevent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BusinessEventConfigurationTest {

  @Test
  void shouldBeDisabledByDefault() {
    // when
    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder().build();

    // then
    assertThat(configuration.isEnabled()).isFalse();
  }

  @Test
  void shouldUseNoopPublisherByDefault() {
    // when
    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder().build();

    // then
    assertThat(configuration.getPublisher()).isEqualTo(NoopBusinessEventPublisher.NAME);
    assertThat(configuration.isPublisherNoop()).isTrue();
  }

  @Test
  void shouldUseBpmsPrefixByDefault() {
    // when
    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder().build();

    // then
    assertThat(configuration.getPrefix())
        .isEqualTo("bpms")
        .isEqualTo(BusinessEventType.BUSINESS_EVENT_PREFIX);
  }

  @Test
  void shouldStoreCustomPrefix() {
    // when
    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder()
        .prefix("custom")
        .build();

    // then
    assertThat(configuration.getPrefix()).isEqualTo("custom");
  }

  @Test
  void shouldNeverReturnNullPublisherProperties() {
    // when
    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder().build();

    // then
    assertThat(configuration.getPublisherProperties())
        .isNotNull()
        .isEmpty();
  }

  @Test
  void shouldStoreSinglePublisherProperties() {
    // when
    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder()
        .publisherProperty("kafka.bootstrap-servers", "localhost:9092")
        .publisherProperty("kafka.topic", "eximee.business-events")
        .build();

    // then
    assertThat(configuration.getPublisherProperties())
        .containsEntry("kafka.bootstrap-servers", "localhost:9092")
        .containsEntry("kafka.topic", "eximee.business-events");
  }

  @Test
  void shouldStorePublisherPropertiesFromMap() {
    // given
    Map<String, String> publisherProperties = Map.of(
        "kafka.bootstrap-servers", "localhost:9092",
        "kafka.topic", "eximee.business-events"
    );

    // when
    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder()
        .publisherProperties(publisherProperties)
        .build();

    // then
    assertThat(configuration.getPublisherProperties())
        .containsEntry("kafka.bootstrap-servers", "localhost:9092")
        .containsEntry("kafka.topic", "eximee.business-events");
  }

  @Test
  void shouldReturnUnmodifiablePublisherProperties() {
    // given
    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder()
        .publisherProperty("kafka.topic", "eximee.business-events")
        .build();

    Map<String, String> publisherProperties = configuration.getPublisherProperties();

    // when & then
    assertThatThrownBy(() -> publisherProperties.put("kafka.topic", "changed"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldCopyPublisherPropertiesPassedToBuilder() {
    // given
    Map<String, String> publisherProperties = new HashMap<>();
    publisherProperties.put("kafka.topic", "eximee.business-events");

    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder()
        .publisherProperties(publisherProperties)
        .build();

    // when
    publisherProperties.put("kafka.topic", "changed");

    // then
    assertThat(configuration.getPublisherProperties())
        .containsEntry("kafka.topic", "eximee.business-events");
  }

  @Test
  void shouldEnableEveryEventTypeByDefault() {
    // when
    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder().build();

    // then
    assertThat(configuration.getEnabledEventTypes()).containsExactly(BusinessEventTypeFilter.ALL);
    assertThat(configuration.getDisabledEventTypes()).isEmpty();
  }

  @Test
  void shouldStoreConfiguredEventTypes() {
    // when
    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder()
        .enabledEventTypes(Set.of("task-instance:*"))
        .disabledEventTypes(Set.of("task-instance:update"))
        .build();

    // then
    assertThat(configuration.getEnabledEventTypes()).containsExactly("task-instance:*");
    assertThat(configuration.getDisabledEventTypes()).containsExactly("task-instance:update");
  }

  @Test
  void shouldFallBackToDefaultAllowlistWhenUnset() {
    // when — an explicit null means "not configured", not "publish nothing"
    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder()
        .enabledEventTypes(null)
        .disabledEventTypes(null)
        .build();

    // then
    assertThat(configuration.getEnabledEventTypes()).containsExactly(BusinessEventTypeFilter.ALL);
    assertThat(configuration.getDisabledEventTypes()).isEmpty();
  }

  @Test
  void shouldHonourExplicitlyEmptyAllowlist() {
    // when
    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder()
        .enabledEventTypes(Set.of())
        .build();

    // then
    assertThat(configuration.getEnabledEventTypes()).isEmpty();
  }

  @Test
  void shouldReturnImmutableEventTypes() {
    // given
    Set<String> enabledEventTypes = new LinkedHashSet<>();
    enabledEventTypes.add("job:create");

    BusinessEventConfiguration configuration = BusinessEventConfiguration.builder()
        .enabledEventTypes(enabledEventTypes)
        .build();

    Set<String> returnedEventTypes = configuration.getEnabledEventTypes();

    // when & then
    assertThatThrownBy(() -> returnedEventTypes.add("job:fail"))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
