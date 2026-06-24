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
package org.eximeebpms.bpm.container.impl.jboss.test;

import org.eximeebpms.bpm.container.impl.jboss.config.ManagedJtaProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.impl.cfg.IdGenerator;
import org.eximeebpms.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ManagedJtaProcessEngineConfiguration} ID generator selection.
 */
public class ManagedJtaProcessEngineConfigurationIdGeneratorTest {

  /**
   * Test subclass exposing protected initIdGenerator() for unit testing.
   */
  static class TestableConfig extends ManagedJtaProcessEngineConfiguration {
    @Override
    public void initIdGenerator() {
      super.initIdGenerator();
    }
  }

  @Test
  public void defaultIdGeneratorIsStrongUuidGeneratorV7() {
    // given
    TestableConfig config = new TestableConfig();

    // when
    config.initIdGenerator();

    // then
    IdGenerator generator = config.getIdGenerator();
    assertThat(generator).isInstanceOf(StrongUuidGenerator.class);
    UUID uuid = UUID.fromString(generator.getNextId());
    assertThat(uuid.version()).isEqualTo(7);
  }

  @Test
  public void existingIdGeneratorIsNotOverridden() {
    // given — already configured externally (e.g. by MscManagedProcessEngineController)
    TestableConfig config = new TestableConfig();
    StrongUuidGenerator customGenerator = new StrongUuidGenerator();
    config.setIdGenerator(customGenerator);

    // when
    config.initIdGenerator();

    // then — existing generator is preserved
    assertThat(config.getIdGenerator()).isSameAs(customGenerator);
  }

}
