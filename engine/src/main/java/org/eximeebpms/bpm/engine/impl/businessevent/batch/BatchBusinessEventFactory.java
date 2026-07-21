package org.eximeebpms.bpm.engine.impl.businessevent.batch;

import org.eximeebpms.bpm.engine.batch.Batch;
import org.eximeebpms.bpm.engine.impl.batch.BatchEntity;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventFactorySupport;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.util.ClockUtil;

public class BatchBusinessEventFactory extends BusinessEventFactorySupport {
    public BatchBusinessEvent createStartEvent(Batch batch) {
        return createEvent((BatchEntity) batch, BusinessEventTypes.BATCH_START);
    }

    public BatchBusinessEvent createEndEvent(Batch batch) {
        return createEvent((BatchEntity) batch, BusinessEventTypes.BATCH_END);
    }

    public BatchBusinessEvent createUpdateEvent(Batch batch) {
        return createEvent((BatchEntity) batch, BusinessEventTypes.BATCH_UPDATE);
    }

    protected BatchBusinessEvent createEvent(BatchEntity batch, BusinessEventTypes eventType) {
        BatchBusinessEvent event = new BatchBusinessEvent();

        event.setId(batch.getId());
        event.setType(batch.getType());
        event.setTotalJobs(batch.getTotalJobs());
        event.setBatchJobsPerSeed(batch.getBatchJobsPerSeed());
        event.setInvocationsPerBatchJob(batch.getInvocationsPerBatchJob());
        event.setSeedJobDefinitionId(batch.getSeedJobDefinitionId());
        event.setMonitorJobDefinitionId(batch.getMonitorJobDefinitionId());
        event.setBatchJobDefinitionId(batch.getBatchJobDefinitionId());
        event.setTenantId(batch.getTenantId());
        event.setEventType(eventType.getEventName());
        event.setBusinessEventType(eventType.getBusinessEventName());

        if (BusinessEventTypes.BATCH_START.equals(eventType)) {
            event.setStartTime(batch.getStartTime());
            event.setCreateUserId(Context.getCommandContext().getAuthenticatedUserId());
        }

        if (BusinessEventTypes.BATCH_END.equals(eventType)) {
            event.setEndTime(ClockUtil.getCurrentTime());
        }

        if (BusinessEventTypes.BATCH_UPDATE.equals(eventType)) {
            event.setExecutionStartTime(batch.getExecutionStartTime());
        }

        return event;
    }
}
