package org.eximeebpms.bpm.engine.impl.businessevent.process;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.ExecutionListener;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProcessor;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProducer;

public class BusinessEventProcessInstanceExecutionListener implements ExecutionListener {

  public static final String START = "start";
  public static final String END = "end";
  public static final String UPDATE = "update";

  @Override
  public void notify(final DelegateExecution execution) {
    BusinessEventProcessor.processBusinessEvents(new BusinessEventProcessor.BusinessEventCreator() {
      @Override
      public BusinessEvent createBusinessEvent(BusinessEventProducer producer) {
        return BusinessEventProcessInstanceExecutionListener.this.createBusinessEvent(producer, execution);
      }
    });
  }

  protected BusinessEvent createBusinessEvent(
      BusinessEventProducer producer,
      DelegateExecution execution) {

    final String eventName = execution.getEventName();

    return switch (eventName) {
      case START -> producer.createProcessInstanceStartEvt(execution);
      case END -> producer.createProcessInstanceEndEvt(execution);
      case UPDATE -> producer.createProcessInstanceUpdateEvt(execution);
      default -> null;
    };
  }
}
