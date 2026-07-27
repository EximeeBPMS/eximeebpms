package org.eximeebpms.bpm.engine.impl.businessevent.useroperationlog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.oplog.UserOperationLogContext;
import org.eximeebpms.bpm.engine.impl.oplog.UserOperationLogContextEntry;
import org.eximeebpms.bpm.engine.impl.persistence.entity.PropertyChange;
import org.junit.jupiter.api.Test;

class UserOperationLogBusinessEventFactoryTest {

  private final UserOperationLogBusinessEventFactory factory = new UserOperationLogBusinessEventFactory();

  @Test
  void shouldCreateOneEventPerPropertyChange() {
    // given
    UserOperationLogContext context = new UserOperationLogContext();
    context.setOperationId("op-1");
    context.setUserId("user-1");

    UserOperationLogContextEntry entry = new UserOperationLogContextEntry("Suspend", "ProcessInstance");
    entry.setProcessInstanceId("process-instance-id");
    entry.setProcessDefinitionKey("myProcess");
    entry.setExecutionId("execution-id");
    entry.setRootProcessInstanceId("root-process-instance-id");
    entry.setTenantId("tenant-id");
    entry.setCategory("Operator");
    entry.setPropertyChanges(Arrays.asList(
        new PropertyChange("suspensionState", "active", "suspended"),
        new PropertyChange("includeProcessInstances", null, "true")
    ));
    context.addEntry(entry);

    // when
    List<BusinessEvent> events = factory.createEvents(context);

    // then
    assertThat(events).hasSize(2);

    UserOperationLogBusinessEvent first = (UserOperationLogBusinessEvent) events.get(0);
    assertThat(first.getEventType()).isEqualTo(BusinessEventTypes.USER_OPERATION_LOG.getEventName());
    assertThat(first.getBusinessEventType()).isEqualTo(BusinessEventTypes.USER_OPERATION_LOG.getBusinessEventName());
    assertThat(first.getOperationId()).isEqualTo("op-1");
    assertThat(first.getUserId()).isEqualTo("user-1");
    assertThat(first.getOperationType()).isEqualTo("Suspend");
    assertThat(first.getEntityType()).isEqualTo("ProcessInstance");
    assertThat(first.getProcessInstanceId()).isEqualTo("process-instance-id");
    assertThat(first.getProcessDefinitionKey()).isEqualTo("myProcess");
    assertThat(first.getExecutionId()).isEqualTo("execution-id");
    assertThat(first.getRootProcessInstanceId()).isEqualTo("root-process-instance-id");
    assertThat(first.getTenantId()).isEqualTo("tenant-id");
    assertThat(first.getCategory()).isEqualTo("Operator");
    assertThat(first.getProperty()).isEqualTo("suspensionState");
    assertThat(first.getOrgValue()).isEqualTo("active");
    assertThat(first.getNewValue()).isEqualTo("suspended");
    assertThat(first.getTimestamp()).isNotNull();

    UserOperationLogBusinessEvent second = (UserOperationLogBusinessEvent) events.get(1);
    assertThat(second.getOperationId()).isEqualTo("op-1");
    assertThat(second.getProperty()).isEqualTo("includeProcessInstances");
    assertThat(second.getOrgValue()).isNull();
    assertThat(second.getNewValue()).isEqualTo("true");
  }

  @Test
  void shouldCreateEventsAcrossMultipleEntries() {
    // given
    UserOperationLogContext context = new UserOperationLogContext();
    context.setOperationId("op-2");
    context.setUserId("user-2");

    UserOperationLogContextEntry processEntry = new UserOperationLogContextEntry("Migrate", "ProcessInstance");
    processEntry.setProcessInstanceId("process-instance-id");
    processEntry.setPropertyChanges(Collections.singletonList(new PropertyChange("processDefinitionId", "v1", "v2")));
    context.addEntry(processEntry);

    UserOperationLogContextEntry taskEntry = new UserOperationLogContextEntry("Migrate", "Task");
    taskEntry.setTaskId("task-id");
    taskEntry.setPropertyChanges(Collections.singletonList(new PropertyChange("taskDefinitionKey", "t1", "t2")));
    context.addEntry(taskEntry);

    // when
    List<BusinessEvent> events = factory.createEvents(context);

    // then
    assertThat(events).hasSize(2);
    assertThat(events)
        .extracting(event -> ((UserOperationLogBusinessEvent) event).getEntityType())
        .containsExactly("ProcessInstance", "Task");
    assertThat(events)
        .allSatisfy(event -> assertThat(((UserOperationLogBusinessEvent) event).getOperationId()).isEqualTo("op-2"));
  }

}
