package org.eximeebpms.bpm.spring.boot.starter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {TestApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "eximeebpms.bpm.trusted-code.enabled=false",
        "eximeebpms.bpm.trusted-code.mode=AUDIT",
        "eximeebpms.bpm.trusted-code.block-script-tasks=false",
        "eximeebpms.bpm.trusted-code.block-script-execution-listeners=false",
        "eximeebpms.bpm.trusted-code.block-script-task-listeners=true",
        "eximeebpms.bpm.trusted-code.block-external-script-resources=false"
    }
)
public class TrustedCodePropertiesIT {

  @Autowired
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @Test
  public void shouldApplyTrustedCodePropertiesToProcessEngineConfiguration() {
    assertThat(processEngineConfiguration.isTrustedCodeEnabled()).isFalse();
    assertThat(processEngineConfiguration.getTrustedCodePolicyMode()).isEqualTo("AUDIT");
    assertThat(processEngineConfiguration.isTrustedCodeBlockScriptTasks()).isFalse();
    assertThat(processEngineConfiguration.isTrustedCodeBlockScriptExecutionListeners()).isFalse();
    assertThat(processEngineConfiguration.isTrustedCodeBlockScriptTaskListeners()).isTrue();
    assertThat(processEngineConfiguration.isTrustedCodeBlockExternalScriptResources()).isFalse();
  }

  @Test
  public void shouldFailOnInvalidTrustedCodePolicyMode() {
    Throwable thrown = catchThrowable(() -> {
      ConfigurableApplicationContext context = null;
      try {
        context = new SpringApplication(TestApplication.class).run(
            "--spring.main.web-application-type=none",
            "--eximeebpms.bpm.trusted-code.mode=banana"
        );
      } finally {
        if (context != null) {
          context.close();
        }
      }
    });

    assertThat(thrown)
        .isNotNull()
        .hasRootCauseInstanceOf(IllegalArgumentException.class)
        .extracting(Throwable::getCause)
        .satisfies(cause -> assertThat(cause.getMessage())
            .contains("Invalid trusted code policy mode")
            .contains("OFF")
            .contains("AUDIT")
            .contains("ENFORCE")
        );
  }
}
