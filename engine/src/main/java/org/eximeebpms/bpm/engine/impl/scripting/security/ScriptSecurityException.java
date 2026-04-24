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

import java.util.Optional;

import org.eximeebpms.bpm.engine.ProcessEngineException;

public class ScriptSecurityException extends ProcessEngineException {

  private static final long serialVersionUID = 1L;

  private final String decisionCode;

  public ScriptSecurityException(String message) {
    this(message, null, null);
  }

  public ScriptSecurityException(String message, String decisionCode) {
    this(message, decisionCode, null);
  }

  public ScriptSecurityException(String message, Throwable cause) {
    this(message, null, cause);
  }

  public ScriptSecurityException(String message, String decisionCode, Throwable cause) {
    super(message, cause);
    this.decisionCode = normalize(decisionCode).orElse(null);
  }

  public ScriptSecurityException(String message, ScriptSecurityDecision decision) {
    this(message, decision.getCode().orElse(null), null);
  }

  public Optional<String> getDecisionCode() {
    return Optional.ofNullable(decisionCode);
  }

  private static Optional<String> normalize(String value) {
    return Optional.ofNullable(value)
        .map(String::trim)
        .filter(normalized -> !normalized.isEmpty());
  }
}
