package org.eximeebpms.bpm.engine.impl.businessevent;

import java.io.Serializable;
import java.util.Optional;

import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;

/**
 * A business event type.
 *
 * See {@link BusinessEventTypes} for a set of built-in events
 *
 * @author Daniel Meyer
 * @since 7.2
 */
public interface BusinessEventType extends Serializable {

  String BUSINESS_EVENT_PREFIX = "bpms";

  /**
   * The type of the entity.
   */
  String getEntityType();

  /**
   * The name of the event fired on the entity
   */
  String getEventName();

  /**
   * The full name of the event passed to the publisher.
   * By default, this is a combination of the configured business event prefix
   * ({@link BusinessEventConfiguration#getPrefix()}, falling back to
   * {@link #BUSINESS_EVENT_PREFIX} when no engine context is available), the
   * event type and the entity name, separated by a colon.
   */
  default String getBusinessEventName() {
    String prefix = Optional.ofNullable(Context.getProcessEngineConfiguration())
        .map(ProcessEngineConfigurationImpl::getBusinessEventConfiguration)
        .map(BusinessEventConfiguration::getPrefix)
        .orElse(BUSINESS_EVENT_PREFIX);
    return prefix + ":" + getEntityType() + ":" + getEventName();
  }
}
