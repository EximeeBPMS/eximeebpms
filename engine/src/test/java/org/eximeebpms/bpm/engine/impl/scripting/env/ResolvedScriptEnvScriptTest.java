package org.eximeebpms.bpm.engine.impl.scripting.env;

import static org.assertj.core.api.Assertions.assertThat;

import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptOrigin;
import org.junit.Test;

public class ResolvedScriptEnvScriptTest {

  @Test
  public void trustedShouldCreatePlatformOriginScript() {
    ResolvedScriptEnvScript script = ResolvedScriptEnvScript.trusted(
        "Java.type('org.eximeebpms.spin.Spin');",
        "provider");

    assertThat(script.getSource()).isEqualTo("Java.type('org.eximeebpms.spin.Spin');");
    assertThat(script.getOrigin()).isEqualTo(ScriptOrigin.PLATFORM);
    assertThat(script.getProvider()).isEqualTo("provider");
  }

  @Test
  public void userShouldCreateUserOriginScript() {
    ResolvedScriptEnvScript script = ResolvedScriptEnvScript.user("1 + 1;", "provider");

    assertThat(script.getOrigin()).isEqualTo(ScriptOrigin.USER);
  }

  @Test
  public void platformShouldCreatePlatformOriginScript() {
    ResolvedScriptEnvScript script = ResolvedScriptEnvScript.platform("1 + 1;", "provider");

    assertThat(script.getOrigin()).isEqualTo(ScriptOrigin.PLATFORM);
  }
}
