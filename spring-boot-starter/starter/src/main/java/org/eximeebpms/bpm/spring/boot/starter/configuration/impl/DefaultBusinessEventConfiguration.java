package org.eximeebpms.bpm.spring.boot.starter.configuration.impl;

import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventConfiguration;
import org.eximeebpms.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsBusinessEventConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.util.BusinessEventPublisherPropertiesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class DefaultBusinessEventConfiguration extends AbstractEximeeBpmsConfiguration implements EximeeBpmsBusinessEventConfiguration {

  @Autowired
  protected Environment environment;

  @Override
  public void preInit(SpringProcessEngineConfiguration configuration) {
    configuration.setBusinessEventConfiguration(BusinessEventConfiguration.builder()
        .enabled(eximeeBpmsBpmProperties.getBusinessEvents().isEnabled())
        .outboxRetentionMs(eximeeBpmsBpmProperties.getBusinessEvents().getBusinessEventOutboxRetentionMs())
        .outboxCleanupIntervalMs(eximeeBpmsBpmProperties.getBusinessEvents().getBusinessEventOutboxCleanupIntervalMs())
        .dispatchIntervalMs(eximeeBpmsBpmProperties.getBusinessEvents().getBusinessEventDispatchIntervalMs())
        .dispatcherBatchSize(eximeeBpmsBpmProperties.getBusinessEvents().getBusinessEventDispatcherBatchSize())
        .publisher(eximeeBpmsBpmProperties.getBusinessEvents().getPublisher())
        .publisherProperties(BusinessEventPublisherPropertiesResolver.resolve(
            eximeeBpmsBpmProperties.getBusinessEvents().getPublisherProperties(),
            environment
        ))
        .build());
  }
}
