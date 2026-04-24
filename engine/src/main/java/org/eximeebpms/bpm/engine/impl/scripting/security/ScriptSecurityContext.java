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

public final class ScriptSecurityContext {

  private final String language;
  private final String source;
  private final ScriptSourceType sourceType;
  private final String activityId;
  private final String processDefinitionId;
  private final String processDefinitionKey;
  private final String caseDefinitionId;
  private final String processDefinitionName;

  private ScriptSecurityContext(Builder builder) {
    this.language = requireNonBlank(builder.language, "language");
    this.source = normalize(builder.source).orElse("");
    this.sourceType = Objects.requireNonNullElse(builder.sourceType, ScriptSourceType.UNKNOWN);
    this.activityId = normalize(builder.activityId).orElse(null);
    this.processDefinitionId = normalize(builder.processDefinitionId).orElse(null);
    this.processDefinitionKey = normalize(builder.processDefinitionKey).orElse(null);
    this.caseDefinitionId = normalize(builder.caseDefinitionId).orElse(null);
    this.processDefinitionName = normalize(builder.processDefinitionName).orElse(null);
  }

  public static Builder builder(String language) {
    return new Builder(language);
  }

  public String getLanguage() {
    return language;
  }

  public String getSource() {
    return source;
  }

  public ScriptSourceType getSourceType() {
    return sourceType;
  }

  public Optional<String> getActivityId() {
    return Optional.ofNullable(activityId);
  }

  public Optional<String> getProcessDefinitionId() {
    return Optional.ofNullable(processDefinitionId);
  }

  public Optional<String> getProcessDefinitionKey() {
    return Optional.ofNullable(processDefinitionKey);
  }

  public Optional<String> getCaseDefinitionId() {
    return Optional.ofNullable(caseDefinitionId);
  }

  public Optional<String> getProcessDefinitionName() {
    return Optional.ofNullable(processDefinitionName);
  }

  public boolean hasSource() {
    return !source.isBlank();
  }

  private static String requireNonBlank(String value, String fieldName) {
    return normalize(value)
        .orElseThrow(() -> new IllegalArgumentException(fieldName + " must not be blank"));
  }

  private static Optional<String> normalize(String value) {
    return Optional.ofNullable(value)
        .map(String::trim)
        .filter(normalized -> !normalized.isEmpty());
  }

  @Override
  public String toString() {
    return "ScriptSecurityContext{"
        + "language='" + language + '\''
        + ", sourceType=" + sourceType
        + ", activityId=" + activityId
        + ", processDefinitionKey=" + processDefinitionKey
        + ", caseDefinitionId=" + caseDefinitionId
        + ", processDefinitionId=" + processDefinitionId
        + ", processDefinitionName=" + processDefinitionName
        + '}';
  }

  public static final class Builder {

    private final String language;
    private String source;
    private ScriptSourceType sourceType;
    private String activityId;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String caseDefinitionId;
    private String processDefinitionName;

    private Builder(String language) {
      this.language = language;
    }

    public Builder source(String source) {
      this.source = source;
      return this;
    }

    public Builder sourceType(ScriptSourceType sourceType) {
      this.sourceType = sourceType;
      return this;
    }

    public Builder activityId(String activityId) {
      this.activityId = activityId;
      return this;
    }

    public Builder processDefinitionId(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
      return this;
    }

    public Builder processDefinitionKey(String processDefinitionKey) {
      this.processDefinitionKey = processDefinitionKey;
      return this;
    }

    public Builder caseDefinitionId(String caseDefinitionId) {
      this.caseDefinitionId = caseDefinitionId;
      return this;
    }

    public Builder processDefinitionName(String processDefinitionName) {
      this.processDefinitionName = processDefinitionName;
      return this;
    }

    public ScriptSecurityContext build() {
      return new ScriptSecurityContext(this);
    }
  }
}
