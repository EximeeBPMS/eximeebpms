package org.eximeebpms.bpm.engine.impl.businessevent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class BusinessEventTypeTest {

  private static final BusinessEventType SAMPLE = BusinessEventTypes.PROCESS_INSTANCE_START;

  @Test
  void shouldFallbackToConstantPrefixWhenNoEngineContext() {
    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getProcessEngineConfiguration).thenReturn(null);

      // when
      String businessEventName = SAMPLE.getBusinessEventName();

      // then
      assertThat(businessEventName).isEqualTo("bpms:process-instance:start");
    }
  }

  @Test
  void shouldUseDefaultPrefixFromEngineConfiguration() {
    ProcessEngineConfigurationImpl processEngineConfiguration = mock(ProcessEngineConfigurationImpl.class);
    when(processEngineConfiguration.getBusinessEventConfiguration())
        .thenReturn(BusinessEventConfiguration.builder().build());

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getProcessEngineConfiguration).thenReturn(processEngineConfiguration);

      // when
      String businessEventName = SAMPLE.getBusinessEventName();

      // then
      assertThat(businessEventName).isEqualTo("bpms:process-instance:start");
    }
  }

  @Test
  void shouldUseConfiguredCustomPrefix() {
    ProcessEngineConfigurationImpl processEngineConfiguration = mock(ProcessEngineConfigurationImpl.class);
    when(processEngineConfiguration.getBusinessEventConfiguration())
        .thenReturn(BusinessEventConfiguration.builder().prefix("custom").build());

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getProcessEngineConfiguration).thenReturn(processEngineConfiguration);

      // when
      String businessEventName = SAMPLE.getBusinessEventName();

      // then
      assertThat(businessEventName).isEqualTo("custom:process-instance:start");
    }
  }

}
