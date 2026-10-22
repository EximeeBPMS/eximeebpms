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
package org.eximeebpms.bpm.engine.impl.cfg;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.Test;

/**
 * The engine resolves its SQL dialect from {@code DatabaseMetaData.getDatabaseProductName()},
 * and an unmapped name is fatal - {@code initDatabaseType} fails with "couldn't deduct database
 * type from database product name". These assertions pin the names that must keep resolving, so
 * a supported database cannot silently drop out of the table.
 */
public class DatabaseTypeMappingTest {

  protected Properties mappings = ProcessEngineConfigurationImpl.getDefaultDatabaseTypeMappings();

  @Test
  public void shouldMapSupportedProductNamesToTheirDialect() {
    assertThat(mappings.getProperty("H2")).isEqualTo("h2");
    assertThat(mappings.getProperty("MySQL")).isEqualTo("mysql");
    assertThat(mappings.getProperty("Oracle")).isEqualTo("oracle");
    assertThat(mappings.getProperty("PostgreSQL")).isEqualTo("postgres");
    assertThat(mappings.getProperty("Microsoft SQL Server")).isEqualTo("mssql");
    assertThat(mappings.getProperty("DB2")).isEqualTo("db2");
  }

  /**
   * MariaDB runs on the MySQL dialect - all seven mysql create scripts apply to MariaDB 11.4
   * unchanged. The distinction that matters is the driver, not the server: MySQL Connector/J
   * against a MariaDB server reports "MySQL" and has always resolved, while MariaDB Connector/J
   * reports "MariaDB", which went unmapped and failed engine startup outright.
   */
  @Test
  public void shouldMapMariaDbOntoTheMySqlDialect() {
    assertThat(mappings.getProperty("MariaDB")).isEqualTo("mysql");
  }
}
