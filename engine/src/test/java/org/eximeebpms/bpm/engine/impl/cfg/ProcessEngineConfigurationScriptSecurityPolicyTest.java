package org.eximeebpms.bpm.engine.impl.cfg;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import org.eximeebpms.bpm.engine.impl.scripting.env.ScriptingEnvironment;
import org.eximeebpms.bpm.engine.impl.scripting.security.DefaultScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityDecision;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityPolicy;
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
  public void shouldInitializeDefaultScriptSecurityPolicy() {
    // when
    ScriptSecurityPolicy policy = processEngineConfiguration.getScriptSecurityPolicy();

    // then
    assertThat(policy).isNotNull();
    assertThat(policy).isInstanceOf(DefaultScriptSecurityPolicy.class);
  }

  @Test
  public void shouldPropagateConfiguredScriptSecurityPolicyToScriptingEnvironment() throws Exception {
    // given
    ScriptSecurityPolicy customPolicy = context -> ScriptSecurityDecision.allow();

    // when
    processEngineConfiguration.setScriptSecurityPolicy(customPolicy);
    processEngineConfiguration.setScriptingEnvironment(null);
    processEngineConfiguration.initScripting();

    // then
    assertThat(processEngineConfiguration.getScriptSecurityPolicy()).isSameAs(customPolicy);
    assertThat(extractScriptSecurityPolicy(processEngineConfiguration.getScriptingEnvironment())).isSameAs(customPolicy);
  }

  protected ScriptSecurityPolicy extractScriptSecurityPolicy(ScriptingEnvironment scriptingEnvironment) throws Exception {
    Field field = ScriptingEnvironment.class.getDeclaredField("scriptSecurityPolicy");
    field.setAccessible(true);
    return (ScriptSecurityPolicy) field.get(scriptingEnvironment);
  }
}
