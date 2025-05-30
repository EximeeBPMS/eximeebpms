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
package org.eximeebpms.bpm.engine.rest.impl;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.eximeebpms.bpm.engine.BadUserRequestException;
import org.eximeebpms.bpm.engine.batch.Batch;
import org.eximeebpms.bpm.engine.history.HistoricProcessInstanceQuery;
import org.eximeebpms.bpm.engine.rest.ModificationRestService;
import org.eximeebpms.bpm.engine.rest.dto.ModificationDto;
import org.eximeebpms.bpm.engine.rest.dto.batch.BatchDto;
import org.eximeebpms.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.eximeebpms.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.eximeebpms.bpm.engine.rest.exception.InvalidRequestException;
import org.eximeebpms.bpm.engine.runtime.ModificationBuilder;
import org.eximeebpms.bpm.engine.runtime.ProcessInstanceQuery;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ModificationRestServiceImpl extends AbstractRestProcessEngineAware implements ModificationRestService {

  public ModificationRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public void executeModification(ModificationDto modificationExecutionDto) {
     try {
       createModificationBuilder(modificationExecutionDto).execute();
     } catch (BadUserRequestException e) {
       throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
     }
  }

  @Override
  public BatchDto executeModificationAsync(ModificationDto modificationExecutionDto) {
    Batch batch = null;
    try {
      batch = createModificationBuilder(modificationExecutionDto).executeAsync();
    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
    return BatchDto.fromBatch(batch);
  }

  private ModificationBuilder createModificationBuilder(ModificationDto dto) {
    ModificationBuilder builder = getProcessEngine().getRuntimeService().createModification(dto.getProcessDefinitionId());

    if (dto.getInstructions() != null && !dto.getInstructions().isEmpty()) {
      dto.applyTo(builder, getProcessEngine(), objectMapper);
    }

    List<String> processInstanceIds = dto.getProcessInstanceIds();
    builder.processInstanceIds(processInstanceIds);

    ProcessInstanceQueryDto processInstanceQueryDto = dto.getProcessInstanceQuery();
    if (processInstanceQueryDto != null) {
      ProcessInstanceQuery processInstanceQuery = processInstanceQueryDto.toQuery(getProcessEngine());
      builder.processInstanceQuery(processInstanceQuery);
    }

    HistoricProcessInstanceQueryDto historicProcessInstanceQueryDto = dto.getHistoricProcessInstanceQuery();
    if(historicProcessInstanceQueryDto != null) {
      HistoricProcessInstanceQuery historicProcessInstanceQuery = historicProcessInstanceQueryDto.toQuery(getProcessEngine());
      builder.historicProcessInstanceQuery(historicProcessInstanceQuery);
    }

    if (dto.isSkipCustomListeners()) {
      builder.skipCustomListeners();
    }

    if (dto.isSkipIoMappings()) {
      builder.skipIoMappings();
    }

    if (dto.getAnnotation() != null) {
      builder.setAnnotation(dto.getAnnotation());
    }

    return builder;
  }
  
}
