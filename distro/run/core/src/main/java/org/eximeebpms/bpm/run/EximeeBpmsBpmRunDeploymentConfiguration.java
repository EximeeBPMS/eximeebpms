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

import org.apache.commons.lang3.StringUtils;
import org.eximeebpms.bpm.spring.boot.starter.configuration.impl.DefaultDeploymentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EximeeBpmsBpmRunDeploymentConfiguration extends DefaultDeploymentConfiguration {
  private final Logger logger = LoggerFactory.getLogger(EximeeBpmsBpmRunDeploymentConfiguration.class);
  private final String deploymentDir;

  public EximeeBpmsBpmRunDeploymentConfiguration(String deploymentDir) {
    this.deploymentDir = deploymentDir;
  }

  @Override
  public Set<Resource> getDeploymentResources() {
    if (!StringUtils.isEmpty(deploymentDir)) {
      Path resourceDir = Paths.get(deploymentDir);

      try (Stream<Path> stream = Files.walk(resourceDir)) {
        return stream
                .filter(file -> !Files.isDirectory(file))
                .map(FileSystemResource::new)
                .collect(Collectors.toSet());
      } catch (IOException e) {
          logger.error("Failed to load deployment resources from {}", resourceDir);
      }
    }
    return Collections.emptySet();
  }

  protected String getNormalizedDeploymentDir() {
    String result = deploymentDir;

    if(File.separator.equals("\\")) {
      result = result.replace("\\", "/");
    }
    return result;
  }
}