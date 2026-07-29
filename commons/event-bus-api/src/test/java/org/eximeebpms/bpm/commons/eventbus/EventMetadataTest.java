package org.eximeebpms.bpm.commons.eventbus;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventMetadataTest {

  @Test
  void shouldPreserveProcessContextWhenNoProcessContextIsFalse() {
    EventMetadata metadata = EventMetadata.builder()
        .timestamp(Instant.now())
        .uuid(UUID.randomUUID().toString())
        .type("TestEvent")
        .version("1.0")
        .origin("test")
        .processInstanceId("process-instance-id")
        .processDefinitionKey("process-definition-key")
        .noProcessContext(false)
        .build();

    assertThat(metadata.noProcessContext()).isFalse();
    assertThat(metadata.processInstanceId()).isEqualTo("process-instance-id");
    assertThat(metadata.processDefinitionKey()).isEqualTo("process-definition-key");
  }

  @Test
  void shouldSetSyntheticProcessContextWhenNoProcessContextIsTrue() {
    EventMetadata metadata = EventMetadata.builder()
        .timestamp(Instant.now())
        .uuid(UUID.randomUUID().toString())
        .type("TestEvent")
        .version("1.0")
        .origin("test")
        .noProcessContext(true)
        .build();

    assertThat(metadata.noProcessContext()).isTrue();
    assertThat(metadata.processInstanceId()).isEqualTo("no-process-context");
    assertThat(metadata.processDefinitionKey()).isEqualTo("no-process-context");
  }
}
