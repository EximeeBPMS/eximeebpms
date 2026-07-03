package org.eximeebpms.bpm.engine.test.standalone.scripting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Set;
import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.scripting.SourceExecutableScript;
import org.eximeebpms.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.eximeebpms.bpm.engine.impl.scripting.env.ScriptingEnvironment;
import org.eximeebpms.bpm.engine.impl.scripting.security.DefaultScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.InMemoryScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityException;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ScriptingEnvironmentAuditModeScriptSecurityTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected ScriptingEnvironment scriptingEnvironment;
  protected ScriptingEngines scriptingEngines;
  protected InMemoryScriptViolationStore violationStore;
  protected boolean previousScriptSecurityEnabled;
  protected ScriptSecurityPolicy previousScriptSecurityPolicy;
  protected ScriptingEnvironment previousScriptingEnvironment;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    previousScriptSecurityEnabled = processEngineConfiguration.isScriptSecurityEnabled();
    previousScriptSecurityPolicy = processEngineConfiguration.getScriptSecurityPolicy();
    previousScriptingEnvironment = processEngineConfiguration.getScriptingEnvironment();

    violationStore = new InMemoryScriptViolationStore(100);
    ScriptSecurityPolicy auditPolicy = new DefaultScriptSecurityPolicy(Set.of(), true, violationStore);

    processEngineConfiguration.setScriptSecurityEnabled(true);
    processEngineConfiguration.setScriptSecurityPolicy(auditPolicy);

    scriptingEngines = processEngineConfiguration.getScriptingEngines();

    scriptingEnvironment = new ScriptingEnvironment(
        processEngineConfiguration.getScriptFactory(),
        processEngineConfiguration.getEnvScriptResolvers() != null
            ? processEngineConfiguration.getEnvScriptResolvers()
            : new ArrayList<>(),
        scriptingEngines,
        auditPolicy);

    processEngineConfiguration.setScriptingEnvironment(scriptingEnvironment);
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setScriptingEnvironment(previousScriptingEnvironment);
    processEngineConfiguration.setScriptSecurityPolicy(previousScriptSecurityPolicy);
    processEngineConfiguration.setScriptSecurityEnabled(previousScriptSecurityEnabled);
  }

  @Test
  public void shouldNotBlockScriptExecutionInAuditModeForForbiddenPattern() {
    SourceExecutableScript script = createScript("System.getenv('HOME');");
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = createBindings(scriptEngine);

    try {
      execute(script, scriptEngine, bindings);
    } catch (ScriptSecurityException e) {
      fail("ScriptSecurityException must not be thrown in audit mode, but was: " + e.getMessage());
    } catch (Exception e) {
      // script may fail at runtime for unrelated reasons — that is acceptable
    }
  }

  @Test
  public void shouldNotBlockRuntimeExecScriptInAuditMode() {
    SourceExecutableScript script = createScript("Runtime.getRuntime().exec('id');");
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = createBindings(scriptEngine);

    try {
      execute(script, scriptEngine, bindings);
    } catch (ScriptSecurityException e) {
      fail("ScriptSecurityException must not be thrown in audit mode, but was: " + e.getMessage());
    } catch (Exception e) {
      // script may fail at runtime — acceptable
    }
  }

  @Test
  public void shouldRecordViolationEventInStoreWhenAuditModeForbiddenScriptRuns() {
    SourceExecutableScript script = createScript("System.getenv('HOME');");
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = createBindings(scriptEngine);

    try {
      execute(script, scriptEngine, bindings);
    } catch (ScriptSecurityException e) {
      fail("ScriptSecurityException must not be thrown in audit mode");
    } catch (Exception e) {
      // script may fail at runtime — acceptable, violation should still be recorded
    }

    assertThat(violationStore.getTotalCount()).isEqualTo(1);
    ScriptViolationEvent event = violationStore.getRecent(1).get(0);
    assertThat(event.ruleCode()).isEqualTo("SCRIPT_SECURITY_SYSTEM_GETENV");
    assertThat(event.language()).isEqualTo(ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE);
  }

  @Test
  public void shouldStillAllowSafeScriptInAuditMode() {
    SourceExecutableScript script = createScript("1 + 1;");
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = createBindings(scriptEngine);

    Object result = execute(script, scriptEngine, bindings);

    assertThat(result).isEqualTo(2);
    assertThat(violationStore.getTotalCount()).isEqualTo(0);
  }

  @Test
  public void shouldEnforceBlockingWhenModeIsSwitchedBackToEnforce() {
    SourceExecutableScript script = createScript("System.getenv('HOME');");
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = createBindings(scriptEngine);

    ScriptSecurityPolicy enforcePolicy = new DefaultScriptSecurityPolicy();
    processEngineConfiguration.setScriptSecurityPolicy(enforcePolicy);
    scriptingEnvironment = new ScriptingEnvironment(
        processEngineConfiguration.getScriptFactory(),
        processEngineConfiguration.getEnvScriptResolvers() != null
            ? processEngineConfiguration.getEnvScriptResolvers()
            : new ArrayList<>(),
        scriptingEngines,
        enforcePolicy);
    processEngineConfiguration.setScriptingEnvironment(scriptingEnvironment);

    assertThatThrownBy(() -> execute(script, scriptEngine, bindings))
        .isInstanceOf(ScriptSecurityException.class)
        .hasMessageContaining("Access to environment variables is forbidden")
        .extracting(t -> ((ScriptSecurityException) t).getDecisionCode().orElse(null))
        .isEqualTo("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  protected Object execute(SourceExecutableScript script, ScriptEngine scriptEngine, Bindings bindings) {
    return processEngineConfiguration.getCommandExecutorTxRequired()
        .execute(commandContext -> scriptingEnvironment.execute(script, null, bindings, scriptEngine));
  }

  protected SourceExecutableScript createScript(String source) {
    return new SourceExecutableScript(ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE, source);
  }

  protected ScriptEngine getJavaScriptEngine() {
    return processEngineConfiguration.getCommandExecutorTxRequired()
        .execute(commandContext -> scriptingEngines.getScriptEngineForLanguage(
            ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE));
  }

  protected Bindings createBindings(ScriptEngine scriptEngine) {
    return scriptingEngines.createBindings(scriptEngine, null);
  }
}
