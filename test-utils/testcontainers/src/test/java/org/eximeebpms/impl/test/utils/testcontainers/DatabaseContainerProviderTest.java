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
package org.eximeebpms.impl.test.utils.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testcontainers.DockerClientFactory;

/**
 * Guards the `jdbc:tc:` path the `testcontainers` build profile composes.
 *
 * <p>This used to be {@code @Ignore}d outright, which is why nobody noticed that its
 * images had been pointing at a registry with no DNS record ever since the Camunda
 * rebrand. It now skips only when Docker is genuinely unavailable, so it reports
 * rather than hides (BPMS-696).
 */
@RunWith(Parameterized.class)
public class DatabaseContainerProviderTest {

  @Parameterized.Parameter(0)
  public String jdbcUrl;
  @Parameterized.Parameter(1)
  public String versionStatement;
  @Parameterized.Parameter(2)
  public String expectedVersion;

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> scenarios() {
    return Arrays.asList(new Object[][] {
      { "jdbc:tc:postgresql:18:///process-engine", "SELECT version();", "18." },
      { "jdbc:tc:mysql:8.4:///process-engine", "SELECT version();", "8.4" },
      { "jdbc:tc:mariadb:12.3:///process-engine", "SELECT version();", "12.3" },
      { "jdbc:tc:sqlserver:2025-latest:///process-engine;trustServerCertificate=true;",
        "SELECT @@VERSION", "2025" },
      { "jdbc:tc:sqlserver:2017-latest:///process-engine;trustServerCertificate=true;",
        "SELECT @@VERSION", "2017" },
      { "jdbc:tc:oracle:23-slim:///process-engine", "SELECT banner FROM v$version", "Oracle" },
      { "jdbc:tc:db2:11.5.8.0:///test", "SELECT service_level FROM TABLE(sysproc.env_get_inst_info())", "11.5" },
    });
  }

  /**
   * Every build runs the first scenario only. It is the cheapest image of the set and it
   * exercises what actually rots: the profile wiring and the jdbc:tc: URL the build composes.
   * The rest pull roughly 6 GB between them and start containers as heavy as DB2, which does
   * not belong on a pull-request build sharing a 5Gi runner - run them with
   * -Dtestcontainers.databases=all when changing an image or a provider.
   */
  private static final boolean ALL = "all".equals(System.getProperty("testcontainers.databases"));

  @Test
  public void shouldConnectThroughTestcontainersJdbcUrl() throws SQLException {
    assumeTrue("Docker is not available", DockerClientFactory.instance().isDockerAvailable());
    assumeTrue("Set -Dtestcontainers.databases=all to run every database",
      ALL || jdbcUrl.startsWith("jdbc:tc:postgresql:"));

    try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
      ResultSet rs = connection.prepareStatement(versionStatement).executeQuery();
      assertThat(rs.next()).isTrue();
      assertThat(rs.getString(1)).contains(expectedVersion);
    }
  }

}
