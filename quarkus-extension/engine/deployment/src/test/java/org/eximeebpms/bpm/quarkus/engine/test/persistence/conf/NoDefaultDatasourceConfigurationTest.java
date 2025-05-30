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
package org.eximeebpms.bpm.quarkus.engine.test.persistence.conf;

import io.quarkus.test.QuarkusUnitTest;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;

import jakarta.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;

public class NoDefaultDatasourceConfigurationTest {

  @RegisterExtension
  static QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .withConfigurationResource("org/eximeebpms/bpm/quarkus/engine/test/persistence/conf/secondary-datasource-application.properties")
      .assertException(throwable -> assertThat(throwable)
          .hasMessage("No bean found for required type [interface io.agroal.api.AgroalDataSource] and qualifiers [[@jakarta.enterprise.inject.Default()]]")
          .isInstanceOf(UnsatisfiedResolutionException.class))
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

  @Inject
  protected ProcessEngine processEngine;

  @Test
  public void shouldExpectException() {
  }

}
