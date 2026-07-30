package org.eximeebpms.bpm.engine.impl.businessevent;

import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublishResult;
import org.eximeebpms.bpm.commons.eventbus.BusinessEventPublisher;
import org.eximeebpms.bpm.commons.eventbus.Event;

@Slf4j
public class NoopBusinessEventPublisher implements BusinessEventPublisher {

  public static final String NAME = "noop";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public BusinessEventPublishResult publish(Event event) {
    log.debug("Business event ignored by noop publisher [metadata={}]", event.metadata());
    return BusinessEventPublishResult.success();
  }
}
