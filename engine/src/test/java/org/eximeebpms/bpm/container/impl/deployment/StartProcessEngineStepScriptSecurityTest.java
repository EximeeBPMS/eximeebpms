package org.eximeebpms.bpm.container.impl.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.eximeebpms.bpm.engine.ManagementService;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.businessevent.script.BusinessEventScriptViolationListener;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbAwareScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.scripting.security.DefaultScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.NoOpScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityMode;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationStore;
import org.junit.Test;

/**
 * Unit tests for {@link StartProcessEngineStep#preConfigureScriptSecurity} and
 * {@link StartProcessEngineStep#wireScriptSecurityPolicy}, the mechanism that gives a
 * plain-XML/container (Tomcat) deployment the same DB-backed, hot-reloadable, REST-driven,
 * violation-persisting script security configuration as the Spring Boot starter.
 */
public class StartProcessEngineStepScriptSecurityTest {

  private final StartProcessEngineStep step = new StartProcessEngineStep(null) {
    // minimal override — we only test wireScriptSecurityPolicy
  };

  @Test
  public void wiresManagementServiceAndSeedsDbWhenPolicyIsDbAware() {
    // given
    DbAwareScriptSecurityPolicy.Config initialConfig =
        DbAwareScriptSecurityPolicy.Config.audit(Set.of("legacyInvoiceProcess"));
    DbAwareScriptSecurityPolicy policy =
        new DbAwareScriptSecurityPolicy(initialConfig, NoOpScriptViolationStore.INSTANCE);

    ProcessEngineConfigurationImpl config = mock(ProcessEngineConfigurationImpl.class);
    when(config.getScriptSecurityPolicy()).thenReturn(policy);

    ProcessEngine processEngine = mock(ProcessEngine.class);
    ManagementService managementService = mock(ManagementService.class);
    when(processEngine.getManagementService()).thenReturn(managementService);
    when(managementService.getProperties()).thenReturn(Collections.emptyMap());

    // when
    step.wireScriptSecurityPolicy(config, processEngine);

    // then — the DB row didn't exist, so it gets seeded from the XML-configured initialConfig
    verify(managementService).setProperty(DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.AUDIT.name());
    verify(managementService).setProperty(DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, "legacyInvoiceProcess");
  }

  @Test
  public void doesNotSeedWhenDbRowAlreadyExists() {
    // given
    DbAwareScriptSecurityPolicy.Config initialConfig =
        DbAwareScriptSecurityPolicy.Config.audit(Set.of());
    DbAwareScriptSecurityPolicy policy =
        new DbAwareScriptSecurityPolicy(initialConfig, NoOpScriptViolationStore.INSTANCE);

    ProcessEngineConfigurationImpl config = mock(ProcessEngineConfigurationImpl.class);
    when(config.getScriptSecurityPolicy()).thenReturn(policy);

    ProcessEngine processEngine = mock(ProcessEngine.class);
    ManagementService managementService = mock(ManagementService.class);
    when(processEngine.getManagementService()).thenReturn(managementService);
    Map<String, String> existing = Map.of(DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.ENFORCE.name());
    when(managementService.getProperties()).thenReturn(existing);

    // when
    step.wireScriptSecurityPolicy(config, processEngine);

    // then — a DB row already existed (e.g. from a previous start), so it must not be overwritten
    verify(managementService, never()).setProperty(eq(DbAwareScriptSecurityPolicy.PROP_MODE), any());
  }

  @Test
  public void doesNothingWhenScriptSecurityIsDisabled() {
    // given — DISABLED means initScriptSecurityPolicy() sets scriptSecurityPolicy to null
    ProcessEngineConfigurationImpl config = mock(ProcessEngineConfigurationImpl.class);
    when(config.getScriptSecurityPolicy()).thenReturn(null);

    ProcessEngine processEngine = mock(ProcessEngine.class);

    // when / then — must not throw, must not touch the ManagementService at all
    step.wireScriptSecurityPolicy(config, processEngine);
    verifyNoInteractions(processEngine);
  }

  @Test
  public void doesNothingWhenPolicyIsNotDbAware() {
    // given — a caller supplied a fully custom, non-DB-backed policy
    ProcessEngineConfigurationImpl config = mock(ProcessEngineConfigurationImpl.class);
    when(config.getScriptSecurityPolicy()).thenReturn(new DefaultScriptSecurityPolicy());

    ProcessEngine processEngine = mock(ProcessEngine.class);

    // when / then
    step.wireScriptSecurityPolicy(config, processEngine);
    verifyNoInteractions(processEngine);
  }

  @Test
  public void preConfigureInstallsDbBackedViolationStoreAndBusinessEventListener() {
    // given
    ProcessEngineConfigurationImpl config = new StandaloneInMemProcessEngineConfiguration();
    config.setScriptSecurityMode(ScriptSecurityMode.AUDIT.name());

    // when
    step.preConfigureScriptSecurity(config);

    // then
    assertThat(config.getScriptViolationStore()).isInstanceOf(DbScriptViolationStore.class);
    assertThat(config.getScriptViolationListeners())
        .hasAtLeastOneElementOfType(BusinessEventScriptViolationListener.class);
  }

  @Test
  public void preConfigureDoesNotOverwriteAnAlreadyCustomizedViolationStore() {
    // given — a ProcessEnginePlugin (or embedder) already installed its own store
    ProcessEngineConfigurationImpl config = new StandaloneInMemProcessEngineConfiguration();
    config.setScriptSecurityMode(ScriptSecurityMode.ENFORCE.name());
    ScriptViolationStore custom = mock(ScriptViolationStore.class);
    config.setScriptViolationStore(custom);

    // when
    step.preConfigureScriptSecurity(config);

    // then
    assertThat(config.getScriptViolationStore()).isSameAs(custom);
  }

  @Test
  public void preConfigureDoesNothingWhenScriptSecurityIsDisabled() {
    // given
    ProcessEngineConfigurationImpl config = new StandaloneInMemProcessEngineConfiguration();
    config.setScriptSecurityMode(ScriptSecurityMode.DISABLED.name());

    // when
    step.preConfigureScriptSecurity(config);

    // then — untouched defaults
    assertThat(config.getScriptViolationStore()).isInstanceOf(NoOpScriptViolationStore.class);
    assertThat(config.getScriptViolationListeners()).isEmpty();
  }
}
