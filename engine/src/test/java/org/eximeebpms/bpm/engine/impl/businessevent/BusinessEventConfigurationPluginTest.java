package org.eximeebpms.bpm.engine.impl.businessevent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.eximeebpms.bpm.container.impl.metadata.PropertyHelper;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.junit.jupiter.api.Test;

/**
 * Covers the XML-configured path into {@link BusinessEventConfigurationPlugin}. The same
 * {@link PropertyHelper#applyProperties(Object, Map)} call backs both standalone
 * {@code bpm-platform.xml} ({@code StartProcessEngineStep#configurePlugins}) and the WildFly
 * subsystem ({@code MscManagedProcessEngineController#addProcessEnginePlugins}), so testing it
 * here covers both containers.
 */
class BusinessEventConfigurationPluginTest {

  private static BusinessEventConfigurationPlugin pluginWith(Map<String, String> xmlProperties) {
    BusinessEventConfigurationPlugin plugin = new BusinessEventConfigurationPlugin();
    PropertyHelper.applyProperties(plugin, xmlProperties);
    return plugin;
  }

  private static ProcessEngineConfigurationImpl preInit(BusinessEventConfigurationPlugin plugin) {
    ProcessEngineConfigurationImpl configuration = new StandaloneInMemProcessEngineConfiguration();
    plugin.preInit(configuration);
    return configuration;
  }

  @Test
  void shouldSplitCommaSeparatedEventTypes() {
    // given
    Map<String, String> xmlProperties = new HashMap<>();
    xmlProperties.put("enabled", "true");
    xmlProperties.put("disabledEventTypes", "variable-instance:*,activity-instance:*");

    // when
    ProcessEngineConfigurationImpl configuration = preInit(pluginWith(xmlProperties));

    // then
    assertThat(configuration.getBusinessEventConfiguration().getDisabledEventTypes())
        .containsExactlyInAnyOrder("variable-instance:*", "activity-instance:*");
  }

  @Test
  void shouldApplyConfiguredEventTypesToTheFilter() {
    // given
    Map<String, String> xmlProperties = new HashMap<>();
    xmlProperties.put("enabled", "true");
    xmlProperties.put("enabledEventTypes", "task-instance:*,process-instance:start");
    xmlProperties.put("disabledEventTypes", "task-instance:update");

    // when
    ProcessEngineConfigurationImpl configuration = preInit(pluginWith(xmlProperties));
    BusinessEventTypeFilter typeFilter = configuration.getBusinessEventTypeFilter();

    // then
    assertThat(typeFilter.isEnabled(BusinessEventTypes.TASK_INSTANCE_CREATE)).isTrue();
    assertThat(typeFilter.isEnabled(BusinessEventTypes.TASK_INSTANCE_UPDATE)).isFalse();
    assertThat(typeFilter.isEnabled(BusinessEventTypes.PROCESS_INSTANCE_START)).isTrue();
    assertThat(typeFilter.isEnabled(BusinessEventTypes.PROCESS_INSTANCE_END)).isFalse();
  }

  @Test
  void shouldKeepDefaultsWhenEventTypesAreNotConfigured() {
    // given
    Map<String, String> xmlProperties = new HashMap<>();
    xmlProperties.put("enabled", "true");

    // when
    ProcessEngineConfigurationImpl configuration = preInit(pluginWith(xmlProperties));

    // then
    assertThat(configuration.getBusinessEventConfiguration().getEnabledEventTypes())
        .containsExactly(BusinessEventTypeFilter.ALL);
    assertThat(configuration.getBusinessEventTypeFilter().isAllEnabled()).isTrue();
  }

  @Test
  void shouldRejectUnknownEventTypeWhenTheFilterIsResolved() {
    // given
    Map<String, String> xmlProperties = new HashMap<>();
    xmlProperties.put("enabled", "true");
    xmlProperties.put("enabledEventTypes", "task-instance:finish");

    ProcessEngineConfigurationImpl configuration = preInit(pluginWith(xmlProperties));

    // when/then
    assertThatThrownBy(configuration::getBusinessEventTypeFilter)
        .isInstanceOf(InvalidBusinessEventTypeException.class)
        .hasMessageContaining("task-instance")
        .hasMessageContaining("finish");
  }
}
