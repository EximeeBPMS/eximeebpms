package org.eximeebpms.bpm.events.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eximeebpms.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.eximeebpms.bpm.engine.impl.history.event.HistoryEvent;
import org.eximeebpms.bpm.engine.impl.history.handler.CompositeHistoryEventHandler;
import org.eximeebpms.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.eximeebpms.bpm.events.EventEnvelope;
import org.eximeebpms.bpm.events.EventSource;
import org.eximeebpms.bpm.events.cfg.EventBroadcastingProperties;
import org.eximeebpms.bpm.events.publisher.EventPublisher;
import org.eximeebpms.bpm.events.publisher.NoopEventPublisher;
import org.junit.jupiter.api.Test;

class EventBroadcastingProcessEnginePluginTest {

  @Test
  void shouldNotRegisterHistoryHandlerWhenDisabled() {
    final StandaloneProcessEngineConfiguration configuration = new StandaloneProcessEngineConfiguration();
    final TestHistoryEventHandler existingHandler = new TestHistoryEventHandler();
    configuration.setHistoryEventHandler(existingHandler);

    final EventBroadcastingProcessEnginePlugin plugin = new EventBroadcastingProcessEnginePlugin(
        EventBroadcastingProperties.disabled(),
        source -> Optional.empty(),
        NoopEventPublisher.INSTANCE
    );

    plugin.preInit(configuration);

    assertThat(configuration.getHistoryEventHandler()).isSameAs(existingHandler);
  }

  @Test
  void shouldRegisterBroadcastingHistoryHandlerWhenEnabled() {
    final StandaloneProcessEngineConfiguration configuration = new StandaloneProcessEngineConfiguration();
    final TestHistoryEventHandler existingHandler = new TestHistoryEventHandler();
    configuration.setHistoryEventHandler(existingHandler);

    final EventBroadcastingProcessEnginePlugin plugin = new EventBroadcastingProcessEnginePlugin(
        EventBroadcastingProperties.enabledWithDefaults(),
        source -> Optional.empty(),
        NoopEventPublisher.INSTANCE
    );

    plugin.preInit(configuration);

    assertThat(configuration.getHistoryEventHandler())
        .isInstanceOf(CompositeHistoryEventHandler.class);
  }

  @Test
  void shouldDelegateHistoryEventToExistingAndBroadcastingHandlersWhenEnabled() {
    final StandaloneProcessEngineConfiguration configuration = new StandaloneProcessEngineConfiguration();
    final CountingHistoryEventHandler existingHandler = new CountingHistoryEventHandler();
    configuration.setHistoryEventHandler(existingHandler);

    final List<EventEnvelope> publishedEvents = new ArrayList<>();

    final EventBroadcastingProcessEnginePlugin plugin = new EventBroadcastingProcessEnginePlugin(
        EventBroadcastingProperties.enabledWithDefaults(),
        historyEvent -> Optional.of(
            EventEnvelope.builder()
                .id("event-id")
                .type("test-event")
                .source(EventSource.HISTORY.value())
                .occurredAt(java.time.Instant.now())
                .build()
        ),
        publishedEvents::add
    );

    plugin.preInit(configuration);

    final HistoryEvent historyEvent = new HistoryEvent();
    configuration.getHistoryEventHandler().handleEvent(historyEvent);

    assertThat(existingHandler.count).isEqualTo(1);
    assertThat(publishedEvents).hasSize(1);
    assertThat(publishedEvents.get(0).getId()).isEqualTo("event-id");
  }

  @Test
  void shouldUseConfiguredExecutorForAsyncPublishing() {
    final StandaloneProcessEngineConfiguration configuration =
        new StandaloneProcessEngineConfiguration();

    final CountingHistoryEventHandler existingHandler = new CountingHistoryEventHandler();
    configuration.setHistoryEventHandler(existingHandler);

    final CountingExecutor executor = new CountingExecutor();
    final CountingPublisher publisher = new CountingPublisher();

    final EventBroadcastingProcessEnginePlugin plugin =
        new EventBroadcastingProcessEnginePlugin(
            EventBroadcastingProperties.builder()
                .enabled(true)
                .async(true)
                .build(),
            historyEvent -> Optional.of(
                EventEnvelope.builder()
                    .id("event-id")
                    .type("test-event")
                    .source(EventSource.HISTORY.value())
                    .occurredAt(java.time.Instant.now())
                    .build()
            ),
            publisher
        );

    plugin.setExecutor(executor);
    plugin.preInit(configuration);

    configuration.getHistoryEventHandler().handleEvent(new HistoryEvent());

    assertThat(executor.invocations).isEqualTo(1);
    assertThat(publisher.invocations).isEqualTo(1);
  }

  private static class TestHistoryEventHandler implements HistoryEventHandler {

    @Override
    public void handleEvent(HistoryEvent historyEvent) {
      // no-op
    }

    @Override
    public void handleEvents(List<HistoryEvent> historyEvents) {
      historyEvents.forEach(this::handleEvent);
    }
  }

  private static class CountingHistoryEventHandler implements HistoryEventHandler {

    private int count;

    @Override
    public void handleEvent(HistoryEvent historyEvent) {
      count++;
    }

    @Override
    public void handleEvents(List<HistoryEvent> historyEvents) {
      historyEvents.forEach(this::handleEvent);
    }
  }

  private static final class CountingExecutor implements java.util.concurrent.Executor {

    private int invocations;

    @Override
    public void execute(Runnable command) {
      invocations++;
      command.run();
    }
  }

  private static final class CountingPublisher implements EventPublisher {

    private int invocations;

    @Override
    public void publish(EventEnvelope event) {
      invocations++;
    }
  }
}
