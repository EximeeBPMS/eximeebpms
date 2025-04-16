# EximeeBPMS Quarkus Extensions

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm.quarkus/camunda-bpm-quarkus-engine/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm.quarkus/camunda-bpm-quarkus-engine) [![camunda manual latest](https://img.shields.io/badge/manual-latest-brown.svg)](https://docs.eximeebpms.org/manual/latest/user-guide/quarkus-integration/)

This sub-project provides EximeeBPMS Quarkus Extensions that allow you to add behavior to your Quarkus 
application by adding dependencies to the classpath.

You can find the documentation on the EximeeBPMS Quarkus Extensions 
[here](https://docs.eximeebpms.org/manual/latest/user-guide/quarkus-integration/).

We also provide some useful examples at our 
[camunda-bpm-examples](https://github.com/camunda/camunda-bpm-examples/tree/master/quarkus-extension) repository.

```xml
<dependency>
  <dependency>
    <groupId>org.eximeebpms.bpm.quarkus</groupId>
    <artifactId>eximeebpms-bpm-quarkus-engine</artifactId>
    <version>${version.eximeebpms}</version><!-- place EximeeBPMS version here -->
  </dependency>
</dependency>
```

To configure a EximeeBPMS Quarkus extension, you can use an `application.properties` file. It
can look like the following:

```properties
# process engine configuration
quarkus.eximeebpms.generic-config.cmmn-enabled=false
quarkus.eximeebpms.generic-config.dmn-enabled=false
quarkus.eximeebpms.generic-config.history=none

# job executor configuration
quarkus.eximeebpms.job-executor.thread-pool.max-pool-size=12
quarkus.eximeebpms.job-executor.thread-pool.queue-size=5
quarkus.eximeebpms.job-executor.generic-config.max-jobs-per-acquisition=5
quarkus.eximeebpms.job-executor.generic-config.lock-time-in-millis=500000
quarkus.eximeebpms.job-executor.generic-config.wait-time-in-millis=7000
quarkus.eximeebpms.job-executor.generic-config.max-wait=65000
quarkus.eximeebpms.job-executor.generic-config.backoff-time-in-millis=5

# custom data source configuration and selection
quarkus.datasource.my-datasource.db-kind=h2
quarkus.datasource.my-datasource.username=eximeebpms
quarkus.datasource.my-datasource.password=eximeebpms
quarkus.datasource.my-datasource.jdbc.url=jdbc:h2:mem:eximeebpms;TRACE_LEVEL_FILE=0;DB_CLOSE_ON_EXIT=FALSE
quarkus.eximeebpms.datasource=my-datasource
```

### Local Build

#### Executing the Tests
```mvn clean install -Pquarkus-tests```


---------
#### Quarkus and JUEL bytecode incompatibilities

**Context**: JUEL was built with a different Java version. Quarkus won't pick up new build changes. For more information, check #3419 ([comment](https://github.com/camunda/camunda-bpm-platform/issues/3419#issuecomment-1720916174))

**Solution**: If you notice juel exceptions like below, delete `/juel/target` folder and run the Quarkus build again.

<details>

<summary>Stacktrace</summary>

```java
Caused by: java.lang.VerifyError: Bad type on operand stack
Exception Details:
Location:
org/eximeebpms/bpm/engine/impl/el/JuelExpressionManager.<init>(Ljava/util/Map;)V @28: putfield
Reason:
Type 'org/eximeebpms/bpm/impl/juel/ExpressionFactoryImpl' (current frame, stack[1]) is not assignable to 'org/eximeebpms/bpm/impl/juel/jakarta/el/ExpressionFactory'
Current Frame:
bci: @28
flags: { }
locals: { 'org/eximeebpms/bpm/engine/impl/el/JuelExpressionManager', 'java/util/Map' }
stack: { 'org/eximeebpms/bpm/engine/impl/el/JuelExpressionManager', 'org/eximeebpms/bpm/impl/juel/ExpressionFactoryImpl' }
Bytecode:
0000000: 2ab7 0007 2abb 000c 59b7 000e b500 0f2a
0000010: 03b5 0013 2abb 0017 59b7 0019 b500 1a2a
0000020: 2bb5 001e b1```
</details>
