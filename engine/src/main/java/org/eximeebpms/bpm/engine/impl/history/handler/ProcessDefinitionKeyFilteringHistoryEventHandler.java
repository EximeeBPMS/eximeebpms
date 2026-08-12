package org.eximeebpms.bpm.engine.impl.history.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eximeebpms.bpm.engine.impl.history.event.HistoricDecisionEvaluationEvent;
import org.eximeebpms.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.eximeebpms.bpm.engine.impl.history.event.HistoryEvent;

/**
 * Drops history events for process definitions listed in {@code excludedProcessDefinitionKeys}
 * before they reach {@code delegate}, regardless of the configured history level.
 *
 * <p>Filters at persistence time only: the {@link HistoryEvent} is still constructed by the
 * producer before this handler runs — this decorator only prevents it from being written to
 * {@code ACT_HI_*}. Events with no resolvable process definition key (e.g. batch history,
 * {@code HistoricBatchEntity}, which is never scoped to a single process definition) are never
 * excluded, by design — there is nothing to match against.
 */
public class ProcessDefinitionKeyFilteringHistoryEventHandler implements HistoryEventHandler {

  protected final HistoryEventHandler delegate;
  protected final Set<String> excludedProcessDefinitionKeys;

  public ProcessDefinitionKeyFilteringHistoryEventHandler(HistoryEventHandler delegate, Set<String> excludedProcessDefinitionKeys) {
    this.delegate = delegate;
    this.excludedProcessDefinitionKeys = excludedProcessDefinitionKeys;
  }

  @Override
  public void handleEvent(HistoryEvent historyEvent) {
    if (!isExcluded(historyEvent)) {
      delegate.handleEvent(historyEvent);
    }
  }

  @Override
  public void handleEvents(List<HistoryEvent> historyEvents) {
    List<HistoryEvent> filteredEvents = new ArrayList<>(historyEvents.size());
    for (HistoryEvent historyEvent : historyEvents) {
      if (!isExcluded(historyEvent)) {
        filteredEvents.add(historyEvent);
      }
    }

    if (!filteredEvents.isEmpty()) {
      delegate.handleEvents(filteredEvents);
    }
  }

  protected boolean isExcluded(HistoryEvent historyEvent) {
    String processDefinitionKey = extractProcessDefinitionKey(historyEvent);
    return processDefinitionKey != null && excludedProcessDefinitionKeys.contains(processDefinitionKey);
  }

  /**
   * {@link HistoricDecisionEvaluationEvent} — the envelope actually dispatched to
   * {@link HistoryEventHandler} for a DMN evaluation — never carries its own
   * {@code processDefinitionKey}; only its nested root {@link HistoricDecisionInstanceEntity}
   * does. Every other {@link HistoryEvent} subtype carries the key directly (when it has a
   * process context at all).
   */
  protected String extractProcessDefinitionKey(HistoryEvent historyEvent) {
    if (historyEvent instanceof HistoricDecisionEvaluationEvent historicDecisionEvaluationEvent) {
      HistoricDecisionInstanceEntity rootHistoricDecisionInstance =
          historicDecisionEvaluationEvent.getRootHistoricDecisionInstance();
      return rootHistoricDecisionInstance != null ? rootHistoricDecisionInstance.getProcessDefinitionKey() : null;
    }

    return historyEvent.getProcessDefinitionKey();
  }

}
