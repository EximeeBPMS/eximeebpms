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

import java.util.Date;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;

public class ScriptViolationBusinessEventFactory extends BusinessEventFactorySupport {

  /**
   * Creates a {@link BusinessScriptViolationEventEntity} from a recorded violation.
   * When {@code violation.processDefinitionId()} is non-null, the process definition name and
   * version are resolved from the DB via {@link #fillProcessDefinitionData}. Otherwise only
   * {@code processDefinitionKey} is set (e.g. for deployment-time violations).
   */
  public BusinessEvent createScriptViolationEvent(ScriptViolationEvent violation) {
    BusinessScriptViolationEventEntity event = new BusinessScriptViolationEventEntity();

    event.setEventType(BusinessEventTypes.SCRIPT_VIOLATION_CREATE.getEventName());
    event.setBusinessEventType(BusinessEventTypes.SCRIPT_VIOLATION_CREATE.getBusinessEventName());

    event.setTimestamp(Date.from(violation.timestamp()));
    event.setActivityId(violation.activityId());
    event.setLanguage(violation.language());
    event.setSourceType(violation.sourceType() != null ? violation.sourceType().name() : null);
    event.setOrigin(violation.origin() != null ? violation.origin().name() : null);
    event.setRuleCode(violation.ruleCode());
    event.setReason(violation.reason());

    if (violation.processDefinitionId() != null) {
      fillProcessDefinitionData(event, violation.processDefinitionId());
    } else {
      event.setProcessDefinitionKey(violation.processDefinitionKey());
    }

    return event;
  }
}
