package org.eximeebpms.bpm.engine.impl.businessevent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;

class BusinessEventTypeFilterTest {

  private static BusinessEventTypeFilter filter(Set<String> enabled, Set<String> disabled) {
    return BusinessEventTypeFilter.of(BusinessEventConfiguration.builder()
        .enabledEventTypes(enabled)
        .disabledEventTypes(disabled)
        .build());
  }

  @Test
  void shouldEnableEverythingByDefault() {
    // when
    BusinessEventTypeFilter filter = BusinessEventTypeFilter.of(BusinessEventConfiguration.builder().build());

    // then
    assertThat(filter.isAllEnabled()).isTrue();
    for (BusinessEventTypes type : BusinessEventTypes.values()) {
      assertThat(filter.isEnabled(type)).as(type.getToken()).isTrue();
    }
  }

  @Test
  void shouldEnableEverythingWhenConfigurationIsMissing() {
    // when
    BusinessEventTypeFilter filter = BusinessEventTypeFilter.of(null);

    // then
    assertThat(filter.isAllEnabled()).isTrue();
    assertThat(filter.isEnabled(BusinessEventTypes.TASK_INSTANCE_COMPLETE)).isTrue();
  }

  @Test
  void shouldMatchSingleType() {
    // when
    BusinessEventTypeFilter filter = filter(Set.of("task-instance:complete"), Set.of());

    // then
    assertThat(filter.isAllEnabled()).isFalse();
    assertThat(filter.isEnabled(BusinessEventTypes.TASK_INSTANCE_COMPLETE)).isTrue();
    assertThat(filter.isEnabled(BusinessEventTypes.TASK_INSTANCE_CREATE)).isFalse();
    assertThat(filter.isEnabled(BusinessEventTypes.PROCESS_INSTANCE_START)).isFalse();
  }

  @Test
  void shouldMatchEntityWildcard() {
    // when
    BusinessEventTypeFilter filter = filter(Set.of("variable-instance:*"), Set.of());

    // then
    assertThat(filter.isEnabled(BusinessEventTypes.VARIABLE_INSTANCE_CREATE)).isTrue();
    assertThat(filter.isEnabled(BusinessEventTypes.VARIABLE_INSTANCE_UPDATE)).isTrue();
    assertThat(filter.isEnabled(BusinessEventTypes.VARIABLE_INSTANCE_DELETE)).isTrue();
    assertThat(filter.isEnabled(BusinessEventTypes.TASK_INSTANCE_CREATE)).isFalse();
  }

  @Test
  void shouldTreatBareEntityAsEntityWildcard() {
    // when
    BusinessEventTypeFilter filter = filter(Set.of("variable-instance"), Set.of());

    // then
    assertThat(filter.isEnabled(BusinessEventTypes.VARIABLE_INSTANCE_CREATE)).isTrue();
    assertThat(filter.isEnabled(BusinessEventTypes.TASK_INSTANCE_CREATE)).isFalse();
  }

  @Test
  void shouldLetDenylistWinOverAllowlist() {
    // when
    BusinessEventTypeFilter filter = filter(Set.of("*"), Set.of("variable-instance:*", "activity-instance:start"));

    // then
    assertThat(filter.isAllEnabled()).isFalse();
    assertThat(filter.isEnabled(BusinessEventTypes.VARIABLE_INSTANCE_UPDATE)).isFalse();
    assertThat(filter.isEnabled(BusinessEventTypes.ACTIVITY_INSTANCE_START)).isFalse();
    assertThat(filter.isEnabled(BusinessEventTypes.ACTIVITY_INSTANCE_END)).isTrue();
    assertThat(filter.isEnabled(BusinessEventTypes.PROCESS_INSTANCE_START)).isTrue();
  }

  @Test
  void shouldLetDenylistNarrowAnEntityWildcardAllowlist() {
    // when
    BusinessEventTypeFilter filter = filter(Set.of("task-instance:*"), Set.of("task-instance:update"));

    // then
    assertThat(filter.isEnabled(BusinessEventTypes.TASK_INSTANCE_CREATE)).isTrue();
    assertThat(filter.isEnabled(BusinessEventTypes.TASK_INSTANCE_UPDATE)).isFalse();
  }

  @Test
  void shouldDisableEverythingOnEmptyAllowlist() {
    // when
    BusinessEventTypeFilter filter = filter(Set.of(), Set.of());

    // then
    assertThat(filter.isAllEnabled()).isFalse();
    for (BusinessEventTypes type : BusinessEventTypes.values()) {
      assertThat(filter.isEnabled(type)).as(type.getToken()).isFalse();
    }
  }

  @Test
  void shouldNotBeAllEnabledWhenDenylistIsUsed() {
    // when
    BusinessEventTypeFilter filter = filter(Set.of("*"), Set.of("job:create"));

    // then
    assertThat(filter.isAllEnabled()).isFalse();
    assertThat(filter.isEnabled(BusinessEventTypes.JOB_CREATE)).isFalse();
    assertThat(filter.isEnabled(BusinessEventTypes.JOB_FAIL)).isTrue();
  }

  @Test
  void shouldAcceptPrefixQualifiedToken() {
    // when
    BusinessEventTypeFilter filter = filter(Set.of("bpms:task-instance:complete"), Set.of());

    // then
    assertThat(filter.isEnabled(BusinessEventTypes.TASK_INSTANCE_COMPLETE)).isTrue();
    assertThat(filter.isEnabled(BusinessEventTypes.TASK_INSTANCE_CREATE)).isFalse();
  }

