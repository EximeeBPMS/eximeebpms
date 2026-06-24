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
package org.eximeebpms.bpm.run.test.config.id;

import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.cfg.IdGenerator;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.eximeebpms.bpm.run.EximeeBpmsBpmRun;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that EximeeBPMS Run uses StrongUuidGenerator (UUID v7) by default.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { EximeeBpmsBpmRun.class })
@ActiveProfiles(profiles = { "test-auth-disabled", "test-monitoring-disabled" })
public class DefaultIdGeneratorRunTest {

  @Autowired
  ProcessEngine engine;

  @Test
  public void defaultIdGeneratorIsStrongUuidGeneratorV7() {
    // given
    IdGenerator idGenerator = ((ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration()).getIdGenerator();

    // then
    assertThat(idGenerator).isInstanceOf(StrongUuidGenerator.class);
    UUID uuid = UUID.fromString(idGenerator.getNextId());
    assertThat(uuid.version()).isEqualTo(7);
  }

}

