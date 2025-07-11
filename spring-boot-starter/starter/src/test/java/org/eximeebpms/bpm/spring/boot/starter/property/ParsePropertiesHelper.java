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
package org.eximeebpms.bpm.spring.boot.starter.property;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import jakarta.annotation.PostConstruct;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ParsePropertiesHelper.TestConfig.class)
public abstract class ParsePropertiesHelper {

  @EnableConfigurationProperties(CamundaBpmProperties.class)
  public static class TestConfig {
  }

  @Autowired
  protected CamundaBpmProperties properties;

  protected MetricsProperty metrics;
  protected WebappProperty webapp;
  protected RestApiProperty restApiProperty;
  protected JobExecutionProperty jobExecution;

  @PostConstruct
  public void init() {
    metrics = properties.getMetrics();
    webapp = properties.getWebapp();
    restApiProperty = properties.getRestApi();
    jobExecution = properties.getJobExecution();
  }
}
