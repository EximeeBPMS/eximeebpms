import java.sql.DriverManager

File dbDir = new File(basedir, "target/db")
assert dbDir.exists() : "expected a database under ${dbDir}, migration must not have run"

// IFEXISTS=TRUE: without it, a missing .mv.db file makes H2 silently create a fresh
// empty database instead of failing the connection - the table-absence asserts below
// would then trivially pass against an empty DB even if the migration never ran.
String url = "jdbc:h2:file:" + new File(basedir, "target/db/process-engine").getAbsolutePath() + ";IFEXISTS=TRUE"
Class.forName("org.h2.Driver")
def conn = DriverManager.getConnection(url, "sa", "")
try {
  def meta = conn.getMetaData()
  ["ACT_RU_CASE_EXECUTION", "ACT_RE_CASE_DEF", "ACT_RU_CASE_SENTRY_PART", "ACT_HI_CASEACTINST", "ACT_HI_CASEINST"].each { table ->
    def rs = meta.getTables(null, null, table, null)
    assert !rs.next() : "expected ${table} to be dropped by the 1.3-to-1.4 migration, but it still exists"
    rs.close()
  }
} finally {
  conn.close()
}

return true
