package org.eximeebpms.bpm.engine.impl.cfg;

import static org.assertj.core.api.Assertions.assertThat;

import org.eximeebpms.bpm.engine.impl.el.JuelExpressionManager;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityAwareExpressionManager;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityContext;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityDecision;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSourceType;
import org.eximeebpms.bpm.engine.impl.scripting.security.SecureJuelExpressionManager;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ProcessEngineConfigurationScriptSecurityPolicyTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @Test
  public void shouldInitializeScriptSecurityPolicy() {
    // given
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("1 + 1;")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .build();

    // when
    ScriptSecurityPolicy policy = processEngineConfiguration.getScriptSecurityPolicy();

    // then
    assertThat(policy).isNotNull();
    assertThat(policy.evaluate(context)).isNotNull();
  }

  @Test
  public void shouldUseConfiguredScriptSecurityPolicy() {
    // given
    ScriptSecurityPolicy customPolicy = context -> ScriptSecurityDecision.allow();

    // when
    processEngineConfiguration.setScriptSecurityPolicy(customPolicy);
    processEngineConfiguration.setScriptingEnvironment(null);
    processEngineConfiguration.initScripting();

    // then
    assertThat(processEngineConfiguration.getScriptSecurityPolicy()).isSameAs(customPolicy);
    assertThat(processEngineConfiguration.getScriptingEnvironment()).isNotNull();
  }

  @Test
  public void shouldUseSecureExpressionManagerWhenScriptSecurityIsEnabled() {
    // given
    ProcessEngineConfigurationImpl configuration = new StandaloneInMemProcessEngineConfiguration();
    configuration.setScriptSecurityEnabled(true);

    // when
    configuration.initBeans();
    configuration.initScriptSecurityPolicy();
    configuration.initExpressionManager();

    // then
    assertThat(configuration.getExpressionManager()).isInstanceOf(SecureJuelExpressionManager.class);
  }

  @Test
  public void shouldUseDefaultExpressionManagerWhenScriptSecurityIsDisabled() {
    // given
    ProcessEngineConfigurationImpl configuration = new StandaloneInMemProcessEngineConfiguration();
    configuration.setScriptSecurityEnabled(false);

    // when
    configuration.initBeans();
    configuration.initScriptSecurityPolicy();
    configuration.initExpressionManager();

    // then
    assertThat(configuration.getExpressionManager()).isInstanceOf(JuelExpressionManager.class);
    assertThat(configuration.getExpressionManager()).isNotInstanceOf(SecureJuelExpressionManager.class);
  }

  @Test
  public void shouldInjectScriptSecurityPolicyIntoAwareCustomExpressionManager() {
    // given
    ScriptSecurityAwareExpressionManager customExpressionManager = new ScriptSecurityAwareExpressionManager();

    StandaloneInMemProcessEngineConfiguration configuration = new StandaloneInMemProcessEngineConfiguration();
    configuration.setScriptSecurityEnabled(true);
    configuration.setExpressionManager(customExpressionManager);

    // when
    configuration.initScriptSecurityPolicy();
    configuration.initExpressionManager();

    // then
    assertThat(configuration.getExpressionManager()).isSameAs(customExpressionManager);
    assertThat(customExpressionManager.getScriptSecurityPolicy()).isNotNull();
  }

  @Test
  public void shouldKeepNonAwareCustomExpressionManagerWhenScriptSecurityIsEnabled() {
    // given
    JuelExpressionManager customExpressionManager = new JuelExpressionManager();

    StandaloneInMemProcessEngineConfiguration configuration = new StandaloneInMemProcessEngineConfiguration();
    configuration.setScriptSecurityEnabled(true);
    configuration.setExpressionManager(customExpressionManager);

    // when
    configuration.initScriptSecurityPolicy();
    configuration.initExpressionManager();

    // then
    assertThat(configuration.getExpressionManager()).isSameAs(customExpressionManager);
    assertThat(configuration.getExpressionManager()).isNotInstanceOf(SecureJuelExpressionManager.class);
  }

  @Test
  public void shouldUseSecureJuelExpressionManagerWhenScriptSecurityIsEnabledAndExpressionManagerIsNotConfigured() {
    // given
    StandaloneInMemProcessEngineConfiguration configuration = new StandaloneInMemProcessEngineConfiguration();
    configuration.setScriptSecurityEnabled(true);

    // when
    configuration.initScriptSecurityPolicy();
    configuration.initExpressionManager();

    // then
    assertThat(configuration.getExpressionManager()).isInstanceOf(SecureJuelExpressionManager.class);
  }
}
