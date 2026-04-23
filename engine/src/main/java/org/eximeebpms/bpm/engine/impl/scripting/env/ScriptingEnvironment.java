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
package org.eximeebpms.bpm.engine.impl.scripting.env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import org.eximeebpms.bpm.application.AbstractProcessApplication;
import org.eximeebpms.bpm.application.ProcessApplicationInterface;
import org.eximeebpms.bpm.application.ProcessApplicationReference;
import org.eximeebpms.bpm.application.ProcessApplicationUnavailableException;
import org.eximeebpms.bpm.engine.ProcessEngineException;
import org.eximeebpms.bpm.engine.delegate.DelegateCaseExecution;
import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.VariableScope;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.persistence.entity.TaskEntity;
import org.eximeebpms.bpm.engine.impl.scripting.DynamicResourceExecutableScript;
import org.eximeebpms.bpm.engine.impl.scripting.DynamicSourceExecutableScript;
import org.eximeebpms.bpm.engine.impl.scripting.ExecutableScript;
import org.eximeebpms.bpm.engine.impl.scripting.ResourceExecutableScript;
import org.eximeebpms.bpm.engine.impl.scripting.ScriptFactory;
import org.eximeebpms.bpm.engine.impl.scripting.SourceExecutableScript;
import org.eximeebpms.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityContext;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityDecision;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityException;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSourceType;

/**
 * <p>The scripting environment contains scripts that provide an environment to
 * a user provided script. The environment may contain additional libraries or imports.</p>
 *
 * <p>The environment performs lazy initialization of scripts: the first time a script of a given
 * script language is executed, the environment will use the {@link ScriptEnvResolver ScriptEnvResolvers}
 * for resolving the environment scripts for that language. The scripts (if any) are then pre-compiled
 * and cached for reuse.</p>
 *
 * @author Daniel Meyer
 */
public class ScriptingEnvironment {

  /**
   * the cached environment scripts per script language
   */
  protected Map<String, List<ExecutableScript>> env = new HashMap<>();

  /**
   * the resolvers
   */
  protected List<ScriptEnvResolver> envResolvers;

  /**
   * the script factory used for compiling env scripts
   */
  protected ScriptFactory scriptFactory;

  /**
   * the scripting engines
   */
  protected ScriptingEngines scriptingEngines;

  /**
   * the script security policy
   */
  protected ScriptSecurityPolicy scriptSecurityPolicy;

  public ScriptingEnvironment(
      ScriptFactory scriptFactory,
      List<ScriptEnvResolver> scriptEnvResolvers,
      ScriptingEngines scriptingEngines,
      ScriptSecurityPolicy scriptSecurityPolicy) {
    this.scriptFactory = scriptFactory;
    this.envResolvers = scriptEnvResolvers;
    this.scriptingEngines = scriptingEngines;
    this.scriptSecurityPolicy = scriptSecurityPolicy;
  }

  /**
   * execute a given script in the environment
   *
   * @param script the {@link ExecutableScript} to execute
   * @param scope the scope in which to execute the script
   * @return the result of the script evaluation
   */
  public Object execute(ExecutableScript script, VariableScope scope) {

    ScriptEngine scriptEngine = scriptingEngines.getScriptEngineForLanguage(script.getLanguage());
    Bindings bindings = scriptingEngines.createBindings(scriptEngine, scope);

    return execute(script, scope, bindings, scriptEngine);
  }

  public Object execute(ExecutableScript script, VariableScope scope, Bindings bindings, ScriptEngine scriptEngine) {

    List<ExecutableScript> envScripts = getEnvScripts(script, scriptEngine);
    // first, evaluate the env scripts (if any)
    for (ExecutableScript envScript : envScripts) {
      enforceScriptSecurity(envScript, scope);
      envScript.execute(scriptEngine, scope, bindings);
    }

    enforceScriptSecurity(script, scope);
    // next evaluate the actual script
    return script.execute(scriptEngine, scope, bindings);
  }

  protected void enforceScriptSecurity(ExecutableScript script, VariableScope scope) {
    if (scriptSecurityPolicy == null) {
      return;
    }

    ScriptSecurityContext context = createSecurityContext(script, scope);
    ScriptSecurityDecision decision = scriptSecurityPolicy.evaluate(context);
    if (decision.isDenied()) {
      throw new ScriptSecurityException(
          buildSecurityExceptionMessage(scope, decision),
          decision.getCode().orElse(null));
    }
  }

  protected ScriptSecurityContext createSecurityContext(ExecutableScript script, VariableScope scope) {
    ScriptSecurityContext.Builder builder = ScriptSecurityContext.builder(script.getLanguage())
        .source(resolveScriptSource(script, scope))
        .sourceType(resolveSourceType(script));

    if (scope instanceof DelegateExecution execution) {
      builder
          .activityId(execution.getCurrentActivityId())
          .processDefinitionId(execution.getProcessDefinitionId());
    } else if (scope instanceof TaskEntity task) {
      if (task.getExecution() != null) {
        builder
            .activityId(task.getExecution().getActivityId())
            .processDefinitionId(task.getProcessDefinitionId());
      }
      if (task.getCaseExecution() != null) {
        builder
            .activityId(task.getCaseExecution().getActivityId())
            .caseDefinitionId(task.getCaseDefinitionId());
      }
    } else if (scope instanceof DelegateCaseExecution caseExecution) {
      builder
          .activityId(caseExecution.getActivityId())
          .caseDefinitionId(caseExecution.getCaseDefinitionId());
    }

    return builder.build();
  }

