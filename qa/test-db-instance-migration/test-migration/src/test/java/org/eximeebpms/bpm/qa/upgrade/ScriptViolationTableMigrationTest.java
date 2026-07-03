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
package org.eximeebpms.bpm.qa.upgrade;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

public class ScriptViolationTableMigrationTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule("eximeebpms.cfg.xml");

  @Test
  public void scriptViolationTableShouldExistAfterMigration() throws Exception {
    // given
    DataSource ds = rule.getProcessEngine()
        .getProcessEngineConfiguration()
        .getDataSource();

    // when
    try (Connection conn = ds.getConnection()) {
      DatabaseMetaData meta = conn.getMetaData();
      String tableName = "ACT_RU_SCRIPT_VIOLATION";

      // then — try exact name first; Oracle/DB2 may uppercase table names
      try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
        if (rs.next()) {
          return;
        }
      }
      try (ResultSet rs = meta.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
        assertThat(rs.next())
            .as("Table ACT_RU_SCRIPT_VIOLATION should exist after migration")
            .isTrue();
      }
    }
  }
}
