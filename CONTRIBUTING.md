# How to contribute

* [File bugs or feature requests](#file-bugs-or-feature-requests)
* [Build from source](#build-from-source)
* [Create a pull request](#create-a-pull-request)
* [Contribution checklist](#contribution-checklist)
* [Commit message conventions](#commit-message-conventions)
* [Security maintenance branches](#security-maintenance-branches)
* [License headers](#license-headers)

# File bugs or feature requests

Found a bug or have a feature request? [Open a GitHub Issue](https://github.com/EximeeBPMS/eximeebpms/issues/new/choose) in this repository — that's the right place for anything coming from outside the core team.

When creating an issue:

* Give enough context so that a person who doesn't know your project can understand the request.
* Be concise — only add what's needed to understand the core of the problem.
* For bug reports: describe the steps to reproduce, specify your environment (EximeeBPMS version, modules used, database, application server).
* For feature requests: describe the expected behavior and, if possible, provide mockup code or a test case.
* Found a security vulnerability? Do **not** open a public issue — follow our [security policy](SECURITY.md) instead.

## Triage

New issues start with the `needs-triage` label. A maintainer reviews them, tags them (`bug`, `enhancement`, `question`, ...), and links the corresponding internal Jira ticket (project **BPMS**) used to schedule and track the actual fix — the GitHub issue itself is not migrated anywhere, it just stays open as the public record of the request.

Issues are closed once the linked change ships, referencing the PR or commit that resolved them. Released changes — regardless of whether they started as a GitHub issue or an internal Jira ticket — are aggregated in [CHANGELOG.md](CHANGELOG.md) for every release.

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
1. Open a pull request against `main` in [https://github.com/EximeeBPMS/eximeebpms](https://github.com/EximeeBPMS/eximeebpms).
1. Reference the Jira ticket in the PR description, if applicable — external contributors without access to the internal Jira instance can skip this (see [Commit message conventions](#commit-message-conventions)).
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

## Security / CVE remediation commits

EximeeBPMS has fixed security issues since its 1.0.0 fork from Camunda 7 —
see the `### Security` entries in `CHANGELOG.md`. What changes starting after
the 1.3.0 release is not whether we fix vulnerabilities, but how we document
it: from that point on, we cite the specific CVE identifier(s) a commit
addresses, rather than describing the fix only in generic terms.

When a commit's purpose is to remediate a published CVE (a dependency bump or
a code fix), use this format for both the commit message and the pull
request title:

```
BPMS-NNN - chore(deps): update <dependency> to fix CVE-YYYY-NNNNN
```

For a change addressing more than one CVE:

```
BPMS-NNN - chore(deps): update <dependency> to fix CVEs: CVE-YYYY-NNNNN, CVE-YYYY-MMMMM
```

With the PR number appended, as usual:

```
BPMS-421 - chore(deps): update jackson-databind to fix CVE-2026-12345 (#215)
BPMS-422 - chore(deps): update spring-boot to 3.5.11 to fix CVEs: CVE-2026-24733, CVE-2026-24734 in embedded Tomcat 10.1.50 (#216)
```

Rules:
* Applies starting after the 1.3.0 release, to **every**
  dependency/security bump commit and its PR title, whether it originates
  from Dependabot, Renovate, or manual remediation, as soon as a CVE
  identifier is known. Commits made before this convention was introduced
  are not retroactively renamed.
* If no CVE ID is assigned yet at merge time, use the ordinary
  `BPMS-NNN - chore(deps): bump <dep> from X to Y` form, and add the CVE ID to
  the commit/PR title in a follow-up once assigned.
* The corresponding `CHANGELOG.md` `### Security` entry for the release
  **must** cite the same CVE ID(s) — see the convention note at the top of
  `CHANGELOG.md`.
* As with the general commit convention, the `BPMS-NNN` prefix is optional
  for commits from external contributors or automated tools (Dependabot,
  Renovate) — external reporters and contributors don't have access to the
  internal Jira instance, so the CVE identifier itself is the publicly
  meaningful reference in that case.

# Security maintenance branches

[SECURITY.md](SECURITY.md) commits us to shipping security fixes for the
latest release and the previous minor release. Day-to-day development still
happens entirely on `main` (see [Create a pull request](#create-a-pull-request))
— a maintenance branch for the *previous* minor line only exists when it's
actually needed for a backport, not as a standing branch kept alive for
every release regardless of whether anything needs to be backported to it.

**Naming:** `release/<major>.<minor>.x`, e.g. `release/1.2.x`.

**When it's created:** on demand, the first time a security fix needs to be
backported to the previous supported minor version — not automatically the
moment a new minor version ships. A maintainer cuts it from that version's
release tag:

```bash
git fetch origin --tags
git branch release/1.2.x v1.2.0
git push origin release/1.2.x
```

If the branch already exists (an earlier fix was already backported to this
line), reuse it instead of creating a new one.

**Lifetime:** kept for as long as that version line is within the supported
window (see Supported Versions in `SECURITY.md`). Once a version falls out
of support, its maintenance branch — if one was ever created — can be
deleted; there's no obligation to keep one around if it was never needed.

**Backporting a fix:**

1. Fix and merge the issue on `main` first, targeting the current release —
   either as new work, a port of a fix already available upstream (Camunda
   7) or in a dependency's own patched release, or a port from Consdata's
   internal, non-public codebase (fixes normally flow from there into this
   public repository; a security fix reported directly here is the accepted
   exception to that direction). A patch release for this is not tied to the
   regular release cadence — it can and should go out via `release.yml` as
   soon as the fix is ready, independent of any larger feature release in
   progress.
2. `git cherry-pick` the fix commit(s) onto the relevant
   `release/<major>.<minor>.x` branch.
3. Open a pull request against that branch — same review rules as `main`
   (at least 1 approval).
4. Trigger the `Release` workflow (`release.yml`) from the GitHub Actions UI,
   selecting `release/<major>.<minor>.x` in the **"Use workflow from"** branch
   picker (instead of `main`), with `release_version` set to the next patch
   on that line (e.g. `1.2.1`) and `development_version` set to the one after
   it (e.g. `1.2.2`). No changes to `release.yml` are needed — it does not
   hardcode a branch/ref, so it operates against whichever branch it is
   dispatched from.
5. Update `CHANGELOG.md` with a `### Security` entry citing the CVE ID(s)
   under the new patch version, on both `main` (if not already fixed there)
   and the maintenance branch.

Known limitation (accepted for now): Dependabot is currently only configured
against the default branch (`main`), so it will not open automated update PRs
against `release/<major>.<minor>.x` branches. Backports are manual/cherry-pick
only. Extending automated scanning to maintenance branches is tracked
separately and is out of scope of this policy.

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
