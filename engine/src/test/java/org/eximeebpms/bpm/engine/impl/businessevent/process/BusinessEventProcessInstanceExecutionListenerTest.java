package org.eximeebpms.bpm.engine.impl.businessevent.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.ExecutionListener;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessEventProcessInstanceExecutionListenerTest {

  private final BusinessEventProcessInstanceExecutionListener listener = new BusinessEventProcessInstanceExecutionListener();

  @Mock
  private DelegateExecution execution;

  @Mock
  private BusinessEventProducer producer;

  @Mock
  private BusinessEvent expectedEvent;

  @Test
  void shouldCreateProcessInstanceStartEvent() {
    // given
    when(execution.getEventName()).thenReturn(ExecutionListener.EVENTNAME_START);
    when(producer.createProcessInstanceStartEvt(execution)).thenReturn(expectedEvent);

    // when
    final BusinessEvent event = listener.createBusinessEvent(producer, execution);

    // then
    assertThat(event).isSameAs(expectedEvent);

    verify(producer).createProcessInstanceStartEvt(execution);
    verify(producer, never()).createProcessInstanceEndEvt(any());
    verify(producer, never()).createProcessInstanceUpdateEvt(any());
  }

  @Test
  void shouldCreateProcessInstanceEndEvent() {
    // given
    when(execution.getEventName()).thenReturn(ExecutionListener.EVENTNAME_END);
    when(producer.createProcessInstanceEndEvt(execution)).thenReturn(expectedEvent);

    // when
    final BusinessEvent event = listener.createBusinessEvent(producer, execution);

    // then
    assertThat(event).isSameAs(expectedEvent);

    verify(producer).createProcessInstanceEndEvt(execution);
    verify(producer, never()).createProcessInstanceStartEvt(any());
    verify(producer, never()).createProcessInstanceUpdateEvt(any());
  }

  @Test
  void shouldCreateProcessInstanceUpdateEvent() {
    // given
    when(execution.getEventName()).thenReturn(BusinessEventProcessInstanceExecutionListener.UPDATE);
    when(producer.createProcessInstanceUpdateEvt(execution)).thenReturn(expectedEvent);

    // when
    final BusinessEvent event = listener.createBusinessEvent(producer, execution);

    // then
    assertThat(event).isSameAs(expectedEvent);

    verify(producer).createProcessInstanceUpdateEvt(execution);
    verify(producer, never()).createProcessInstanceStartEvt(any());
    verify(producer, never()).createProcessInstanceEndEvt(any());
  }

  @Test
  void shouldReturnNullForUnsupportedEvent() {
    // given
    when(execution.getEventName()).thenReturn("unsupported");

    // when
    final BusinessEvent event = listener.createBusinessEvent(producer, execution);

    // then
    assertThat(event).isNull();

    verify(producer, never()).createProcessInstanceStartEvt(any());
    verify(producer, never()).createProcessInstanceEndEvt(any());
    verify(producer, never()).createProcessInstanceUpdateEvt(any());
  }
}
