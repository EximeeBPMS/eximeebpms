package org.eximeebpms.bpm.engine.impl.businessevent.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Date;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.incident.IncidentState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidentBusinessEventFactoryTest {

  private final IncidentBusinessEventFactory factory = new IncidentBusinessEventFactory();

  @Mock
  private IncidentEntity incident;

  @Mock
  private ExecutionEntity execution;

  @Mock
  private CommandContext commandContext;

  @Mock
  private DbEntityManager dbEntityManager;

  @Mock
  private ProcessDefinitionEntity processDefinitionEntity;

  @Test
  void shouldFillProcessDefinitionDataWhenProcessDefinitionExists() {
    // given
    mockBaseIncident();
    when(incident.getProcessDefinitionId()).thenReturn("process-definition-id");
    when(dbEntityManager.selectById(ProcessDefinitionEntity.class, "process-definition-id")).thenReturn(processDefinitionEntity);
    when(commandContext.getDbEntityManager()).thenReturn(dbEntityManager);
    when(processDefinitionEntity.getId()).thenReturn("process-definition-id");
    when(processDefinitionEntity.getKey()).thenReturn("process-definition-key");
    when(processDefinitionEntity.getVersion()).thenReturn(3);
    when(processDefinitionEntity.getName()).thenReturn("process-definition-name");

    try (MockedStatic<Context> context = mockStatic(Context.class)) {
      context.when(Context::getCommandContext).thenReturn(commandContext);

      // when
      final BusinessEvent businessEvent = factory.createBusinessIncidentEvt(incident, BusinessEventTypes.INCIDENT_CREATE);

      // then
      final BusinessIncidentEventEntity event = (BusinessIncidentEventEntity) businessEvent;
      assertThat(event.getProcessDefinitionId()).isEqualTo("process-definition-id");
      assertThat(event.getProcessDefinitionKey()).isEqualTo("process-definition-key");
      assertThat(event.getProcessDefinitionVersion()).isEqualTo(3);
      assertThat(event.getProcessDefinitionName()).isEqualTo("process-definition-name");
    }
  }

  @Test
  void shouldBuildIncidentCreateEvent() {
    // given
    mockBaseIncident();
    final Date timestamp = new Date();
    when(incident.getIncidentTimestamp()).thenReturn(timestamp);

    // when
    final BusinessEvent businessEvent = factory.createBusinessIncidentEvt(incident, BusinessEventTypes.INCIDENT_CREATE);

    // then
    assertThat(businessEvent).isInstanceOf(BusinessIncidentEventEntity.class);

    final BusinessIncidentEventEntity event = (BusinessIncidentEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.INCIDENT_CREATE.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.INCIDENT_CREATE.getBusinessEventName());
    assertThat(event.getId()).isEqualTo("incident-id");
    assertThat(event.getProcessInstanceId()).isEqualTo("process-instance-id");
    assertThat(event.getExecutionId()).isEqualTo("execution-id");
    assertThat(event.getCreateTime()).isEqualTo(timestamp);
    assertThat(event.getIncidentType()).isEqualTo("failedJob");
    assertThat(event.getActivityId()).isEqualTo("activity-id");
    assertThat(event.getFailedActivityId()).isEqualTo("failed-activity-id");
    assertThat(event.getCauseIncidentId()).isEqualTo("cause-incident-id");
    assertThat(event.getRootCauseIncidentId()).isEqualTo("root-cause-incident-id");
    assertThat(event.getConfiguration()).isEqualTo("configuration");
    assertThat(event.getIncidentMessage()).isEqualTo("incident-message");
    assertThat(event.getTenantId()).isEqualTo("tenant-id");
    assertThat(event.getJobDefinitionId()).isEqualTo("job-definition-id");
    assertThat(event.getHistoryConfiguration()).isEqualTo("history-configuration");
    assertThat(event.getAnnotation()).isEqualTo("annotation");
    assertThat(event.getRootProcessInstanceId()).isEqualTo("root-process-instance-id");

    assertThat(event.getIncidentState()).isEqualTo(IncidentState.DEFAULT.getStateCode());
    assertThat(event.isOpen()).isTrue();
    assertThat(event.isResolved()).isFalse();
    assertThat(event.isDeleted()).isFalse();
    assertThat(event.getEndTime()).isNull();
  }

  @Test
  void shouldBuildIncidentResolveEventWithEndTimeAndResolvedState() {
    // given
    mockBaseIncident();

    // when
    final BusinessEvent businessEvent = factory.createBusinessIncidentEvt(incident, BusinessEventTypes.INCIDENT_RESOLVE);

    // then
    final BusinessIncidentEventEntity event = (BusinessIncidentEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.INCIDENT_RESOLVE.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.INCIDENT_RESOLVE.getBusinessEventName());
    assertThat(event.getIncidentState()).isEqualTo(IncidentState.RESOLVED.getStateCode());
    assertThat(event.isResolved()).isTrue();
    assertThat(event.isOpen()).isFalse();
    assertThat(event.isDeleted()).isFalse();
    assertThat(event.getEndTime()).isNotNull();
  }

  @Test
  void shouldBuildIncidentDeleteEventWithEndTimeAndDeletedState() {
    // given
    mockBaseIncident();

    // when
    final BusinessEvent businessEvent = factory.createBusinessIncidentEvt(incident, BusinessEventTypes.INCIDENT_DELETE);

    // then
    final BusinessIncidentEventEntity event = (BusinessIncidentEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.INCIDENT_DELETE.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.INCIDENT_DELETE.getBusinessEventName());
    assertThat(event.getIncidentState()).isEqualTo(IncidentState.DELETED.getStateCode());
    assertThat(event.isDeleted()).isTrue();
    assertThat(event.isOpen()).isFalse();
    assertThat(event.isResolved()).isFalse();
    assertThat(event.getEndTime()).isNotNull();
  }

  @Test
  void shouldBuildIncidentMigrateEventWithoutEndTimeAndOpenState() {
    // given
    mockBaseIncident();

    // when
    final BusinessEvent businessEvent = factory.createBusinessIncidentEvt(incident, BusinessEventTypes.INCIDENT_MIGRATE);

    // then
    final BusinessIncidentEventEntity event = (BusinessIncidentEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.INCIDENT_MIGRATE.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.INCIDENT_MIGRATE.getBusinessEventName());
    assertThat(event.getIncidentState()).isEqualTo(IncidentState.DEFAULT.getStateCode());
    assertThat(event.getEndTime()).isNull();
  }

  @Test
  void shouldBuildIncidentUpdateEventWithoutEndTimeAndOpenState() {
    // given
    mockBaseIncident();

    // when
    final BusinessEvent businessEvent = factory.createBusinessIncidentEvt(incident, BusinessEventTypes.INCIDENT_UPDATE);

    // then
    final BusinessIncidentEventEntity event = (BusinessIncidentEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.INCIDENT_UPDATE.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.INCIDENT_UPDATE.getBusinessEventName());
    assertThat(event.getIncidentState()).isEqualTo(IncidentState.DEFAULT.getStateCode());
    assertThat(event.getEndTime()).isNull();
  }

  @Test
  void shouldNotFillRootProcessInstanceIdWhenExecutionIsNull() {
    // given
    mockBaseIncident();
    when(incident.getExecution()).thenReturn(null);

    // when
    final BusinessEvent businessEvent = factory.createBusinessIncidentEvt(incident, BusinessEventTypes.INCIDENT_CREATE);

    // then
    final BusinessIncidentEventEntity event = (BusinessIncidentEventEntity) businessEvent;
    assertThat(event.getRootProcessInstanceId()).isNull();
  }

  private void mockBaseIncident() {
    when(incident.getId()).thenReturn("incident-id");
    when(incident.getProcessInstanceId()).thenReturn("process-instance-id");
    when(incident.getExecutionId()).thenReturn("execution-id");
    when(incident.getProcessDefinitionId()).thenReturn(null);
    when(incident.getIncidentType()).thenReturn("failedJob");
    when(incident.getActivityId()).thenReturn("activity-id");
    when(incident.getFailedActivityId()).thenReturn("failed-activity-id");
    when(incident.getCauseIncidentId()).thenReturn("cause-incident-id");
    when(incident.getRootCauseIncidentId()).thenReturn("root-cause-incident-id");
    when(incident.getConfiguration()).thenReturn("configuration");
    when(incident.getIncidentMessage()).thenReturn("incident-message");
    when(incident.getTenantId()).thenReturn("tenant-id");
    when(incident.getJobDefinitionId()).thenReturn("job-definition-id");
    when(incident.getHistoryConfiguration()).thenReturn("history-configuration");
    when(incident.getAnnotation()).thenReturn("annotation");
    when(incident.getExecution()).thenReturn(execution);
    lenient().when(execution.getRootProcessInstanceId()).thenReturn("root-process-instance-id");
  }
}
