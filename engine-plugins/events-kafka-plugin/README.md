# EximeeBPMS Events Kafka Plugin

Kafka-backed event broadcasting plugin for EximeeBPMS.

## Configuration

| Property | Default | Description |
|---------|--------|------------|
| enabled | false | Enables event broadcasting |
| bootstrapServers | — | Kafka bootstrap servers (required when enabled=true) |
| topic | — | Kafka topic (required when enabled=true) |
| clientId | eximeebpms-events | Kafka client id |
| acks | all | Kafka producer acknowledgement mode |
| enableIdempotence | true | Enables idempotent producer |
| keyHeader | processKey | Header used as Kafka message key |
| waitForAck | false | Wait for Kafka acknowledgement (sync mode) |
| retries | 2147483647 | Number of retries performed by Kafka producer |
| retryBackoffMs | 100 | Delay between Kafka producer retry attempts in milliseconds |
| deliveryTimeoutMs | 120000 | Upper bound for total time to report success or failure |
| requestTimeoutMs | 30000 | Maximum time to wait for broker response |
| failOnPublishError | false | Fail engine transaction when publish fails |
| async | true | Publish events asynchronously |

### Error handling modes

| Mode | Configuration | Behavior |
|-----|-------------|---------|
| Best-effort (default) | waitForAck=false, failOnPublishError=false | Errors are logged only |
| Strong consistency | waitForAck=true, failOnPublishError=true | Exceptions propagate to engine (transaction may fail) |

## bpm-platform.xml

```xml

<plugin>
  <class>org.eximeebpms.bpm.events.kafka.plugin.KafkaEventBroadcastingProcessEnginePluginorg.eximeebpms.bpm.events.kafka.plugin.KafkaEventBroadcastingProcessEnginePlugin</class>
  <properties>
    <property name="enabled">true</property>
    <property name="bootstrapServers">localhost:9092</property>
    <property name="topic">bpms-events</property>

    <property name="clientId">eximeebpms-events</property>
    <property name="acks">all</property>
    <property name="enableIdempotence">true</property>
    <property name="keyHeader">processKey</property>
    <property name="waitForAck">false</property>
    <property name="retries">2147483647</property>
    <property name="retryBackoffMs">100</property>
    <property name="deliveryTimeoutMs">120000</property>
    <property name="requestTimeoutMs">30000</property>

    <property name="failOnPublishError">false</property>
    <property name="async">true</property>
  </properties>
</plugin>
```

To propagate Kafka publishing failures to the process engine transaction, configure:

- `failOnPublishError=true`
- `waitForAck=true`

This makes publishing synchronous and may increase process execution latency.

Retries are handled by the Kafka producer client using `retries`, `retryBackoffMs`,
`deliveryTimeoutMs` and `requestTimeoutMs`.
