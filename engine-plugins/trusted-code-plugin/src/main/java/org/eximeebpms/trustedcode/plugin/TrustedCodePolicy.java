package org.eximeebpms.trustedcode.plugin;

import java.util.Objects;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TrustedCodePolicy {

  private final TrustedCodePolicyMode mode;
  private final boolean blockScriptTasks;
  private final boolean blockScriptExecutionListeners;
  private final boolean blockScriptTaskListeners;
  private final boolean blockExternalScriptResources;

  public TrustedCodePolicy(
      TrustedCodePolicyMode mode,
      boolean blockScriptTasks,
      boolean blockScriptExecutionListeners,
      boolean blockScriptTaskListeners,
      boolean blockExternalScriptResources) {

    this.mode = Objects.requireNonNull(mode, "mode must not be null");
    this.blockScriptTasks = blockScriptTasks;
    this.blockScriptExecutionListeners = blockScriptExecutionListeners;
    this.blockScriptTaskListeners = blockScriptTaskListeners;
    this.blockExternalScriptResources = blockExternalScriptResources;

    validate();
  }

  public static TrustedCodePolicy defaultPolicy() {
    return new TrustedCodePolicy(
        TrustedCodePolicyMode.defaultMode(),
        true,
        true,
        true,
        true
    );
  }

  public boolean isEnabled() {
    return mode != TrustedCodePolicyMode.OFF;
  }

  public boolean isAuditMode() {
    return mode == TrustedCodePolicyMode.AUDIT;
  }

  public boolean isEnforceMode() {
    return mode == TrustedCodePolicyMode.ENFORCE;
  }

  private void validate() {
    if (mode == TrustedCodePolicyMode.OFF) {
      return;
    }

    if (!blockScriptTasks
        && !blockScriptExecutionListeners
        && !blockScriptTaskListeners
        && !blockExternalScriptResources) {
      throw new IllegalArgumentException("Trusted code policy is enabled but no trusted-code rules are active");
    }
  }
}
