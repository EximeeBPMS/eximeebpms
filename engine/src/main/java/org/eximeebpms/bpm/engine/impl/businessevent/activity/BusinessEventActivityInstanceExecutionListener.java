package org.eximeebpms.bpm.engine.impl.businessevent.activity;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.ExecutionListener;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProcessor;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProducer;

public class BusinessEventActivityInstanceExecutionListener implements ExecutionListener {

  @Override
  public void notify(final DelegateExecution execution) {
    BusinessEventProcessor.processBusinessEvents(new BusinessEventProcessor.BusinessEventCreator() {
      @Override
      public BusinessEvent createBusinessEvent(BusinessEventProducer producer) {
        return BusinessEventActivityInstanceExecutionListener.this.createBusinessEvent(producer, execution);
      }
    });
  }

  protected BusinessEvent createBusinessEvent(
      BusinessEventProducer producer,
      DelegateExecution execution) {

    final String eventName = execution.getEventName();

    return switch (eventName) {
      case ExecutionListener.EVENTNAME_START -> producer.createActivityInstanceStartEvt(execution);
      case ExecutionListener.EVENTNAME_END -> producer.createActivityInstanceEndEvt(execution);
      default -> null;
    };
  }
}
