package org.eximeebpms.bpm.engine.impl.businessevent;

import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.runtime.Incident;

public abstract class BusinessEventFactorySupport {

  protected void fillProcessDefinitionData(BusinessEvent event, ExecutionEntity execution) {
    String processDefinitionId = execution.getProcessDefinitionId();

    if (processDefinitionId != null) {
      fillProcessDefinitionData(event, processDefinitionId);
    } else {
      event.setProcessDefinitionId(execution.getProcessDefinitionId());
      event.setProcessDefinitionKey(execution.getProcessDefinitionKey());
    }
  }

  protected void fillProcessDefinitionData(BusinessEvent event, Incident incident) {
    String processDefinitionId = incident.getProcessDefinitionId();

    if (processDefinitionId != null) {
      fillProcessDefinitionData(event, processDefinitionId);
    } else {
      event.setProcessDefinitionId(incident.getProcessDefinitionId());
    }
  }

  protected void fillProcessDefinitionData(BusinessEvent event, JobEntity jobEntity) {
    String processDefinitionId = jobEntity.getProcessDefinitionId();

    if (processDefinitionId != null) {
      fillProcessDefinitionData(event, processDefinitionId);
    } else {
      event.setProcessDefinitionId(jobEntity.getProcessDefinitionId());
      event.setProcessDefinitionKey(jobEntity.getProcessDefinitionKey());
    }
  }

  protected void fillProcessDefinitionData(BusinessEvent event, String processDefinitionId) {
    ProcessDefinitionEntity entity = getProcessDefinitionEntity(processDefinitionId);

    if (entity != null) {
      event.setProcessDefinitionId(entity.getId());
      event.setProcessDefinitionKey(entity.getKey());
      event.setProcessDefinitionVersion(entity.getVersion());
      event.setProcessDefinitionName(entity.getName());
    }
  }

  protected ProcessDefinitionEntity getProcessDefinitionEntity(String processDefinitionId) {
    DbEntityManager dbEntityManager = Context.getCommandContext() != null
        ? Context.getCommandContext().getDbEntityManager()
        : null;

    if (dbEntityManager != null) {
      return dbEntityManager.selectById(ProcessDefinitionEntity.class, processDefinitionId);
    }

    return null;
  }

  protected void initSequenceCounter(long sequenceCounter, BusinessEvent event) {
    event.setSequenceCounter(sequenceCounter);
  }

  protected void initSequenceCounter(ExecutionEntity execution, BusinessEvent event) {
    if (execution != null) {
      initSequenceCounter(execution.getSequenceCounter(), event);
    }
  }
}
