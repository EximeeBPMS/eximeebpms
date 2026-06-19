# How to contribute

* [File bugs or feature requests](#file-bugs-or-feature-requests)
* [Build from source](#build-from-source)
* [Create a pull request](#create-a-pull-request)
* [Contribution checklist](#contribution-checklist)
* [Commit message conventions](#commit-message-conventions)
* [License headers](#license-headers)

# File bugs or feature requests

Issues are tracked in the internal Jira instance at [https://jira.consdata.pl](https://jira.consdata.pl), project key **BPMS**.

When creating an issue:

* Give enough context so that a person who doesn't know your project can understand the request.
* Be concise — only add what's needed to understand the core of the problem.
* For bug reports: describe the steps to reproduce, specify your environment (EximeeBPMS version, modules used, database, application server).
* For feature requests: describe the expected behavior and, if possible, provide mockup code or a test case.

# Build from source

## Prerequisites

See the [Prerequisites](README.md#prerequisites) section in the root README.

## Maven settings

Copy `settings/maven/nexus-settings.xml` to `~/.m2/settings.xml` and fill in your Nexus credentials. This gives Maven access to the internal artifact repository where `eximeebpms-enterprise-release-parent` and other internal artifacts are hosted.

## Building

```bash
# Full build
./mvnw clean install

# Without tests
./mvnw clean install -DskipTests

# Without frontend (when Node.js is not available locally)
./mvnw clean install -pl '!webapps,!webapps/assembly,!webapps/assembly-jakarta'

# Single module
./mvnw clean install -f engine/pom.xml
```

## Integration tests

Integration tests live in `qa/` and are not part of the default build:

```bash
./mvnw clean install -f qa/pom.xml -Pengine-integration,tomcat,h2
./mvnw clean install -f qa/pom.xml -Pengine-integration,tomcat,postgresql
```

See [CLAUDE.md](CLAUDE.md) for the full test matrix and CI script details.

# Create a pull request

1. Create a feature branch from `main`. Use the naming convention `BPMS-NNN-short-description` (e.g. `BPMS-332-fix-chrome-dependencies`).
1. Implement your change and go through the [contribution checklist](#contribution-checklist).
1. Open a pull request against `main` in [https://github.com/EximeeBPMS/eximeebpms-enterprise](https://github.com/EximeeBPMS/eximeebpms-enterprise).
1. Reference the Jira ticket in the PR description.
1. At least **1 approval** from a team member is required to merge.

There are no long-lived release or hotfix branches — all contributions go through `main`.

# Contribution checklist

Before requesting a review:

1. **Code style** — is your code formatted according to the project style?
   * 2 spaces, no tabs; max line length 120 characters.
   * Import the formatter profile (`settings/eclipse/formatter.xml` or `settings/intellij-idea/formatter.xml`) into your IDE.
   * Alternatively, run `./mvnw clean install -Plicense-header-check` to auto-format license headers.
1. **Tests** — is your change covered by unit tests? Look at existing tests in the same module for conventions.
1. **Commit messages** — do they follow the [commit message conventions](#commit-message-conventions)?
1. **License headers** — do new files carry the correct [license header](#license-headers)?

# Commit message conventions

Format:

```
BPMS-NNN - <description>
```

For changes merged via a pull request, the PR number is appended:

```
BPMS-NNN - <description> (#PR)
```

Examples:

```
BPMS-332 - fix Chrome dependencies for Ubuntu 24.04 in CI
BPMS-167 - bump spring-boot & spring-framework (#112)
```

Rules:
* Use the `BPMS-NNN` prefix for all changes tracked in Jira.
* The description should be concise and written in imperative form (e.g. *fix*, *add*, *bump* — not *fixed*, *added*).
* Commits without a Jira prefix are acceptable for minor changes coming from upstream or external contributors.

# License headers

Every `.java` source file must carry the Apache 2.0 license header.

**Files inherited from upstream Camunda** keep the original `Camunda Services GmbH` header — this attribution is required by the Apache 2.0 license and must not be removed.

**New files created specifically for EximeeBPMS** carry the following header:

```
Copyright EximeeBPMS contributors
...and/or licensed to EximeeBPMS contributors under one or more
contributor license agreements. See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
Licensed under the Apache License, Version 2.0.
You may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

To auto-fix missing or malformed headers:

```bash
./mvnw clean install -Plicense-header-check
```

If you use the IDE settings from `settings/`, the header is inserted automatically when you create a new `.java` file.
