package org.eximeebpms.bpm.engine.impl.history.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eximeebpms.bpm.engine.impl.batch.history.HistoricBatchEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricDecisionEvaluationEvent;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoryEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProcessDefinitionKeyFilteringHistoryEventHandlerTest {

  protected RecordingHistoryEventHandler delegate;
  protected ProcessDefinitionKeyFilteringHistoryEventHandler handler;

  @Before
  public void setUp() {
    delegate = new RecordingHistoryEventHandler();
    handler = new ProcessDefinitionKeyFilteringHistoryEventHandler(delegate, Set.of("excluded-process", "another-excluded-process"));
  }

  @Test
  public void shouldDropEventForExcludedProcessDefinitionKey() {
    // given
    HistoricProcessInstanceEventEntity event = new HistoricProcessInstanceEventEntity();
    event.setProcessDefinitionKey("excluded-process");

    // when
    handler.handleEvent(event);

    // then
    Assert.assertTrue(delegate.handledEvents.isEmpty());
  }

  @Test
  public void shouldPassThroughEventForNonExcludedProcessDefinitionKey() {
    // given
    HistoricProcessInstanceEventEntity event = new HistoricProcessInstanceEventEntity();
    event.setProcessDefinitionKey("some-other-process");

    // when
    handler.handleEvent(event);

    // then
    Assert.assertEquals(List.of(event), delegate.handledEvents);
  }

  @Test
  public void shouldPassThroughEventWithNoProcessDefinitionKey() {
    // given — a HistoricBatchEntity never carries a processDefinitionKey at all (see
    // ProcessDefinitionKeyFilteringHistoryEventHandler's class-level docs); confirms it is
    // never excluded, since there is nothing to match against.
    HistoricBatchEntity event = new HistoricBatchEntity();

    // when
    handler.handleEvent(event);

    // then
    Assert.assertEquals(List.of(event), delegate.handledEvents);
  }

  @Test
  public void shouldPassThroughEventWithProcessDefinitionKeyNotInExclusionList() {
    // given
    HistoricIncidentEventEntity event = new HistoricIncidentEventEntity();
    event.setProcessDefinitionKey("some-other-process");

    // when
    handler.handleEvent(event);

    // then
    Assert.assertEquals(List.of(event), delegate.handledEvents);
  }

  @Test
  public void shouldDropDecisionEvaluationEventWhenRootDecisionInstanceKeyIsExcluded() {
    // given — the envelope itself never carries processDefinitionKey; only the nested root
    // decision instance does (see ProcessDefinitionKeyFilteringHistoryEventHandler's docs).
    HistoricDecisionInstanceEntity rootDecisionInstance = new HistoricDecisionInstanceEntity();
    rootDecisionInstance.setProcessDefinitionKey("excluded-process");

    HistoricDecisionEvaluationEvent event = new HistoricDecisionEvaluationEvent();
    event.setRootHistoricDecisionInstance(rootDecisionInstance);

    // when
    handler.handleEvent(event);

    // then
    Assert.assertTrue(delegate.handledEvents.isEmpty());
  }

  @Test
  public void shouldPassThroughDecisionEvaluationEventWhenRootDecisionInstanceKeyIsNotExcluded() {
    // given
    HistoricDecisionInstanceEntity rootDecisionInstance = new HistoricDecisionInstanceEntity();
    rootDecisionInstance.setProcessDefinitionKey("some-other-process");

    HistoricDecisionEvaluationEvent event = new HistoricDecisionEvaluationEvent();
    event.setRootHistoricDecisionInstance(rootDecisionInstance);

    // when
    handler.handleEvent(event);

    // then
    Assert.assertEquals(List.of(event), delegate.handledEvents);
  }

  @Test
  public void shouldPassThroughDecisionEvaluationEventWithNoRootDecisionInstance() {
    // given — a standalone DecisionService evaluation has no process context at all.
    HistoricDecisionEvaluationEvent event = new HistoricDecisionEvaluationEvent();

    // when
    handler.handleEvent(event);

    // then
    Assert.assertEquals(List.of(event), delegate.handledEvents);
  }

  @Test
  public void shouldFilterHandleEventsToOnlyNonExcludedEvents() {
    // given
    HistoricProcessInstanceEventEntity excludedEvent = new HistoricProcessInstanceEventEntity();
    excludedEvent.setProcessDefinitionKey("excluded-process");

    HistoricProcessInstanceEventEntity keptEvent = new HistoricProcessInstanceEventEntity();
    keptEvent.setProcessDefinitionKey("some-other-process");

    List<HistoryEvent> events = new ArrayList<>(List.of(excludedEvent, keptEvent));

    // when
    handler.handleEvents(events);

    // then
    Assert.assertEquals(List.of(keptEvent), delegate.handledEvents);
  }

  @Test
  public void shouldNotCallDelegateHandleEventsWhenEverythingIsFilteredOut() {
    // given
    HistoricProcessInstanceEventEntity excludedEvent = new HistoricProcessInstanceEventEntity();
    excludedEvent.setProcessDefinitionKey("another-excluded-process");

    // when
    handler.handleEvents(new ArrayList<>(List.of(excludedEvent)));

    // then
    Assert.assertEquals(0, delegate.handleEventsInvocationCount);
  }

  protected static class RecordingHistoryEventHandler implements HistoryEventHandler {

    protected final List<HistoryEvent> handledEvents = new ArrayList<>();
    protected int handleEventsInvocationCount = 0;

    @Override
    public void handleEvent(HistoryEvent historyEvent) {
      handledEvents.add(historyEvent);
    }

    @Override
    public void handleEvents(List<HistoryEvent> historyEvents) {
      handleEventsInvocationCount++;
      handledEvents.addAll(historyEvents);
    }
  }

}
