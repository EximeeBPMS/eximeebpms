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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import lombok.Setter;
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
import org.eximeebpms.bpm.engine.impl.scripting.security.DefaultScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptOrigin;
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
   * The cached platform environment scripts per script language.
   * <p>
   * Process-application environment scripts are intentionally not stored here.
   * They remain stored in AbstractProcessApplication#getEnvironmentScripts()
   * as Map<String, List<ExecutableScript>> to preserve the existing public contract.
   * </p>
   */
  protected Map<String, List<ResolvedExecutableEnvScript>> env = new ConcurrentHashMap<>();

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
   * the script security policy explicitly configured for this scripting environment
   */
  @Setter
  protected ScriptSecurityPolicy scriptSecurityPolicy;

  /**
   * Default script security policy reused when no custom policy is configured.
   */
  protected final ScriptSecurityPolicy defaultScriptSecurityPolicy = new DefaultScriptSecurityPolicy();

  public ScriptingEnvironment(
      ScriptFactory scriptFactory,
      List<ScriptEnvResolver> scriptEnvResolvers,
      ScriptingEngines scriptingEngines) {
    this(scriptFactory, scriptEnvResolvers, scriptingEngines, null);
  }

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
    List<ResolvedExecutableEnvScript> envScripts = getEnvScripts(script, scriptEngine);

    // first, evaluate the env scripts (if any)
    for (ResolvedExecutableEnvScript envScript : envScripts) {
      enforceScriptSecurity(
          envScript.executableScript(),
          scope,
          envScript.origin(),
          envScript.provider());

      envScript.executableScript().execute(scriptEngine, scope, bindings);
    }

    enforceScriptSecurity(script, scope, ScriptOrigin.USER, null);

    // next evaluate the actual script
    return script.execute(scriptEngine, scope, bindings);
  }

  protected void enforceScriptSecurity(ExecutableScript script, VariableScope scope) {
    enforceScriptSecurity(script, scope, ScriptOrigin.USER, null);
  }

  protected void enforceScriptSecurity(
      ExecutableScript script,
      VariableScope scope,
      ScriptOrigin origin,
      String provider) {
    ScriptSecurityPolicy policy = resolveScriptSecurityPolicy();

    if (policy == null) {
      return;
    }

    ScriptSecurityContext context = createSecurityContext(script, scope, origin, provider);
    String source = context.getSource();

    if (source == null || source.isBlank()) {
      return;
    }

    ScriptSecurityDecision decision = policy.evaluate(context);
    if (decision.isDenied()) {
      throw new ScriptSecurityException(
          buildSecurityExceptionMessage(scope, decision),
          decision.getCode().orElse(null));
    }
  }

  protected ScriptSecurityPolicy resolveScriptSecurityPolicy() {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();

    if (configuration == null) {
      return scriptSecurityPolicy != null
          ? scriptSecurityPolicy
          : defaultScriptSecurityPolicy;
    }

    if (!configuration.isScriptSecurityEnabled()) {
      return null;
    }

    if (configuration.getScriptSecurityPolicy() != null) {
      return configuration.getScriptSecurityPolicy();
    }

    if (scriptSecurityPolicy != null) {
      return scriptSecurityPolicy;
    }

    return defaultScriptSecurityPolicy;
  }

  protected ScriptSecurityContext createSecurityContext(
      ExecutableScript script,
      VariableScope scope,
      ScriptOrigin origin,
      String provider) {

    ScriptSecurityContext.Builder builder = ScriptSecurityContext.builder(script.getLanguage())
        .source(resolveScriptSource(script, scope))
        .sourceType(resolveSourceType(script))
        .origin(origin)
        .provider(provider);

    if (scope instanceof DelegateExecution execution) {
      final String processDefinitionId = execution.getProcessDefinitionId();

      builder
          .activityId(execution.getCurrentActivityId())
          .processDefinitionId(processDefinitionId)
          .processDefinitionKey(extractProcessDefinitionKey(processDefinitionId));
    } else if (scope instanceof TaskEntity task) {
      final String processDefinitionId = task.getProcessDefinitionId();

      builder
          .processDefinitionId(processDefinitionId)
          .processDefinitionKey(extractProcessDefinitionKey(processDefinitionId));

      if (task.getExecution() != null) {
        builder.activityId(task.getExecution().getActivityId());
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

  protected String extractProcessDefinitionKey(String processDefinitionId) {
    if (processDefinitionId == null) {
      return null;
    }

    int separatorIndex = processDefinitionId.indexOf(':');
    return separatorIndex < 0
        ? processDefinitionId
        : processDefinitionId.substring(0, separatorIndex);
  }

  protected String resolveScriptSource(ExecutableScript script, VariableScope scope) {
    if (script instanceof DynamicResourceExecutableScript dynamicResourceExecutableScript) {
      return dynamicResourceExecutableScript.getScriptSource(scope);
    }

    if (script instanceof DynamicSourceExecutableScript dynamicSourceExecutableScript) {
      return dynamicSourceExecutableScript.getScriptSource(scope);
    }

    if (script instanceof ResourceExecutableScript resourceExecutableScript) {
      return resourceExecutableScript.resolveScriptSource();
    }

    if (script instanceof SourceExecutableScript sourceExecutableScript) {
      return sourceExecutableScript.getScriptSource();
    }

    return null;
  }

  protected ScriptSourceType resolveSourceType(ExecutableScript script) {
    if (script instanceof DynamicResourceExecutableScript) {
      return ScriptSourceType.DYNAMIC_RESOURCE;
    }
    if (script instanceof DynamicSourceExecutableScript) {
      return ScriptSourceType.DYNAMIC_SOURCE;
    }
    if (script instanceof ResourceExecutableScript) {
      return ScriptSourceType.RESOURCE;
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

  protected List<ResolvedExecutableEnvScript> getEnvScripts(
      ExecutableScript script,
      ScriptEngine scriptEngine) {

    List<ResolvedExecutableEnvScript> envScripts = getEnvScripts(script.getLanguage().toLowerCase());

    if (envScripts.isEmpty()) {
      envScripts = getEnvScripts(scriptEngine.getFactory().getLanguageName().toLowerCase());
    }

    return envScripts;
  }

  /**
   * Returns environment scripts for the given language and initializes them lazily.
   *
   * <p>If a process application is available and fetching script engines from the process
   * application is enabled, the original process-application cache contract is preserved:
   * {@code AbstractProcessApplication#getEnvironmentScripts()} stores
   * {@code Map<String, List<ExecutableScript>>}.</p>
   *
   * <p>Those cached scripts are still resolved by engine {@link ScriptEnvResolver}s and
   * represent trusted engine/plugin bootstrap code, not user-authored BPMN scripts. Therefore
   * they are wrapped as {@link ScriptOrigin#PLATFORM} for script-security evaluation.</p>
   *
   * <p>If no process-application environment is available, environment scripts are resolved
   * and cached in this scripting environment as
   * {@code Map<String, List<ResolvedExecutableEnvScript>>}.</p>
   *
   * @param scriptLanguage the script language
   * @return a list of executable environment scripts; never {@code null}
   */
  protected List<ResolvedExecutableEnvScript> getEnvScripts(String scriptLanguage) {
    ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
    ProcessApplicationReference processApplication = Context.getCurrentProcessApplication();

    if (config != null && config.isEnableFetchScriptEngineFromProcessApplication() && processApplication != null) {
      Map<String, List<ExecutableScript>> processApplicationEnv = getPaEnvScripts(processApplication);

      if (processApplicationEnv != null) {
        return getProcessApplicationEnvScripts(processApplicationEnv, scriptLanguage);
      }
    }

    return getPlatformEnvScripts(scriptLanguage);
  }

  protected List<ResolvedExecutableEnvScript> getProcessApplicationEnvScripts(Map<String, List<ExecutableScript>> environment, String scriptLanguage) {
    List<ExecutableScript> envScripts = environment.computeIfAbsent(
        scriptLanguage,
        this::initProcessApplicationEnvScriptsForLanguage);

    return wrapEnvScripts(
        envScripts,
        ScriptOrigin.PLATFORM,
        AbstractProcessApplication.class.getName());
  }

  protected List<ResolvedExecutableEnvScript> getPlatformEnvScripts(String scriptLanguage) {
    return env.computeIfAbsent(scriptLanguage, this::initPlatformEnvScriptsForLanguage);
  }

  protected List<ResolvedExecutableEnvScript> wrapEnvScripts(List<ExecutableScript> envScripts, ScriptOrigin origin, String provider) {
    List<ResolvedExecutableEnvScript> resolvedScripts = new ArrayList<>();

    for (ExecutableScript envScript : envScripts) {
      resolvedScripts.add(new ResolvedExecutableEnvScript(envScript, origin, provider));
    }

    return resolvedScripts;
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

  protected List<ExecutableScript> initProcessApplicationEnvScriptsForLanguage(String language) {
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

  protected List<ResolvedExecutableEnvScript> initPlatformEnvScriptsForLanguage(String language) {
    List<ResolvedExecutableEnvScript> scripts = new ArrayList<>();

    for (ScriptEnvResolver resolver : envResolvers) {
      if (resolver instanceof TrustedScriptEnvResolver trustedResolver) {
        ResolvedScriptEnvScript[] resolvedScripts = trustedResolver.resolveTrusted(language);

        if (resolvedScripts != null) {
          for (ResolvedScriptEnvScript resolvedScript : resolvedScripts) {
            scripts.add(new ResolvedExecutableEnvScript(
                scriptFactory.createScriptFromSource(language, resolvedScript.getSource()),
                resolvedScript.getOrigin(),
                resolvedScript.getProvider()));
          }
        }

        continue;
      }

      String[] resolvedScripts = resolver.resolve(language);

      if (resolvedScripts != null) {
        for (String resolvedScript : resolvedScripts) {
          scripts.add(new ResolvedExecutableEnvScript(
              scriptFactory.createScriptFromSource(language, resolvedScript),
              ScriptOrigin.USER,
              resolver.getClass().getName()));
        }
      }
    }

    return scripts;
  }
}
