package org.eximeebpms.trustedcode.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class TrustedCodePolicyModeTest {

  @Test
  public void shouldReturnDefaultModeForNullValue() {
    assertThat(TrustedCodePolicyMode.from(null))
        .isEqualTo(TrustedCodePolicyMode.defaultMode());
  }

  @Test
  public void shouldReturnDefaultModeForBlankValue() {
    assertThat(TrustedCodePolicyMode.from("   "))
        .isEqualTo(TrustedCodePolicyMode.defaultMode());
  }

  @Test
  public void shouldParseModeIgnoringCaseAndWhitespace() {
    assertThat(TrustedCodePolicyMode.from(" audit "))
        .isEqualTo(TrustedCodePolicyMode.AUDIT);

    assertThat(TrustedCodePolicyMode.from("enforce"))
        .isEqualTo(TrustedCodePolicyMode.ENFORCE);

    assertThat(TrustedCodePolicyMode.from("OFF"))
        .isEqualTo(TrustedCodePolicyMode.OFF);
  }

  @Test
  public void shouldFailForInvalidMode() {
    assertThatThrownBy(() -> TrustedCodePolicyMode.from("banana"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid trusted code policy mode")
        .hasMessageContaining("OFF")
        .hasMessageContaining("AUDIT")
        .hasMessageContaining("ENFORCE");
  }
}
