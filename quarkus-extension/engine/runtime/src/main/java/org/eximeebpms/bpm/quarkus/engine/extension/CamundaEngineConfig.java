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
package org.eximeebpms.bpm.quarkus.engine.extension;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import java.util.Map;
import java.util.Optional;

@ConfigMapping(prefix = "quarkus.camunda")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface CamundaEngineConfig {

  /**
   * The Camunda ProcessEngineConfiguration properties. For more details,
   * @see <a href="https://docs.eximeebpms.org/manual/latest/reference/deployment-descriptors/tags/process-engine/#configuration-properties">Process Engine Configuration Properties</a>.
   */
  Map<String, String> genericConfig();

  /**
   * The Camunda JobExecutor config. It provides available job acquisition thread configuration
   * properties. These properties only take effect in a Quarkus environment.
   *
   * The JobExecutor is responsible for running Camunda jobs.
   */
  CamundaJobExecutorConfig jobExecutor();

  /**
   * Select a datasource by name or the default datasource is used.
   */
  Optional<String> datasource();

  /**
   * Select the ID generator strategy. Supported values:
   * <ul>
   *   <li>{@code strong} (default) — UUID v7, time-ordered and cryptographically random (RFC 9562).</li>
   *   <li>{@code uuid-v1} — <b>Deprecated.</b> UUID v1, time-based with MAC address. Will be removed in 1.4.0.</li>
   * </ul>
   */
  Optional<String> idGenerator();

}
