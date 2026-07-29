package org.eximeebpms.bpm.engine.impl.cfg;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventConfiguration;
import org.eximeebpms.bpm.engine.impl.businessevent.NoopBusinessEventPublisher;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandInterceptor;
import org.junit.jupiter.api.Test;

class ProcessEngineConfigurationImplBusinessEventsTest {

  @Test
  void shouldInitializeNoopPublisherWhenBusinessEventsAreDisabled() {
    // given
    TestProcessEngineConfiguration configuration = new TestProcessEngineConfiguration();

    // when
    configuration.initBusinessEventsForTest();

    // then
    assertThat(configuration.getBusinessEventPublisher())
        .isInstanceOf(NoopBusinessEventPublisher.class);
  }

  @Test
  void shouldInitializeNoopPublisherWhenExplicitlyConfigured() {
    // given
    TestProcessEngineConfiguration configuration = new TestProcessEngineConfiguration();
    BusinessEventConfiguration businessEventConfiguration = BusinessEventConfiguration.builder()
        .enabled(true)
        .publisher("noop")
        .build();

    configuration.setBusinessEventConfiguration(businessEventConfiguration);

    // when
    configuration.initBusinessEventsForTest();

    // then
    assertThat(configuration.getBusinessEventPublisher())
        .isInstanceOf(NoopBusinessEventPublisher.class);
  }

  private static class TestProcessEngineConfiguration extends ProcessEngineConfigurationImpl {

    void initBusinessEventsForTest() {
      initBusinessEvents();
    }

    @Override
    protected Collection<? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
      return List.of();
    }

    @Override
    protected Collection<? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
      return List.of();
    }
  }
}
