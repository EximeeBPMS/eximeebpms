package org.eximeebpms.bpm.engine.impl.scripting.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
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
    ScriptSecurityContext context = context("javascript", "1 + 1;");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isAllowed()).isTrue();
    assertThat(decision.getReason()).isEmpty();
    assertThat(decision.getCode()).isEmpty();
  }

  @Test
  public void shouldBlockSystemGetenvAccess() {
    ScriptSecurityContext context = context("javascript", "System.getenv('HOME');");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Access to environment variables is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldBlockProcessBuilderAccess() {
    ScriptSecurityContext context = context("groovy", "new ProcessBuilder('sh', '-c', 'id').start();");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Process execution via ProcessBuilder is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_PROCESS_BUILDER");
  }

  @Test
  public void shouldBlockRuntimeAccess() {
    ScriptSecurityContext context = context("groovy", "Runtime.getRuntime().exec('id');");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Runtime process execution is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_RUNTIME_EXEC");
  }

  @Test
  public void shouldBlockWhitespaceObfuscatedSystemGetenvAccess() {
    ScriptSecurityContext context = context("javascript", "System   .   getenv ( 'HOME' ) ;");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldBlockJavaNetAccess() {
    ScriptSecurityContext context = context("groovy", "new java.net.Socket('127.0.0.1', 443);");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Network access is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_NET");
  }

  @Test
  public void shouldAllowForbiddenScriptForAllowlistedProcessDefinitionKey() {
    policy = new DefaultScriptSecurityPolicy(Set.of("legacyInvoiceProcess"));

    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("System.getenv('HOME');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .processDefinitionKey("legacyInvoiceProcess")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isAllowed()).isTrue();
    assertThat(decision.getReason()).isEmpty();
    assertThat(decision.getCode()).isEmpty();
  }

  @Test
  public void shouldDenyForbiddenScriptForNonAllowlistedProcessDefinitionKey() {
    policy = new DefaultScriptSecurityPolicy(Set.of("legacyInvoiceProcess"));

    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("System.getenv('HOME');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .processDefinitionKey("newSecureProcess")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldMatchAllowlistedProcessDefinitionKeyIgnoringCaseAndWhitespace() {
    policy = new DefaultScriptSecurityPolicy(Set.of(" legacyInvoiceProcess "));

    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("System.getenv('HOME');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .processDefinitionKey("LEGACYinvoicePROCESS")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isAllowed()).isTrue();
  }

  @Test
  public void shouldDenyForbiddenScriptWhenAllowlistIsEmptyAndProcessDefinitionKeyIsPresent() {
    DefaultScriptSecurityPolicy policy = new DefaultScriptSecurityPolicy(Set.of());

    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("System.getenv('HOME');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .processDefinitionKey("anyProcess")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldAllowSpinFunctionCall() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("execution.setVariable('name', S('<test />').name());")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isAllowed()).isTrue();
  }

  @Test
  public void shouldBlockGenericJavaTypeLookupForUserScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Java.type('org.eximeebpms.spin.Spin');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.USER)
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_TYPE");
  }

  @Test
  public void shouldBlockDangerousJavaTypeForUserScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Java.type('java.lang.System').getenv('HOME');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.USER)
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_LANG_SYSTEM");
  }

  @Test
  public void shouldAllowPlatformEnvironmentScriptWithSpinJavaType() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Java.type('org.eximeebpms.spin.Spin');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.PLATFORM)
        .provider("platform-environment")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isAllowed()).isTrue();
  }

  @Test
  public void shouldBlockPackagesHostClassLookupEvenForPlatformScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Packages.org.eximeebpms.spin.Spin;")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.PLATFORM)
        .provider("platform-environment")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_TYPE");
  }

  @Test
  public void shouldBlockDangerousJavaTypeForPlatformScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Java.type('java.lang.System').getenv('HOME');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.PLATFORM)
        .provider("platform-environment")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_LANG_SYSTEM");
  }

  @Test
  public void shouldBlockUserPackagesHostClassLookup() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Packages.org.eximeebpms.spin.Spin;")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.USER)
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_TYPE");
  }

  @Test
  public void shouldBlockPackagesJavaNetAccess() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Packages.java.net.Socket;")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.USER)
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Network access is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_NET");
  }

  @Test
  public void shouldBlockJavaTypeLookupForProcessApplicationScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Java.type('org.eximeebpms.spin.Spin');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.PROCESS_APPLICATION)
        .provider("process-application")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_TYPE");
  }

  @Test
  public void shouldBlockGenericJavaTypeLookupForPlatformScriptWhenClassIsNotAllowlisted() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Java.type('com.example.SomeClass');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.PLATFORM)
        .provider("platform-environment")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_TYPE");
  }

  protected ScriptSecurityContext context(String language, String source) {
    return ScriptSecurityContext.builder(language)
        .source(source)
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .build();
  }
}
