package org.eximeebpms.bpm.engine.impl.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eximeebpms.bpm.engine.businessevent.BusinessEventOutbox;
import org.eximeebpms.bpm.engine.impl.db.DbEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessEventOutboxEntity implements DbEntity, BusinessEventOutbox {
    /**
     * Sequential primary key that defines the guaranteed delivery order.
     * The relay must process records strictly in ascending {@code id} order
     * and must NOT skip a failing record — delivery of subsequent records
     * is blocked until this one succeeds.
     */
    private Long id;

    /** When the outbox record was created */
    private Date createdDate;

    /** Serialized event payload (JSON) */
    private String businessEvent;

    /** Event type */
    private String eventType;

    /** Source process instance correlation key */
    private String processInstanceId;

    /** Root process instance id (top-level process, even for call activities) */
    private String rootProcessInstanceId;

    /** Process definition */
    private String processDefinitionKey;

    /** Task id correlation key */
    private String taskId;

    /** Whether this outbox record has been relayed successfully */
    private boolean processed;

    /** When the record was successfully relayed; null until processed */
    private Date processedDate;

    @Override
    public String getId() {
        return id == null ? null : id.toString();
    }

    @Override
    public void setId(String id) {
        if (id == null) {
            this.id = null;
        }
        this.id = Long.valueOf(id);
    }

    @Override
    public Object getPersistentState() {
        Map<String, Object> state = new HashMap<>();
        state.put("processed", processed);
        state.put("processedDate", processedDate);
        return state;
    }

    public Long getIdAsLong() {
        return id;
    }
}
