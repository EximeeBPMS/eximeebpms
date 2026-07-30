package org.eximeebpms.bpm.businessevent.kafka.plugin;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

@FunctionalInterface
public interface KafkaProducerFactory {

  static KafkaProducerFactory defaultFactory() {
    return producerConfig -> new KafkaProducer<>(
        producerConfig,
        new StringSerializer(),
        new ByteArraySerializer()
    );
  }

  Producer<String, byte[]> create(Properties producerConfig);
}
