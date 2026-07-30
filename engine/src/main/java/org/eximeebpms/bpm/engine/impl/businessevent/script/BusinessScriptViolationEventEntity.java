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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessDetailEventEntity;

/**
 * Business event entity for a script security violation.
 * Published as {@code camunda7:script-violation:create} to the business event outbox.
 *
 * <p>{@code processInstanceId} and {@code rootProcessInstanceId} are {@code null} for
 * deployment-time violations (detected during BPMN parsing), and populated for runtime
 * violations (detected during script-task execution).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessScriptViolationEventEntity extends BusinessDetailEventEntity {

  /** ID of the BPMN activity where the violation was detected (e.g. {@code ServiceTask_1}). */
  protected String activityId;

  /** Script language, e.g. {@code javascript}, {@code groovy}. */
  protected String language;

  /** Source type as string (see {@link org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSourceType}). */
  protected String sourceType;

  /** Script origin as string (see {@link org.eximeebpms.bpm.engine.impl.scripting.security.ScriptOrigin}). */
  protected String origin;

  /** Rule code that triggered the violation, e.g. {@code SCRIPT_SECURITY_SYSTEM_GETENV}. */
  protected String ruleCode;

  /** Human-readable violation reason. */
  protected String reason;

}
