package org.eximeebpms.bpm.engine.impl.businessevent;

import java.io.Serializable;

/**
 * A business event type.
 *
 * See {@link BusinessEventTypes} for a set of built-in events
 *
 * @author Daniel Meyer
 * @since 7.2
 */
public interface BusinessEventType extends Serializable {

  String BUSINESS_EVENT_PREFIX = "camunda7";

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
   * By default, this is a combination of the event type and the entity name, separated by a colon.
   */
  default String getBusinessEventName() {
    return BUSINESS_EVENT_PREFIX + ":" + getEntityType() + ":" + getEventName();
  }
}
