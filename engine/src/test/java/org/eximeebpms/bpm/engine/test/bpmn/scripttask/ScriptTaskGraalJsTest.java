/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eximeebpms.bpm.engine.test.bpmn.scripttask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.oracle.truffle.js.scriptengine.GraalJSEngineFactory;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.eximeebpms.bpm.engine.ScriptEvaluationException;
import org.eximeebpms.bpm.engine.impl.scripting.engine.DefaultScriptEngineResolver;
import org.eximeebpms.bpm.engine.impl.scripting.engine.ScriptEngineResolver;
import org.eximeebpms.bpm.engine.impl.scripting.security.DefaultScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ScriptTaskGraalJsTest extends AbstractScriptTaskTest {

  private static final String GRAALJS = "graal.js";

  @Parameter(0)
  public boolean configureHostAccess;

  @Parameter(1)
  public boolean enableExternalResources;

  @Parameter(2)
  public boolean enableNashornCompat;

  @Parameter(3)
  public boolean scriptSecurityEnabled;

  protected ScriptEngineResolver defaultScriptEngineResolver;
  protected ScriptSecurityPolicy previousScriptSecurityPolicy;
  protected boolean spinEnabled = false;
  protected boolean previousEnableScriptEngineLoadExternalResources;
  protected boolean previousConfigureScriptEngineHostAccess;
  protected boolean previousEnableScriptEngineNashornCompatibility;
  protected boolean previousScriptSecurityEnabled;

  @Parameters(name = "{index}: host={0}, io={1}, nashorn={2}, security={3}")
  public static Collection<Object[]> setups() {
    return Arrays.asList(new Object[][]{
        {false, false, false, true},
        {true, false, false, true},
        {false, true, false, true},
        {false, false, true, true},
        {true, true, false, true},
        {true, false, true, true},
        {false, true, true, true},
        {true, true, true, true},

        // backward compatibility: script security disabled
        {false, false, false, false},
        {true, false, false, false},
        {false, true, false, false},
        {false, false, true, false},
        {true, true, false, false},
        {true, false, true, false},
        {false, true, true, false},
        {true, true, true, false},
    });
  }

  @Before
  public void setup() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    previousEnableScriptEngineLoadExternalResources =
        processEngineConfiguration.isEnableScriptEngineLoadExternalResources();
    previousConfigureScriptEngineHostAccess =
        processEngineConfiguration.isConfigureScriptEngineHostAccess();
    previousEnableScriptEngineNashornCompatibility =
        processEngineConfiguration.isEnableScriptEngineNashornCompatibility();
    previousScriptSecurityEnabled =
        processEngineConfiguration.isScriptSecurityEnabled();
    previousScriptSecurityPolicy =
        processEngineConfiguration.getScriptSecurityPolicy();

    spinEnabled = processEngineConfiguration.getEnvScriptResolvers().stream()
        .anyMatch(resolver -> resolver.getClass().getSimpleName().equals("SpinScriptEnvResolver"));

    defaultScriptEngineResolver = processEngineConfiguration.getScriptEngineResolver();

    processEngineConfiguration.setConfigureScriptEngineHostAccess(configureHostAccess);
    processEngineConfiguration.setEnableScriptEngineLoadExternalResources(enableExternalResources);
    processEngineConfiguration.setEnableScriptEngineNashornCompatibility(enableNashornCompat);
    processEngineConfiguration.setScriptSecurityEnabled(scriptSecurityEnabled);
    processEngineConfiguration.setScriptSecurityPolicy(new DefaultScriptSecurityPolicy());

    processEngineConfiguration.setScriptEngineResolver(new TestScriptEngineResolver(
        processEngineConfiguration.getScriptEngineResolver().getScriptEngineManager()));
  }

  @After
  public void resetConfiguration() {
    processEngineConfiguration.setScriptSecurityEnabled(previousScriptSecurityEnabled);
    processEngineConfiguration.setScriptSecurityPolicy(previousScriptSecurityPolicy);
    processEngineConfiguration.setEnableScriptEngineLoadExternalResources(previousEnableScriptEngineLoadExternalResources);
    processEngineConfiguration.setConfigureScriptEngineHostAccess(previousConfigureScriptEngineHostAccess);
    processEngineConfiguration.setEnableScriptEngineNashornCompatibility(previousEnableScriptEngineNashornCompatibility);
    processEngineConfiguration.setScriptEngineResolver(defaultScriptEngineResolver);
  }

  @Test
  public void testJavascriptProcessVarVisibility() {
    deployProcess(GRAALJS,
        "execution.setVariable('foo', 'a');"
            + "if (typeof foo !== 'undefined') { "
            + "  throw 'Variable foo should be defined as script variable.';"
            + "}"
            + "var foo = 'b';"
            + "if(execution.getVariable('foo') != 'a') {"
            + "  throw 'Execution should contain variable foo';"
            + "}"
            + "if(foo != 'b') {"
            + "  throw 'Script variable must override the visibiltity of the execution variable.';"
            + "}");

    if (enableNashornCompat || configureHostAccess) {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

      Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
      assertEquals("a", variableValue);
    } else {
      assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey("testProcess"))
          .isInstanceOf(ScriptEvaluationException.class)
          .hasMessageContaining(spinEnabled ? "ReferenceError" : "TypeError");
    }
  }

  @Test
  public void testJavascriptFunctionInvocation() {
    deployProcess(GRAALJS,
        "function sum(a,b){"
            + "  return a+b;"
            + "};"
            + "var result = sum(1,2);"
            + "execution.setVariable('foo', result);");

    if (enableNashornCompat || configureHostAccess) {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

      Object variable = runtimeService.getVariable(pi.getId(), "foo");
      assertThat(variable).isIn(3, 3.0);
    } else {
      assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey("testProcess"))
          .isInstanceOf(ScriptEvaluationException.class)
          .hasMessageContaining(spinEnabled ? "ReferenceError" : "TypeError");
    }
  }

  @Test
  public void testJsVariable() {
    String scriptText = "var foo = 1;";

    deployProcess(GRAALJS, scriptText);

    if (spinEnabled && !enableNashornCompat && !configureHostAccess) {
      assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey("testProcess"))
          .isInstanceOf(ScriptEvaluationException.class)
          .hasMessageContaining("ReferenceError");
    } else {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

      Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
      assertNull(variableValue);
    }
  }

  @Test
  public void testJavascriptVariableSerialization() {
    String scriptSource =
        "execution.setVariable('date', new java.util.Date(0));"
            + "execution.setVariable('myVar', new org.eximeebpms.bpm.engine.test.bpmn.scripttask.MySerializable('test'));";

    Throwable throwable = catchThrowable(() -> {
      ProcessInstance pi = deployAndStartProcessReturningInstance(GRAALJS, scriptSource);

      if (shouldAllowJavaObjectCreation()) {
        Date date = (Date) runtimeService.getVariable(pi.getId(), "date");
        assertEquals(0, date.getTime());

        MySerializable myVar = (MySerializable) runtimeService.getVariable(pi.getId(), "myVar");
        assertEquals("test", myVar.getName());
      }
    });

    if (shouldAllowJavaObjectCreation()) {
      assertThat(throwable).isNull();
      return;
    }

    assertThat(throwable).isNotNull();
    assertThat(throwable.getMessage())
        .containsAnyOf(
            "Process deployment blocked by script security policy",
            "Script execution blocked by script security policy",
            "script security policy",
            "Direct Java object creation is forbidden",
            "SCRIPT_SECURITY_NEW_JAVA",
            "ReferenceError",
            "java\" is not defined",
            "\"java\" is not defined",
            "java is not defined",
            "Operation is not allowed");
  }

  @Test
  public void shouldLoadExternalScript() {
    String scriptSource =
        "load(\"" + getNormalizedResourcePath("/org/eximeebpms/bpm/engine/test/bpmn/scripttask/sum.js") + "\");"
            + "execution.setVariable('foo', sum(3, 4));";

    Throwable throwable = catchThrowable(() -> {
      ProcessInstance pi = deployAndStartProcessReturningInstance(GRAALJS, scriptSource);

      if (shouldExecuteExternalScript()) {
        Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
        assertEquals(7, variableValue);
      }
    });

    if (shouldExecuteExternalScript()) {
      assertThat(throwable).isNull();
      return;
    }

    assertThat(throwable).isNotNull();
    assertThat(throwable.getMessage())
        .containsAnyOf(
            "Process deployment blocked by script security policy",
            "Script execution blocked by script security policy",
            "script security policy",
            "Loading external scripts is forbidden",
            "SCRIPT_SECURITY_LOAD",
            "Operation is not allowed",
            "ReferenceError",
            "TypeError",
            "Script execution blocked");
  }

  @Test
  public void shouldBlockDangerousJavaObjectCreationWhenScriptSecurityEnabled() {
    String scriptSource =
        "var proc = new java.lang.ProcessBuilder('sh', '-c', 'id').start();";

    Throwable throwable = deployAndStartProcess(GRAALJS, scriptSource);

    if (scriptSecurityEnabled) {
      assertThat(throwable).isNotNull();
      assertThat(throwable.getMessage())
          .containsAnyOf(
              "Process deployment blocked by script security policy",
              "Script execution blocked by script security policy",
              "script security policy",
              "Process execution via ProcessBuilder is forbidden",
              "SCRIPT_SECURITY_PROCESS_BUILDER",
              "ReferenceError",
              "java\" is not defined",
              "\"java\" is not defined",
              "java is not defined",
              "Operation is not allowed");
      return;
    }

    if (!shouldAllowJavaObjectCreation()) {
      assertThat(throwable).isNotNull();
      assertThat(throwable.getMessage())
          .containsAnyOf(
              "ReferenceError",
              "java\" is not defined",
              "\"java\" is not defined",
              "java is not defined",
              "Operation is not allowed");
      return;
    }

    assertThat(throwable).isNull();
  }

  protected Throwable deployAndStartProcess(String language, String scriptSource) {
    return catchThrowable(() -> {
      deployProcess(language, scriptSource);
      runtimeService.startProcessInstanceByKey("testProcess");
    });
  }

  protected ProcessInstance deployAndStartProcessReturningInstance(String language, String scriptSource) {
    deployProcess(language, scriptSource);
    return runtimeService.startProcessInstanceByKey("testProcess");
  }

  private boolean shouldExecuteExternalScript() {
    return !scriptSecurityEnabled && (enableNashornCompat || (enableExternalResources && configureHostAccess));
  }

  private boolean shouldAllowJavaObjectCreation() {
    return !scriptSecurityEnabled && (configureHostAccess || enableNashornCompat);
  }

  protected static class TestScriptEngineResolver extends DefaultScriptEngineResolver {

    public TestScriptEngineResolver(ScriptEngineManager scriptEngineManager) {
      super(scriptEngineManager);
    }

    @Override
    protected ScriptEngine getScriptEngine(String language) {
      if (GRAALJS.equalsIgnoreCase(language)) {
        GraalJSScriptEngine scriptEngine = new GraalJSEngineFactory().getScriptEngine();
        configureScriptEngines(language, scriptEngine);
        return scriptEngine;
      }
      return super.getScriptEngine(language);
    }
  }
}
