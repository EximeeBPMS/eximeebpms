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
package org.eximeebpms.bpm.engine.rest.dto;

import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;

public class ScriptViolationEventDto {

  private String timestamp;
  private String processDefinitionKey;
  private String activityId;
  private String language;
  private String sourceType;
  private String origin;
  private String ruleCode;
  private String reason;

  public static ScriptViolationEventDto fromEvent(ScriptViolationEvent event) {
    ScriptViolationEventDto dto = new ScriptViolationEventDto();
    dto.timestamp = event.timestamp() != null ? event.timestamp().toString() : null;
    dto.processDefinitionKey = event.processDefinitionKey();
    dto.activityId = event.activityId();
    dto.language = event.language();
    dto.sourceType = event.sourceType() != null ? event.sourceType().name() : null;
    dto.origin = event.origin() != null ? event.origin().name() : null;
    dto.ruleCode = event.ruleCode();
    dto.reason = event.reason();
    return dto;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  public String getRuleCode() {
    return ruleCode;
  }

  public void setRuleCode(String ruleCode) {
    this.ruleCode = ruleCode;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
