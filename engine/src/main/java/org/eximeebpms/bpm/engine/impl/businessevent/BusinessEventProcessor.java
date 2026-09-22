package org.eximeebpms.bpm.engine.impl.businessevent;

import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;

import java.util.ArrayList;
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
     * <p>Events whose type is excluded by the configured
     * {@code enabledEventTypes}/{@code disabledEventTypes} filter are dropped. A creator that
     * declares its type up front (see {@link BusinessEventCreator#getDeclaredType()}) is not even
     * asked to build its event, which is what makes disabling a high-volume type actually save
     * work rather than only save an outbox row.</p>
     *
     * @param creator the creator is used to create the {@link BusinessEvent} which should be thrown
     */
    public static void processBusinessEvents(BusinessEventCreator creator) {
        ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();

        if (configuration == null || !configuration.isBusinessEventsEnabled()) {
            return;
        }

        BusinessEventTypeFilter typeFilter = configuration.getBusinessEventTypeFilter();

        BusinessEventType declaredType = creator.getDeclaredType();
        if (declaredType != null && !typeFilter.isEnabled(declaredType)) {
            return;
        }

        BusinessEvent singleEvent = creator.createBusinessEvent(businessEventProducer);
        if (singleEvent != null && typeFilter.isEnabled(singleEvent.getBusinessEventType())) {
            businessEventHandler.handleEvent(singleEvent);
            creator.postHandleSingleBusinessEventCreated(singleEvent);
        }

        List<BusinessEvent> eventList = retainEnabled(typeFilter, creator.createBusinessEvents(businessEventProducer));
        businessEventHandler.handleEvents(eventList);
    }

    protected static List<BusinessEvent> retainEnabled(BusinessEventTypeFilter typeFilter, List<BusinessEvent> events) {
        if (typeFilter.isAllEnabled() || events == null || events.isEmpty()) {
            return events == null ? Collections.emptyList() : events;
        }

        List<BusinessEvent> enabled = new ArrayList<>(events.size());
        for (BusinessEvent event : events) {
            if (event != null && typeFilter.isEnabled(event.getBusinessEventType())) {
                enabled.add(event);
            }
        }

        return enabled;
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

        /**
         * The type this creator produces, when it is known before the event is built.
         *
         * <p>Returning it lets {@link #processBusinessEvents(BusinessEventCreator)} skip the
         * creation entirely when that type is disabled — worth doing on hot paths, where building
         * an event costs entity lookups. The default is {@code null}, meaning "not declared": the
         * event is built and then filtered on its own type, which is always correct and is the
         * right answer for a creator whose type is only settled by runtime state.</p>
         */
        public BusinessEventType getDeclaredType() {
            return null;
        }
    }
}
