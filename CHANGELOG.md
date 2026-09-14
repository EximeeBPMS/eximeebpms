# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

EximeeBPMS (and the upstream Camunda 7 codebase it started from) has
addressed security issues throughout its history — see the `### Security`
entries below in earlier releases. Starting after the 1.3.0 release, every
`### Security` entry must additionally cite the CVE identifier(s) it
addresses (e.g. "Fix CVE-2026-12345 in jackson-databind"), matching the
commit message convention documented in
[CONTRIBUTING.md](CONTRIBUTING.md#security--cve-remediation-commits). This is
a change to how fixes are documented, not a change to whether they're made —
entries from before this convention remain as originally published, without
retroactively added CVE IDs.

---

## [Unreleased]

### Breaking changes
- **CMMN support is removed** from the engine, the migration tooling and the test suite: case definitions no longer deploy, and the `1.3-to-1.4` schema migration drops the CMMN history and definition tables. **Migration:** complete or terminate every active case instance before upgrading — the migration refuses to make any schema change while rows remain in `ACT_RU_CASE_EXECUTION` (see Fixed). Once that guard passes, CMMN history and deployed case definitions are dropped unconditionally, so export anything worth keeping first.
- **The javax (legacy) namespace is dropped** — only Jakarta artifacts are built and published. **Migration:** recompile against the `jakarta.*` APIs and replace every `javax`-targeted artifact with its Jakarta counterpart; there is no javax-compatible build of this release.
- **The Tomcat 9 and WildFly 26 distributions are removed.** **Migration:** upgrade the container first — a Tomcat 9 or WildFly 26 installation has no distribution to deploy onto in this release.
- **The standalone Tomcat distribution moves from Tomcat 10.1 to Tomcat 11.0.25** — a Servlet 6.1 / Jakarta EE 11 container, up from Servlet 6.0 / Jakarta EE 10. 1.3.0 shipped Tomcat 10.1.50; the container generation changed in a grouped Dependabot bump (#168, 2026-08-09, to 11.0.24), with 11.0.25 following as a patch. This affects the `eximeebpms-bpm-tomcat` distribution — a web application archive deployed to your own, separately managed Tomcat is not forced onto Tomcat 11 by this change. **Migration:** review any `server.xml`/`context.xml` carried over from a 10.1 installation against Tomcat 11, and check that servlet filters and container-provided libraries in your own deployment are Servlet 6.1 compatible.
- **The shaded JUEL artifact is removed.** **Migration:** depend on the unshaded artifact instead; the shaded coordinates are no longer published.
- **`camunda:caseRef` on a call activity is now rejected at deploy time** instead of failing when the process instance is started: `BpmnParse` reports `The attribute 'caseRef' is no longer supported: calling a CMMN case from a call activity was removed together with CMMN support.` **Migration:** remove the attribute from every call activity that still carries it and redeploy those process definitions before upgrading — a model that keeps it will now fail deployment rather than deploy and break later.
- Replaced the deprecated MSSQL `image` data type with `varbinary(max)` for `ACT_ID_INFO.PASSWORD_`, `ACT_GE_BYTEARRAY.BYTES_`, and `ACT_HI_COMMENT.FULL_MSG_` (carried in the `1.3-to-1.4` upgrade script) — Microsoft has deprecated `image`/`text`/`ntext` and plans to remove them in a future SQL Server version. New installs get `varbinary(max)` directly via the updated create/Liquibase-baseline scripts. **Migration:** the upgrade's `ALTER COLUMN` on `ACT_GE_BYTEARRAY.BYTES_` rewrites the whole column and can take a while / hold a table-level lock on installations with a large deployment/variable-byte-array table — plan this upgrade during a maintenance window if that table is large.
- The telemetry/diagnostics payload's serialized field `camunda-integration` is renamed to `eximeebpms-integration`, and the product name it reports is `EximeeBPMS BPM Runtime`. **Migration:** anything parsing collected diagnostics data — a dashboard, a SIEM pipeline, an inventory job — must be updated; the old key is no longer emitted. The OpenAPI specification and the REST API reference rendered from it document the new name as well.
- The engine's boolean `scriptSecurityEnabled` configuration is replaced by the three-valued `scriptSecurityMode` (`ENFORCE` — the default, `AUDIT`, `DISABLED`). `isScriptSecurityEnabled()`/`setScriptSecurityEnabled(boolean)` on `ProcessEngineConfigurationImpl` are gone. **Migration:** use `setScriptSecurityMode(String)`, with `isScriptSecurityDisabled()`/`isScriptSecurityAuditMode()` to query it — `setScriptSecurityEnabled(false)` becomes `setScriptSecurityMode("DISABLED")`. The property is settable from `bpm-platform.xml` as well, and an unrecognized value now fails engine startup instead of being silently ignored. The Spring Boot property `eximeebpms.bpm.script-security.mode` keeps its name and its accepted values; only the Java enum behind it moved out of `ScriptSecurityProperty` into the engine, as `ScriptSecurityMode`.
- The Spring Boot property `eximeebpms.bpm.script-security.cleanup-cron` is removed, along with the `@Scheduled` bean behind it. Violation cleanup is the engine job described under Added, so it no longer requires `@EnableScheduling` on the application and no longer honours a cron expression — it runs on a fixed 24-hour interval, still gated on `eximeebpms.bpm.script-security.retention-days` being positive. **Migration:** applications that only customized the schedule can drop the property; applications that relied on a different cleanup frequency no longer have that knob.
- The Spring Boot starter's `eximeebpms.bpm.admin-user.*` bootstrap now fails startup with an explicit error when `admin-user.id` is set alongside an external identity provider — either an OAuth2/OIDC client registration (`spring.security.oauth2.client.registration.*`) or any read-only identity provider, such as the LDAP plugin. Previously the first case silently created a local admin account behind SSO's back, and the second crashed with an opaque `ProcessEngineException` about a missing `WritableIdentityProvider` session factory. **Migration:** the new opt-in property `eximeebpms.bpm.admin-user.allow-with-external-identity-provider` (default `false`) restores the old behaviour deliberately: with a writable provider the account is created as before, and with a read-only provider creation is skipped with a `WARN` instead, since writing to it is impossible either way.
### Added
- Native business events with transactional outbox
- JDK 25 compatibility
- Extract Script Guard's rule set into a standalone module (`commons/script-guard-rules`, `org.eximeebpms.commons:eximeebpms-commons-script-guard-rules`) with no process-engine runtime dependency, so external applications can validate BPMN scripts/expressions against the same rules before deploying a process definition
- Script security now behaves the same on plain-XML/container deployments (Tomcat, WildFly, `bpm-platform.xml`) as it already did under Spring Boot: violations are persisted to `ACT_RU_SCRIPT_VIOLATION`, forwarded to the business-event outbox, and the configured mode and allowlist are seeded into `ACT_GE_PROPERTY` on first start — so the script-security REST API's hot policy reload works on those deployments too, instead of the policy being fixed at startup.
- Retention cleanup of recorded script violations is now an engine-native, self-rescheduling job: it is bootstrapped whenever `scriptViolationRetentionDays` (Spring Boot: `eximeebpms.bpm.script-security.retention-days`) is positive, deletes violations older than that, and reschedules itself 24 hours ahead. It runs on every deployment model, not just Spring Boot.
- New `historyExcludedProcessDefinitionKeys` configuration (Spring Boot: `eximeebpms.bpm.history-excluded-process-definition-keys`; `ProcessEngineConfiguration.setHistoryExcludedProcessDefinitionKeys(Set<String>)`) lets operators list process definition keys for which no history is recorded, regardless of the configured history level. Filters at persistence time; `HistoricBatchEntity` history is never covered, since a batch is never scoped to a single process definition.
### Deprecated
- Every `camunda`-named public identifier on the BPMN and DMN Model APIs now has an `EximeeBpms`-named counterpart, and the `camunda`-named one is deprecated for removal in **1.5.0**. Affected: 80 fluent-builder methods across 19 `Abstract*Builder` classes (`camundaAsyncBefore()` → `eximeeBpmsAsyncBefore()`, `camundaClass()` → `eximeeBpmsClass()`, …), 147 accessors on the 34 interfaces under `org.eximeebpms.bpm.model.bpmn.instance` (`getCamundaFormKey()` → `getEximeeBpmsFormKey()`, …), and 6 accessors on `org.eximeebpms.bpm.model.dmn.instance.Decision`/`InputClause` — 405 declarations in all once overloads are counted, every one of them annotated `@Deprecated(forRemoval = true)`. The deprecated name is a delegating alias; on the model interfaces it is a `default` method, so no external implementer of a model interface is broken. This completes a rebrand that had previously renamed the extension-element *types* (`CamundaExecutionListener` → `EximeeBpmsExecutionListener`) but not their accessors, which is why `UserTask` until now shipped `getEximeeBpmsFormRef()` alongside `getCamundaFormKey()`.

  Purely additive: no member is removed or changed in this release, no signature moves, and existing code keeps compiling and behaving identically for the whole of 1.4.x.

  **The XML wire format is unchanged.** The extension namespace URI stays `http://camunda.org/schema/1.0/bpmn` (and `…/1.0/dmn`), attribute local names stay (`formKey`, `async`, `class`, …), and no `.bpmn` or `.dmn` file needs editing — the parser resolves extension attributes by namespace URI, never by Java identifier or XML prefix. Only Java names change.

  Migration is a mechanical rename: `camundaX` → `eximeeBpmsX`, `getCamundaX` → `getEximeeBpmsX`. Two members that were already deprecated get counterparts that are themselves born deprecated, so a bulk rename still compiles rather than silently shedding the warning: `camundaAsync()`/`camundaAsync(boolean)` (use `eximeeBpmsAsyncBefore()`/`eximeeBpmsAsyncAfter()`) and `Decision.getCamundaHistoryTimeToLive(Integer)` with its setter (use the `String` variant).
### Removed
- Remove unused Camunda 7.2.0 reference
- The `enabledEventTypes` property of the business events engine plugin (`<property name="enabledEventTypes">` in `bpm-platform.xml`, standalone deployments only — it was never exposed through the Spring Boot starter or the Quarkus extension) is removed. It was parsed and stored, but nothing in the produce → outbox-write → dispatch pipeline ever read it, so setting it had no effect; no other filtering mechanism replaces it.
- `UuidV1Generator`, the legacy UUID v1 id generator kept as a deprecated fallback since 1.3.0, is removed. Configuring `uuid-v1` — `id-generator` in `bpm-platform.xml`, `eximeebpms.bpm.id-generator` in the Spring Boot starter, `quarkus.camunda.id-generator` in the Quarkus extension, or the WildFly subsystem's equivalent — no longer fails: the engine falls back to the default `StrongUuidGenerator` (UUID v7) and logs a warning (`EnginePersistenceLogger`, code `111`). Leftover `uuid-v1` configuration therefore keeps the engine starting, but new ids are UUID v7; existing UUID v1 ids stay valid and untouched.
### Changed
- Split SQL migration scripts between version 1.3 and 1.4; make SonarQube scan non-blocking
- Add `engine-plugins` to `check-engine` profile
- Batch job configuration byte arrays (`AbstractBatchJobHandler.saveConfiguration`, `ProcessSetRemovalTimeResultHandler.saveConfiguration`) are now written with a name (`batch.jobConfiguration`), a resource type and a creation timestamp, instead of leaving all three null. They were the only rows in `ACT_GE_BYTEARRAY` that could not be attributed to a mechanism or dated, and they are referenced only from the polymorphic `ACT_RU_JOB.HANDLER_CFG_` column, which cannot carry a foreign key — so this is the only way to account for them when auditing the table. Additive: no schema change, no behaviour change, and the existing reclaim path (`JobEntity#delete()` → `onDelete`) is untouched. Applies to new rows only.
- `PropertyHelper` (used to apply `bpm-platform.xml` `<property>` values via reflection) now supports comma-separated `Set<String>`/`List<String>` setter targets, not just `int`/`long`/`float`/`boolean`/`String`. As a side effect, `adminGroups`, `adminUsers`, and `registeredDeployments` — previously unset-able via plain XML property syntax — are now configurable that way too.
- Bump versions:
  - Gson: `2.8.9` → `2.14.0`
  - Jackson: `2.15.2` → `2.22.2`
  - Mockito: `5.10.0` → `5.23.0`
  - AssertJ: `3.27.6` → `3.27.7`
  - Groovy: `5.0.4` → `5.0.8`
  - Jakarta EL API: `4.0.0` → `6.0.1`
  - Jakarta XML Bind API: `4.0.2` → `4.0.5`
  - Quarkus: `3.28.4` → `3.36.1`
  - Spring Boot: `4.0.3` → `4.1.1`
  - Spring Framework: `7.0.5` → `7.0.9`
  - Tomcat: `10.1.50` → `11.0.25` — a container-generation change, described under Breaking changes above
  - `tomcat-embed-core`/`tomcat-embed-el`/`tomcat-embed-websocket`: `11.0.24` → `11.0.25`, matching the standalone Tomcat distribution. Spring Boot's own dependency management pins them at 11.0.24 and pulls them in transitively through `spring-boot-starter-web`, so the Spring Boot starter and the `run` distribution now override them explicitly — otherwise a Spring Boot deployment ran a different Tomcat patch release than the standalone one.
  - Tomcat JDBC / Tomcat Juli: `7.0.33` → `11.0.25` (a hardcoded literal in `qa/performance-tests-engine/pom.xml`, not a `${version.tomcat}` reference — it stays in sync only because Dependabot's grouped "tomcat" bump touches this file too; a manual `version.tomcat` change would not)
  - WildFly: `37.0.0.Final` → `41.0.1.Final`
  - WildFly Core: `29.0.0.Final` → `33.0.1.Final`
  - WildFly Arquillian container adapters: `5.0.1.Final` → `5.1.0.Final`
  - Arquillian BOM: `1.1.10.Final` → `1.10.2.Final`
  - ShrinkWrap Resolvers: `3.3.4` → `3.3.7`
  - Maven Dependency Plugin: `2.8` → `3.11.0`
  - eximeebpms-monitor: `1.2.0` → `1.7.0`
  - H2: `2.3.232` → `2.4.240`
  - Liquibase: `5.0.1` → `5.0.3`
  - MySQL Connector/J: `8.3.0` → `9.7.0`
  - Oracle JDBC (ojdbc11): `23.5.0.24.07` → `23.26.2.0.0`
  - PostgreSQL JDBC: `42.5.5` → `42.7.12`
  - Microsoft SQL Server JDBC: `8.4.1.jre8` → `13.4.0.jre11`
  - Kafka Clients: `3.9.2` (new in this release, with the business-events outbox)
  - RESTEasy (engine-rest-jakarta): `6.2.1.Final` → `6.2.3.Final`
  - RESTEasy (assembly-jakarta / webapps): `6.2.8.Final` → `6.2.16.Final`
  - Netty: `4.1.89.Final` → `4.1.137.Final`
  - Apache Ant: `1.7.1` → `1.10.17`
  - Java UUID Generator: `5.1.0` → `5.2.0`
  - testcontainers: `1.16.0` → `2.0.5` (test scope; not previously listed)
  - OpenTelemetry: `1.66.0` (test scope, new pin; pulled in by `selenium-remote-driver`'s tracing instrumentation in `qa/integration-tests-webapps`)
  - SLF4J: `1.7.26` → **`2.0.19`** — a generation change on the logging facade, not a patch bump. SLF4J 2.0 discovers its binding through the `ServiceLoader` mechanism instead of 1.7's `StaticLoggerBinder`, so an application that supplies its own logging backend must supply one built for 2.0 (Logback 1.3+, log4j-slf4j2-impl, and so on). A 1.7-era binding is not an error at startup — SLF4J reports that no providers were found and every log call becomes a no-op, which is easy to miss. Affects the artifacts that expose `slf4j-api` at compile scope: `eximeebpms-commons-logging`, the Run distribution's core module, and the business-events Kafka engine plugin. A Spring Boot application is unaffected in practice, since Spring Boot 4's own dependency management already puts it on the 2.x line.
### Fixed
- The business-event type names for variable instances were corrected from `created`/`updated`/`deleted` to `create`/`update`/`delete` (`BusinessEventTypes.VARIABLE_INSTANCE_CREATE`/`_UPDATE`/`_DELETE`), so they use the same tense as every other event type. Not a breaking change: business events are new in this release, so no previously published event name changed.
- Add a daily schedule fallback (with a delay-tolerant gate) to `dependency-submission.yml`, so manifests untouched by a recent push still get their dependency graph refreshed, matching the fix applied to the same workflow in `eximeebpms-enterprise`
- Jython & BOM fixes; integration test fixes
- Fix invalid `X-Authorized-User` header value for OAuth users with non-ASCII characters (#62)
- Fix flaky `ExclusiveJobAcquisitionTest`; fix flaky integration tests; remove redundant `DROP INDEX` in business-event drop scripts; fix integration test failures in CI matrix (wildfly+webapps+alldb)
- Fix remaining CI and integration test issues (engine startup, ShrinkWrap CI resolution, `HistoryCleanupTest` timezone, CMMN migration leftovers, LoginIT fixes)
- Refactor instance-migration test fixtures to EximeeBPMS versioning
- Wait for tasks to finish before stopping `ExecutorRunner`
- Fix `LoginIT` timeout error; add extended logging
- Integration test fixes after SLF4J bump
- Fix integration test databases
- Various CI/workflow stability fixes (self-hosted runner migration, Dependabot concurrency, Slack notifications, build speed-ups)
- Fix `update-sbom.yml` retriggering itself in an infinite loop — its own commit message (`chore(deps): update SBOM`) matched the workflow's own push trigger, so every successful run re-triggered another, hammering the self-hosted runner pool non-stop for days
- The `1.3-to-1.4` migration (CMMN removal) now halts before making any schema changes if active CMMN case instances exist in `ACT_RU_CASE_EXECUTION`, via a Liquibase `<preConditions onFail="HALT">` guard on the changeSet. CMMN history and deployed definitions are still dropped unconditionally whenever the migration proceeds — this guard protects active runtime instances only, not history.
- A history event's binary payload was written to `ACT_GE_BYTEARRAY` by the history event *producer*, before any `HistoryEventHandler` had decided whether the corresponding `ACT_HI_*` row would exist. A handler that declined to persist the event therefore left an unreferenced row behind — nothing pointed at it, its `REMOVAL_TIME_` was null so no cleanup sweep could reach it, and no query could tell it apart from live data. Affected job logs (`job.exceptionByteArray`), external-task logs (`historicExternalTaskLog.exceptionByteArray`) and object-typed DMN decision inputs/outputs. Historic variable updates were already immune, because `DbHistoryEventHandler` creates their byte array itself; that placement now applies to all four. This is reachable through the documented `HistoryEventHandler` extension point, so it affected any filtering handler — including this engine's own `historyExcludedProcessDefinitionKeys`. **Contract note for custom handlers:** a handler that persists these events through its own path, rather than delegating to `DbHistoryEventHandler`, now receives the payload on the event (`getExceptionStacktraceBytes()`, `getErrorDetailsBytes()`, `materializeValue()`) with the byte-array id unset, and must create the byte array itself if it wants the payload stored.
- `HistoricBatchEntity` declared its own `protected String id`, shadowing the identically named field on its superclass `HistoryEvent`. Neither `getId()` nor `setId()` was overridden, so every code path — persistence included — always used `HistoryEvent`'s field and the subclass one was dead. Getter-based serialization (Jackson, as used by the REST API) never noticed, but a reflective field-based serializer walking the class hierarchy — Gson in particular — saw two fields named `id` and failed with `IllegalArgumentException: ... declares multiple JSON fields named 'id'`, making batch history events unserializable for any custom `HistoryEventHandler` built on one. The shadowing field is removed; `getId()`/`setId()` behave exactly as before. Inherited from upstream Camunda, not specific to this fork.
### Security
- Resolve CVE-2023-35116 via jackson-databind 2.21.3 upgrade (see Security Notice EXBPMS-7)
- Fix Jython vulnerability (CVE-2016-4000, see Security Notice EXBPMS-8)
- Resolve 9 CVEs via Spring Framework and Tomcat upgrades (Security Notices EXBPMS-9, EXBPMS-10): CVE-2026-22735, CVE-2026-22737, CVE-2026-22740, CVE-2026-22741 and CVE-2026-22745 in Spring Framework; CVE-2026-24733, CVE-2026-24734, CVE-2026-29129 and CVE-2026-29145 in Tomcat and Tomcat Native. This release ships Spring Framework 7.0.9 and Tomcat 11.0.25, both past the versions in which the fixes first landed.
- Resolve 4 CVEs in Netty and Apache Ant (Security Notice EXBPMS-11): CVE-2024-29025 — Netty's `HttpPostRequestDecoder` driven to unbounded memory allocation by a crafted multipart/chunked POST; CVE-2021-36373 and CVE-2021-36374 — Apache Ant allocating unbounded memory while reading a crafted tar or zip-derived archive during the build; CVE-2020-1945 — Apache Ant leaking build information through the predictable default system temp directory. This release ships Netty 4.1.137.Final and Apache Ant 1.10.17.
- Bumped `httpclient5`/`httpcore5` to 5.6.4/5.4.3, `testcontainers` to 2.0.5, `unirest-java` to 3.14.5 (`RestIT`/`DateSerializationIT` updated for its 3.x `kong.unirest.json.*` response types, replacing `org.json.*`), the QA Spring Boot runtime module's `h2` to 2.4.240, and `netty` to 4.1.137.Final (forced via an explicit `netty-bom` import in `engine-rest-jakarta`, ahead of the older version `resteasy-netty4` bundles transitively) — closing the Critical/High-severity Dependabot alerts against these dependencies. `webapps` module excluded from this pass. Covered by Security Notice EXBPMS-12, which lists the 23 CVEs closed across Apache HttpComponents Core and Netty: CVE-2026-33870, CVE-2026-42584, CVE-2026-42587, CVE-2026-44249, CVE-2026-44891, CVE-2026-45416, CVE-2026-45674, CVE-2026-47691, CVE-2026-50010, CVE-2026-54399, CVE-2026-54428, CVE-2026-55831, CVE-2026-55833, CVE-2026-55851, CVE-2026-56745, CVE-2026-56817, CVE-2026-56819, CVE-2026-56820, CVE-2026-56821, CVE-2026-56822, CVE-2026-59901, CVE-2026-59902, CVE-2026-73507.
- Closed the remaining non-`webapps` Critical/High Dependabot alerts: `httpcore5`/`httpcore5-h2` still resolved to the older 5.2.4 in `distro/run/qa/*` (the `spring-boot-dependencies` BOM imported in `distro/run/pom.xml` was nearer than `parent/pom.xml`'s own pin, so it won); `plexus-utils` (pulled in by `wildfly-subsystem-test-framework` via `maven-resolver-provider`) bumped to 3.6.1; `xalan` (pulled in by `jboss-jstl-api_1.2_spec` via `jboss-javaee-6.0`) bumped to 2.7.3 — the existing exclusion for it in `qa/integration-tests-webapps/pom.xml` never worked (wrong groupId: `org.apache.xalan` instead of `xalan`). Same advisory scope as the entry above (Security Notice EXBPMS-12).
- Migrated `wiremock` (test-only, `connect/http-client`, `connect/soap-http-client`, `engine`, `engine-rest/engine-rest-openapi`, `qa/test-old-engine`) from 2.27.2 (`com.github.tomakehurst:wiremock`) to 3.13.2 (`org.wiremock:wiremock`), closing the Critical/High `jackson-core`/`jackson-databind`/`jetty-server`/`jetty-webapp` alerts it bundled transitively. No test source changes needed — the `com.github.tomakehurst.wiremock.*` Java package is unchanged in 3.x, only the Maven coordinates moved. Two follow-on fixes were needed: `handlebars`, still bundled at the vulnerable 4.3.1 even in 3.13.2, is now forced to 4.5.4 via an explicit `dependencyManagement` override in `parent/pom.xml`; and `json-path`, excluded from wiremock's own transitive tree everywhere it's used (kept at the project's pinned 2.9.0 instead) but not replaced by anything, is now added back explicitly — wiremock 3.x's `WireMockServer` constructor loads it eagerly (2.x didn't), so its prior absence only surfaced now as `NoClassDefFoundError`. The Jetty half of this work is covered by Security Notice EXBPMS-13 (CVE-2026-10050); the jackson-core/jackson-databind alerts it also closed were Dependabot findings against test-scope transitives with no EximeeBPMS security notice of their own.
- Closed the remaining 69 Maven Dependabot alerts, all of them wiremock-, json-path-, kafka-clients- or maven-resolver-pinned transitives with no local override. `jackson-core`/`jackson-databind` (`connect/http-client`, `connect/soap-http-client`, `engine`) and `json-smart` (`connect/http-client`, `connect/soap-http-client`) still resolved the bundled 2.20.1/2.5.0 despite the project's `${version.jackson}`/`${version.json-smart}` properties already sitting at patched values — those properties were never wired into a `dependencyManagement` entry for these artifacts in this part of the reactor; forcing `jackson-databind` also requires forcing `jackson-annotations` and `jackson-datatype-jsr310` to the same minor, or `ObjectMapper`'s static initializer fails with `NoClassDefFoundError`. WireMock moved to its officially published Jetty-12/EE10 drop-in (`org.wiremock:wiremock-jetty12`), because the broadest affected Jetty advisory has no fix on the 11.x line WireMock bundled and a plain version override is not viable across Jetty 12's `ee8`/`ee9`/`ee10` module restructuring; its own Jetty pin is forced past CVE-2026-1605 and CVE-2026-10050 (GHSA-2fvj-hgj9-j2gr) to 12.0.39 via a `jetty-bom`/`jetty-ee10-bom` import. That pin stays deliberately separate from the 12.1.12 used by `webapps`: aligning the two broke `engine-rest-openapi`'s WireMock stub matching, where Jetty 12.1.x normalizes the request `Content-Type` charset parameter's casing differently before it reaches the matcher. Also forced `guava` 33.7.1-jre and `httpclient` 4.5.14 in `distro/wildfly` (test/provided-scope `maven-resolver` transitives), `lz4-java` 1.11.2 across every `kafka-clients` consumer, and `opentelemetry` 1.65.0 in `qa/integration-tests-webapps` (pulled in by `selenium-remote-driver`'s tracing instrumentation).
- Migrated `webapps` off the EOL Jetty 11 line, which had no remaining OSS patch for CVE-2026-10050: its `jetty-webapp` test dependency and the `jetty-maven-plugin` local-development server both move to the `org.eclipse.jetty.ee10` artifacts at 12.1.12. `WebAppContext.setResourceBase(String)` is gone in Jetty 12 (`setBaseResourceAsString(String)` replaces it), the session-cookie context parameter moved to `org.eclipse.jetty.session.SessionCookie`, and the documented local-dev command is now the fully-qualified `org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:run` goal. Running that server end to end also surfaced two unrelated pre-existing gaps in the `develop` profile, fixed here: `gson` had to be promoted out of `provided` scope alongside the other Jakarta APIs, and the demo process `pa5/call-activities-with-references.bpmn` still contained a `camunda:caseRef` call activity that the engine now rejects at deploy time after the CMMN removal.
### Documentation
- Update documentation and branding for EximeeBPMS

---

## [1.3.0] – 2026-07-17

### Added
- Multithreaded task handling via `ThreadPoolExecutor`; Task execution statistics; Graceful shutdown: unlock pending tasks on stop; SPI listener for external task execution stats
- Introduce script security policy with runtime and BPMN parse enforcement
- Add audit mode and violation store to script security; script security REST API with hot-reload policy and run distribution support; DB-backed script security policy with violation tracking; `ScriptViolationListener` and SIEM integration via `BusinessEventPublisher`; script violation OpenAPI spec and Spring Boot listener wiring; REST integration tests

### Changed
- Make OAuth2 endpoints configurable; respect webapp path
- Introduce UUID v7 as default `StrongUuidGenerator`; time-based UUID v1 kept as deprecated legacy fallback

### Fixed
- Fix task query `OR` for candidate user and candidate group

---

## [1.2.0] – 2026-03-10

### Changed
- Bump Spring Boot and Spring Framework:
  - Spring Boot: `3.5.6` → `4.0.3`
  - Spring Framework: `5.3.39 / 6.2.11` → `7.0.5`
  - REST Assured: `5.5.6` → `6.0.0`
  - JUnit Jupiter: → `6.0.3`
  - Jakarta Servlet API: → `6.1.0`
- Update libraries:
  - Hibernate: `5.6.5.Final` → `7.2.0.Final`
  - Jakarta Persistence API: `3.1.0` → `3.2.0`
  - AssertJ: `2.9.1` → `3.27.6`
  - Logback Classic: `1.2.11` → `1.2.13`
  - Selenium Java: `4.10.0` → `4.39.0`
  - ShrinkWrap Resolvers: `2.2.7` → `3.3.4`
  - Tomcat 10: `10.1.43` → `10.1.50`
  - Tomcat 9: `9.0.107` → `9.0.113`
  - Jetty: `9.4.57.v20241219` → `11.0.26`
  - Maven Surefire Plugin: `2.22.2` → `3.5.5`
  - Cargo Maven Plugin: `1.10.20` → `1.10.26`

---

## [1.1.2] – 2025-10-25

### Fixed
- Fix EximeeBPMS dependency paths
- Fix snapshot release workflow

---

## [1.1.1] – 2025-10-24

### Changed
- Bump versions:
  - Liquibase: `4.8.0` → `5.0.1`
  - REST Assured: `4.5.0` → `5.5.6`
  - Quarkus: `3.27.0` → `3.28.4`
  - Spring Boot: `3.5.5` → `3.5.6`
  - Spring Framework: `6.2.10` → `6.2.11`
  - simple-jndi: `0.24.0` → `0.25.0`
  - commons-fileupload: `1.5` → `1.6.0`

### Security
- Fix critical and high vulnerabilities

---

## [1.1.0] – 2025-10-19

### Added
- Add WildFly 27 support (POM adjustments, remove unsupported env)
- Fix WildFly package paths for serve and release; fix release workflow file path and missing secret
- Metrics exposed via EximeeBPMS Monitor (`eximeebpms-run`)
- JaCoCo plugin for test coverage reports
- Sonar integration in Maven build; bump libs; rename run classes; add build profile
- Publish snapshot versions to Sonatype; add snapshot release workflow
- Add deployment for WildFly & Run distributions
- Include `distro-tomcat` and `distro-jboss` in `full` profile
- QA module: restrict to `integration-test` profile; `dep-qa` test scope only

### Changed
- Sync upstream Camunda changes (June 2024)
- Sync upstream Camunda changes up to v7.24.0-alpha1
- Exclude Tomcat from default build
- Exclude WildFly from default build
- Remove migration integration tests related to old Camunda versions
- Remove Camunda forum badge from README
- Fix `distro-ce` profile in release workflow
- Bump versions:
  - Apache HttpComponents: `4.5.10` → `5.3.1`
  - Apache HttpClient 5: `5.4.1` → `5.5`
  - Jersey JSON: `1.15` → `1.19.4`
  - Undertow Servlet: `2.3.0.Final` → `2.3.18.Final`
  - Commons Lang 3: `3.9` → `3.18.0`
  - gson-fire: `1.8.3` → `1.9.0`
  - Gson: `2.8.9` → `2.12.1`
  - JUnit 4: `4.13.1` → `4.13.2`
  - OkHttp: `3.14.2` → `4.12.0`
  - OpenAPI Generator Maven Plugin: `4.2.3` → `7.12.0`
  - Swagger Annotations: `1.5.22` → `1.6.14`
  - Bouncy Castle: `1.47` → `1.70`
  - json-smart: `2.5.0` → `2.5.2`
  - Quarkus: `3.20.0` → `3.27.0`
  - ShrinkWrap Resolvers: `2.2.2` → `2.2.7`
  - Spring Boot: `3.4.4` → `3.5.5`
  - Spring Framework: `6.2.4` → `6.2.10`
  - Tomcat 10: `10.1.36` → `10.1.43`
  - Tomcat 9: `9.0.100` → `9.0.107`
  - WildFly: `35.0.0.Final` → `37.0.0.Final`
  - WildFly Core test framework: `27.0.0.Final` → `29.0.0.Final`
  - FEEL Scala engine: `1.19.1` → `1.19.3`
  - Joda-Time: `2.12.5` → `2.14.0`
  - MyBatis: `3.5.15` → `3.5.19`
  - Java UUID Generator: `4.3.0` → `5.1.0`
  - Maven Javadoc Plugin: `3.3.1` → `3.11.2`
  - Jetty: `9.4.31.v20200723` → `9.4.57.v20241219`
  - Guava: `28.2-jre` → `33.4.6-jre`
  - XMLUnit Core: `2.6.2` → `2.10.1`
  - wsdl4j: `1.6.2` → `1.6.3`
  - xml-apis: `1.4.01` → `2.0.2`
  - Cargo Maven Plugin: `1.10.16` → `1.10.20`
  - Build Helper Maven Plugin: `1.9.1` → `3.6.0`

### Fixed
- Fix implicit narrowing conversion in compound assignment
- Fix integration tests and integration tests workflow
- Fix release and build workflow permissions
- Fix Slack workflow permissions
- Fix deployment configuration printout
- Fix setting version in `license-book`
- Fix wrong namespace (multiple iterations); fix custom m2 catalog
- Fix snapshot deployments module list; fix snapshot release token
- DEVOPS-896 – Fix Maven wrapper invocation

### Security
- Bump jersey-json to address security vulnerability
- Potential fix for code scanning alert: incomplete string escaping/encoding

---

## [1.0.0] – 2025-04-16

Initial release of EximeeBPMS — a BPMN 2.0 engine and ecosystem, forked from Camunda Platform 7.23.0.
