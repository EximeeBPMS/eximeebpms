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
package org.eximeebpms.bpm.engine.rest.history;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eximeebpms.bpm.engine.rest.dto.history.HistoryCleanupConfigurationDto;
import org.eximeebpms.bpm.engine.rest.dto.runtime.JobDto;

public interface HistoryCleanupRestService {

  public static final String PATH = "/cleanup";

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  JobDto cleanupAsync(@QueryParam("immediatelyDue") @DefaultValue("true") boolean immediatelyDue);

  @GET
  @Path("/job")
  @Produces(MediaType.APPLICATION_JSON)
  JobDto findCleanupJob();

  @GET
  @Path("/jobs")
  @Produces(MediaType.APPLICATION_JSON)
  List<JobDto> findCleanupJobs();

  @GET
  @Path("/configuration")
  @Produces(MediaType.APPLICATION_JSON)
  HistoryCleanupConfigurationDto getHistoryCleanupConfiguration();
}
