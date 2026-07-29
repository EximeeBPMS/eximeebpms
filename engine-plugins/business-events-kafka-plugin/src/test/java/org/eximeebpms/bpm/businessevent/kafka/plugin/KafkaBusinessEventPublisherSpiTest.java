package org.eximeebpms.bpm.businessevent.kafka.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ServiceLoader;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublisher;
import org.junit.jupiter.api.Test;

class KafkaBusinessEventPublisherSpiTest {

  @Test
  void shouldExposeKafkaPublisherThroughServiceLoader() {
    ServiceLoader<BusinessEventPublisher> serviceLoader =
        ServiceLoader.load(BusinessEventPublisher.class);

    assertThat(serviceLoader)
        .anySatisfy(publisher -> {
          assertThat(publisher).isInstanceOf(KafkaBusinessEventPublisher.class);
          assertThat(publisher.getName()).isEqualTo(KafkaPublisherProperties.PUBLISHER_NAME);
        });
  }
}
