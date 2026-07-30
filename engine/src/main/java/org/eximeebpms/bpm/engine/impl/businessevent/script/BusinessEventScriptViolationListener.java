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
package org.eximeebpms.bpm.engine.impl.businessevent.script;

import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProcessor;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProducer;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationListener;

/**
 * Publishes script violation events to the business event outbox ({@code ACT_RU_BUS_EVT_OBX})
 * so they can be forwarded to SIEM or other external systems via the configured
 * {@link org.eximeebpms.bpm.commons.eventbus.BusinessEventPublisher}.
 *
 * <p>The event type published is {@code camunda7:script-violation:create}.
 *
 * <p>No-op when business events are disabled in the engine configuration
 * ({@code processEngineConfiguration.isBusinessEventsEnabled() == false}).
 */
public class BusinessEventScriptViolationListener implements ScriptViolationListener {

  @Override
  public void onViolation(ScriptViolationEvent event) {
    if (Context.getProcessEngineConfiguration() == null) {
      return;
    }
    BusinessEventProcessor.processBusinessEvents(new BusinessEventProcessor.BusinessEventCreator() {
      @Override
      public BusinessEvent createBusinessEvent(BusinessEventProducer producer) {
        return producer.createScriptViolationEvt(event);
      }
    });
  }
}
