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
package org.eximeebpms.bpm.quarkus.engine.test.id;

import io.quarkus.test.QuarkusUnitTest;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.TaskService;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.task.Task;
import org.eximeebpms.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import jakarta.inject.Inject;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that setting {@code quarkus.camunda.id-generator=uuid-v1} configures UuidV1Generator.
 *
 * @deprecated This test will be removed when UuidV1Generator is removed in EximeeBPMS 1.4.0.
 */
@SuppressWarnings("removal")
class ConfigureUuidV1IdGeneratorTest {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
      .overrideConfigKey("quarkus.camunda.id-generator", "uuid-v1");

  @Inject
  public TaskService taskService;

  @Inject
  protected ProcessEngine processEngine;

  @Test
  void shouldConfigureUuidV1IdGenerator() {
    Task task = taskService.newTask();
    taskService.saveTask(task);

    String id = taskService.createTaskQuery().singleResult().getId();

    // UUID v1 has version bit = 1
    assertThat(UUID.fromString(id).version()).isEqualTo(1);

    ProcessEngineConfigurationImpl engineConfig =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    assertThat(engineConfig.getIdGenerator())
        .isInstanceOf(org.eximeebpms.bpm.engine.impl.persistence.UuidV1Generator.class);

    taskService.deleteTask(id, true);
  }

}

