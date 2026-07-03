package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.time.Instant;

public record ScriptViolationEvent(
    Instant timestamp,
    String processDefinitionKey,
    String processDefinitionId,
    String activityId,
    String language,
    ScriptSourceType sourceType,
    ScriptOrigin origin,
    String ruleCode,
    String reason
) {}
