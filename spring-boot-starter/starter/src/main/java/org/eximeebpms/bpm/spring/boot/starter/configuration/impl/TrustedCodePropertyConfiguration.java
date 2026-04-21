package org.eximeebpms.bpm.spring.boot.starter.configuration.impl;

import org.eximeebpms.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.Ordering;
import org.eximeebpms.bpm.spring.boot.starter.property.TrustedCodeProperties;
import org.springframework.core.annotation.Order;

@Order(Ordering.DEFAULT_ORDER)
public class TrustedCodePropertyConfiguration extends AbstractCamundaConfiguration {

  @Override
  public void preInit(SpringProcessEngineConfiguration configuration) {
    TrustedCodeProperties trustedCode = camundaBpmProperties.getTrustedCode();

    configuration.setTrustedCodeEnabled(trustedCode.isEnabled());
    configuration.setTrustedCodePolicyMode(trustedCode.getMode());
    configuration.setTrustedCodeBlockScriptTasks(trustedCode.isBlockScriptTasks());
    configuration.setTrustedCodeBlockScriptExecutionListeners(trustedCode.isBlockScriptExecutionListeners());
    configuration.setTrustedCodeBlockScriptTaskListeners(trustedCode.isBlockScriptTaskListeners());
    configuration.setTrustedCodeBlockExternalScriptResources(trustedCode.isBlockExternalScriptResources());
  }
}
