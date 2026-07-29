package org.eximeebpms.bpm.engine;

import org.eximeebpms.bpm.engine.businessevent.BusinessEventQuery;

public interface BusinessEventService {

    BusinessEventQuery createBusinessEventOutboxQuery();

}
