package org.eximeebpms.trustedcode.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class TrustedCodePolicyTest {

  @Test
  public void shouldCreateDefaultPolicy() {
    TrustedCodePolicy policy = TrustedCodePolicy.defaultPolicy();

    assertThat(policy.getMode()).isEqualTo(TrustedCodePolicyMode.defaultMode());
    assertThat(policy.isBlockScriptTasks()).isTrue();
    assertThat(policy.isBlockScriptExecutionListeners()).isTrue();
    assertThat(policy.isBlockScriptTaskListeners()).isTrue();
    assertThat(policy.isBlockExternalScriptResources()).isTrue();
  }

  @Test
  public void shouldBeEnabledForAuditMode() {
    TrustedCodePolicy policy = new TrustedCodePolicy(
        TrustedCodePolicyMode.AUDIT,
        true,
        true,
        true,
        true
    );

    assertThat(policy.isEnabled()).isTrue();
    assertThat(policy.isAuditMode()).isTrue();
    assertThat(policy.isEnforceMode()).isFalse();
  }

  @Test
  public void shouldBeEnabledForEnforceMode() {
    TrustedCodePolicy policy = new TrustedCodePolicy(
        TrustedCodePolicyMode.ENFORCE,
        true,
        true,
        true,
        true
    );

    assertThat(policy.isEnabled()).isTrue();
    assertThat(policy.isAuditMode()).isFalse();
    assertThat(policy.isEnforceMode()).isTrue();
  }

  @Test
  public void shouldBeDisabledForOffMode() {
    TrustedCodePolicy policy = new TrustedCodePolicy(
        TrustedCodePolicyMode.OFF,
        false,
        false,
        false,
        false
    );

    assertThat(policy.isEnabled()).isFalse();
    assertThat(policy.isAuditMode()).isFalse();
    assertThat(policy.isEnforceMode()).isFalse();
  }

  @Test
  public void shouldFailIfPolicyIsEnabledButNoRulesAreActive() {
    assertThatThrownBy(() -> new TrustedCodePolicy(
        TrustedCodePolicyMode.ENFORCE,
        false,
        false,
        false,
        false
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("no trusted-code rules are active");
  }
}
