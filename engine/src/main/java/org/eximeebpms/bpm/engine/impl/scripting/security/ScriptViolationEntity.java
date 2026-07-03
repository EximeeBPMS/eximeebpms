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
package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.eximeebpms.bpm.engine.impl.db.DbEntity;

@Getter
@Setter
public class ScriptViolationEntity implements DbEntity, Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String id;
  private Date timestamp;
  private String processDefinitionKey;
  private String activityId;
  private String language;
  private String sourceType;
  private String origin;
  private String ruleCode;
  private String reason;

  @Override
  public Object getPersistentState() {
    Map<String, Object> state = new HashMap<>();
    state.put("id", id);
    state.put("timestamp", timestamp);
    state.put("processDefinitionKey", processDefinitionKey);
    state.put("activityId", activityId);
    state.put("language", language);
    state.put("sourceType", sourceType);
    state.put("origin", origin);
    state.put("ruleCode", ruleCode);
    state.put("reason", reason);
    return state;
  }

  public static ScriptViolationEntity fromEvent(ScriptViolationEvent event) {
    ScriptViolationEntity entity = new ScriptViolationEntity();
    entity.setTimestamp(event.timestamp() != null ? Date.from(event.timestamp()) : null);
    entity.setProcessDefinitionKey(event.processDefinitionKey());
    entity.setActivityId(event.activityId());
    entity.setLanguage(event.language());
    entity.setSourceType(event.sourceType() != null ? event.sourceType().name() : null);
    entity.setOrigin(event.origin() != null ? event.origin().name() : null);
    entity.setRuleCode(event.ruleCode());
    entity.setReason(event.reason());
    return entity;
  }

  public ScriptViolationEvent toEvent() {
    return new ScriptViolationEvent(
        timestamp != null ? timestamp.toInstant() : null,
        processDefinitionKey,
        null, // processDefinitionId is not persisted in ACT_RU_SCRIPT_VIOLATION
        activityId,
        language,
        sourceType != null ? ScriptSourceType.valueOf(sourceType) : null,
        origin != null ? ScriptOrigin.valueOf(origin) : null,
        ruleCode,
        reason);
  }
}
