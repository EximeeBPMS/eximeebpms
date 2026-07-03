/*
 * Copyright EximeeBPMS contributors
 *
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
package org.eximeebpms.bpm.spring.boot.starter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbAwareScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationListener;
import org.eximeebpms.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {TestApplication.class, ScriptViolationListenerWiringIT.TestConfig.class},
    webEnvironment = WebEnvironment.NONE,
    properties = {
        "eximeebpms.bpm.script-security.mode=AUDIT",
        "eximeebpms.bpm.admin-user.id=admin"
    }
)
public class ScriptViolationListenerWiringIT {

  @Autowired
  private ProcessEngine processEngine;

  @Autowired
  private RecordingScriptViolationListener recordingListener;

  @Test
  public void shouldWireSpringBeanListenerIntoSecurityPolicy() {
    // given
    ProcessEngineConfigurationImpl config =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    DbAwareScriptSecurityPolicy policy =
        (DbAwareScriptSecurityPolicy) config.getScriptSecurityPolicy();

    // when
    List<ScriptViolationListener> listeners = policy.getListeners();

    // then — the @Bean RecordingScriptViolationListener was collected from ApplicationContext
    assertThat(listeners).anyMatch(l -> l instanceof RecordingScriptViolationListener);
  }

  @Test
  public void shouldCallAllRegisteredListenersWhenViolationOccurs() {
    // given
    ProcessEngineConfigurationImpl config =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    DbAwareScriptSecurityPolicy policy =
        (DbAwareScriptSecurityPolicy) config.getScriptSecurityPolicy();

    // when — simulate a violation by calling each listener directly through the policy's list
    ScriptViolationEvent testEvent = new ScriptViolationEvent(
        java.time.Instant.now(), "testProcess", null, "task1",
        "javascript",
        org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSourceType.INLINE_SOURCE,
        org.eximeebpms.bpm.engine.impl.scripting.security.ScriptOrigin.USER,
        "TEST_RULE", "wiring test");

    policy.getListeners().stream()
        .filter(l -> l instanceof RecordingScriptViolationListener)
        .map(l -> (RecordingScriptViolationListener) l)
        .forEach(l -> l.onViolation(testEvent));

    // then
    assertThat(recordingListener.getRecordedViolations()).hasSize(1);
    assertThat(recordingListener.getRecordedViolations().get(0).ruleCode()).isEqualTo("TEST_RULE");
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public RecordingScriptViolationListener recordingScriptViolationListener() {
      return new RecordingScriptViolationListener();
    }
  }

  static class RecordingScriptViolationListener implements ScriptViolationListener {
    private final List<ScriptViolationEvent> recorded = new ArrayList<>();

    @Override
    public void onViolation(ScriptViolationEvent event) {
      recorded.add(event);
    }

    public List<ScriptViolationEvent> getRecordedViolations() {
      return recorded;
    }
  }
}
