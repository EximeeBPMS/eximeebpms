package org.eximeebpms.bpm.events.engine;

import java.util.List;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.eximeebpms.bpm.engine.impl.history.event.HistoryEvent;
import org.eximeebpms.bpm.engine.impl.history.handler.CompositeHistoryEventHandler;
import org.eximeebpms.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.eximeebpms.bpm.events.EventBroadcaster;
import org.eximeebpms.bpm.events.cfg.EventBroadcastingProperties;
import org.eximeebpms.bpm.events.engine.history.BroadcastingHistoryEventHandler;
import org.eximeebpms.bpm.events.engine.history.NoopHistoryEventMapper;
import org.eximeebpms.bpm.events.mapper.EventMapper;
import org.eximeebpms.bpm.events.publisher.EventPublisher;
import org.eximeebpms.bpm.events.publisher.EventPublisherFactory;

@Slf4j
@RequiredArgsConstructor
public class EventBroadcastingProcessEnginePlugin implements ProcessEnginePlugin {

  private final EventBroadcastingProperties properties;
  private final EventMapper<HistoryEvent> historyEventMapper;
  private final EventPublisher eventPublisher;
  @Setter
  private Executor executor;

  public static EventBroadcastingProcessEnginePlugin createDefault(
      EventBroadcastingProperties properties,
      EventPublisher eventPublisher) {

    return new EventBroadcastingProcessEnginePlugin(
        properties,
        new NoopHistoryEventMapper(),
        eventPublisher
    );
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    if (!properties.isEnabled()) {
      log.info("Event broadcasting is disabled");
      return;
    }

    final HistoryEventHandler currentHistoryEventHandler =
        processEngineConfiguration.getHistoryEventHandler();

    final EventPublisher configuredPublisher = EventPublisherFactory.create(
        eventPublisher,
        properties,
        resolveExecutor()
    );

    final BroadcastingHistoryEventHandler broadcastingHistoryEventHandler = new BroadcastingHistoryEventHandler(
        new EventBroadcaster<>(properties, historyEventMapper, configuredPublisher)
    );

    processEngineConfiguration.setHistoryEventHandler(
        compose(currentHistoryEventHandler, broadcastingHistoryEventHandler)
    );

    log.info("Event broadcasting history handler registered");
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    // no-op
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    // no-op
  }

  private HistoryEventHandler compose(HistoryEventHandler currentHistoryEventHandler, HistoryEventHandler broadcastingHistoryEventHandler) {
    if (currentHistoryEventHandler == null) {
      return broadcastingHistoryEventHandler;
    }
    return new CompositeHistoryEventHandler(List.of(currentHistoryEventHandler, broadcastingHistoryEventHandler));
  }

  private Executor resolveExecutor() {
    if (executor != null) {
      return executor;
    }

    return Runnable::run;
  }
}
