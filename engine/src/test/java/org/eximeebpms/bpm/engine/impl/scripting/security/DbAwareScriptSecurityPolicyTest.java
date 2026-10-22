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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.AUDIT.name(),
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
        DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.ENFORCE.name(),
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    policy.setManagementService(management);
    policy.evaluate(BLOCKED_CONTEXT); // warm the cache

    // when — config changes in DB but cache is still valid; stub is intentionally unreachable
    lenient().when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.AUDIT.name(),
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
        DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.ENFORCE.name(),
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    policy.setManagementService(management);
    policy.evaluate(BLOCKED_CONTEXT); // warm the cache

    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.AUDIT.name(),
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
        DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.ENFORCE.name(),
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    policy.setManagementService(management);
    policy.evaluate(BLOCKED_CONTEXT); // warm the cache

    Thread.sleep(20); // wait for TTL to expire

    // when
    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.AUDIT.name(),
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    ScriptSecurityDecision decision = policy.evaluate(BLOCKED_CONTEXT);

    // then
    assertThat(decision.isAudit()).isTrue();
  }

  @Test
  public void shouldTreatUnrecognizedStoredModeAsEnforce() {
    // given — e.g. a legacy row predating stricter mode validation, or a direct
    // ManagementService.setProperty bypassing the REST API's own check
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.audit(Set.of()),
        NoOpScriptViolationStore.INSTANCE);

    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, "not-a-real-mode",
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    policy.setManagementService(management);

    // when
    ScriptSecurityDecision decision = policy.evaluate(BLOCKED_CONTEXT);

    // then — falls back to ENFORCE, not the (AUDIT) initialConfig and not DISABLED
    assertThat(decision.isAllowed()).isFalse();
    assertThat(decision.isAudit()).isFalse();
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
  public void switchingFromAuditToDisabledViaRestActuallyDisablesEnforcement() {
    // given — engine started in AUDIT (e.g. seeded at first boot), matching
    // ScriptSecurityRestServiceImpl.getConfig()'s reported state before the switch
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.audit(Set.of()),
        NoOpScriptViolationStore.INSTANCE);

    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.AUDIT.name(),
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    policy.setManagementService(management);

    assertThat(policy.evaluate(BLOCKED_CONTEXT).isAudit())
        .as("sanity check: engine is actually in AUDIT before the switch")
        .isTrue();

    // when — mirrors exactly what ScriptSecurityRestServiceImpl.updateConfig() does for
    // PUT /script-security/config { "mode": "DISABLED" }: write the DB property, then
    // invalidate this node's cache so the change is picked up immediately
    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.DISABLED.name(),
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));
    policy.invalidateCache();
    ScriptSecurityDecision decision = policy.evaluate(BLOCKED_CONTEXT);

    // then — loadConfig() now recognizes DISABLED explicitly and refreshCache() swaps in
    // AlwaysAllowScriptSecurityPolicy instead of building a DefaultScriptSecurityPolicy, so a
    // runtime switch to DISABLED via REST behaves like DISABLED, not like a fallen-through
    // ENFORCE (see the regression this locks in: previously only "AUDIT" was special-cased,
    // so "DISABLED" fell through to auditMode=false — i.e. stricter enforcement than before).
    assertThat(decision.isAllowed()).isTrue();
    assertThat(decision.isAudit()).isFalse();
  }

  @Test
  public void shouldAllowAllowlistedProcessDefinitionKey() {
    // given
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.enforce(Set.of()),
        NoOpScriptViolationStore.INSTANCE);

    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.ENFORCE.name(),
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

  @Test
  public void wireAndSeedShouldSeedDbFromInitialConfigWhenAbsent() {
    // given
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.audit(Set.of("legacyInvoiceProcess")),
        NoOpScriptViolationStore.INSTANCE);

    when(management.getProperties()).thenReturn(Map.of());

    // when
    policy.wireAndSeed(management);

    // then
    verify(management).setProperty(DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.AUDIT.name());
    verify(management).setProperty(DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, "legacyInvoiceProcess");
  }

  @Test
  public void wireAndSeedShouldNotOverwriteExistingDbRow() {
    // given
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.audit(Set.of()),
        NoOpScriptViolationStore.INSTANCE);

    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.ENFORCE.name()));

    // when — bpm-platform.xml/YAML says AUDIT, but a DB row already exists from a prior start
    policy.wireAndSeed(management);

    // then — the existing DB row wins; this deployment's own config no longer has any effect
    verify(management, never()).setProperty(org.mockito.ArgumentMatchers.eq(DbAwareScriptSecurityPolicy.PROP_MODE),
        org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void wireAndSeedShouldWireManagementServiceSoSubsequentEvaluationsReadFromDb() {
    // given
    DbAwareScriptSecurityPolicy policy = new DbAwareScriptSecurityPolicy(
        DbAwareScriptSecurityPolicy.Config.enforce(Set.of()),
        NoOpScriptViolationStore.INSTANCE);

    when(management.getProperties()).thenReturn(Map.of(
        DbAwareScriptSecurityPolicy.PROP_MODE, ScriptSecurityMode.AUDIT.name(),
        DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, ""));

    // when
    policy.wireAndSeed(management);
    ScriptSecurityDecision decision = policy.evaluate(BLOCKED_CONTEXT);

    // then — even though initialConfig was ENFORCE, the (pre-existing) DB row's AUDIT wins
    assertThat(decision.isAudit()).isTrue();
  }
}
