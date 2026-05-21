package org.eximeebpms.bpm.engine.impl.cfg;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.eximeebpms.bpm.engine.impl.scripting.security.DefaultScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityContext;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityDecision;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSourceType;
import org.junit.Test;

public class ScriptSecurityConfigurationTest {

  @Test
  public void shouldConfigureDefaultScriptSecurityPolicyWithAllowlistedProcessDefinitionKeys() {
    // given
    StandaloneInMemProcessEngineConfiguration configuration =
        new StandaloneInMemProcessEngineConfiguration();

    configuration.setScriptSecurityEnabled(true);
    configuration.setScriptSecurityAllowlistedProcessDefinitionKeys(Set.of("legacyInvoiceProcess"));

    // when
    configuration.initScriptSecurityPolicy();

    // then
    assertThat(configuration.getScriptSecurityPolicy())
        .isInstanceOf(DefaultScriptSecurityPolicy.class);

    ScriptSecurityDecision decision = configuration.getScriptSecurityPolicy().evaluate(
        ScriptSecurityContext.builder("javascript")
            .source("System.getenv('HOME')")
            .sourceType(ScriptSourceType.INLINE_SOURCE)
            .processDefinitionKey("legacyInvoiceProcess")
            .build());

    assertThat(decision.isAllowed()).isTrue();
  }

  @Test
  public void shouldUseEmptyAllowlistWhenScriptSecurityAllowlistedProcessDefinitionKeysIsNull() {
    // given
    StandaloneInMemProcessEngineConfiguration configuration =
        new StandaloneInMemProcessEngineConfiguration();

    configuration.setScriptSecurityEnabled(true);
    configuration.setScriptSecurityAllowlistedProcessDefinitionKeys(null);

    // when
    configuration.initScriptSecurityPolicy();

    // then
    ScriptSecurityDecision decision = configuration.getScriptSecurityPolicy().evaluate(
        ScriptSecurityContext.builder("javascript")
            .source("System.getenv('HOME')")
            .sourceType(ScriptSourceType.INLINE_SOURCE)
            .processDefinitionKey("legacyInvoiceProcess")
            .build());

    assertThat(decision.isAllowed()).isFalse();
  }
}
