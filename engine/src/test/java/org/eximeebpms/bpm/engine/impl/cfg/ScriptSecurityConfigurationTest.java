package org.eximeebpms.bpm.engine.impl.cfg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.eximeebpms.bpm.engine.ProcessEngineException;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbAwareScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityContext;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityDecision;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityMode;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSourceType;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationListener;
import org.junit.Test;

public class ScriptSecurityConfigurationTest {

  @Test
  public void shouldConfigureDefaultScriptSecurityPolicyWithAllowlistedProcessDefinitionKeys() {
    // given
    StandaloneInMemProcessEngineConfiguration configuration =
        new StandaloneInMemProcessEngineConfiguration();

    configuration.setScriptSecurityMode(ScriptSecurityMode.ENFORCE.name());
    configuration.setScriptSecurityAllowlistedProcessDefinitionKeys(Set.of("legacyInvoiceProcess"));

    // when
    configuration.initScriptSecurityPolicy();

    // then
    assertThat(configuration.getScriptSecurityPolicy())
        .isInstanceOf(DbAwareScriptSecurityPolicy.class);

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

    configuration.setScriptSecurityMode(ScriptSecurityMode.ENFORCE.name());
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

  @Test
  public void shouldPassConfiguredViolationStoreAndListenersIntoBuiltPolicy() {
    // given — this is what both StartProcessEngineStep (Tomcat) and
    // DefaultProcessEngineConfiguration (Spring Boot) set up before build
    StandaloneInMemProcessEngineConfiguration configuration =
        new StandaloneInMemProcessEngineConfiguration();
    configuration.setScriptSecurityMode(ScriptSecurityMode.AUDIT.name());
    DbScriptViolationStore violationStore = new DbScriptViolationStore(configuration);
    configuration.setScriptViolationStore(violationStore);
    ScriptViolationListener listener = event -> { };
    configuration.addScriptViolationListener(listener);

    // when
    configuration.initScriptSecurityPolicy();

    // then
    DbAwareScriptSecurityPolicy policy = (DbAwareScriptSecurityPolicy) configuration.getScriptSecurityPolicy();
    assertThat(policy.getListeners()).containsExactly(listener);
  }

  @Test
  public void shouldRejectUnrecognizedScriptSecurityModeAndAbortEngineBuild() {
    // given — e.g. a typo in bpm-platform.xml's scriptSecurityMode property
    StandaloneInMemProcessEngineConfiguration configuration =
        new StandaloneInMemProcessEngineConfiguration();
    configuration.setScriptSecurityMode("ENFORCEE");

    // when / then
    assertThatThrownBy(configuration::initScriptSecurityPolicy)
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("scriptSecurityMode")
        .hasMessageContaining("ENFORCEE");
  }

  @Test
  public void shouldRejectNullScriptSecurityMode() {
    // given
    StandaloneInMemProcessEngineConfiguration configuration =
        new StandaloneInMemProcessEngineConfiguration();
    configuration.setScriptSecurityMode(null);

    // when / then
    assertThatThrownBy(configuration::initScriptSecurityPolicy)
        .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void shouldAcceptModeRegardlessOfCase() {
    // given — bpm-platform.xml/YAML values are matched case-insensitively elsewhere
    // (isScriptSecurityDisabled()/isScriptSecurityAuditMode()); validation must agree
    StandaloneInMemProcessEngineConfiguration configuration =
        new StandaloneInMemProcessEngineConfiguration();
    configuration.setScriptSecurityMode("audit");

    // when / then — must not throw
    configuration.initScriptSecurityPolicy();
    assertThat(configuration.getScriptSecurityPolicy()).isInstanceOf(DbAwareScriptSecurityPolicy.class);
  }
}
