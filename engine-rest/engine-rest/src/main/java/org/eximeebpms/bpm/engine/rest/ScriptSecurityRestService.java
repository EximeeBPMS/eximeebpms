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
package org.eximeebpms.bpm.engine.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import org.eximeebpms.bpm.engine.rest.dto.CountResultDto;
import org.eximeebpms.bpm.engine.rest.dto.ScriptSecurityConfigDto;
import org.eximeebpms.bpm.engine.rest.dto.ScriptViolationEventDto;

@Produces(MediaType.APPLICATION_JSON)
public interface ScriptSecurityRestService {

  String PATH = "/script-security";

  @GET
  @Path("/violations")
  @Produces(MediaType.APPLICATION_JSON)
  List<ScriptViolationEventDto> getViolations(
      @QueryParam("maxResults") Integer maxResults,
      @QueryParam("firstResult") Integer firstResult);

  @GET
  @Path("/violations/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getViolationsCount();

  @GET
  @Path("/config")
  @Produces(MediaType.APPLICATION_JSON)
  ScriptSecurityConfigDto getConfig();

  @PUT
  @Path("/config")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  ScriptSecurityConfigDto updateConfig(ScriptSecurityConfigDto dto);
}
