package org.eximeebpms.bpm.engine.impl.businessevent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eximeebpms.bpm.engine.impl.businessevent.variable.BusinessVariableUpdateEventEntity;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;

import java.util.Date;
import java.util.List;

public class DbBusinessEventHandler implements BusinessEventHandler {
    public static final String ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private final Gson gson = new GsonBuilder()
            .setDateFormat(ISO_DATE_TIME)
            .create();

    public void handleEvent(BusinessEvent businessEvent) {
        final DbEntityManager dbEntityManager = getDbEntityManager();
        BusinessEventOutboxEntity businessEventOutbox = BusinessEventOutboxEntity.builder()
                .createdDate(new Date())
                .eventType(businessEvent.getBusinessEventType())
                .processInstanceId(businessEvent.getProcessInstanceId())
                .rootProcessInstanceId(businessEvent.getRootProcessInstanceId())
                .processDefinitionKey(businessEvent.getProcessDefinitionKey())
                .businessEvent(gson.toJson(businessEvent))
                .build();
        if (businessEvent instanceof BusinessVariableUpdateEventEntity businessVariableUpdateEventEntity) {
            businessEventOutbox.setTaskId(businessVariableUpdateEventEntity.getTaskId());
        }
        dbEntityManager.insertWithoutId(businessEventOutbox);
    }

    public void handleEvents(List<BusinessEvent> businessEvents) {
        for (BusinessEvent businessEvent : businessEvents) {
            handleEvent(businessEvent);
        }
    }

    protected DbEntityManager getDbEntityManager() {
        return Context.getCommandContext().getDbEntityManager();
    }

}
