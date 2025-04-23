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
package org.eximeebpms.bpm.qa.performance.engine.util;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eximeebpms.bpm.qa.performance.engine.framework.PerfTestException;

/**
 * @author Daniel Meyer
 *
 */
public class JsonUtil {

  private static ObjectMapper mapper;

  public static void writeObjectToFile(String filename, Object object) {

    final ObjectMapper mapper = getMapper();

    try {

      File resultFile = new File(filename);
      if(resultFile.exists()) {
        resultFile.delete();
      }
      resultFile.createNewFile();

      mapper.writerWithDefaultPrettyPrinter().writeValue(resultFile, object);

    } catch(Exception e) {
      throw new PerfTestException("Cannot write object to file "+filename, e);

    }

  }

  public static <T> T readObjectFromFile(String filename, Class<T> type) {

    final ObjectMapper mapper = getMapper();

    try {
      return mapper.readValue(new File(filename), type);

    } catch(Exception e) {
      throw new PerfTestException("Cannot read object from file "+filename, e);

    }
  }

  public static ObjectMapper getMapper() {
    if(mapper == null) {
      mapper = new ObjectMapper();
      SerializationConfig config = mapper
          .getSerializationConfig()
          .without(SerializationFeature.FAIL_ON_EMPTY_BEANS);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
      mapper.setConfig(config);

    }
    return mapper;
  }
}
