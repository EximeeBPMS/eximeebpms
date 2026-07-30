package org.eximeebpms.bpm.engine.impl.jobexecutor.businesseventoutboxcleanup;

import com.google.gson.JsonObject;
import org.eximeebpms.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.eximeebpms.bpm.engine.impl.util.JsonUtil;

/**
 * Configuration for the {@link BusinessEventOutboxCleanupJobHandler}.
 * Currently carries no state; the retention period is read from
 * {@link org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl#getBusinessEventOutboxRetentionMs()}
 * at execution time so that it can be changed without touching persistent job data.
 */
public class BusinessEventOutboxCleanupJobHandlerConfiguration implements JobHandlerConfiguration {

  @Override
  public String toCanonicalString() {
    return JsonUtil.createObject().toString();
  }

  public static BusinessEventOutboxCleanupJobHandlerConfiguration fromJson(JsonObject jsonObject) {
    return new BusinessEventOutboxCleanupJobHandlerConfiguration();
  }
}

