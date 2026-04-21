package org.eximeebpms.trustedcode.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.ProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.ProcessEngineException;
import org.eximeebpms.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TrustedCodePolicyProcessEnginePluginTest {

  private static final String SCRIPT_TASK_PROCESS = "org/eximeebpms/trustedcode/plugin/script-task-process.bpmn";
  private static final String EXECUTION_LISTENER_PROCESS = "org/eximeebpms/trustedcode/plugin/execution-listener-process.bpmn";
  private static final String TASK_LISTENER_PROCESS = "org/eximeebpms/trustedcode/plugin/task-listener-process.bpmn";
  private static final String EXTERNAL_SCRIPT_RESOURCE_PROCESS = "org/eximeebpms/trustedcode/plugin/external-script-resource-process.bpmn";
  private static final String SAFE_PROCESS = "org/eximeebpms/trustedcode/plugin/safe-process.bpmn";
  public static final String JDBC_URL = "jdbc:h2:mem:trusted-code-plugin-test;DB_CLOSE_DELAY=1000";

  private ProcessEngine processEngine;

  @Before
  public void setUp() {
    processEngine = buildProcessEngine(JDBC_URL, configuration -> {});
  }

  @After
  public void tearDown() {
    closeProcessEngine(processEngine);
  }

  @Test
  public void shouldRejectDeploymentWithScriptTask() {
    assertRejectedDeployment(SCRIPT_TASK_PROCESS,
        "SCRIPT_TASK_FORBIDDEN",
        "bpmn:ScriptTask",
        "(scriptTask)");
  }

  @Test
  public void shouldRejectDeploymentWithScriptExecutionListener() {
    assertRejectedDeployment(EXECUTION_LISTENER_PROCESS,
        "SCRIPT_EXECUTION_LISTENER_FORBIDDEN");
  }

  @Test
  public void shouldRejectDeploymentWithScriptTaskListener() {
    assertRejectedDeployment(TASK_LISTENER_PROCESS,
        "SCRIPT_TASK_LISTENER_FORBIDDEN");
  }

  @Test
  public void shouldRejectDeploymentWithExternalScriptResource() {
    assertRejectedDeployment(EXTERNAL_SCRIPT_RESOURCE_PROCESS,
        "EXTERNAL_SCRIPT_RESOURCE_FORBIDDEN");
  }

  @Test
  public void shouldAllowDeploymentWithoutTrustedCodeElements() {
    var deployment = deploy(processEngine, SAFE_PROCESS);

    assertThat(deployment).isNotNull();
    assertThat(deployment.getId()).isNotBlank();
  }

  @Test
  public void shouldAllowDeploymentWithScriptTaskWhenTrustedCodeIsDisabled() {
    ProcessEngine disabledProcessEngine = null;

    try {
      disabledProcessEngine = buildProcessEngine(
          "jdbc:h2:mem:trusted-code-plugin-disabled-test;DB_CLOSE_DELAY=1000",
          configuration -> configuration.setTrustedCodeEnabled(false)
      );

      var deployment = deploy(disabledProcessEngine, SCRIPT_TASK_PROCESS);

      assertThat(deployment).isNotNull();
      assertThat(deployment.getId()).isNotBlank();
    } finally {
      closeProcessEngine(disabledProcessEngine);
    }
  }

  @Test
  public void shouldAllowDeploymentWithScriptTaskInAuditMode() {
    ProcessEngine auditProcessEngine = null;

    try {
      auditProcessEngine = buildProcessEngine(
          "jdbc:h2:mem:trusted-code-plugin-audit-test;DB_CLOSE_DELAY=1000",
          configuration -> configuration.setTrustedCodePolicyMode("AUDIT")
      );

      var deployment = deploy(auditProcessEngine, SCRIPT_TASK_PROCESS);

      assertThat(deployment).isNotNull();
      assertThat(deployment.getId()).isNotBlank();
    } finally {
      closeProcessEngine(auditProcessEngine);
    }
  }

  private void assertRejectedDeployment(String classpathResource, String... expectedMessageParts) {
    var assertion = assertThatThrownBy(() -> deploy(processEngine, classpathResource))
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Deployment rejected by trusted-code policy");

    for (String expectedMessagePart : expectedMessageParts) {
      assertion.hasMessageContaining(expectedMessagePart);
    }
  }

  private org.eximeebpms.bpm.engine.repository.Deployment deploy(
      ProcessEngine engine,
      String classpathResource) {

    return engine.getRepositoryService()
        .createDeployment()
        .addClasspathResource(classpathResource)
        .deploy();
  }

  private ProcessEngine buildProcessEngine(
      String jdbcUrl,
      java.util.function.Consumer<StandaloneInMemProcessEngineConfiguration> customizer) {

    StandaloneInMemProcessEngineConfiguration configuration =
        (StandaloneInMemProcessEngineConfiguration)
            ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();

    configuration.setJdbcUrl(jdbcUrl);
    configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    customizer.accept(configuration);
    configuration.getProcessEnginePlugins().add(new TrustedCodePolicyProcessEnginePlugin());

    return configuration.buildProcessEngine();
  }

  private void closeProcessEngine(ProcessEngine engine) {
    if (engine != null) {
      engine.close();
    }
  }
}