  @Test
  void shouldAcceptPrefixQualifiedTokenForConfiguredPrefix() {
    // when
    BusinessEventTypeFilter filter = BusinessEventTypeFilter.of(BusinessEventConfiguration.builder()
        .prefix("camunda7")
        .enabledEventTypes(Set.of("camunda7:task-instance:complete"))
        .build());

    // then
    assertThat(filter.isEnabled(BusinessEventTypes.TASK_INSTANCE_COMPLETE)).isTrue();
  }

  @Test
  void shouldRejectTokenCarryingForeignPrefix() {
    // given
    Set<String> enabled = Set.of("camunda7:task-instance:complete");
    Set<String> disabled = Set.of();

    // when/then
    assertThatThrownBy(() -> filter(enabled, disabled))
        .isInstanceOf(InvalidBusinessEventTypeException.class)
        .hasMessageContaining("camunda7")
        .hasMessageContaining("bpms");
  }

  @Test
  void shouldIgnoreCaseAndSurroundingWhitespace() {
    // when
    BusinessEventTypeFilter filter = filter(Set.of("  TASK-INSTANCE:Complete  "), Set.of());

    // then
    assertThat(filter.isEnabled(BusinessEventTypes.TASK_INSTANCE_COMPLETE)).isTrue();
  }

  @Test
  void shouldIgnoreBlankEntries() {
    // when
    BusinessEventTypeFilter filter = filter(Set.of("job:create", "   ", ""), Set.of());

    // then
    assertThat(filter.getEnabledTokens()).containsExactly("job:create");
  }

  @Test
  void shouldRejectUnknownEntity() {
    // given
    Set<String> enabled = Set.of("taskinstance:complete");
    Set<String> disabled = Set.of();

    // when/then
    assertThatThrownBy(() -> filter(enabled, disabled))
        .isInstanceOf(InvalidBusinessEventTypeException.class)
        .hasMessageContaining("taskinstance")
        .hasMessageContaining("not a known business event entity");
  }

  @Test
  void shouldRejectUnknownEventOnKnownEntity() {
    // given
    Set<String> enabled = Set.of("task-instance:finish");
    Set<String> disabled = Set.of();

    // when/then
    assertThatThrownBy(() -> filter(enabled, disabled))
        .isInstanceOf(InvalidBusinessEventTypeException.class)
        .hasMessageContaining("has no 'finish' event");
  }

  @Test
  void shouldRejectUnknownTokenInDenylistToo() {
    // given
    Set<String> enabled = Set.of("*");
    Set<String> disabled = Set.of("variable-instance:nope");

    // when/then
    assertThatThrownBy(() -> filter(enabled, disabled))
        .isInstanceOf(InvalidBusinessEventTypeException.class)
        .hasMessageContaining("disabledEventTypes");
  }

  @Test
  void shouldRejectWildcardEntitySegment() {
    // given
    Set<String> enabled = Set.of("*:create");
    Set<String> disabled = Set.of();

    // when/then
    assertThatThrownBy(() -> filter(enabled, disabled))
        .isInstanceOf(InvalidBusinessEventTypeException.class)
        .hasMessageContaining("must not be a wildcard");
  }

  @Test
  void shouldRejectTooManySegments() {
    // given
    Set<String> enabled = Set.of("a:b:c:d");
    Set<String> disabled = Set.of();

    // when/then
    assertThatThrownBy(() -> filter(enabled, disabled))
        .isInstanceOf(InvalidBusinessEventTypeException.class);
  }

  @Test
  void shouldRejectEmptyEventSegment() {
    // given
    Set<String> enabled = Set.of("task-instance:");
    Set<String> disabled = Set.of();

    // when/then
    assertThatThrownBy(() -> filter(enabled, disabled))
        .isInstanceOf(InvalidBusinessEventTypeException.class);
  }

  @Test
  void shouldMatchFullyQualifiedEventName() {
    // given
    BusinessEventTypeFilter filter = filter(Set.of("task-instance:*"), Set.of("task-instance:delete"));

    // when/then
    assertThat(filter.isEnabled("bpms:task-instance:complete")).isTrue();
    assertThat(filter.isEnabled("bpms:task-instance:delete")).isFalse();
    assertThat(filter.isEnabled("bpms:process-instance:start")).isFalse();
  }

  @Test
  void shouldMatchFullyQualifiedEventNameRegardlessOfPrefix() {
    // given — the outbox row carries whatever prefix was configured when it was written
    BusinessEventTypeFilter filter = filter(Set.of("task-instance:complete"), Set.of());

    // when/then
    assertThat(filter.isEnabled("camunda7:task-instance:complete")).isTrue();
  }

  @Test
  void shouldNotMatchNullOrUnparseableName() {
    // given
    BusinessEventTypeFilter filter = filter(Set.of("task-instance:complete"), Set.of());

    // when/then
    assertThat(filter.isEnabled((String) null)).isFalse();
    assertThat(filter.isEnabled("complete")).isFalse();
    assertThat(filter.isEnabled((BusinessEventType) null)).isFalse();
  }

  @Test
  void shouldListEffectiveTokens() {
    // when
    BusinessEventTypeFilter filter = filter(Set.of("batch:*"), Set.of("batch:update"));

    // then
    assertThat(filter.getEffectiveTokens()).containsExactlyInAnyOrder("batch:start", "batch:end");
  }

  @Test
  void shouldKeepCustomEventTypesEnabledUnderWildcardAllowlist() {
    // given — a type outside BusinessEventTypes must not be filtered out just for being unknown
    BusinessEventType custom = new BusinessEventType() {
      @Override
      public String getEntityType() {
        return "custom-entity";
      }

      @Override
      public String getEventName() {
        return "custom-event";
      }
    };

    // when
    BusinessEventTypeFilter filter = filter(Set.of("*"), Set.of("job:create"));

    // then
    assertThat(filter.isEnabled(custom)).isTrue();
  }
}
