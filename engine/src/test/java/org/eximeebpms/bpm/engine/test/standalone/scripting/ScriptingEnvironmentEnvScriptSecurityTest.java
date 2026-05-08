package org.eximeebpms.bpm.engine.test.standalone.scripting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eximeebpms.bpm.application.ProcessApplicationInterface;
import org.eximeebpms.bpm.engine.impl.scripting.env.ScriptEnvResolver;
import org.eximeebpms.bpm.engine.impl.scripting.security.DefaultScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityException;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityPolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ScriptingEnvironmentEnvScriptSecurityTest extends AbstractScriptEnvironmentTest {

  protected boolean previousScriptSecurityEnabled;
  protected ScriptSecurityPolicy previousScriptSecurityPolicy;

  @Override
  protected ScriptEnvResolver getResolver() {
    return language -> new String[] { "System.getenv('HOME');" };
  }

  @Override
  protected String getScript() {
    return "1 + 1;";
  }

  @Before
  public void enableScriptSecurity() {
    previousScriptSecurityEnabled = processEngineConfiguration.isScriptSecurityEnabled();
    previousScriptSecurityPolicy = processEngineConfiguration.getScriptSecurityPolicy();

    processEngineConfiguration.setScriptSecurityEnabled(true);
    processEngineConfiguration.setScriptSecurityPolicy(new DefaultScriptSecurityPolicy());
  }

  @After
  public void resetScriptSecurity() {
    processEngineConfiguration.setScriptSecurityPolicy(previousScriptSecurityPolicy);
    processEngineConfiguration.setScriptSecurityEnabled(previousScriptSecurityEnabled);
  }

  @Test
  public void shouldBlockForbiddenEnvironmentScriptBeforeMainScriptEvaluation() {
    // given
    ProcessApplicationInterface processApplication = this.processApplication;

    // when / then
    assertThatThrownBy(() -> executeScript(processApplication, "javascript"))
        .isInstanceOf(ScriptSecurityException.class)
        .hasMessageContaining("Script execution blocked by script security policy")
        .hasMessageContaining("Access to environment variables is forbidden");
  }
}
