# Security Policy

EximeeBPMS is an open-source BPMN 2.0 process automation platform, forked
from Camunda 7 and maintained by [Consdata](https://www.consdata.com/). It is
used to run business-critical, and in some cases regulated, workloads, so we
treat vulnerability handling as a first-class concern.

## Supported Versions

Starting with the **1.4.0** release, we provide security fixes for the
latest released minor version and the previous minor version, maintained via
a short-lived maintenance branch (see
[CONTRIBUTING.md](CONTRIBUTING.md#security-maintenance-branches)). Until
1.4.0 ships, the maintenance-branch mechanism is not yet in place, so only
the latest released version is supported:

| Version | Supported                          |
| ------- | ----------------------------------- |
| 1.3.x   | Yes (latest, until 1.4.0 ships)    |
| ≤ 1.2.x | No                                  |

From 1.4.0 onward:

| Version | Supported                 |
| ------- | -------------------------- |
| 1.4.x   | Yes                         |
| 1.3.x   | Yes (security fixes only)  |
| ≤ 1.2.x | No                          |

When a later minor version ships, the support window shifts again: the
previous "latest" becomes the "previous minor" and the version before that
falls out of support.

## Reporting a Vulnerability

**Please do not report security vulnerabilities through public GitHub
issues, discussions, or pull requests.**

Report privately using GitHub's private vulnerability reporting feature:

**[github.com/EximeeBPMS/eximeebpms/security/advisories/new](https://github.com/EximeeBPMS/eximeebpms/security/advisories/new)**

This opens a draft security advisory visible only to the maintainers, so the
report is not disclosed publicly until we are ready.

When reporting, please include as much of the following as you can:

- The affected component/module and version(s) (e.g. engine, REST API,
  Cockpit/Admin/Tasklist webapps, `eximeebpms-run` / Spring Boot starter,
  Tomcat/WildFly distributions)
- A description of the vulnerability and its impact
- Steps to reproduce, or a proof of concept
- Any known workaround
- Whether the issue is already public, or has been reported against upstream
  Camunda 7

## Our Process

1. **Acknowledgment** — We acknowledge new reports within **3 business
   days**.
2. **Triage & assessment** — We assess impact and severity (using CVSS v3.1
   as a guide) and determine which supported version(s) are affected.
3. **Fix development** — We develop and test a fix privately, tied to the
   draft advisory, targeting the current release and, where applicable, the
   previous supported minor version (see Supported Versions). This may be
   new work, or porting a fix that already exists upstream (Camunda 7), in a
   dependency's own patched release, or elsewhere in Consdata's broader
   EximeeBPMS product line.
4. **Release & disclosure** — We publish a dedicated patch release as soon
   as the fix is ready — independent of our regular feature-release schedule
   — and publish the corresponding GitHub Security Advisory, requesting a
   CVE identifier where warranted, at the same time or shortly after.
5. **Credit** — With your permission, we credit you by name in the advisory.

We will keep you informed of progress along the way. If a fix for the
underlying issue becomes public elsewhere before we have released our own
fix — for example, already patched upstream or in a dependency's release —
we treat that as effectively public knowledge and prioritize our release
accordingly, ahead of the timelines above if needed.

## Coordinated Disclosure

Please give us the opportunity to investigate and address a report before
disclosing it publicly. Do not disclose details of a suspected vulnerability
in public issue trackers, mailing lists, or social media until a fix has
shipped and we have published the corresponding advisory. We aim to resolve
and disclose confirmed vulnerabilities within 90 days of confirmation, and
will coordinate a mutually agreeable disclosure date with you if that is not
achievable. If the underlying fix becomes public elsewhere before then (for
example, already patched upstream or in a dependency release), the effective
disclosure timeline is shortened accordingly — please let us know if you
become aware of this, so we can prioritize our release.

## Scope

**In scope:**

- The EximeeBPMS engine, REST API, and web applications (Cockpit, Admin,
  Tasklist) as published from this repository
- EximeeBPMS distributions built from this repository: `eximeebpms-run` /
  Spring Boot starter, Tomcat and WildFly distributions
- Vulnerabilities in code that EximeeBPMS carries or has modified relative to
  upstream Camunda 7 — including issues already fixed upstream, since fixes
  are not inherited automatically

**Out of scope:**

- Denial of service caused purely by traffic/request volume (rate limiting is
  a deployment/infrastructure concern, not a code vulnerability)
- Vulnerabilities that exist only in a third-party dependency with no
  demonstrated exploit path through EximeeBPMS itself (please also report
  these to the dependency's maintainers; we track and remediate known
  dependency CVEs through routine version bumps — see `CHANGELOG.md`)
- Vulnerabilities that require local, already-privileged access to the host
  or database running EximeeBPMS
- Social engineering or phishing directed at Consdata staff, contributors, or
  the community
- Issues affecting only versions outside the Supported Versions table above
- The upstream Camunda Modeler desktop application (a separate project, not
  distributed from this repository)
- Reports generated purely by automated scanners with no accompanying
  assessment of exploitability in the context of EximeeBPMS

## Legal Notice

EximeeBPMS is maintained by Consdata sp. z o.o. as an open-source project.
The response times stated above are a good-faith operational commitment, not
a contractual Service Level Agreement. Customers with a separate commercial
support agreement covering EximeeBPMS with Consdata are governed by the terms
of that agreement where they differ from this policy.

We will not pursue legal action against researchers who make a good-faith
effort to comply with this policy, avoid privacy violations and service
disruption, and report findings promptly and privately as described above.
