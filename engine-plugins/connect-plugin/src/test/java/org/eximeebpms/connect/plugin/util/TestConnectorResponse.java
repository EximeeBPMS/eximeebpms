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
package org.eximeebpms.connect.plugin.util;

import java.util.Map;

import org.eximeebpms.connect.impl.AbstractConnectorResponse;


/**
 * @author Daniel Meyer
 *
 */
public class TestConnectorResponse extends AbstractConnectorResponse {

  private Map<String, Object> responseParameters;

  public TestConnectorResponse(Map<String, Object> responseParameters) {
    this.responseParameters = responseParameters;
  }

  protected void collectResponseParameters(Map<String, Object> arg0) {
    if(responseParameters != null) {
      arg0.putAll(responseParameters);
    }
  }

}
