/*
 * Copyright EximeeBPMS contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.junit.Test;
import org.testcontainers.DockerClientFactory;

/**
 * MySQL's driver sends fractional seconds by default and the server rounds them, which moves a
 * timestamp into the next second when the fraction is .5 or above - so a value written as
 * 12:00:28.789 comes back as 12:00:29, and equality queries stop matching what the caller wrote.
 * The mysql profile guards against it with sendFractionalSeconds=false on the JDBC URL
 * (docs/specs/process-engine-database.md).
 *
 * <p>That guard had no test, and BPMS-696 dropped it while rewriting database.tc.params. This is
 * the test that would have caught it. MariaDB deliberately has no equivalent: its driver truncates
 * rather than rounds, verified to shift by zero seconds with and without the parameter.
 */
public class MySqlFractionalSecondsTest {

  private static final int ROUNDS_UP = 789_000_000;

  @Test
  public void shouldNotShiftATimestampIntoTheNextSecond() throws SQLException {
    assumeTrue("Docker is not available", DockerClientFactory.instance().isDockerAvailable());

    String url = "jdbc:tc:mysql:8.4:///test?sendFractionalSeconds=false";
    try (Connection connection = DriverManager.getConnection(url)) {
      try (Statement statement = connection.createStatement()) {
        statement.execute("create table fractional_seconds_probe (ts timestamp)");
      }

      Timestamp written = new Timestamp(System.currentTimeMillis());
      written.setNanos(ROUNDS_UP);
      try (PreparedStatement insert =
          connection.prepareStatement("insert into fractional_seconds_probe values (?)")) {
        insert.setTimestamp(1, written);
        insert.executeUpdate();
      }

      try (Statement statement = connection.createStatement();
          ResultSet rs = statement.executeQuery("select ts from fractional_seconds_probe")) {
        assertThat(rs.next()).isTrue();
        Timestamp stored = rs.getTimestamp(1);
        assertThat(stored.getTime() / 1000)
          .as("timestamp must stay in the second it was written in")
          .isEqualTo(written.getTime() / 1000);
      }
    }
  }
}
