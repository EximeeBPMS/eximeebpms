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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.eximeebpms.bpm.engine.ManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DbAwareScriptSecurityPolicyTest {

  @Mock
  private ManagementService management;

  private static final ScriptSecurityContext BLOCKED_CONTEXT =
      ScriptSecurityContext.builder("javascript")
          .source("System.getenv('HOME');")
          .sourceType(ScriptSourceType.INLINE_SOURCE)
          .build();

  @Test
  public void shouldUseInitialConfigBeforeManagementServiceIsWired() {
    // given
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.enforce(Set.of()),
        NoOpScriptViolationStore.INSTANCE);

    // when
    ScriptSecurityDecision decision = policy.evaluate(BLOCKED_CONTEXT);

    // then
    assertThat(decision.isAllowed()).isFalse();
    assertThat(decision.isAudit()).isFalse();
  }

  @Test
  public void shouldReadConfigFromDbAfterManagementServiceIsWired() {
    // given
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.enforce(Set.of()),
        NoOpScriptViolationStore.INSTANCE);

    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, "AUDIT",
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    policy.setManagementService(management);

    // when
    ScriptSecurityDecision decision = policy.evaluate(BLOCKED_CONTEXT);

    // then
    assertThat(decision.isAudit()).isTrue();
  }

  @Test
  public void shouldCacheConfigWithinTtl() {
    // given
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.enforce(Set.of()),
        NoOpScriptViolationStore.INSTANCE,
        60_000L);

    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, "ENFORCE",
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    policy.setManagementService(management);
    policy.evaluate(BLOCKED_CONTEXT); // warm the cache

    // when — config changes in DB but cache is still valid; stub is intentionally unreachable
    lenient().when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, "AUDIT",
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    ScriptSecurityDecision decision = policy.evaluate(BLOCKED_CONTEXT);

    // then — cached ENFORCE mode is used
    assertThat(decision.isAudit()).isFalse();
  }

  @Test
  public void shouldPickUpNewConfigAfterCacheInvalidation() {
    // given
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.enforce(Set.of()),
        NoOpScriptViolationStore.INSTANCE,
        60_000L);

    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, "ENFORCE",
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    policy.setManagementService(management);
    policy.evaluate(BLOCKED_CONTEXT); // warm the cache

    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, "AUDIT",
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));

    // when
    policy.invalidateCache();
    ScriptSecurityDecision decision = policy.evaluate(BLOCKED_CONTEXT);

    // then
    assertThat(decision.isAudit()).isTrue();
  }

  @Test
  public void shouldPickUpNewConfigAfterTtlExpires() throws InterruptedException {
    // given
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.enforce(Set.of()),
        NoOpScriptViolationStore.INSTANCE,
        10L); // 10 ms TTL

    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, "ENFORCE",
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    policy.setManagementService(management);
    policy.evaluate(BLOCKED_CONTEXT); // warm the cache

    Thread.sleep(20); // wait for TTL to expire

    // when
    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, "AUDIT",
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    ScriptSecurityDecision decision = policy.evaluate(BLOCKED_CONTEXT);

    // then
    assertThat(decision.isAudit()).isTrue();
  }

  @Test
  public void shouldFallBackToInitialConfigWhenDbReadFails() {
    // given
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.audit(Set.of()),
        NoOpScriptViolationStore.INSTANCE);

    when(management.getProperties()).thenThrow(new RuntimeException("DB unavailable"));
    policy.setManagementService(management);

    // when
    ScriptSecurityDecision decision = policy.evaluate(BLOCKED_CONTEXT);

    // then — falls back to initialConfig which is AUDIT
    assertThat(decision.isAudit()).isTrue();
  }

  @Test
  public void shouldAllowAllowlistedProcessDefinitionKey() {
    // given
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.enforce(Set.of()),
        NoOpScriptViolationStore.INSTANCE);

    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, "ENFORCE",
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, "my-process"));
    policy.setManagementService(management);

    ScriptSecurityContext allowlistedCtx = ScriptSecurityContext.builder("javascript")
        .source("System.getenv('HOME');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .processDefinitionKey("my-process")
        .build();

    // when
    ScriptSecurityDecision decision = policy.evaluate(allowlistedCtx);

    // then
    assertThat(decision.isAllowed()).isTrue();
  }
}
