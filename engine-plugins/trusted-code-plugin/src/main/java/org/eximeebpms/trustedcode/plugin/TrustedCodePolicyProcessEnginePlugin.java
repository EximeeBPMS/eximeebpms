package org.eximeebpms.trustedcode.plugin;

import java.util.ArrayList;
import java.util.List;
import org.eximeebpms.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.eximeebpms.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

public class TrustedCodePolicyProcessEnginePlugin extends AbstractProcessEnginePlugin {

  private final TrustedCodePolicy explicitPolicy;

  public TrustedCodePolicyProcessEnginePlugin() {
    this.explicitPolicy = null;
  }

  public TrustedCodePolicyProcessEnginePlugin(TrustedCodePolicy explicitPolicy) {
    this.explicitPolicy = explicitPolicy;
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    if (!isPluginEnabled(processEngineConfiguration)) {
      return;
    }

    TrustedCodePolicy policy = resolvePolicy(processEngineConfiguration);
    addTrustedCodeParseListener(processEngineConfiguration, policy);
  }

  private boolean isPluginEnabled(ProcessEngineConfigurationImpl processEngineConfiguration) {
    if (explicitPolicy != null) {
      return true;
    }

    return processEngineConfiguration.isTrustedCodeEnabled();
  }

  private void addTrustedCodeParseListener(ProcessEngineConfigurationImpl processEngineConfiguration, TrustedCodePolicy policy) {
    List<BpmnParseListener> preParseListeners =
        processEngineConfiguration.getCustomPreBPMNParseListeners();

    if (preParseListeners == null) {
      preParseListeners = new ArrayList<>();
      processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
    }

    preParseListeners.add(new TrustedCodeBpmnParseListener(policy));
  }

  private TrustedCodePolicy resolvePolicy(ProcessEngineConfigurationImpl processEngineConfiguration) {
    if (explicitPolicy != null) {
      return explicitPolicy;
    }

    return new TrustedCodePolicy(
        TrustedCodePolicyMode.from(processEngineConfiguration.getTrustedCodePolicyMode()),
        processEngineConfiguration.isTrustedCodeBlockScriptTasks(),
        processEngineConfiguration.isTrustedCodeBlockScriptExecutionListeners(),
        processEngineConfiguration.isTrustedCodeBlockScriptTaskListeners(),
        processEngineConfiguration.isTrustedCodeBlockExternalScriptResources()
    );
  }
}
