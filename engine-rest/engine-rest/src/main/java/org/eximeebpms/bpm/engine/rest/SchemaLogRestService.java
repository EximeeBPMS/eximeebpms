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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.eximeebpms.bpm.engine.rest.dto.SchemaLogEntryDto;
import org.eximeebpms.bpm.engine.rest.dto.SchemaLogQueryDto;

/**
 * @author Miklas Boskamp
 *
 */
@Produces(MediaType.APPLICATION_JSON)
public interface SchemaLogRestService {

  public static final String PATH = "/schema/log";

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<SchemaLogEntryDto> getSchemaLog(@Context Request request, @Context UriInfo uriInfo, @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<SchemaLogEntryDto> querySchemaLog(SchemaLogQueryDto dto, @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);
}
