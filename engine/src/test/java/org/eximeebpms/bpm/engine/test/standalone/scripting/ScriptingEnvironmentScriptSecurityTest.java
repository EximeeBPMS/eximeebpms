package org.eximeebpms.bpm.engine.test.standalone.scripting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.scripting.SourceExecutableScript;
import org.eximeebpms.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.eximeebpms.bpm.engine.impl.scripting.env.ScriptingEnvironment;
import org.eximeebpms.bpm.engine.impl.scripting.security.DefaultScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityException;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ScriptingEnvironmentScriptSecurityTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected ScriptingEnvironment scriptingEnvironment;
  protected ScriptingEngines scriptingEngines;
  protected boolean previousScriptSecurityEnabled;
  protected ScriptSecurityPolicy previousScriptSecurityPolicy;
  protected ScriptingEnvironment previousScriptingEnvironment;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    previousScriptSecurityEnabled = processEngineConfiguration.isScriptSecurityEnabled();
    previousScriptSecurityPolicy = processEngineConfiguration.getScriptSecurityPolicy();
    previousScriptingEnvironment = processEngineConfiguration.getScriptingEnvironment();

    processEngineConfiguration.setScriptSecurityEnabled(true);

    if (processEngineConfiguration.getScriptSecurityPolicy() == null) {
      processEngineConfiguration.setScriptSecurityPolicy(new DefaultScriptSecurityPolicy());
    }

    scriptingEngines = processEngineConfiguration.getScriptingEngines();

    scriptingEnvironment = new ScriptingEnvironment(
        processEngineConfiguration.getScriptFactory(),
        processEngineConfiguration.getEnvScriptResolvers() != null
            ? processEngineConfiguration.getEnvScriptResolvers()
            : new ArrayList<>(),
        scriptingEngines,
        processEngineConfiguration.getScriptSecurityPolicy());

    processEngineConfiguration.setScriptingEnvironment(scriptingEnvironment);
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setScriptSecurityEnabled(previousScriptSecurityEnabled);
    processEngineConfiguration.setScriptSecurityPolicy(previousScriptSecurityPolicy);
    processEngineConfiguration.setScriptingEnvironment(previousScriptingEnvironment);
  }

  @Test
  public void shouldAllowSafeInlineScript() {
    // given
    SourceExecutableScript script = createScript("1 + 1;");
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = createBindings(scriptEngine);

    // when
    Object result = execute(script, scriptEngine, bindings);

    // then
    assertThat(result).isEqualTo(2);
  }

  @Test
  public void shouldBlockSystemGetenvBeforeEvaluation() {
    // given
    SourceExecutableScript script = createScript("System.getenv('HOME');");
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = createBindings(scriptEngine);

    // when / then
    assertBlocked(
        script,
        scriptEngine,
        bindings,
        "Access to environment variables is forbidden",
        "SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldBlockSystemGetPropertyBeforeEvaluation() {
    // given
    SourceExecutableScript script = createScript("System.getProperty('user.home');");
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = createBindings(scriptEngine);

    // when / then
    assertBlocked(
        script,
        scriptEngine,
        bindings,
        "Access to JVM system properties is forbidden",
        "SCRIPT_SECURITY_SYSTEM_GET_PROPERTY");
  }

  @Test
  public void shouldBlockProcessBuilderBeforeEvaluation() {
    // given
    SourceExecutableScript script = createScript("new ProcessBuilder('sh', '-c', 'id').start();");
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = createBindings(scriptEngine);

    // when / then
    assertBlocked(
        script,
        scriptEngine,
        bindings,
        "Process execution via ProcessBuilder is forbidden",
        "SCRIPT_SECURITY_PROCESS_BUILDER");
  }

  @Test
  public void shouldBlockRuntimeExecBeforeEvaluation() {
    // given
    SourceExecutableScript script = createScript("Runtime.getRuntime().exec('id');");
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = createBindings(scriptEngine);

    // when / then
    assertBlocked(
        script,
        scriptEngine,
        bindings,
        "Runtime process execution is forbidden",
        "SCRIPT_SECURITY_RUNTIME_EXEC");
  }

  @Test
  public void shouldBlockJavaNetAccessBeforeEvaluation() {
    // given
    SourceExecutableScript script = createScript("new java.net.Socket('127.0.0.1', 443);");
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = createBindings(scriptEngine);

    // when / then
    assertBlocked(
        script,
        scriptEngine,
        bindings,
        "Network access is forbidden",
        "SCRIPT_SECURITY_JAVA_NET");
  }

  @Test
  public void shouldBlockWhitespaceObfuscatedSystemGetenvBeforeEvaluation() {
    // given
    SourceExecutableScript script = createScript("System   .   getenv ( 'HOME' ) ;");
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = createBindings(scriptEngine);

    // when / then
    assertBlocked(
        script,
        scriptEngine,
        bindings,
        "Access to environment variables is forbidden",
        "SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  protected void assertBlocked(
      SourceExecutableScript script,
      ScriptEngine scriptEngine,
      Bindings bindings,
      String expectedReason,
      String expectedDecisionCode) {

    assertThatThrownBy(() -> execute(script, scriptEngine, bindings))
        .isInstanceOf(ScriptSecurityException.class)
        .hasMessageContaining("Script execution blocked by script security policy")
        .hasMessageContaining(expectedReason)
        .extracting(throwable -> ((ScriptSecurityException) throwable).getDecisionCode().orElse(null))
        .isEqualTo(expectedDecisionCode);
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
