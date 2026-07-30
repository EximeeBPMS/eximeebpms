package org.eximeebpms.bpm.engine.impl.persistence.entity;

import org.eximeebpms.bpm.engine.businessevent.BusinessEventOutbox;
import org.eximeebpms.bpm.engine.impl.BusinessEventQueryImpl;
import org.eximeebpms.bpm.engine.impl.Page;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.db.ListQueryParameterObject;
import org.eximeebpms.bpm.engine.impl.persistence.AbstractManager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessEventManager extends AbstractManager {

    protected void configureQuery(BusinessEventQueryImpl query) {
        getTenantManager().configureQuery(query);
    }

    protected ListQueryParameterObject configureParameterizedQuery(Object parameter) {
        return getTenantManager().configureQuery(parameter);
    }

    public void delete(BusinessEventOutbox businessEventOutbox) {
        getDbEntityManager().delete(BusinessEventOutboxEntity.class, "deleteBusinessEventOutbox", businessEventOutbox.getIdAsLong());
    }

    @SuppressWarnings("unchecked")
    public List<BusinessEventOutbox> findBusinessEventOutboxEntitiesByQueryCriteria(BusinessEventQueryImpl businessEventQuery, Page page) {
        configureQuery(businessEventQuery);
        return getDbEntityManager().selectList("selectBusinessEventOutboxEntityByQueryCriteria", businessEventQuery, page);
    }

    public void deleteByProcessInstanceId(String processInstanceId) {
        if (isBusinessEventUsed()) {
            getDbEntityManager().delete(BusinessEventOutboxEntity.class,
                    "deleteBusinessEventsByProcessInstanceId", processInstanceId);
        }
    }

    public void deleteByTaskId(String id) {
        if (isBusinessEventUsed()) {
            getDbEntityManager().delete(BusinessEventOutboxEntity.class,
                    "deleteBusinessEventsByTaskId", id);
        }
    }

    /**
     * Returns the next batch of unprocessed outbox records ordered by {@code ID_} ascending.
     * Called exclusively by {@link org.eximeebpms.bpm.engine.businessevent.BusinessEventDispatcher}.
     *
     * @param batchSize maximum number of records to return
     */
    @SuppressWarnings("unchecked")
    public List<BusinessEventOutboxEntity> findUnprocessedEventsForDispatch(int batchSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("processed", false);
        return getDbEntityManager().selectList(
                "selectUnprocessedBusinessEventsForDispatch",
                params,
                new Page(0, batchSize));
    }

    /**
     * Marks the given outbox record as processed and records the timestamp.
     * Called by the relay after a successful publish.
     *
     * @param entity the entity obtained from {@link #findUnprocessedEventsForDispatch}
     */
    public void markAsProcessed(BusinessEventOutboxEntity entity) {
        getDbSqlSession().executeUpdate(
                "updateMarkBusinessEventProcessed",
                Map.of("processed", true,
                        "processedDate", new Date(),
                "idAsLong", entity.getIdAsLong()));
        getDbSqlSession().flush();
    }

    /**
     * Deletes all processed outbox records whose {@code processedDate} is older than the given cutoff.
     * Called periodically by the business-event outbox cleanup job.
     *
     * @param cutoffDate records processed before this date will be deleted
     */
    public void deleteProcessedOlderThan(Date cutoffDate) {
        getDbEntityManager().delete(BusinessEventOutboxEntity.class,
                "deleteProcessedBusinessEventsOlderThan", cutoffDate);
    }

    private boolean isBusinessEventUsed() {
        return Context.getProcessEngineConfiguration().isBusinessEventsEnabled();
    }
}
