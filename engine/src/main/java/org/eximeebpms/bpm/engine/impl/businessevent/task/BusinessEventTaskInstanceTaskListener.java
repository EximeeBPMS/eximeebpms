package org.eximeebpms.bpm.engine.impl.businessevent.task;

import org.eximeebpms.bpm.engine.delegate.DelegateTask;
import org.eximeebpms.bpm.engine.delegate.TaskListener;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProcessor;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProducer;

public class BusinessEventTaskInstanceTaskListener implements TaskListener {

  @Override
  public void notify(final DelegateTask delegateTask) {
    BusinessEventProcessor.processBusinessEvents(new BusinessEventProcessor.BusinessEventCreator() {
      @Override
      public BusinessEvent createBusinessEvent(final BusinessEventProducer producer) {
        return switch (delegateTask.getEventName()) {
          case TaskListener.EVENTNAME_CREATE -> producer.createTaskInstanceCreateEvt(delegateTask);
          case TaskListener.EVENTNAME_ASSIGNMENT, TaskListener.EVENTNAME_UPDATE -> producer.createTaskInstanceUpdateEvt(delegateTask);
          case TaskListener.EVENTNAME_COMPLETE -> producer.createTaskInstanceCompleteEvt(delegateTask);
          case TaskListener.EVENTNAME_DELETE -> producer.createTaskInstanceDeleteEvt(delegateTask);
          default -> null;
        };
      }
    });
  }
}
