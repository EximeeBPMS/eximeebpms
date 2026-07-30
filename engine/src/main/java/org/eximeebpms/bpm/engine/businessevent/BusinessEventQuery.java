package org.eximeebpms.bpm.engine.businessevent;

import org.eximeebpms.bpm.engine.query.Query;

public interface BusinessEventQuery extends Query<BusinessEventQuery, BusinessEventOutbox> {

    BusinessEventQuery processInstanceId(String processInstanceId);

    BusinessEventQuery eventType(String eventType);

}
