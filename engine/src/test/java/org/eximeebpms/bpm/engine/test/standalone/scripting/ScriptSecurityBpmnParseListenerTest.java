package org.eximeebpms.bpm.engine.test.standalone.scripting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eximeebpms.bpm.engine.ProcessEngineException;
import org.eximeebpms.bpm.engine.repository.Deployment;
import org.eximeebpms.bpm.engine.repository.DeploymentBuilder;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

public class ScriptSecurityBpmnParseListenerTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Test
  public void shouldBlockDeploymentOfProcessWithForbiddenScriptTask() {
    DeploymentBuilder deploymentBuilder = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-forbidden-script-task.bpmn20.xml");

    assertThatThrownBy(deploymentBuilder::deploy)
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Process deployment blocked by script security policy")
        .hasMessageContaining("dangerousScriptTask")
        .hasMessageContaining("Access to environment variables is forbidden");
  }

  @Test
  public void shouldAllowDeploymentOfProcessWithSafeScriptTask() {
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-safe-script-task.bpmn20.xml")
        .deploy();

    try {
      assertThat(deployment).isNotNull();
    } finally {
      engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void shouldBlockDeploymentOfProcessWithForbiddenSequenceFlowCondition() {
    assertThatThrownBy(() -> engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-forbidden-sequence-flow.bpmn20.xml")
        .deploy())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Process deployment blocked by script security policy")
        .hasMessageContaining("flow1")
        .hasMessageContaining("Access to environment variables is forbidden");
  }

  @Test
  public void shouldAllowDeploymentOfProcessWithSafeSequenceFlowCondition() {
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-safe-sequence-flow.bpmn20.xml")
        .deploy();

    try {
      assertThat(deployment).isNotNull();
    } finally {
      engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void shouldBlockDeploymentOfProcessWithForbiddenIoMappingScript() {
    assertThatThrownBy(() -> engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-forbidden-io-mapping.bpmn20.xml")
        .deploy())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Process deployment blocked by script security policy")
        .hasMessageContaining("taskWithIoMapping")
        .hasMessageContaining("Access to environment variables is forbidden");
  }

  @Test
  public void shouldAllowDeploymentOfProcessWithSafeIoMappingScript() {
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-safe-io-mapping.bpmn20.xml")
        .deploy();

    try {
      assertThat(deployment).isNotNull();
    } finally {
      engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void shouldBlockDeploymentOfProcessWithForbiddenExecutionListenerScript() {
    assertThatThrownBy(() -> engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-forbidden-execution-listener.bpmn20.xml")
        .deploy())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Process deployment blocked by script security policy")
        .hasMessageContaining("Access to environment variables is forbidden");
  }

  @Test
  public void shouldAllowDeploymentOfProcessWithSafeExecutionListenerScript() {
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-safe-execution-listener.bpmn20.xml")
        .deploy();

    try {
      assertThat(deployment).isNotNull();
    } finally {
      engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void shouldBlockDeploymentOfProcessWithForbiddenTaskListenerScript() {
    assertThatThrownBy(() -> engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-forbidden-task-listener.bpmn20.xml")
        .deploy())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Process deployment blocked by script security policy")
        .hasMessageContaining("userTaskWithForbiddenTaskListener")
        .hasMessageContaining("Access to environment variables is forbidden");
  }

  @Test
  public void shouldAllowDeploymentOfProcessWithSafeTaskListenerScript() {
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-safe-task-listener.bpmn20.xml")
        .deploy();

    try {
      assertThat(deployment).isNotNull();
    } finally {
      engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void shouldBlockDeploymentOfProcessWithForbiddenServiceTaskExecutionListenerScript() {
    assertThatThrownBy(() -> engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-forbidden-service-task-execution-listener.bpmn20.xml")
        .deploy())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Process deployment blocked by script security policy")
        .hasMessageContaining("serviceTaskWithForbiddenExecutionListener")
        .hasMessageContaining("Access to environment variables is forbidden");
  }

  @Test
  public void shouldAllowDeploymentOfProcessWithSafeServiceTaskExecutionListenerScript() {
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource(
            "org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-safe-service-task-execution-listener.bpmn20.xml")
        .deploy();

    try {
      assertThat(deployment).isNotNull();
    } finally {
      engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void shouldBlockDeploymentOfProcessWithForbiddenStartEventExecutionListenerScript() {
    assertThatThrownBy(() -> engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource(
            "org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-forbidden-start-event-execution-listener.bpmn20.xml")
        .deploy())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Process deployment blocked by script security policy")
        .hasMessageContaining("start")
        .hasMessageContaining("Access to environment variables is forbidden");
  }

  @Test
  public void shouldAllowDeploymentOfProcessWithSafeStartEventExecutionListenerScript() {
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource(
            "org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-safe-start-event-execution-listener.bpmn20.xml")
        .deploy();

    try {
      assertThat(deployment).isNotNull();
    } finally {
      engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void shouldBlockDeploymentOfProcessWithForbiddenScriptTaskExecutionListenerScript() {
    assertThatThrownBy(() -> engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource(
            "org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-forbidden-script-task-execution-listener.bpmn20.xml")
        .deploy())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Process deployment blocked by script security policy")
        .hasMessageContaining("scriptTaskWithForbiddenExecutionListener")
        .hasMessageContaining("Access to environment variables is forbidden");
  }

  @Test
  public void shouldAllowDeploymentOfProcessWithSafeScriptTaskExecutionListenerScript() {
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource(
            "org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-safe-script-task-execution-listener.bpmn20.xml")
        .deploy();

    try {
      assertThat(deployment).isNotNull();
    } finally {
      engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void shouldBlockDeploymentOfProcessWithForbiddenEndEventExecutionListenerScript() {
    assertThatThrownBy(() -> engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource(
            "org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-forbidden-end-event-execution-listener.bpmn20.xml")
        .deploy())
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Process deployment blocked by script security policy")
        .hasMessageContaining("end")
        .hasMessageContaining("Access to environment variables is forbidden");
  }

  @Test
  public void shouldAllowDeploymentOfProcessWithSafeEndEventExecutionListenerScript() {
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource(
            "org/eximeebpms/bpm/engine/test/standalone/scripting/script-security-safe-end-event-execution-listener.bpmn20.xml")
        .deploy();

    try {
      assertThat(deployment).isNotNull();
    } finally {
      engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }
}
