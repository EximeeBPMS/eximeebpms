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
package org.eximeebpms.connect.soap.httpclient;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.http.client.methods.HttpPost;
import org.eximeebpms.connect.Connectors;
import org.eximeebpms.connect.httpclient.soap.SoapHttpConnector;
import org.eximeebpms.connect.httpclient.soap.impl.SoapHttpConnectorImpl;
import org.eximeebpms.connect.impl.DebugRequestInterceptor;
import org.eximeebpms.connect.spi.Connector;
import org.junit.Before;
import org.junit.Test;

public class SoapHttpConnectorTest {

  public SoapHttpConnector connector;

  @Before
  public void createConnector() {
    connector = new SoapHttpConnectorImpl();
  }

  @Test
  public void shouldDiscoverConnector() {
    Connector soap = Connectors.getConnector(SoapHttpConnector.ID);
    assertThat(soap).isNotNull();
  }

  @Test
  public void shouldCreateHttpPostRequestByDefault() {
    DebugRequestInterceptor interceptor = new DebugRequestInterceptor(false);
    connector.addRequestInterceptor(interceptor);
    connector.createRequest().url("http://camunda.org").payload("test").soapAction("as").execute();

    Object target = interceptor.getTarget();
    assertThat(target).isInstanceOf(HttpPost.class);
  }

}
