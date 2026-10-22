File log = new File(basedir, "build.log")
assert log.exists() : "build.log not found at ${log}"

String text = log.text
assert text.contains("Cannot upgrade to 1.4") :
  "Expected the guard's onFailMessage in build.log, build must have failed for a different reason:\n${text}"
assert text.contains("ACT_RU_CASE_EXECUTION") :
  "Expected the guard's precondition table name in build.log:\n${text}"

return true
