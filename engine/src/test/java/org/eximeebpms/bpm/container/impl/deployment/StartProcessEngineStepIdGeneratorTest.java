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

import org.eximeebpms.bpm.engine.impl.cfg.IdGenerator;
import org.eximeebpms.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StartProcessEngineStep#createIdGenerator(Map)}.
 */
public class StartProcessEngineStepIdGeneratorTest {

  private final StartProcessEngineStep step = new StartProcessEngineStep(null) {
    // minimal override — we only test createIdGenerator
  };

  @Test
  public void defaultGeneratorIsStrongUuidGeneratorV7() {
    // when
    IdGenerator generator = step.createIdGenerator(Collections.emptyMap());

    // then
    assertThat(generator).isInstanceOf(StrongUuidGenerator.class);
    UUID uuid = UUID.fromString(generator.getNextId());
    assertThat(uuid.version()).isEqualTo(7);
  }

  @Test
  public void unknownPropertyValueFallsBackToStrongUuidGenerator() {
    // when
    IdGenerator generator = step.createIdGenerator(Map.of("id-generator", "unknown-value"));

    // then
    assertThat(generator).isInstanceOf(StrongUuidGenerator.class);
  }

  @Test
  @SuppressWarnings("removal")
  public void uuidV1PropertyConfiguresUuidV1Generator() {
    // when
    IdGenerator generator = step.createIdGenerator(Map.of("id-generator", "uuid-v1"));

    // then
    assertThat(generator).isInstanceOf(org.eximeebpms.bpm.engine.impl.persistence.UuidV1Generator.class);
    UUID uuid = UUID.fromString(generator.getNextId());
    assertThat(uuid.version()).isEqualTo(1);
  }

}

