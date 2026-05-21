package org.eximeebpms.bpm.engine.test.standalone.scripting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.scripting.ExecutableScript;
import org.eximeebpms.bpm.engine.impl.scripting.ResourceExecutableScript;
import org.eximeebpms.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.eximeebpms.bpm.engine.impl.scripting.env.ScriptingEnvironment;
import org.eximeebpms.bpm.engine.impl.scripting.security.DefaultScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityException;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.repository.Deployment;
import org.eximeebpms.bpm.engine.runtime.Execution;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ScriptingEnvironmentResourceScriptSecurityTest {

  protected static final String PROCESS_RESOURCE =
      "org/eximeebpms/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String FORBIDDEN_SCRIPT_RESOURCE =
      "org/eximeebpms/bpm/engine/test/standalone/scripting/forbidden-resource-script.js";
  protected static final String SAFE_SCRIPT_RESOURCE =
      "org/eximeebpms/bpm/engine/test/standalone/scripting/safe-resource-script.js";
  protected static final String PROCESS_DEFINITION_KEY = "oneTaskProcess";

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected ScriptingEnvironment scriptingEnvironment;
  protected ScriptingEngines scriptingEngines;
  protected boolean previousScriptSecurityEnabled;
  protected ScriptSecurityPolicy previousScriptSecurityPolicy;
  protected ScriptingEnvironment previousScriptingEnvironment;

  protected String deploymentId;
  protected String executionId;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    previousScriptSecurityEnabled = processEngineConfiguration.isScriptSecurityEnabled();
    previousScriptSecurityPolicy = processEngineConfiguration.getScriptSecurityPolicy();
    previousScriptingEnvironment = processEngineConfiguration.getScriptingEnvironment();

    processEngineConfiguration.setScriptSecurityEnabled(true);

    ScriptSecurityPolicy testScriptSecurityPolicy = new DefaultScriptSecurityPolicy();
    processEngineConfiguration.setScriptSecurityPolicy(testScriptSecurityPolicy);

    scriptingEngines = processEngineConfiguration.getScriptingEngines();

    scriptingEnvironment = new ScriptingEnvironment(
        processEngineConfiguration.getScriptFactory(),
        processEngineConfiguration.getEnvScriptResolvers() != null
            ? processEngineConfiguration.getEnvScriptResolvers()
            : new ArrayList<>(),
        scriptingEngines,
        testScriptSecurityPolicy);

    processEngineConfiguration.setScriptingEnvironment(scriptingEnvironment);

    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource(PROCESS_RESOURCE)
        .addClasspathResource(FORBIDDEN_SCRIPT_RESOURCE)
        .addClasspathResource(SAFE_SCRIPT_RESOURCE)
        .deploy();

    deploymentId = deployment.getId();

    ProcessInstance processInstance = engineRule.getRuntimeService()
        .startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    Execution execution = engineRule.getRuntimeService()
        .createExecutionQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    executionId = execution.getId();
  }

  @After
  public void tearDown() {
    if (deploymentId != null) {
      engineRule.getRepositoryService().deleteDeployment(deploymentId, true);
      deploymentId = null;
    }

    processEngineConfiguration.setScriptingEnvironment(previousScriptingEnvironment);
    processEngineConfiguration.setScriptSecurityPolicy(previousScriptSecurityPolicy);
    processEngineConfiguration.setScriptSecurityEnabled(previousScriptSecurityEnabled);
  }

  @Test
  public void shouldBlockForbiddenResourceScriptBeforeEvaluation() {
    // given
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = scriptingEngines.createBindings(scriptEngine, null);
    ResourceExecutableScript script = new ResourceExecutableScript(
        ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE,
        FORBIDDEN_SCRIPT_RESOURCE);

    // when / then
    assertThatThrownBy(() -> execute(script, scriptEngine, bindings))
        .isInstanceOf(ScriptSecurityException.class)
        .hasMessageContaining("Script execution blocked by script security policy")
        .hasMessageContaining("Access to environment variables is forbidden")
        .extracting(throwable -> ((ScriptSecurityException) throwable).getDecisionCode().orElse(null))
        .isEqualTo("SCRIPT_SECURITY_SYSTEM_GETENV");
  }

  @Test
  public void shouldExecuteSafeResourceScript() {
    // given
    ScriptEngine scriptEngine = getJavaScriptEngine();
    Bindings bindings = scriptingEngines.createBindings(scriptEngine, null);
    ResourceExecutableScript script = new ResourceExecutableScript(
        ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE,
        SAFE_SCRIPT_RESOURCE);

    // when
    Object result = execute(script, scriptEngine, bindings);

    // then
    assertThat(result).isNotNull();
    assertThat(result.toString()).isEqualTo("2");
  }

  protected Object execute(ExecutableScript script, ScriptEngine scriptEngine, Bindings bindings) {
    return processEngineConfiguration.getCommandExecutorTxRequired()
        .execute(commandContext -> {
          ExecutionEntity executionEntity = commandContext.getExecutionManager().findExecutionById(executionId);

          Context.setExecutionContext(executionEntity);
          try {
            return scriptingEnvironment.execute(script, executionEntity, bindings, scriptEngine);
          } finally {
            Context.removeExecutionContext();
          }
        });
  }

  protected ScriptEngine getJavaScriptEngine() {
    return processEngineConfiguration.getCommandExecutorTxRequired()
        .execute(commandContext -> scriptingEngines.getScriptEngineForLanguage(
            ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE));
  }
}
