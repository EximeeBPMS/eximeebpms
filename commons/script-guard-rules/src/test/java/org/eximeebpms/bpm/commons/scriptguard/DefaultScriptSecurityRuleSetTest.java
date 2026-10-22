package org.eximeebpms.bpm.commons.scriptguard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DefaultScriptSecurityRuleSetTest {

  private final ScriptSecurityRuleSet ruleSet = DefaultScriptSecurityRuleSet.INSTANCE;

  private ScriptValidationResult validate(String source) {
    return ruleSet.validate(source, ScriptOrigin.USER);
  }

  private ScriptValidationResult validate(String source, ScriptOrigin origin) {
    return ruleSet.validate(source, origin);
  }

  @Test
  void shouldAllowSafeScript() {
    assertThat(validate("1 + 1;").isClean()).isTrue();
  }

  @Test
  void shouldAllowSafeTaskListenerScript() {
    assertThat(validate("if(!!task.getVariable('approver')) { task.setAssignee(approver); }").isClean()).isTrue();
  }

  @Test
  void shouldAllowSpinFunctionCall() {
    assertThat(validate("execution.setVariable('name', S('<test />').name());").isClean()).isTrue();
  }

  @Test
  void shouldBlockLoadFunction() {
    ScriptValidationResult result = validate("load('/tmp/script.js');");

    assertThat(result.isClean()).isFalse();
    assertThat(result.getMatches()).hasSize(1);
    assertThat(result.getMatches().get(0).ruleCode()).isEqualTo("SCRIPT_SECURITY_LOAD");
    assertThat(result.getMatches().get(0).reason()).isEqualTo("Loading external scripts is forbidden");
  }

  @Test
  void shouldBlockSystemGetenvAccess() {
    ScriptValidationResult result = validate("System.getenv('HOME');");

    assertThat(result.getMatches().get(0).ruleCode()).isEqualTo("SCRIPT_SECURITY_SYSTEM_GETENV");
    assertThat(result.getMatches().get(0).reason()).isEqualTo("Access to environment variables is forbidden");
  }

  @Test
  void shouldBlockWhitespaceObfuscatedSystemGetenvAccess() {
    assertThat(validate("System   .   getenv ( 'HOME' ) ;").getMatches().get(0).ruleCode())
        .isEqualTo("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  void shouldBlockJavaLangSystemGetenvAccess() {
    assertThat(validate("java.lang.System.getenv('HOME');").getMatches().get(0).ruleCode())
        .isEqualTo("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  void shouldBlockSystemGetPropertyAccess() {
    assertThat(validate("System.getProperty('user.home');").getMatches().get(0).ruleCode())
        .isEqualTo("SCRIPT_SECURITY_SYSTEM_GET_PROPERTY");
  }

  @Test
  void shouldPreferGetenvSpecialCaseOverEarlierRuleWhenScriptMatchesBoth() {
    // Runtime.getRuntime() alone matches SCRIPT_SECURITY_RUNTIME_EXEC, which precedes
    // System.getenv's own rule in the main list — but the getenv/getProperty regex
    // special-case is evaluated before the main rule list (mirrors
    // DefaultScriptSecurityPolicy.evaluate's precedence exactly), so this must still
    // report SCRIPT_SECURITY_SYSTEM_GETENV, not SCRIPT_SECURITY_RUNTIME_EXEC.
    ScriptValidationResult result = validate("Runtime.getRuntime(); System.getenv('HOME');");

    assertThat(result.getMatches().get(0).ruleCode()).isEqualTo("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  void shouldBlockProcessBuilderAccess() {
    assertThat(validate("new ProcessBuilder('sh', '-c', 'id').start();").getMatches().get(0).ruleCode())
        .isEqualTo("SCRIPT_SECURITY_PROCESS_BUILDER");
  }

  @Test
  void shouldBlockRuntimeAccess() {
    assertThat(validate("Runtime.getRuntime().exec('id');").getMatches().get(0).ruleCode())
        .isEqualTo("SCRIPT_SECURITY_RUNTIME_EXEC");
  }

  @Test
  void shouldBlockJavaNetAccess() {
    assertThat(validate("new java.net.Socket('127.0.0.1', 443);").getMatches().get(0).ruleCode())
        .isEqualTo("SCRIPT_SECURITY_JAVA_NET");
  }

  @Test
  void shouldBlockJavaIoAccess() {
    assertThat(validate("new java.io.File('/etc/passwd');").getMatches().get(0).ruleCode())
        .isEqualTo("SCRIPT_SECURITY_JAVA_IO");
  }

  @Test
  void shouldBlockReflectionAccess() {
    assertThat(validate("java.lang.Class.forName('java.lang.Runtime');").getMatches().get(0).ruleCode())
        .isEqualTo("SCRIPT_SECURITY_CLASS_FOR_NAME");
  }

  @Test
  void shouldBlockGroovyShellAccess() {
    assertThat(validate("new GroovyShell().evaluate('println 1');").getMatches().get(0).ruleCode())
        .isEqualTo("SCRIPT_SECURITY_GROOVY_SHELL");
  }

  @Test
  void shouldBlockGenericJavaTypeLookupForUserOrigin() {
    ScriptValidationResult result = validate("Java.type('org.eximeebpms.spin.Spin');", ScriptOrigin.USER);

    assertThat(result.getMatches().get(0).ruleCode()).isEqualTo("SCRIPT_SECURITY_JAVA_TYPE");
    assertThat(result.getMatches().get(0).reason()).isEqualTo("Host class lookup is forbidden");
  }

  @Test
  void shouldBlockGenericJavaTypeLookupForProcessApplicationOrigin() {
    assertThat(validate("Java.type('org.eximeebpms.spin.Spin');", ScriptOrigin.PROCESS_APPLICATION).isClean())
        .isFalse();
  }

  @Test
  void shouldAllowJavaTypeLookupForPlatformOrigin() {
    assertThat(validate("Java.type('org.eximeebpms.spin.Spin');", ScriptOrigin.PLATFORM).isClean()).isTrue();
  }

  @Test
  void shouldBlockDangerousJavaTypeEvenForPlatformOrigin() {
    // The generic host-class-lookup exemption is for PLATFORM origin only, but a
    // *specific* dangerous class (java.lang.System) is still caught by its own rule
    // regardless of origin — the PLATFORM exemption only applies to the generic
    // java.type()/Packages. rule, not the specific denylist rules.
    assertThat(validate("Java.type('java.lang.System').getenv('HOME');", ScriptOrigin.PLATFORM).getMatches().get(0)
        .ruleCode()).isEqualTo("SCRIPT_SECURITY_JAVA_LANG_SYSTEM");
  }

  @Test
  void shouldBlockPackagesHostClassLookupForUserOrigin() {
    ScriptValidationResult result = validate("Packages.org.eximeebpms.spin.Spin;", ScriptOrigin.USER);

    assertThat(result.getMatches().get(0).ruleCode()).isEqualTo("SCRIPT_SECURITY_JAVA_TYPE");
    assertThat(result.getMatches().get(0).reason()).isEqualTo("Host class lookup via Packages is forbidden");
  }

  @Test
  void shouldAllowPackagesLookupForPlatformOrigin() {
    assertThat(validate("Packages.org.eximeebpms.spin.Spin;", ScriptOrigin.PLATFORM).isClean()).isTrue();
  }
}
