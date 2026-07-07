# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

Starting with 1.4.0, every `### Security` entry must cite the CVE
identifier(s) it addresses (e.g. "Fix CVE-2026-12345 in jackson-databind"),
matching the commit message convention documented in
[CONTRIBUTING.md](CONTRIBUTING.md#security--cve-remediation-commits). Entries
from before this convention remain as originally published.

---

## [1.3.0] – 2026-07-xx Unreleased

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