  protected String resolveScriptSource(ExecutableScript script, VariableScope scope) {
    if (script instanceof DynamicResourceExecutableScript dynamicResourceExecutableScript) {
      return dynamicResourceExecutableScript.getScriptSource(scope);
    }
    if (script instanceof ResourceExecutableScript resourceExecutableScript) {
      return resourceExecutableScript.getResolvedScriptSource();
    }
    if (script instanceof DynamicSourceExecutableScript dynamicSourceExecutableScript) {
      return dynamicSourceExecutableScript.getScriptSource(scope);
    }
    if (script instanceof SourceExecutableScript sourceExecutableScript) {
      return sourceExecutableScript.getScriptSource();
    }
    return "";
  }

  protected ScriptSourceType resolveSourceType(ExecutableScript script) {
    if (script instanceof DynamicResourceExecutableScript) {
      return ScriptSourceType.DYNAMIC_RESOURCE;
    }
    if (script instanceof ResourceExecutableScript) {
      return ScriptSourceType.RESOURCE;
    }
    if (script instanceof DynamicSourceExecutableScript) {
      return ScriptSourceType.DYNAMIC_SOURCE;
    }
    if (script instanceof SourceExecutableScript) {
      return ScriptSourceType.INLINE_SOURCE;
    }
    return ScriptSourceType.UNKNOWN;
  }

  protected String buildSecurityExceptionMessage(VariableScope scope, ScriptSecurityDecision decision) {
    StringBuilder message = new StringBuilder("Script execution blocked by script security policy");

    decision.getReason().ifPresent(reason -> message.append(": ").append(reason));
    message.append(getScopeExceptionMessage(scope));

    return message.toString();
  }

  protected String getScopeExceptionMessage(VariableScope variableScope) {
    String activityId = null;
    String definitionIdMessage = "";

    if (variableScope instanceof DelegateExecution execution) {
      activityId = execution.getCurrentActivityId();
      definitionIdMessage = " in the process definition with id '" + execution.getProcessDefinitionId() + "'";
    } else if (variableScope instanceof TaskEntity task) {
      if (task.getExecution() != null) {
        activityId = task.getExecution().getActivityId();
        definitionIdMessage = " in the process definition with id '" + task.getProcessDefinitionId() + "'";
      }
      if (task.getCaseExecution() != null) {
        activityId = task.getCaseExecution().getActivityId();
        definitionIdMessage = " in the case definition with id '" + task.getCaseDefinitionId() + "'";
      }
    } else if (variableScope instanceof DelegateCaseExecution caseExecution) {
      activityId = caseExecution.getActivityId();
      definitionIdMessage = " in the case definition with id '" + caseExecution.getCaseDefinitionId() + "'";
    }

    if (activityId == null) {
      return "";
    }

    return " while executing activity '" + activityId + "'" + definitionIdMessage;
  }

  protected Map<String, List<ExecutableScript>> getEnv(String language) {
    ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
    ProcessApplicationReference processApplication = Context.getCurrentProcessApplication();

    Map<String, List<ExecutableScript>> result = null;
    if (config.isEnableFetchScriptEngineFromProcessApplication()) {
      if (processApplication != null) {
        result = getPaEnvScripts(processApplication);
      }
    }

    return result != null ? result : env;
  }

  protected Map<String, List<ExecutableScript>> getPaEnvScripts(ProcessApplicationReference pa) {
    try {
      ProcessApplicationInterface processApplication = pa.getProcessApplication();
      ProcessApplicationInterface rawObject = processApplication.getRawObject();

      if (rawObject instanceof AbstractProcessApplication abstractProcessApplication) {
        return abstractProcessApplication.getEnvironmentScripts();
      }
      return null;
    } catch (ProcessApplicationUnavailableException e) {
      throw new ProcessEngineException("Process Application is unavailable.", e);
    }
  }

  protected List<ExecutableScript> getEnvScripts(ExecutableScript script, ScriptEngine scriptEngine) {
    List<ExecutableScript> envScripts = getEnvScripts(script.getLanguage().toLowerCase());
    if (envScripts.isEmpty()) {
      envScripts = getEnvScripts(scriptEngine.getFactory().getLanguageName().toLowerCase());
    }
    return envScripts;
  }

  /**
   * Returns the env scripts for the given language. Performs lazy initialization of the env scripts.
   *
   * @param scriptLanguage the language
   * @return a list of executable environment scripts. Never null.
   */
  protected List<ExecutableScript> getEnvScripts(String scriptLanguage) {
    Map<String, List<ExecutableScript>> environment = getEnv(scriptLanguage);
    List<ExecutableScript> envScripts = environment.get(scriptLanguage);
    if (envScripts == null) {
      synchronized (this) {
        envScripts = environment.get(scriptLanguage);
        if (envScripts == null) {
          envScripts = initEnvForLanguage(scriptLanguage);
          environment.put(scriptLanguage, envScripts);
        }
      }
    }
    return envScripts;
  }

  /**
   * Initializes the env scripts for a given language.
   *
   * @param language the language
   * @return the list of env scripts. Never null.
   */
  protected List<ExecutableScript> initEnvForLanguage(String language) {
    List<ExecutableScript> scripts = new ArrayList<>();
    for (ScriptEnvResolver resolver : envResolvers) {
      String[] resolvedScripts = resolver.resolve(language);
      if (resolvedScripts != null) {
        for (String resolvedScript : resolvedScripts) {
          scripts.add(scriptFactory.createScriptFromSource(language, resolvedScript));
        }
      }
    }

    return scripts;
  }

}
