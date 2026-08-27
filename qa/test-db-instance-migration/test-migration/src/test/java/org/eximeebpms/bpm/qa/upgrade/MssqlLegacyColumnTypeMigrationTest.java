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
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * Only meaningfully exercises the fix on MSSQL, where these three columns
 * used to be the deprecated {@code image} type; on every other supported
 * database the assertion trivially holds, since they never used it.
 */
public class MssqlLegacyColumnTypeMigrationTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule("eximeebpms.cfg.xml");

  @Test
  public void formerlyImageColumnsShouldNotBeImageAfterMigration() throws Exception {
    // given
    DataSource ds = rule.getProcessEngine()
        .getProcessEngineConfiguration()
        .getDataSource();

    String[][] tableAndColumn = {
        {"ACT_ID_INFO", "PASSWORD_"},
        {"ACT_GE_BYTEARRAY", "BYTES_"},
        {"ACT_HI_COMMENT", "FULL_MSG_"}
    };

    try (Connection conn = ds.getConnection()) {
      for (String[] entry : tableAndColumn) {
        String tableName = entry[0];
        String columnName = entry[1];

        // when — compare case-insensitively: unquoted identifiers are folded
        // to uppercase by H2/Oracle/DB2 but to lowercase by PostgreSQL/MySQL
        String typeName = null;
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "%", "%")) {
          while (rs.next()) {
            if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))
                && columnName.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
              typeName = rs.getString("TYPE_NAME");
              break;
            }
          }
        }

        // then
        assertThat(typeName)
            .as("%s.%s should exist after migration", tableName, columnName)
            .isNotNull();
        assertThat(typeName)
            .as("%s.%s should no longer use the deprecated MSSQL `image` type", tableName, columnName)
            .isNotEqualToIgnoringCase("image");
      }
    }
  }
}
