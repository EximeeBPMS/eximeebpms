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
package org.eximeebpms.bpm.engine.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eximeebpms.bpm.engine.rest.dto.TelemetryConfigurationDto;
import org.eximeebpms.bpm.engine.rest.dto.telemetry.TelemetryDataDto;

@Produces(MediaType.APPLICATION_JSON)
public interface TelemetryRestService {

  String PATH = "/telemetry";

  /**
   * @deprecated The sending telemetry feature is removed.
   * Please remove the endpoint usages as they are no longer needed.
   */
  @Deprecated
  @POST
  @Path("/configuration")
  @Consumes(MediaType.APPLICATION_JSON)
  void configureTelemetry(TelemetryConfigurationDto dto);

  /**
   * @deprecated The sending telemetry feature is removed.
   * Please remove the endpoint usages as they are no longer needed.
   */
  @Deprecated
  @GET
  @Path("/configuration")
  @Produces(MediaType.APPLICATION_JSON)
  TelemetryConfigurationDto getTelemetryConfiguration();

  @GET
  @Path("/data")
  @Produces(MediaType.APPLICATION_JSON)
  TelemetryDataDto getTelemetryData();
}
