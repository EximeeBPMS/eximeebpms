package org.eximeebpms.bpm.engine.impl.businessevent;

import java.util.List;

/**
 * <p>The interface for implementing an business event handler.</p>
 *
 * <p>The {@link BusinessEventHandler} is responsible for consuming the event. Many different
 * implementations of this interface can be imagined. Some implementations might persist the
 * event to a database, others might persist the event to a message queue and handle it
 * asynchronously.</p>
 *
 * <p>The default implementation of this interface is {@link DbBusinessEventHandler} which
 * persists events to a database.</p>
 *
 *
 * @author Daniel Meyer
 *
 */
public interface BusinessEventHandler {

  /**
   * Called by the process engine when an business event is fired.
   *
   * @param businessEvent the {@link BusinessEvent} that is about to be fired.
   */
  public void handleEvent(BusinessEvent businessEvent);

  /**
   * Called by the process engine when an business event is fired.
   *
   * @param businessEvents the {@link BusinessEvent} that is about to be fired.
   */
  public void handleEvents(List<BusinessEvent> businessEvents);

}
