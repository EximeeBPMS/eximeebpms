package org.eximeebpms.bpm.events.engine.outbox;

import org.eximeebpms.bpm.engine.impl.cfg.TransactionListener;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.events.engine.collector.EngineEventContextHolder;

public class EngineEventContextClearingTransactionListener implements TransactionListener {

  @Override
  public void execute(CommandContext commandContext) {
    EngineEventContextHolder.clear();
  }
}
