package org.eximeebpms.bpm.engine.impl.scripting.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.eximeebpms.bpm.commons.scriptguard.DefaultScriptSecurityRuleSet;
import org.eximeebpms.bpm.commons.scriptguard.ScriptSecurityRuleSet;
import org.eximeebpms.bpm.commons.scriptguard.ScriptValidationResult;
import org.junit.Test;

/**
 * REQ-0006 acceptance test: the standalone {@code eximeebpms-commons-script-guard-rules}
 * module and the engine's own {@link DefaultScriptSecurityPolicy} must report the same
 * rule match for identical input, proving the REQ-0005 extraction did not introduce
 * divergence between the two callers of the rule set.
 */
public class ScriptSecurityRuleSetParityTest {

  private record Case(String language, String source, ScriptOrigin origin) {
  }

  private static final List<Case> CASES = List.of(
      new Case("javascript", "1 + 1;", ScriptOrigin.USER),
      new Case("javascript", "load('/tmp/script.js');", ScriptOrigin.USER),
      new Case("javascript", "System.getenv('HOME');", ScriptOrigin.USER),
      new Case("javascript", "System   .   getenv ( 'HOME' ) ;", ScriptOrigin.USER),
      new Case("javascript", "java.lang.System.getenv('HOME');", ScriptOrigin.USER),
      new Case("javascript", "System.getProperty('user.home');", ScriptOrigin.USER),
      new Case("groovy", "new ProcessBuilder('sh', '-c', 'id').start();", ScriptOrigin.USER),
      new Case("javascript", "new java.lang.ProcessBuilder('sh', '-c', 'id').start();", ScriptOrigin.USER),
      new Case("groovy", "Runtime.getRuntime().exec('id');", ScriptOrigin.USER),
      new Case("javascript", "java.lang.Runtime.getRuntime().exec('id');", ScriptOrigin.USER),
      new Case("groovy", "new java.net.Socket('127.0.0.1', 443);", ScriptOrigin.USER),
      new Case("javascript", "new java.io.File('/etc/passwd');", ScriptOrigin.USER),
      new Case("javascript", "java.nio.file.Files.readAllBytes(java.nio.file.Paths.get('/etc/passwd'));", ScriptOrigin.USER),
      new Case("javascript", "java.lang.Class.forName('java.lang.Runtime');", ScriptOrigin.USER),
      new Case("groovy", "new GroovyShell().evaluate('println 1');", ScriptOrigin.USER),
      new Case("javascript", "Java.type('org.eximeebpms.spin.Spin');", ScriptOrigin.USER),
      new Case("javascript", "Java.type('org.eximeebpms.spin.Spin');", ScriptOrigin.PROCESS_APPLICATION),
      new Case("javascript", "Java.type('org.eximeebpms.spin.Spin');", ScriptOrigin.PLATFORM),
      new Case("javascript", "Java.type('java.lang.System').getenv('HOME');", ScriptOrigin.USER),
      new Case("javascript", "Java.type('java.lang.System').getenv('HOME');", ScriptOrigin.PLATFORM),
      new Case("javascript", "Java.type('java.lang.Runtime').getRuntime().exec('id');", ScriptOrigin.PROCESS_APPLICATION),
      new Case("javascript", "Packages.org.eximeebpms.spin.Spin;", ScriptOrigin.USER),
      new Case("javascript", "Packages.org.eximeebpms.spin.Spin;", ScriptOrigin.PLATFORM),
      new Case("javascript", "Packages.java.net.Socket;", ScriptOrigin.USER),
      new Case("javascript", "Runtime.getRuntime(); System.getenv('HOME');", ScriptOrigin.USER));

  @Test
  public void engineAndStandaloneModuleAgreeOnEveryRuleMatch() {
    ScriptSecurityPolicy enginePolicy = new DefaultScriptSecurityPolicy();
    ScriptSecurityRuleSet standaloneRuleSet = DefaultScriptSecurityRuleSet.INSTANCE;

    for (Case testCase : CASES) {
      ScriptSecurityContext context = ScriptSecurityContext.builder(testCase.language())
          .source(testCase.source())
          .sourceType(ScriptSourceType.INLINE_SOURCE)
          .origin(testCase.origin())
          .build();

      ScriptSecurityDecision engineDecision = enginePolicy.evaluate(context);
      ScriptValidationResult standaloneResult = standaloneRuleSet.validate(
          testCase.source(), toRuleSetOrigin(testCase.origin()));

      if (engineDecision.isAllowed()) {
        assertThat(standaloneResult.isClean()).as("case: %s", testCase).isTrue();
      } else {
        assertThat(standaloneResult.isClean()).as("case: %s", testCase).isFalse();
        assertThat(standaloneResult.getMatches().get(0).ruleCode())
            .as("case: %s", testCase)
            .isEqualTo(engineDecision.getCode().orElse(null));
        assertThat(standaloneResult.getMatches().get(0).reason())
            .as("case: %s", testCase)
            .isEqualTo(engineDecision.getReason().orElse(null));
      }
    }
  }

  private static org.eximeebpms.bpm.commons.scriptguard.ScriptOrigin toRuleSetOrigin(ScriptOrigin origin) {
    return switch (origin) {
      case USER -> org.eximeebpms.bpm.commons.scriptguard.ScriptOrigin.USER;
      case PROCESS_APPLICATION -> org.eximeebpms.bpm.commons.scriptguard.ScriptOrigin.PROCESS_APPLICATION;
      case PLATFORM -> org.eximeebpms.bpm.commons.scriptguard.ScriptOrigin.PLATFORM;
      case UNKNOWN -> org.eximeebpms.bpm.commons.scriptguard.ScriptOrigin.UNKNOWN;
    };
  }
}
