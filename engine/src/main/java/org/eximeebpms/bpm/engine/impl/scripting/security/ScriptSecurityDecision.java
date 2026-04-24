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
package org.eximeebpms.bpm.engine.impl.scripting.security;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable decision returned by the script security policy.
 */
public final class ScriptSecurityDecision {

  public enum Outcome {
    ALLOW,
    DENY
  }

  private static final ScriptSecurityDecision ALLOW_DECISION = new ScriptSecurityDecision(Outcome.ALLOW, null, null);

  private final Outcome outcome;
  private final String reason;
  private final String code;

  private ScriptSecurityDecision(Outcome outcome, String reason, String code) {
    this.outcome = Objects.requireNonNull(outcome, "outcome must not be null");
    this.reason = reason;
    this.code = code;
  }

  public static ScriptSecurityDecision allow() {
    return ALLOW_DECISION;
  }

  public static ScriptSecurityDecision deny(String reason) {
    return deny(reason, null);
  }

  public static ScriptSecurityDecision deny(String reason, String code) {
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("Denied decision must contain a non-blank reason");
    }

    return new ScriptSecurityDecision(
        Outcome.DENY,
        reason,
        normalize(code).orElse(null));
  }

  public Outcome getOutcome() {
    return outcome;
  }

  public boolean isAllowed() {
    return outcome == Outcome.ALLOW;
  }

  public boolean isDenied() {
    return outcome == Outcome.DENY;
  }

  public Optional<String> getReason() {
    return Optional.ofNullable(reason);
  }

  public Optional<String> getCode() {
    return Optional.ofNullable(code);
  }

  private static Optional<String> normalize(String value) {
    return Optional.ofNullable(value)
        .map(String::trim)
        .filter(normalized -> !normalized.isEmpty());
  }

  @Override
  public String toString() {
    return "ScriptSecurityDecision{"
        + "outcome=" + outcome
        + ", reason=" + getReason().orElse(null)
        + ", code=" + getCode().orElse(null)
        + '}';
  }
}
