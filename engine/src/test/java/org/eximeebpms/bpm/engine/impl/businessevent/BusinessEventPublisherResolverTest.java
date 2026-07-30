package org.eximeebpms.bpm.engine.impl.businessevent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublishResult;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublisher;
import org.eximeebpms.bpm.commons.eventbus.Event;
import org.eximeebpms.bpm.commons.eventbus.EventMetadata;
import org.eximeebpms.bpm.engine.ProcessEngineException;
import org.junit.jupiter.api.Test;

class BusinessEventPublisherResolverTest {

  @Test
  void shouldResolveNoopPublisherByName() {
    BusinessEventPublisherResolver resolver = new BusinessEventPublisherResolver(
        List.of(new NoopBusinessEventPublisher())
    );

    BusinessEventPublisher publisher = resolver.resolve("noop", Map.of());

    assertThat(publisher).isInstanceOf(NoopBusinessEventPublisher.class);
    assertThat(publisher.getName()).isEqualTo("noop");
  }

  @Test
  void shouldResolveNoopPublisherWhenNameIsBlank() {
    BusinessEventPublisherResolver resolver = new BusinessEventPublisherResolver(
        List.of(new NoopBusinessEventPublisher())
    );

    BusinessEventPublisher publisher = resolver.resolve(" ", Map.of());

    assertThat(publisher).isInstanceOf(NoopBusinessEventPublisher.class);
  }

  @Test
  void shouldFailFastWhenPublisherIsUnknown() {
    BusinessEventPublisherResolver resolver = new BusinessEventPublisherResolver(
        List.of(new NoopBusinessEventPublisher())
    );

    assertThatThrownBy(() -> resolver.resolve("kafka", Map.of()))
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Business event publisher 'kafka' was not found");
  }

  @Test
  void shouldInitializeResolvedPublisherWithProperties() {
    TestBusinessEventPublisher testPublisher = new TestBusinessEventPublisher();

    BusinessEventPublisherResolver resolver = new BusinessEventPublisherResolver(
        List.of(new NoopBusinessEventPublisher(), testPublisher)
    );

    BusinessEventPublisher publisher = resolver.resolve(
        "test",
        Map.of("custom.property", "custom-value")
    );

    assertThat(publisher).isSameAs(testPublisher);
    assertThat(testPublisher.initialized).isTrue();
    assertThat(testPublisher.properties)
        .containsEntry("custom.property", "custom-value");
  }

  private static class TestBusinessEventPublisher implements BusinessEventPublisher {

    private boolean initialized;
    private Map<String, String> properties;

    @Override
    public String getName() {
      return "test";
    }

    @Override
    public void init(Map<String, String> properties) {
      this.initialized = true;
      this.properties = properties;
    }

    @Override
    public BusinessEventPublishResult publish(Event event) {
      return BusinessEventPublishResult.success();
    }
  }

  static class NoopBusinessEventPublisherTest {

    @Test
    void shouldExposeNoopName() {
      try (NoopBusinessEventPublisher publisher = new NoopBusinessEventPublisher()) {
        assertThat(publisher.getName()).isEqualTo(NoopBusinessEventPublisher.NAME);
      }
    }

    @Test
    void shouldNotThrowOnPublish() {
      try (NoopBusinessEventPublisher publisher = new NoopBusinessEventPublisher()) {
        final Event event = Event.builder()
            .metadata(EventMetadata.builder()
                .timestamp(Instant.now())
                .uuid(UUID.randomUUID().toString())
                .type("TestEvent")
                .version("1.0")
                .origin("test")
                .processInstanceId("test-process-key")
                .processDefinitionKey("test-process")
                .noProcessContext(false)
                .build())
            .payload("test-payload")
            .build();
        assertThatCode(() -> publisher.publish(event)).doesNotThrowAnyException();
      }
    }

    @Test
    void shouldNotThrowOnLifecycleMethods() {
      try (NoopBusinessEventPublisher publisher = new NoopBusinessEventPublisher()) {
        assertThatCode(() -> publisher.init(null)).doesNotThrowAnyException();
        assertThatCode(publisher::close).doesNotThrowAnyException();
      }
    }
  }
}
