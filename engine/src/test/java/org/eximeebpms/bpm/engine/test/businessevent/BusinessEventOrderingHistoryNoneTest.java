package org.eximeebpms.bpm.engine.test.businessevent;

import org.eximeebpms.bpm.engine.ProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * The ordering guarantees of {@link BusinessEventOrderingTest} do not depend on history being
 * produced: business events are emitted at the history emission points even when history is off.
 */
public class BusinessEventOrderingHistoryNoneTest extends BusinessEventOrderingTest {

  @Override
  protected void configureEngine(ProcessEngineConfigurationImpl config) {
    // a history level is fixed per database, so this engine gets its own
    config.setJdbcUrl("jdbc:h2:mem:BusinessEventOrderingHistoryNoneTest");
    config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
    config.setHistory(ProcessEngineConfiguration.HISTORY_NONE);
  }
}
