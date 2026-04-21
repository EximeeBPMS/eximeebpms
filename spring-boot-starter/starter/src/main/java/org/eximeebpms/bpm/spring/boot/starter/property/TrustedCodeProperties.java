package org.eximeebpms.bpm.spring.boot.starter.property;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrustedCodeProperties {

  /**
   * Indicates whether the trusted-code policy is enabled.
   *
   * Default: true
   */
  private boolean enabled = true;

  /**
   * Defines the mode of the trusted-code policy.
   * Possible values are OFF, AUDIT, ENFORCE.
   *
   * Default: ENFORCE
   */
  private String mode = "ENFORCE";

  /**
   * If true, script tasks are disallowed for untrusted deployments.
   *
   * Default: true
   */
  private boolean blockScriptTasks = true;

  /**
   * If true, execution listeners using scripts are disallowed for untrusted deployments.
   *
   * Default: true
   */
  private boolean blockScriptExecutionListeners = true;

  /**
   * If true, task listeners using scripts are disallowed for untrusted deployments.
   *
   * Default: true
   */
  private boolean blockScriptTaskListeners = true;

  /**
   * If true, external script resources are disallowed for untrusted deployments.
   *
   * Default: true
   */
  private boolean blockExternalScriptResources = true;

}
