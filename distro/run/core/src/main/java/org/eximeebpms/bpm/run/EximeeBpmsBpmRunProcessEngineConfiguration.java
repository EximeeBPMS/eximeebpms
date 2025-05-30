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
package org.eximeebpms.bpm.run;

import org.eximeebpms.bpm.engine.ProcessEngineException;
import org.eximeebpms.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.eximeebpms.bpm.engine.impl.diagnostics.CamundaIntegration;
import org.eximeebpms.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.eximeebpms.bpm.run.property.EximeeBpmsBpmRunProcessEnginePluginProperty;
import org.eximeebpms.bpm.run.utils.EximeeBpmsBpmRunProcessEnginePluginHelper;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class EximeeBpmsBpmRunProcessEngineConfiguration extends SpringProcessEngineConfiguration {

  private final String normalizedDeploymentDir;

  public EximeeBpmsBpmRunProcessEngineConfiguration(String normalizedDeploymentDir,
                                                    boolean deployChangedOnly,
                                                    List<ProcessEnginePlugin> processEnginePluginsFromContext,
                                                    List<EximeeBpmsBpmRunProcessEnginePluginProperty> processEnginePluginsFromYaml) {
    this.normalizedDeploymentDir = normalizedDeploymentDir;

    setDeployChangedOnly(deployChangedOnly);
    configureProcessEnginePlugins(processEnginePluginsFromContext, processEnginePluginsFromYaml);
  }

  @Override
  protected String getFileResourceName(Resource resource) {
    // only path relative to the root deployment directory as identifier to
    // prevent re-deployments when the path changes (e.g. distro is moved)
    try {
      String resourceAbsolutePath = resource.getURI().toString();
      int startIndex = resourceAbsolutePath.indexOf(normalizedDeploymentDir) + normalizedDeploymentDir.length();
      return resourceAbsolutePath.substring(startIndex);
    } catch (IOException e) {
      throw new ProcessEngineException("Failed to locate resource " + resource.getFilename(), e);
    }
  }

  @Override
  protected void initTelemetryData() {
    super.initTelemetryData();
    Set<String> camundaIntegration = telemetryData.getProduct().getInternals().getCamundaIntegration();
    camundaIntegration.add(CamundaIntegration.CAMUNDA_BPM_RUN);
  }

  protected void configureProcessEnginePlugins(List<ProcessEnginePlugin> processEnginePluginsFromContext,
                                               List<EximeeBpmsBpmRunProcessEnginePluginProperty> processEnginePluginsFromYaml) {
    // register process engine plugins defined in yaml
    EximeeBpmsBpmRunProcessEnginePluginHelper.registerYamlPlugins(processEnginePluginsFromContext, processEnginePluginsFromYaml);

    this.processEnginePlugins.add(new CompositeProcessEnginePlugin(processEnginePluginsFromContext));
  }
}
