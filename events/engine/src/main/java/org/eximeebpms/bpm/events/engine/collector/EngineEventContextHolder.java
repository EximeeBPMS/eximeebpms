package org.eximeebpms.bpm.events.engine.collector;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EngineEventContextHolder {

  private static final ThreadLocal<EngineEventContext> CONTEXT = new ThreadLocal<>();

  public static EngineEventContext getOrCreate() {
    EngineEventContext context = CONTEXT.get();

    if (context == null) {
      context = new EngineEventContext(new BufferingEngineEventCollector());
      CONTEXT.set(context);
    }

    return context;
  }

  public static EngineEventContext getCurrent() {
    return CONTEXT.get();
  }

  public static void clear() {
    CONTEXT.remove();
  }
}
