package org.eximeebpms.bpm.events.publisher;

import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.events.EventEnvelope;

/**
 * Event publisher that writes events to application logs.
 *
 * <p>Useful for local testing, diagnostics and verifying event broadcasting
 * wiring before configuring a real transport such as Kafka.</p>
 */
@Slf4j
public class LoggingEventPublisher implements EventPublisher {

  @Override
  public void publish(EventEnvelope event) {
    log.info(
        "Published event [id={}, type={}, source={}, processInstanceId={}, rootProcessInstanceId={}]",
        event.getId(),
        event.getType(),
        event.getSource(),
        event.getProcessInstanceId(),
        event.getRootProcessInstanceId()
    );

    log.debug("Published event payload: {}", event.getPayload());
  }
}
