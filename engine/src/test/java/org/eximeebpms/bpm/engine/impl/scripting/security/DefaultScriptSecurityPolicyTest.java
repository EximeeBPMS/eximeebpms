package org.eximeebpms.bpm.engine.impl.scripting.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class DefaultScriptSecurityPolicyTest {

  protected ScriptSecurityPolicy policy;

  @Before
  public void setUp() {
    policy = new DefaultScriptSecurityPolicy();
  }

  @Test
  public void shouldAllowSafeScript() {
    // given
    ScriptSecurityContext context = context("javascript", "1 + 1;");

    // when
    ScriptSecurityDecision decision = policy.evaluate(context);

    // then
    assertThat(decision.isAllowed()).isTrue();
    assertThat(decision.getReason()).isEmpty();
    assertThat(decision.getCode()).isEmpty();
  }

  @Test
  public void shouldBlockSystemGetenvAccess() {
    // given
    ScriptSecurityContext context = context("javascript", "System.getenv('HOME');");

    // when
    ScriptSecurityDecision decision = policy.evaluate(context);

    // then
    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Access to environment variables is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldBlockProcessBuilderAccess() {
    // given
    ScriptSecurityContext context = context("groovy", "new ProcessBuilder('sh', '-c', 'id').start();");

    // when
    ScriptSecurityDecision decision = policy.evaluate(context);

    // then
    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Process execution via ProcessBuilder is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_PROCESS_BUILDER");
  }

  @Test
  public void shouldBlockRuntimeAccess() {
    // given
    ScriptSecurityContext context = context("groovy", "Runtime.getRuntime().exec('id');");

    // when
    ScriptSecurityDecision decision = policy.evaluate(context);

    // then
    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Runtime process execution is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_RUNTIME_EXEC");
  }

  @Test
  public void shouldBlockWhitespaceObfuscatedSystemGetenvAccess() {
    // given
    ScriptSecurityContext context = context("javascript", "System   .   getenv ( 'HOME' ) ;");

    // when
    ScriptSecurityDecision decision = policy.evaluate(context);

    // then
    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldBlockJavaNetAccess() {
    // given
    ScriptSecurityContext context = context("groovy", "new java.net.Socket('127.0.0.1', 443);");

    // when
    ScriptSecurityDecision decision = policy.evaluate(context);

    // then
    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Network access is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_NET");
  }

  protected ScriptSecurityContext context(String language, String source) {
    return ScriptSecurityContext.builder(language)
        .source(source)
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .build();
  }
}
