package org.eximeebpms.bpm.engine.impl.businessevent;

import java.io.Serializable;
import java.util.Date;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import lombok.Data;

/**
 * <p>The base class for all business events.</p>
 *
 * <p>A business event contains data about an event that has happened
 * in a process instance. Such an event may be the start of an activity,
 * the end of an activity, a task instance that is created or other similar
 * events...</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class BusinessEvent implements Serializable {

  /** each {@link BusinessEvent} has a unique id */
  protected String id;

  /** the root process instance in which the event has happened */
  protected String rootProcessInstanceId;

  /** the process instance in which the event has happened */
  protected String processInstanceId;

  /** the id of the execution in which the event has happened */
  protected String executionId;

  /** the id of the process definition */
  protected String processDefinitionId;

  /** the key of the process definition */
  protected String processDefinitionKey;

  /** the name of the process definition */
  protected String processDefinitionName;

  /** the version of the process definition */
  protected Integer processDefinitionVersion;

  /** the case instance in which the event has happened */
  protected String caseInstanceId;

  /** the id of the case execution in which the event has happened */
  protected String caseExecutionId;

  /** the id of the case definition */
  protected String caseDefinitionId;

  /** the key of the case definition */
  protected String caseDefinitionKey;

  /** the name of the case definition */
  protected String caseDefinitionName;

  protected String eventType;

  protected String businessEventType;

  protected long sequenceCounter;

  /* the time when the business event will be deleted */
  protected Date removalTime;
  // persistent object implementation ///////////////

  public Object getPersistentState() {
    // events are immutable
    return BusinessEvent.class;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", eventType=" + eventType
           + ", executionId=" + executionId
           + ", processDefinitionId=" + processDefinitionId
           + ", processInstanceId=" + processInstanceId
           + ", rootProcessInstanceId=" + rootProcessInstanceId
           + ", removalTime=" + removalTime
           + "]";
  }

}
