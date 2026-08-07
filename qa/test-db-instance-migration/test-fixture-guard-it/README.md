Guard IT - CMMN migration fail-fast (Liquibase `<preConditions>`)
==================================================================

Verifies the Liquibase `<preConditions onFail="HALT">` guard on the "1.3-to-1.4"
changeSet (`engine/src/main/resources/org/eximeebpms/bpm/engine/db/liquibase/eximeebpms-changelog.xml`):
it must halt the migration before any DDL runs if `ACT_RU_CASE_EXECUTION` is
non-empty (an active CMMN case instance), and must not interfere otherwise.

Deliberately **not** part of the `qa/test-db-instance-migration` reactor (not
listed in that module's `<modules>`) and not inherited from any repository
parent pom: a real guard halt fails the whole Maven reactor for the module
that hits it, which would break the normal green-path build. This harness
uses `maven-invoker-plugin` instead, so each scenario runs as its own
standalone, isolated Maven build with its own expected outcome
(`invoker.properties`' `invoker.buildResult`).

CMMN support (Java API, `CaseService`) was removed from the engine in
BPMS-325 - there is no way to create a real CMMN case instance any more.
Both scenarios below seed/verify `ACT_RU_CASE_EXECUTION` directly via SQL
instead (the table's only `NOT NULL` column is `ID_`).

Run
---

    mvn -f qa/test-db-instance-migration/test-fixture-guard-it/pom.xml verify

Scenarios (`src/it/`)
---------------------

* `cmmn-active-case-halts-migration` - builds a schema up to the `1.3.0` tag,
  seeds one row in `ACT_RU_CASE_EXECUTION`, then continues to `1.4.0`.
  Expected result: **failure** - the precondition must halt before the
  `1.3-to-1.4` changeSet's `<sqlFile>` (and its `DROP TABLE` statements) run.
* `cmmn-no-active-case-migration-succeeds` - builds straight to `1.4.0` with
  `ACT_RU_CASE_EXECUTION` left empty. Expected result: **success**, and the
  CMMN tables (`ACT_RU_CASE_EXECUTION`, `ACT_RE_CASE_DEF`,
  `ACT_RU_CASE_SENTRY_PART`, `ACT_HI_CASEACTINST`, `ACT_HI_CASEINST`) must be
  gone afterwards - proving the guard doesn't interfere with the normal path.

Each scenario runs against its own throwaway H2 file database
(`target/db/process-engine` inside its own cloned IT project directory), via
the same `liquibase-maven-plugin` + `eximeebpms-changelog.xml` combination
`qa/test-db-instance-migration/test-fixture-120` uses for the real migration
path - not a reimplementation of it.

Staged changelog copy
----------------------

The parent `pom.xml`'s `stage-changelog-with-nested-upgrade` antrun execution
copies `engine/src/main/resources/.../db/liquibase/**` plus a filtered subset
of `engine/src/main/resources/.../db/upgrade/*.sql` (excluding everything
before 7.16 - the pre-Liquibase-era scripts) into
`target/db-scripts/liquibase/upgrade/` before the IT projects run. This
mirrors `engine/pom.xml`'s own `copy-upgrade-scripts-liquibase` build step
(`compile` phase) rather than depending on `engine/target/classes` possibly
being stale: Liquibase's `<sqlFile relativeToChangelogFile="true">` resolves
`upgrade/...` relative to wherever the changelog file itself is, and in the
raw source tree `upgrade/` is a *sibling* of `liquibase/`, not nested inside
it.
