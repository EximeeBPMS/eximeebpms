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
package org.eximeebpms.bpm.engine.rest.impl;

import static org.eximeebpms.bpm.engine.authorization.Authorization.ANY;
import static org.eximeebpms.bpm.engine.authorization.Permissions.ALL;
import static org.eximeebpms.bpm.engine.authorization.Resources.SYSTEM;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eximeebpms.bpm.engine.ManagementService;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbAwareScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationStore;
import org.eximeebpms.bpm.engine.rest.ScriptSecurityRestService;
import org.eximeebpms.bpm.engine.rest.dto.CountResultDto;
import org.eximeebpms.bpm.engine.rest.dto.ScriptSecurityConfigDto;
import org.eximeebpms.bpm.engine.rest.dto.ScriptViolationEventDto;
import org.eximeebpms.bpm.engine.rest.exception.InvalidRequestException;

public class ScriptSecurityRestServiceImpl extends AbstractAuthorizedRestResource implements ScriptSecurityRestService {

  public ScriptSecurityRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, SYSTEM, ANY, objectMapper);
  }

  @Override
  public List<ScriptViolationEventDto> getViolations(Integer maxResults, Integer firstResult) {
    if (!isAuthorized(ALL)) {
      throw new InvalidRequestException(Status.FORBIDDEN, "Not authorized to access script security violations");
    }
    int limit = maxResults != null ? maxResults : 50;
    int skip = firstResult != null ? firstResult : 0;
    List<ScriptViolationEvent> events = getViolationStore().getRecent(skip + limit);
    return events.stream()
        .skip(skip)
        .map(ScriptViolationEventDto::fromEvent)
        .collect(Collectors.toList());
  }

  @Override
  public CountResultDto getViolationsCount() {
    if (!isAuthorized(ALL)) {
      throw new InvalidRequestException(Status.FORBIDDEN, "Not authorized to access script security violations");
    }
    return new CountResultDto(getViolationStore().getTotalCount());
  }

  @Override
  public ScriptSecurityConfigDto getConfig() {
    if (!isAuthorized(ALL)) {
      throw new InvalidRequestException(Status.FORBIDDEN, "Not authorized to access script security configuration");
    }
    Map<String, String> props = getProcessEngine().getManagementService().getProperties();
    return toDto(
        props.getOrDefault(DbAwareScriptSecurityPolicy.PROP_MODE, "ENFORCE"),
        props.getOrDefault(DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
  }

  @Override
  public ScriptSecurityConfigDto updateConfig(ScriptSecurityConfigDto dto) {
    if (!isAuthorized(ALL)) {
      throw new InvalidRequestException(Status.FORBIDDEN, "Not authorized to update script security configuration");
    }
    if (dto == null || dto.getMode() == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "mode is required");
    }
    String mode = dto.getMode().toUpperCase();
    if (!Set.of("ENFORCE", "AUDIT", "DISABLED").contains(mode)) {
      throw new InvalidRequestException(Status.BAD_REQUEST,
          "Invalid mode '" + dto.getMode() + "'. Allowed values: ENFORCE, AUDIT, DISABLED");
    }
    Set<String> keys = dto.getAllowlistedKeys() != null ? dto.getAllowlistedKeys() : Set.of();

    ManagementService management = getProcessEngine().getManagementService();
    management.setProperty(DbAwareScriptSecurityPolicy.PROP_MODE, mode);
    management.setProperty(DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, String.join(",", keys));

    ProcessEngineConfigurationImpl config =
        (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    ScriptSecurityPolicy policy = config.getScriptSecurityPolicy();
    if (policy instanceof DbAwareScriptSecurityPolicy dbAware) {
      dbAware.invalidateCache();
    }

    return toDto(mode, String.join(",", keys));
  }

  private ScriptSecurityConfigDto toDto(String mode, String allowlistRaw) {
    ScriptSecurityConfigDto result = new ScriptSecurityConfigDto();
    result.setMode(mode);
    result.setAllowlistedKeys(allowlistRaw.isBlank()
        ? Set.of()
        : Arrays.stream(allowlistRaw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toUnmodifiableSet()));
    return result;
  }

  private ScriptViolationStore getViolationStore() {
    return ((ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration())
        .getScriptViolationStore();
  }
}
