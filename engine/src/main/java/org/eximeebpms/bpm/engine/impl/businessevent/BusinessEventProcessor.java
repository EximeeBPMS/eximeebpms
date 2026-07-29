package org.eximeebpms.bpm.engine.impl.businessevent;

import org.eximeebpms.bpm.engine.impl.context.Context;

import java.util.Collections;
import java.util.List;

public class BusinessEventProcessor {
    private static final BusinessEventProducer businessEventProducer = new DefaultBusinessEventProducer();
    private static final BusinessEventHandler businessEventHandler = new DbBusinessEventHandler();

    /**
     * Process an {@link BusinessEvent} and handle them directly after creation.
     * The {@link BusinessEvent} is created with the help of the given
     * {@link BusinessEventCreator} implementation.
     *
     * @param creator the creator is used to create the {@link BusinessEvent} which should be thrown
     */
    public static void processBusinessEvents(BusinessEventCreator creator) {
        if (Context.getProcessEngineConfiguration().isBusinessEventsEnabled()) {
            BusinessEvent singleEvent = creator.createBusinessEvent(businessEventProducer);
            if (singleEvent != null) {
                businessEventHandler.handleEvent(singleEvent);
                creator.postHandleSingleBusinessEventCreated(singleEvent);
            }

            List<BusinessEvent> eventList = creator.createBusinessEvents(businessEventProducer);
            businessEventHandler.handleEvents(eventList);
        }
    }

    public static class BusinessEventCreator {

        public BusinessEvent createBusinessEvent(BusinessEventProducer producer) {
            return null;
        }

        public List<BusinessEvent> createBusinessEvents(BusinessEventProducer producer) {
            return Collections.emptyList();
        }

        public void postHandleSingleBusinessEventCreated(BusinessEvent event) {
            return;
        }
    }
}
