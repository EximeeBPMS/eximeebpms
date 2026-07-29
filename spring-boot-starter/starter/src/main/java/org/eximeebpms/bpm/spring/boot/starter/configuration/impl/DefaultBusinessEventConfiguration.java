package org.eximeebpms.bpm.spring.boot.starter.configuration.impl;

import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventConfiguration;
import org.eximeebpms.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.CamundaBusinessEventConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.util.BusinessEventPublisherPropertiesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class DefaultBusinessEventConfiguration extends AbstractCamundaConfiguration implements CamundaBusinessEventConfiguration {

  @Autowired
  protected Environment environment;

  @Override
  public void preInit(SpringProcessEngineConfiguration configuration) {
    configuration.setBusinessEventConfiguration(BusinessEventConfiguration.builder()
        .enabled(camundaBpmProperties.getBusinessEvents().isEnabled())
        .outboxRetentionMs(camundaBpmProperties.getBusinessEvents().getBusinessEventOutboxRetentionMs())
        .outboxCleanupIntervalMs(camundaBpmProperties.getBusinessEvents().getBusinessEventOutboxCleanupIntervalMs())
        .dispatchIntervalMs(camundaBpmProperties.getBusinessEvents().getBusinessEventDispatchIntervalMs())
        .dispatcherBatchSize(camundaBpmProperties.getBusinessEvents().getBusinessEventDispatcherBatchSize())
        .publisher(camundaBpmProperties.getBusinessEvents().getPublisher())
        .publisherProperties(BusinessEventPublisherPropertiesResolver.resolve(
            camundaBpmProperties.getBusinessEvents().getPublisherProperties(),
            environment
        ))
        .build());
  }
}
