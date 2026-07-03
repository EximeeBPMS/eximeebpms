package org.eximeebpms.bpm.engine.impl.scripting.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
  public void shouldAllowSafeTaskListenerScript() {
    ScriptSecurityContext context = context(
        "javascript",
        "if(!!task.getVariable('approver')) { task.setAssignee(approver); }");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isAllowed()).isTrue();
    assertThat(decision.getReason()).isEmpty();
    assertThat(decision.getCode()).isEmpty();
  }

  @Test
  public void shouldAllowSafeTaskVariableAssignmentScript() {
    ScriptSecurityContext context = context(
        "javascript",
        "task.setVariable('approver', task.getAssignee());");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isAllowed()).isTrue();
    assertThat(decision.getReason()).isEmpty();
    assertThat(decision.getCode()).isEmpty();
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
  public void shouldBlockLoadFunction() {
    ScriptSecurityContext context = context(
        "javascript",
        "load('/tmp/script.js');");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Loading external scripts is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_LOAD");
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
  public void shouldBlockWhitespaceObfuscatedSystemGetenvAccess() {
    ScriptSecurityContext context = context("javascript", "System   .   getenv ( 'HOME' ) ;");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldBlockJavaLangSystemGetenvAccess() {
    ScriptSecurityContext context = context("javascript", "java.lang.System.getenv('HOME');");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldBlockSystemGetPropertyAccess() {
    ScriptSecurityContext context = context("javascript", "System.getProperty('user.home');");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Access to JVM system properties is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_SYSTEM_GET_PROPERTY");
  }

  @Test
  public void shouldBlockProcessBuilderAccess() {
    ScriptSecurityContext context = context(
        "groovy",
        "new ProcessBuilder('sh', '-c', 'id').start();");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Process execution via ProcessBuilder is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_PROCESS_BUILDER");
  }

  @Test
  public void shouldBlockJavaProcessBuilderAccess() {
    ScriptSecurityContext context = context(
        "javascript",
        "new java.lang.ProcessBuilder('sh', '-c', 'id').start();");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Process execution via ProcessBuilder is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_PROCESS_BUILDER");
  }

  @Test
  public void shouldBlockJavaTypeProcessBuilderAccessForUserScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Java.type('java.lang.ProcessBuilder')")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.USER)
        .build();

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
  public void shouldBlockJavaLangRuntimeAccess() {
    ScriptSecurityContext context = context(
        "javascript",
        "java.lang.Runtime.getRuntime().exec('id');");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_RUNTIME");
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
  public void shouldBlockJavaIoAccess() {
    ScriptSecurityContext context = context("javascript", "new java.io.File('/etc/passwd');");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("File system access is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_IO");
  }

  @Test
  public void shouldBlockJavaNioAccess() {
    ScriptSecurityContext context = context(
        "javascript",
        "java.nio.file.Files.readAllBytes(java.nio.file.Paths.get('/etc/passwd'));");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("NIO file system access is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_NIO_FILE");
  }

  @Test
  public void shouldBlockReflectionAccess() {
    ScriptSecurityContext context = context(
        "javascript",
        "java.lang.Class.forName('java.lang.Runtime');");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_CLASS_FOR_NAME");
  }

  @Test
  public void shouldBlockGroovyShellAccess() {
    ScriptSecurityContext context = context(
        "groovy",
        "new GroovyShell().evaluate('println 1');");

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_GROOVY_SHELL");
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
  public void shouldBlockGenericJavaTypeLookupForUserScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Java.type('org.eximeebpms.spin.Spin');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.USER)
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Host class lookup is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_TYPE");
  }

  @Test
  public void shouldBlockGenericJavaTypeLookupForProcessApplicationScriptWhenClassIsNotDangerous() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Java.type('org.eximeebpms.spin.Spin');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.PROCESS_APPLICATION)
        .provider("process-application")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Host class lookup is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_TYPE");
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
  public void shouldBlockDangerousJavaTypeLookupForProcessApplicationScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Java.type('java.lang.Runtime').getRuntime().exec('id');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.PROCESS_APPLICATION)
        .provider("process-application")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_RUNTIME");
  }

  @Test
  public void shouldBlockPackagesHostClassLookupForUserScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Packages.org.eximeebpms.spin.Spin;")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.USER)
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Host class lookup via Packages is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_TYPE");
  }

  @Test
  public void shouldBlockPackagesLookupForProcessApplicationScriptWhenClassIsNotDangerous() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Packages.org.eximeebpms.spin.Spin;")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.PROCESS_APPLICATION)
        .provider("process-application")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Host class lookup via Packages is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_TYPE");
  }

  @Test
  public void shouldBlockPackagesJavaNetAccessForUserScript() {
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
  public void shouldBlockPackagesJavaNetAccessForPlatformScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Packages.java.net.Socket;")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.PLATFORM)
        .provider("platform-environment")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Network access is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_NET");
  }

  @Test
  public void shouldBlockPackagesJavaNetAccessForProcessApplicationScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Packages.java.net.Socket;")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.PROCESS_APPLICATION)
        .provider("process-application")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Network access is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_NET");
  }

  @Test
  public void shouldBlockJavaTypeFileSystemAccessForProcessApplicationScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Java.type('java.io.File');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.PROCESS_APPLICATION)
        .provider("process-application")
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("File system access is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_IO");
  }

  @Test
  public void shouldBlockNewJavaLangProcessBuilderForUserScript() {
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("var proc = new java.lang.ProcessBuilder('sh', '-c', 'id').start();")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.USER)
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isDenied()).isTrue();
    assertThat(decision.getReason()).contains("Process execution via ProcessBuilder is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_PROCESS_BUILDER");
  }

  @Test
  public void shouldReturnAuditDecisionInAuditModeForRegexMatchedForbiddenScript() {
    policy = new DefaultScriptSecurityPolicy(Set.of(), true);

    ScriptSecurityDecision decision = policy.evaluate(context("javascript", "System.getenv('HOME');"));

    assertThat(decision.isAudit()).isTrue();
    assertThat(decision.isAllowed()).isFalse();
    assertThat(decision.isDenied()).isFalse();
    assertThat(decision.getReason()).contains("Access to environment variables is forbidden");
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldReturnAuditDecisionInAuditModeForDenyRuleMatchedForbiddenScript() {
    policy = new DefaultScriptSecurityPolicy(Set.of(), true);

    ScriptSecurityDecision decision = policy.evaluate(context("groovy", "new ProcessBuilder('sh', '-c', 'id').start();"));

    assertThat(decision.isAudit()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_PROCESS_BUILDER");
  }

  @Test
  public void shouldReturnAuditDecisionInAuditModeForHostClassLookup() {
    policy = new DefaultScriptSecurityPolicy(Set.of(), true);
    ScriptSecurityContext context = ScriptSecurityContext.builder("javascript")
        .source("Java.type('org.eximeebpms.spin.Spin');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .origin(ScriptOrigin.USER)
        .build();

    ScriptSecurityDecision decision = policy.evaluate(context);

    assertThat(decision.isAudit()).isTrue();
    assertThat(decision.getCode()).contains("SCRIPT_SECURITY_JAVA_TYPE");
  }

  @Test
  public void shouldAllowSafeScriptInAuditMode() {
    policy = new DefaultScriptSecurityPolicy(Set.of(), true);

    ScriptSecurityDecision decision = policy.evaluate(context("javascript", "1 + 1;"));

    assertThat(decision.isAllowed()).isTrue();
    assertThat(decision.isAudit()).isFalse();
    assertThat(decision.isDenied()).isFalse();
  }

  @Test
  public void shouldRecordViolationEventToStoreWhenDenied() {
    InMemoryScriptViolationStore store = new InMemoryScriptViolationStore(100);
    policy = new DefaultScriptSecurityPolicy(Set.of(), false, store);

    policy.evaluate(context("groovy", "Runtime.getRuntime().exec('id');"));

    assertThat(store.getTotalCount()).isEqualTo(1);
    ScriptViolationEvent event = store.getRecent(1).get(0);
    assertThat(event.language()).isEqualTo("groovy");
    assertThat(event.ruleCode()).isEqualTo("SCRIPT_SECURITY_RUNTIME_EXEC");
    assertThat(event.reason()).isNotBlank();
    assertThat(event.timestamp()).isNotNull();
  }

  @Test
  public void shouldRecordViolationEventToStoreWhenAudit() {
    InMemoryScriptViolationStore store = new InMemoryScriptViolationStore(100);
    policy = new DefaultScriptSecurityPolicy(Set.of(), true, store);

    policy.evaluate(context("javascript", "System.getenv('HOME');"));

    assertThat(store.getTotalCount()).isEqualTo(1);
    ScriptViolationEvent event = store.getRecent(1).get(0);
    assertThat(event.ruleCode()).isEqualTo("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldNotRecordEventToStoreWhenAllowed() {
    InMemoryScriptViolationStore store = new InMemoryScriptViolationStore(100);
    policy = new DefaultScriptSecurityPolicy(Set.of(), false, store);

    policy.evaluate(context("javascript", "1 + 1;"));

    assertThat(store.getTotalCount()).isEqualTo(0);
    assertThat(store.getRecent(10)).isEmpty();
  }

  @Test
  public void shouldRecordMultipleViolationEventsForMultipleEvaluations() {
    InMemoryScriptViolationStore store = new InMemoryScriptViolationStore(100);
    policy = new DefaultScriptSecurityPolicy(Set.of(), false, store);

    policy.evaluate(context("groovy", "Runtime.getRuntime().exec('id');"));
    policy.evaluate(context("javascript", "new java.io.File('/etc/passwd');"));
    policy.evaluate(context("javascript", "1 + 1;"));  // safe, not recorded

    List<ScriptViolationEvent> events = store.getRecent(10);
    assertThat(store.getTotalCount()).isEqualTo(2);
    assertThat(events).hasSize(2);
    assertThat(events).extracting(ScriptViolationEvent::language).containsExactlyInAnyOrder("groovy", "javascript");
  }

  protected ScriptSecurityContext context(String language, String source) {
    return ScriptSecurityContext.builder(language)
        .source(source)
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .build();
  }
}
