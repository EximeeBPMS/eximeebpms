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
package org.eximeebpms.bpm.container.impl.deployment;

import java.util.HashMap;
import java.util.Map;

import org.eximeebpms.bpm.application.AbstractProcessApplication;
import org.eximeebpms.bpm.container.impl.ContainerIntegrationLogger;
import org.eximeebpms.bpm.container.impl.jmx.services.JmxManagedProcessEngine;
import org.eximeebpms.bpm.container.impl.jmx.services.JmxManagedProcessEngineController;
import org.eximeebpms.bpm.container.impl.metadata.PropertyHelper;
import org.eximeebpms.bpm.container.impl.metadata.spi.ProcessEnginePluginXml;
import org.eximeebpms.bpm.container.impl.metadata.spi.ProcessEngineXml;
import org.eximeebpms.bpm.container.impl.spi.DeploymentOperation;
import org.eximeebpms.bpm.container.impl.spi.DeploymentOperationStep;
import org.eximeebpms.bpm.container.impl.spi.PlatformServiceContainer;
import org.eximeebpms.bpm.container.impl.spi.ServiceTypes;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.ProcessEngineLogger;
import org.eximeebpms.bpm.engine.impl.businessevent.script.BusinessEventScriptViolationListener;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.eximeebpms.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.impl.jobexecutor.JobExecutor;
import org.eximeebpms.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbAwareScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.scripting.security.NoOpScriptViolationStore;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.util.ReflectUtil;

import static org.eximeebpms.bpm.container.impl.deployment.Attachments.PROCESS_APPLICATION;
import static org.eximeebpms.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * <p>Deployment operation step responsible for starting a managed process engine
 * inside the runtime container.</p>
 *
 * @author Daniel Meyer
 *
 */
public class StartProcessEngineStep extends DeploymentOperationStep {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  /** the process engine Xml configuration passed in as a parameter to the operation step */
  protected final ProcessEngineXml processEngineXml;

  public StartProcessEngineStep(ProcessEngineXml processEngineXml) {
    this.processEngineXml = processEngineXml;
  }

  public String getName() {
    return "Start process engine " + processEngineXml.getName();
  }

  public void performOperationStep(DeploymentOperation operationContext) {

    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();
    final AbstractProcessApplication processApplication = operationContext.getAttachment(PROCESS_APPLICATION);

    ClassLoader classLoader = null;

    if(processApplication != null) {
      classLoader = processApplication.getProcessApplicationClassloader();
    }

    String configurationClassName = processEngineXml.getConfigurationClass();

    if(configurationClassName == null || configurationClassName.isEmpty()) {
      configurationClassName = StandaloneProcessEngineConfiguration.class.getName();
    }

    // create & instantiate configuration class
    Class<? extends ProcessEngineConfigurationImpl> configurationClass = loadClass(configurationClassName, classLoader, ProcessEngineConfigurationImpl.class);
    ProcessEngineConfigurationImpl configuration = ReflectUtil.createInstance(configurationClass);

    // set UUID generator — always UUID v7 (StrongUuidGenerator); id-generator=uuid-v1 is a removed legacy value, warned about and ignored
    Map<String, String> properties = new HashMap<>(processEngineXml.getProperties());
    configuration.setIdGenerator(createIdGenerator(properties));
    // remove id-generator before applyProperties — it is kebab-case and has no matching setter
    properties.remove("id-generator");

    // set configuration values
    String name = processEngineXml.getName();
    configuration.setProcessEngineName(name);

    String datasourceJndiName = processEngineXml.getDatasource();
    configuration.setDataSourceJndiName(datasourceJndiName);

    // apply properties
    setJobExecutorActivate(configuration, properties);
    PropertyHelper.applyProperties(configuration, properties);

    // instantiate plugins:
    configurePlugins(configuration, processEngineXml, classLoader);
    addAdditionalPlugins(configuration);

    if(processEngineXml.getJobAcquisitionName() != null && !processEngineXml.getJobAcquisitionName().isEmpty()) {
      JobExecutor jobExecutor = getJobExecutorService(serviceContainer);
      ensureNotNull("Cannot find referenced job executor with name '" + processEngineXml.getJobAcquisitionName() + "'", "jobExecutor", jobExecutor);

      // set JobExecutor on process engine
      configuration.setJobExecutor(jobExecutor);
    }

    additionalConfiguration(configuration);

    // wire script violation persistence/forwarding before the engine is built — the policy
    // constructed inside buildProcessEngine() captures the violation store/listeners at that
    // point, so this must run first (unlike wireScriptSecurityPolicy() below, which wires the
    // ManagementService dynamically and so can safely run after the engine exists).
    preConfigureScriptSecurity(configuration);

    // start the process engine inside the container.
    JmxManagedProcessEngine managedProcessEngineService = createProcessEngineControllerInstance(configuration);
    serviceContainer.startService(ServiceTypes.PROCESS_ENGINE, configuration.getProcessEngineName(), managedProcessEngineService);

    wireScriptSecurityPolicy(configuration, managedProcessEngineService.getProcessEngine());
  }

