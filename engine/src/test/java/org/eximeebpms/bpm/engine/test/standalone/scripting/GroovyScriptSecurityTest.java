package org.eximeebpms.bpm.engine.test.standalone.scripting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eximeebpms.bpm.engine.ProcessEngineException;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.scripting.ExecutableScript;
import org.eximeebpms.bpm.engine.impl.scripting.SourceExecutableScript;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class GroovyScriptSecurityTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected boolean previousScriptSecurityEnabled;

  @Before
  public void setUp() {
    processEngineConfiguration =
        (ProcessEngineConfigurationImpl) processEngineRule.getProcessEngine().getProcessEngineConfiguration();

    previousScriptSecurityEnabled = processEngineConfiguration.isScriptSecurityEnabled();
    processEngineConfiguration.setScriptSecurityEnabled(true);
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setScriptSecurityEnabled(previousScriptSecurityEnabled);
  }

  @Test
  public void shouldBlockSystemExitInGroovyScript() {
    assertThatThrownBy(() -> executeGroovyScript("System.exit(0)"))
        .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void shouldBlockRuntimeExecInGroovyScript() {
    assertThatThrownBy(() -> executeGroovyScript("Runtime.getRuntime().exec('whoami')"))
        .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void shouldBlockFileAccessInGroovyScript() {
    assertThatThrownBy(() -> executeGroovyScript("new File('/tmp/test').text"))
        .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void shouldAllowSimpleGroovyScript() {
    executeGroovyScript("def x = 1 + 2");
  }

  @Test
  public void shouldNotUseGroovySandboxWhenSecurityDisabled() {
    processEngineConfiguration.setScriptSecurityEnabled(false);

    assertThatThrownBy(() -> executeGroovyScript("new File('/path/that/does/not/exist').text"))
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("/path/that/does/not/exist");
  }

  protected Object executeGroovyScript(String scriptSource) {
    return processEngineConfiguration
        .getCommandExecutorTxRequired()
        .execute(commandContext -> {
          ExecutableScript script = new SourceExecutableScript("groovy", scriptSource);
          return processEngineConfiguration.getScriptingEnvironment().execute(script, null);
        });
  }
}
