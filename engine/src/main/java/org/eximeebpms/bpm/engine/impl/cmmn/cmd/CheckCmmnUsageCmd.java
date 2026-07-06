/*
 * Copyright EximeeBPMS contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eximeebpms.bpm.engine.impl.cmmn.cmd;

import java.util.Optional;
import org.eximeebpms.bpm.engine.impl.HistoricCaseInstanceQueryImpl;
import org.eximeebpms.bpm.engine.impl.ProcessEngineLogger;
import org.eximeebpms.bpm.engine.impl.cfg.ConfigurationLogger;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionQueryImpl;
import org.eximeebpms.bpm.engine.impl.cmmn.entity.runtime.CaseInstanceQueryImpl;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.interceptor.Command;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;

/**
 * Detects deployed CMMN case definitions, active case instances or historic case data
 * in the database and logs a deprecation warning if any is found.
 * <p>
 * Executed once per process engine build, as part of {@code ProcessEngineImpl#executeSchemaOperations()}.
 * </p>
 */
public class CheckCmmnUsageCmd implements Command<Void> {

  private static final ConfigurationLogger LOG = ProcessEngineLogger.CONFIG_LOGGER;

  public Void execute(CommandContext commandContext) {
    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    final boolean isCmmnEnabled = Optional.ofNullable(processEngineConfiguration)
        .map(ProcessEngineConfigurationImpl::isCmmnEnabled)
        .orElse(false);

    if (!isCmmnEnabled) {
      return null;
    }

    if (caseDefinitionCount(commandContext) > 0
        || caseInstanceCount(commandContext) > 0
        || (processEngineConfiguration.isDbHistoryUsed() && historicCaseInstanceCount(commandContext) > 0)) {
      LOG.cmmnUsageDetected();
    }

    return null;
  }

  private static long historicCaseInstanceCount(CommandContext commandContext) {
    return commandContext.getHistoricCaseInstanceManager().findHistoricCaseInstanceCountByQueryCriteria(new HistoricCaseInstanceQueryImpl());
  }

  private static long caseInstanceCount(CommandContext commandContext) {
    return commandContext.getCaseExecutionManager().findCaseInstanceCountByQueryCriteria(new CaseInstanceQueryImpl());
  }

  private static long caseDefinitionCount(CommandContext commandContext) {
    return commandContext.getCaseDefinitionManager().findCaseDefinitionCountByQueryCriteria(new CaseDefinitionQueryImpl());
  }

}
