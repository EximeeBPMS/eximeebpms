package org.eximeebpms.bpm.engine.impl;

import org.eximeebpms.bpm.engine.businessevent.BusinessEventOutbox;
import org.eximeebpms.bpm.engine.businessevent.BusinessEventQuery;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;

import java.util.List;

public class BusinessEventQueryImpl extends AbstractVariableQueryImpl<BusinessEventQuery, BusinessEventOutbox> implements BusinessEventQuery {

    protected String processInstanceId;
    protected String eventType;

    public BusinessEventQueryImpl(CommandExecutor commandExecutor) {
        super(commandExecutor);
    }

    public BusinessEventQuery processInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public BusinessEventQuery eventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getEventType() {
        return eventType;
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        checkQueryOk();
        ensureVariablesInitialized();
        return (Long) commandContext
                .getDbEntityManager()
                .selectOne("selectBusinessEventOutboxCountByQueryCriteria", this);
    }

    @Override
    public List<BusinessEventOutbox> executeList(CommandContext commandContext, Page page) {
        checkQueryOk();
        ensureVariablesInitialized();

        return commandContext
                .getBusinessEventManager()
                .findBusinessEventOutboxEntitiesByQueryCriteria(this, page);
    }
}
