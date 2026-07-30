package org.eximeebpms.bpm.businessevent.kafka.plugin;

public class KafkaBusinessEventMetadata {

  public static final String TIMESTAMP = "timestamp";
  public static final String UUID = "uuid";
  public static final String TYPE = "type";
  public static final String VERSION = "version";
  public static final String ORIGIN = "origin";
  public static final String CORRELATION_ID = "correlation-id";
  public static final String PROCESS_INSTANCE_ID = "process-key";
  public static final String PROCESS_DEFINITION_KEY = "process-name";
  public static final String NO_PROCESS_CONTEXT = "no-process-context";

  private KafkaBusinessEventMetadata() {
    throw new UnsupportedOperationException("constants only");
  }
}
