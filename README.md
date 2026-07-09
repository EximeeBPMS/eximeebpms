# EximeeBPMS - The banking-grade open source BPMN platform

[![Build Status](https://github.com/EximeeBPMS/eximeebpms/actions/workflows/snapshots-on-main.yml/badge.svg?branch=main)](https://github.com/EximeeBPMS/eximeebpms/actions/workflows/snapshots-on-main.yml) [![Maven Central](https://img.shields.io/maven-central/v/org.eximeebpms.bpm/eximeebpms-bom?color=blue&logo=apachemaven)](https://central.sonatype.com/search?q=org.eximeebpms) [![eximeebpms manual latest](https://img.shields.io/badge/manual-latest-brown.svg)](https://docs.eximeebpms.org/manual/latest/) [![License](https://img.shields.io/github/license/EximeeBPMS/eximeebpms?color=blue&logo=apache)](https://github.com/EximeeBPMS/eximeebpms/blob/main/LICENSE) [![JVM](https://img.shields.io/badge/JVM-17--21-brightgreen?logo=openjdk)](https://openjdk.org/) [![Discussions](https://img.shields.io/badge/discussions-EximeeBPMS-green)](https://github.com/orgs/EximeeBPMS/discussions) [![GitHub Stars](https://img.shields.io/github/stars/EximeeBPMS/eximeebpms?style=flat)](https://github.com/EximeeBPMS/eximeebpms/stargazers)

EximeeBPMS is a native BPMN 2.0 process engine, forked from Camunda 7, hardened for **mission-critical, banking-grade process automation**. It runs inside the Java Virtual Machine, can be embedded inside any Java application or Runtime Container, and integrates with Java EE and the Spring Framework. On top of the process engine, you get a full stack of tools for human workflow management, operations, and monitoring.

**Who it's for:** teams running regulated, high-availability workloads (finance, insurance, public sector) who need a fully compatible, actively maintained Camunda 7 successor without adopting an open-core or cloud-only model.

**Migrating from Camunda 7:** EximeeBPMS keeps the REST API, database schema, and BPMN/DMN models compatible with Camunda 7. Use the [EximeeBPMS Migration Tool](https://github.com/EximeeBPMS/eximeebpms-migration) (OpenRewrite-based) to move an existing Camunda 7 project over with minimal changes.

- Web Site: https://eximeebpms.org/
- Documentation: https://docs.eximeebpms.org/
- Getting Started: https://docs.eximeebpms.org/get-started/
- Release Notes: [CHANGELOG.md](CHANGELOG.md) · [GitHub Releases](https://github.com/EximeeBPMS/eximeebpms/releases)
- Security Policy: [SECURITY.md](SECURITY.md)
- Discussions: https://github.com/orgs/EximeeBPMS/discussions
- Issue Tracker: https://github.com/EximeeBPMS/eximeebpms/issues
<!-- - Contribution Guidelines: https://eximeebpms.org/contribute/ -->

## Components

EximeeBPMS provides a rich set of components centered around the BPM lifecycle.

#### Process Implementation and Execution

- EximeeBPMS Engine - The core component responsible for executing BPMN 2.0 processes.
- REST API - The REST API provides remote access to running processes.
- Spring, CDI Integration - Programming model integration that allows developers to write Java Applications that interact with running processes.

#### Process Design

- Camunda Modeler - A [standalone desktop application](https://github.com/camunda/camunda-modeler) that allows business users and developers to design & configure processes.

#### Process Operations

- EximeeBPMS Engine - JMX and advanced Runtime Container Integration for process engine monitoring.
- EximeeBPMS Cockpit - Web application tool for process operations.
- EximeeBPMS Admin - Web application for managing users, groups, and their access permissions.

#### Human Task Management

- EximeeBPMS Tasklist - Web application for managing and completing user tasks in the context of processes.

#### And there's more...

- [bpmn.io](https://bpmn.io/) - Toolkits for BPMN, CMMN, and DMN in JavaScript (rendering, modeling)
- [Community Extensions](https://docs.eximeebpms.org/manual/latest/introduction/latest/extensions/) - Extensions on top of EximeeBPMS provided and maintained by our great open source community

## A Framework

In contrast to other vendor BPM platforms, EximeeBPMS strives to be highly integrable and embeddable. We seek to deliver a great experience to developers that want to use BPM technology in their projects.

### Highly Integrable

Out of the box, EximeeBPMS provides infrastructure-level integration with Java EE Application Servers and Servlet Containers.

### Embeddable

Most of the components that make up the platform can even be completely embedded inside an application. For instance, you can add the process engine and the REST API as a library to your application and assemble your custom BPM platform configuration.

## Ecosystem

| Project | Description |
|---------|-------------|
| [eximeebpms-docs](https://github.com/EximeeBPMS/eximeebpms-docs) | Source of the EximeeBPMS documentation site |
| [eximeebpms-migration](https://github.com/EximeeBPMS/eximeebpms-migration) | OpenRewrite-based tool to migrate a Camunda 7 project to EximeeBPMS |
| [eximeebpms-docker](https://github.com/EximeeBPMS/eximeebpms-docker) | Official Docker images for EximeeBPMS releases |
| [eximeebpms-bpm-monitor](https://github.com/EximeeBPMS/eximeebpms-bpm-monitor) | Spring Boot Actuator / Micrometer monitoring extension for EximeeBPMS |
| [eximeebpms-release-parent](https://github.com/EximeeBPMS/eximeebpms-release-parent) | Parent POM used to build and release EximeeBPMS artifacts |
| [eximeebpms-get-started-quickstart](https://github.com/EximeeBPMS/eximeebpms-get-started-quickstart) | Example project for the Quick Start guide |
| [eximeebpms-get-started-spring-boot](https://github.com/EximeeBPMS/eximeebpms-get-started-spring-boot) | Example project for the Spring Boot getting-started guide |
| [eximeebpms-get-started-dmn](https://github.com/EximeeBPMS/eximeebpms-get-started-dmn) | Example project for the DMN getting-started guide |

## Contributing

Please see our [contribution guidelines](CONTRIBUTING.md) for how to raise issues and how to contribute code to our project.

## Tests

To run the tests in this repository, please see our [testing tips and tricks](TESTING.md).


## License

The source files in this repository are made available under the [Apache License Version 2.0](./LICENSE).

EximeeBPMS uses and includes third-party dependencies published under various licenses. By downloading and using EximeeBPMS artifacts, you agree to their terms and conditions. Refer to https://docs.eximeebpms.org/manual/latest/introduction/third-party-libraries/ for an overview of third-party libraries and particularly important third-party licenses we want to make you aware of.

## Security

Please see our [security policy](SECURITY.md) for how to report security vulnerabilities.
