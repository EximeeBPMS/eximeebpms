package org.eximeebpms.bpm.events.engine.interceptor;

import lombok.RequiredArgsConstructor;
import org.eximeebpms.bpm.engine.impl.cfg.TransactionState;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.interceptor.Command;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandInterceptor;
import org.eximeebpms.bpm.events.engine.collector.EngineEventContextHolder;
import org.eximeebpms.bpm.events.engine.outbox.EngineEventContextClearingTransactionListener;
import org.eximeebpms.bpm.events.engine.outbox.OutboxEventStore;
import org.eximeebpms.bpm.events.engine.outbox.OutboxFlushingTransactionListener;

@RequiredArgsConstructor
public class EngineEventCollectingCommandInterceptor extends CommandInterceptor {

  private final OutboxEventStore outboxEventStore;

  @Override
  public <T> T execute(Command<T> command) {
    final CommandContext commandContext = Context.getCommandContext();

    if (commandContext == null) {
      return next.execute(command);
    }

    EngineEventContextHolder.getOrCreate();

    commandContext.getTransactionContext().addTransactionListener(
        TransactionState.COMMITTING,
        new OutboxFlushingTransactionListener(outboxEventStore)
    );

    commandContext.getTransactionContext().addTransactionListener(
        TransactionState.ROLLED_BACK,
        new EngineEventContextClearingTransactionListener()
    );

    commandContext.getTransactionContext().addTransactionListener(
        TransactionState.COMMITTED,
        new EngineEventContextClearingTransactionListener()
    );

    try {
      return next.execute(command);
    } catch (RuntimeException exception) {
      EngineEventContextHolder.clear();
      throw exception;
    }
  }
}
