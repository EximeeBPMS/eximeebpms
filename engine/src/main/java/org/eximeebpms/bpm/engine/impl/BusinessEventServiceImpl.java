package org.eximeebpms.bpm.engine.impl;

import org.eximeebpms.bpm.engine.BusinessEventService;
import org.eximeebpms.bpm.engine.businessevent.BusinessEventQuery;

public class BusinessEventServiceImpl extends ServiceImpl implements BusinessEventService {

    @Override
    public BusinessEventQuery createBusinessEventOutboxQuery() {
        return new BusinessEventQueryImpl(commandExecutor);
    }
}
