package org.eximeebpms.bpm.engine.test.standalone.scripting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.eximeebpms.bpm.engine.delegate.Expression;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.el.FixedValue;
import org.eximeebpms.bpm.engine.impl.scripting.DynamicSourceExecutableScript;
import org.eximeebpms.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.eximeebpms.bpm.engine.impl.scripting.env.ScriptingEnvironment;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityException;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ScriptingEnvironmentDynamicSourceScriptSecurityTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected ScriptingEnvironment scriptingEnvironment;
  protected ScriptingEngines scriptingEngines;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    scriptingEnvironment = processEngineConfiguration.getScriptingEnvironment();
    scriptingEngines = processEngineConfiguration.getScriptingEngines();
  }

  @Test
  public void shouldBlockForbiddenDynamicSourceScriptBeforeEvaluation() {
    // given
    String language = ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE;
    ScriptEngine scriptEngine = getScriptEngine(language);
    Expression expression = new FixedValue("System.getenv('HOME');");
    DynamicSourceExecutableScript script = new DynamicSourceExecutableScript(language, expression);
    Bindings bindings = scriptingEngines.createBindings(scriptEngine, null);

    // when / then
    assertThatThrownBy(() -> execute(script, scriptEngine, bindings))
        .isInstanceOf(ScriptSecurityException.class)
        .hasMessageContaining("Script execution blocked by script security policy")
        .hasMessageContaining("Access to environment variables is forbidden");
  }

  protected Object execute(DynamicSourceExecutableScript script, ScriptEngine scriptEngine, Bindings bindings) {
    return processEngineConfiguration.getCommandExecutorTxRequired()
        .execute(commandContext -> scriptingEnvironment.execute(script, null, bindings, scriptEngine));
  }

  protected ScriptEngine getScriptEngine(String language) {
    return processEngineConfiguration.getCommandExecutorTxRequired()
        .execute(commandContext -> scriptingEngines.getScriptEngineForLanguage(language));
  }
}
