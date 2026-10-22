/*
 * Copyright EximeeBPMS contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.eximeebpms.bpm.engine.test.api.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.eximeebpms.bpm.engine.ManagementService;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import org.eximeebpms.bpm.engine.impl.interceptor.Command;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.eximeebpms.bpm.engine.variable.Variables;
import org.eximeebpms.bpm.engine.variable.value.ObjectValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * BPMS-662: a DMN decision input/output value used to insert its byte array as soon as the
 * producer recorded it, i.e. before anything had decided the row it belongs to would be
 * written. Recording the value must not touch ACT_GE_BYTEARRAY; only materialising it,
 * which the insert path does immediately before writing the row, may.
 */
public class HistoricDecisionValueMaterializationTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl configuration;
  protected ManagementService managementService;

  @Before
  public void init() {
    configuration = (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();
    managementService = engineRule.getManagementService();
  }

  /** Robust against a configured table prefix. */
  protected long byteArrayCount() {
    return managementService.getTableCount().entrySet().stream()
        .filter(entry -> entry.getKey().endsWith("ACT_GE_BYTEARRAY"))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("ACT_GE_BYTEARRAY not reported by getTableCount()"));
  }

  protected void inCommand(Command<Void> command) {
    configuration.getCommandExecutorTxRequired().execute(command);
  }

  protected void deleteByteArray(String byteArrayId) {
    inCommand(commandContext -> {
      commandContext.getByteArrayManager().deleteByteArrayById(byteArrayId);
      return null;
    });
  }

  protected static ObjectValue objectValue() {
    return Variables.objectValue(new ArrayList<>(Arrays.asList("a", "b"))).create();
  }

  @Test
  public void shouldNotCreateInputByteArrayUntilValueIsMaterialized() {
    // given
    HistoricDecisionInputInstanceEntity input = new HistoricDecisionInputInstanceEntity();
    input.setClauseId("clauseId");
    long byteArraysBefore = byteArrayCount();

    // when the producer records the value
    inCommand(commandContext -> {
      input.setValue(objectValue());
      return null;
    });

    // then nothing is written, and the value is still readable
    assertEquals(byteArraysBefore, byteArrayCount());
    assertNull(input.getByteArrayValueId());
    assertNotNull(input.getTypedValue());

    // when the insert path materialises it
    inCommand(commandContext -> {
      input.materializeValue();
      return null;
    });

    // then exactly one byte array exists for it
    assertEquals(byteArraysBefore + 1, byteArrayCount());
    assertNotNull(input.getByteArrayValueId());

    // the owning row is never inserted here, so that byte array is an orphan — exactly the
    // condition this change exists to prevent. Clean it up, or the engine's own
    // database-is-clean check fails the test.
    deleteByteArray(input.getByteArrayValueId());
  }

  @Test
  public void shouldNotCreateOutputByteArrayUntilValueIsMaterialized() {
    // given
    HistoricDecisionOutputInstanceEntity output = new HistoricDecisionOutputInstanceEntity();
    output.setClauseId("clauseId");
    long byteArraysBefore = byteArrayCount();

    // when
    inCommand(commandContext -> {
      output.setValue(objectValue());
      return null;
    });

    // then
    assertEquals(byteArraysBefore, byteArrayCount());
    assertNull(output.getByteArrayValueId());
    assertNotNull(output.getTypedValue());

    // when
    inCommand(commandContext -> {
      output.materializeValue();
      return null;
    });

    // then
    assertEquals(byteArraysBefore + 1, byteArrayCount());
    assertNotNull(output.getByteArrayValueId());

    deleteByteArray(output.getByteArrayValueId());
  }

}