  /**
   * Mirrors what the Spring Boot starter's {@code DefaultProcessEngineConfiguration.configureScriptSecurity()}
   * does in {@code preInit}: installs a DB-backed violation store and the business-event/SIEM
   * violation listener, unless script security is {@code DISABLED} — so a plain-XML/container
   * deployment persists violations to {@code ACT_RU_SCRIPT_VIOLATION} and forwards them to the
   * business-event outbox exactly like a Spring Boot deployment does.
   */
  protected void preConfigureScriptSecurity(ProcessEngineConfigurationImpl configuration) {
    if (configuration.isScriptSecurityDisabled()) {
      return;
    }

    if (configuration.getScriptViolationStore() instanceof NoOpScriptViolationStore) {
      configuration.setScriptViolationStore(new DbScriptViolationStore(configuration));
    }

    configuration.addScriptViolationListener(new BusinessEventScriptViolationListener());
  }

  /**
   * Mirrors what the Spring Boot starter's {@code ScriptSecurityAutoConfiguration} does after the
   * engine is built: wires the now-available {@link org.eximeebpms.bpm.engine.ManagementService}
   * into a {@link DbAwareScriptSecurityPolicy} (if one was installed — i.e. script security isn't
   * {@code DISABLED}) and seeds {@code ACT_GE_PROPERTY} from the {@code bpm-platform.xml}-configured
   * mode/allowlist on first start, so a plain-XML/container deployment gets the same DB-backed,
   * hot-reloadable, REST-driven configuration as a Spring Boot deployment.
   */
  protected void wireScriptSecurityPolicy(ProcessEngineConfigurationImpl configuration, ProcessEngine processEngine) {
    ScriptSecurityPolicy policy = configuration.getScriptSecurityPolicy();
    if (policy instanceof DbAwareScriptSecurityPolicy dbAware) {
      dbAware.wireAndSeed(processEngine.getManagementService());
    }
  }

  protected void setJobExecutorActivate(ProcessEngineConfigurationImpl configuration, Map<String, String> properties) {
    // override job executor auto activate: set to true in shared engine scenario
    // if it is not specified (see #CAM-4817)
    configuration.setJobExecutorActivate(true);
  }

  protected org.eximeebpms.bpm.engine.impl.cfg.IdGenerator createIdGenerator(Map<String, String> properties) {
    if ("uuid-v1".equals(properties.get("id-generator"))) {
      ProcessEngineLogger.PERSISTENCE_LOGGER.uuidV1GeneratorRemoved();
    }
    return new StrongUuidGenerator();
  }

  protected JmxManagedProcessEngineController createProcessEngineControllerInstance(ProcessEngineConfigurationImpl configuration) {
    return new JmxManagedProcessEngineController(configuration);
  }

  /**
   * <p>Instantiates and applies all {@link ProcessEnginePlugin}s defined in the processEngineXml
   */
  protected void configurePlugins(ProcessEngineConfigurationImpl configuration, ProcessEngineXml processEngineXml, ClassLoader classLoader) {

    for (ProcessEnginePluginXml pluginXml : processEngineXml.getPlugins()) {
      // create plugin instance
      Class<? extends ProcessEnginePlugin> pluginClass = loadClass(pluginXml.getPluginClass(), classLoader, ProcessEnginePlugin.class);
      ProcessEnginePlugin plugin = ReflectUtil.createInstance(pluginClass);

      // apply configured properties
      Map<String, String> properties = pluginXml.getProperties();
      PropertyHelper.applyProperties(plugin, properties);

      // add to configuration
      configuration.getProcessEnginePlugins().add(plugin);
    }

  }


  protected JobExecutor getJobExecutorService(final PlatformServiceContainer serviceContainer) {
    // lookup container managed job executor
    String jobAcquisitionName = processEngineXml.getJobAcquisitionName();
    JobExecutor jobExecutor = serviceContainer.getServiceValue(ServiceTypes.JOB_EXECUTOR, jobAcquisitionName);
    return jobExecutor;
  }

  @SuppressWarnings("unchecked")
  protected <T> Class<? extends T> loadClass(String className, ClassLoader customClassloader, Class<T> clazz) {
    try {
      return ReflectUtil.loadClass(className, customClassloader, clazz);
    }
    catch (ClassNotFoundException e) {
      throw LOG.cannotLoadConfigurationClass(className, e);
    }
    catch (ClassCastException e) {
      throw LOG.configurationClassHasWrongType(className, clazz, e);
    }
  }

  /**
   * Add additional plugins that are not declared in the process engine xml.
   */
  protected void addAdditionalPlugins(ProcessEngineConfigurationImpl configuration) {
    // do nothing
  }

  protected void additionalConfiguration(ProcessEngineConfigurationImpl configuration) {
    // do nothing
  }

}
