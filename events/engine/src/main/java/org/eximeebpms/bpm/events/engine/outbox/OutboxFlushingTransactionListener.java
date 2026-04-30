package org.eximeebpms.bpm.events.engine.outbox;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eximeebpms.bpm.engine.impl.cfg.TransactionListener;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.events.engine.EngineEvent;
import org.eximeebpms.bpm.events.engine.collector.EngineEventContext;
import org.eximeebpms.bpm.events.engine.collector.EngineEventContextHolder;

@RequiredArgsConstructor
public class OutboxFlushingTransactionListener implements TransactionListener {

  private final OutboxEventStore outboxEventStore;

  @Override
  public void execute(CommandContext commandContext) {
    final EngineEventContext context = EngineEventContextHolder.getCurrent();

    if (context == null || context.isEmpty()) {
      return;
    }

    final List<EngineEvent> events = context.drainEvents();
    outboxEventStore.saveAll(events);
  }
}
