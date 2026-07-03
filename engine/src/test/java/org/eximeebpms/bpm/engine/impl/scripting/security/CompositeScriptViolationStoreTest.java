/*
 * Copyright EximeeBPMS contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eximeebpms.bpm.engine.impl.scripting.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CompositeScriptViolationStoreTest {

  @Mock
  private ScriptViolationStore first;
  @Mock
  private ScriptViolationStore second;

  @Test
  public void shouldDelegateRecordToAllStores() {
    // given
    CompositeScriptViolationStore composite = new CompositeScriptViolationStore(List.of(first, second));
    ScriptViolationEvent event = sampleEvent();

    // when
    composite.record(event);

    // then
    verify(first).record(event);
    verify(second).record(event);
  }

  @Test
  public void shouldDelegateGetRecentToFirstStore() {
    // given
    ScriptViolationEvent event = sampleEvent();
    when(first.getRecent(10)).thenReturn(List.of(event));
    CompositeScriptViolationStore composite = new CompositeScriptViolationStore(List.of(first, second));

    // when
    List<ScriptViolationEvent> result = composite.getRecent(10);

    // then
    assertThat(result).containsExactly(event);
    verifyNoInteractions(second);
  }

  @Test
  public void shouldDelegateGetTotalCountToFirstStore() {
    // given
    when(first.getTotalCount()).thenReturn(77L);
    CompositeScriptViolationStore composite = new CompositeScriptViolationStore(List.of(first, second));

    // when
    long result = composite.getTotalCount();

    // then
    assertThat(result).isEqualTo(77L);
    verifyNoInteractions(second);
  }

  @Test
  public void shouldRejectEmptyDelegateList() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new CompositeScriptViolationStore(Collections.emptyList()))
        .withMessageContaining("delegates must not be empty");
  }

  @Test
  public void shouldRejectNullDelegateList() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new CompositeScriptViolationStore(null))
        .withMessageContaining("delegates must not be empty");
  }

  private ScriptViolationEvent sampleEvent() {
    return new ScriptViolationEvent(
        Instant.now(), "proc", null, "task", "javascript",
        ScriptSourceType.INLINE_SOURCE, ScriptOrigin.USER,
        "RULE", "reason");
  }
}
