package org.eximeebpms.bpm.engine.test.standalone.scripting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityException;
import org.eximeebpms.bpm.engine.repository.Deployment;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

public class ScriptTaskResourceScriptSecurityTest {

  protected static final String FORBIDDEN_BPMN_RESOURCE =
      "org/eximeebpms/bpm/engine/test/standalone/scripting/script-task-forbidden-resource-script-process.bpmn20.xml";
  protected static final String SAFE_BPMN_RESOURCE =
      "org/eximeebpms/bpm/engine/test/standalone/scripting/script-task-safe-resource-script-process.bpmn20.xml";
  protected static final String FORBIDDEN_SCRIPT_RESOURCE =
      "org/eximeebpms/bpm/engine/test/standalone/scripting/forbidden-resource-script.js";
  protected static final String SAFE_SCRIPT_RESOURCE =
      "org/eximeebpms/bpm/engine/test/standalone/scripting/safe-resource-script.js";
  protected static final String FORBIDDEN_PROCESS_DEFINITION_KEY =
      "scriptTaskForbiddenResourceScriptSecurityProcess";
  protected static final String SAFE_PROCESS_DEFINITION_KEY =
      "scriptTaskSafeResourceScriptSecurityProcess";

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected String deploymentId;

  @After
  public void tearDown() {
    if (deploymentId != null) {
      engineRule.getRepositoryService().deleteDeployment(deploymentId, true);
    }
  }

  @Test
  public void shouldBlockForbiddenResourceScriptTaskAtRuntime() {
    // given
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource(FORBIDDEN_BPMN_RESOURCE)
        .addClasspathResource(FORBIDDEN_SCRIPT_RESOURCE)
        .deploy();

    deploymentId = deployment.getId();

    // when / then
    assertThatThrownBy(() -> engineRule.getRuntimeService()
        .startProcessInstanceByKey(FORBIDDEN_PROCESS_DEFINITION_KEY))
        .isInstanceOf(ScriptSecurityException.class)
        .hasMessageContaining("Script execution blocked by script security policy")
        .hasMessageContaining("Access to environment variables is forbidden")
        .extracting(throwable -> ((ScriptSecurityException) throwable).getDecisionCode().orElse(null))
        .isEqualTo("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldExecuteSafeResourceScriptTaskAtRuntime() {
    // given
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource(SAFE_BPMN_RESOURCE)
        .addClasspathResource(SAFE_SCRIPT_RESOURCE)
        .deploy();

    deploymentId = deployment.getId();

    // when
    engineRule.getRuntimeService().startProcessInstanceByKey(SAFE_PROCESS_DEFINITION_KEY);

    // then
  }
}
