package org.eximeebpms.bpm.engine.impl.businessevent.identitylink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventTypes;
import org.eximeebpms.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdentityLinkBusinessEventFactoryTest {

  private final IdentityLinkBusinessEventFactory factory = new IdentityLinkBusinessEventFactory();

  @Mock
  private IdentityLinkEntity identityLinkEntity;

  @BeforeEach
  void setUp() {
    when(identityLinkEntity.getType()).thenReturn("candidate");
    when(identityLinkEntity.getUserId()).thenReturn("user-id");
    when(identityLinkEntity.getGroupId()).thenReturn("group-id");
    when(identityLinkEntity.getTaskId()).thenReturn("task-id");
    when(identityLinkEntity.getTenantId()).thenReturn("tenant-id");
    when(identityLinkEntity.getProcessDefId()).thenReturn(null);
  }

  @Test
  void shouldBuildIdentityLinkAddEvent() {
    // given
    // when
    final BusinessEvent businessEvent = factory.createAddEvent(identityLinkEntity);

    // then
    assertThat(businessEvent).isInstanceOf(BusinessIdentityLinkEventEntity.class);

    final BusinessIdentityLinkEventEntity event = (BusinessIdentityLinkEventEntity) businessEvent;

    assertThat(event.getEventType()).isEqualTo(BusinessEventTypes.IDENTITY_LINK_ADD.getEventName());
    assertThat(event.getBusinessEventType()).isEqualTo(BusinessEventTypes.IDENTITY_LINK_ADD.getBusinessEventName());
    assertThat(event.getOperationType()).isEqualTo(IdentityLinkBusinessEventFactory.ADD_OPERATION_TYPE);
    assertThat(event.getTime()).isNotNull();
    assertThat(event.getType()).isEqualTo("candidate");
    assertThat(event.getUserId()).isEqualTo("user-id");
    assertThat(event.getGroupId()).isEqualTo("group-id");
    assertThat(event.getTaskId()).isEqualTo("task-id");
    assertThat(event.getTenantId()).isEqualTo("tenant-id");
  }
}
