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
package org.eximeebpms.bpm.engine.rest.sub.repository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eximeebpms.bpm.engine.rest.dto.repository.DecisionRequirementsDefinitionXmlDto;
import org.eximeebpms.bpm.engine.rest.dto.repository.DecisionRequirementsDefinitionDto;

public interface DecisionRequirementsDefinitionResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  DecisionRequirementsDefinitionDto getDecisionRequirementsDefinition();

  @GET
  @Path("/xml")
  @Produces(MediaType.APPLICATION_JSON)
  DecisionRequirementsDefinitionXmlDto getDecisionRequirementsDefinitionDmnXml();

  @GET
  @Path("/diagram")
  Response getDecisionRequirementsDefinitionDiagram();
}
