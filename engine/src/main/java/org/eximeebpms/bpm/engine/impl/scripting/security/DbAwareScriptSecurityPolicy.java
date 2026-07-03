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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eximeebpms.bpm.engine.ManagementService;

/**
 * A {@link ScriptSecurityPolicy} that reads its configuration from {@code ACT_GE_PROPERTY} on
 * every evaluation, with a short-lived TTL cache to avoid a DB round-trip per script. This
 * enables multi-pod hot-reload: any pod that receives {@code POST /actuator/refresh} writes new
 * values to the shared DB, and all other pods pick them up within one TTL period.
 *
 * <p>Before the {@link ManagementService} is wired (i.e., before the engine finishes starting),
 * the policy falls back to the {@code initialConfig} provided at construction time — typically
 * the values read from the application environment.
 */
@Slf4j
public class DbAwareScriptSecurityPolicy implements ScriptSecurityPolicy {

  public static final String PROP_MODE = "script.security.mode";
  public static final String PROP_ALLOWLIST = "script.security.allowlist";

  static final long DEFAULT_TTL_MS = 30_000L;

  private final Config initialConfig;
  private final ScriptViolationStore violationStore;
  private final long ttlMs;
  /**
   * -- GETTER --
   *  Called after the engine starts, wires in the ManagementService and immediately invalidates
   *  the cache so the next evaluate() reads fresh config from the DB.
   */
  @Getter
  private final List<ScriptViolationListener> listeners;

  private final AtomicReference<ManagementService> managementServiceRef = new AtomicReference<>();
  private final AtomicReference<CachedPolicy> cachedPolicyRef = new AtomicReference<>();

  public DbAwareScriptSecurityPolicy(Config initialConfig, ScriptViolationStore violationStore) {
    this(initialConfig, violationStore, DEFAULT_TTL_MS, List.of());
  }

  public DbAwareScriptSecurityPolicy(Config initialConfig, ScriptViolationStore violationStore, long ttlMs) {
    this(initialConfig, violationStore, ttlMs, List.of());
  }

  public DbAwareScriptSecurityPolicy(Config initialConfig, ScriptViolationStore violationStore,
      List<ScriptViolationListener> listeners) {
    this(initialConfig, violationStore, DEFAULT_TTL_MS, listeners);
  }

  public DbAwareScriptSecurityPolicy(Config initialConfig, ScriptViolationStore violationStore, long ttlMs,
      List<ScriptViolationListener> listeners) {
    this.initialConfig = Objects.requireNonNull(initialConfig, "initialConfig must not be null");
    this.violationStore = Objects.requireNonNull(violationStore, "violationStore must not be null");
    this.ttlMs = ttlMs;
    this.listeners = List.copyOf(Objects.requireNonNull(listeners, "listeners must not be null"));
  }

  public void setManagementService(ManagementService managementService) {
    managementServiceRef.set(Objects.requireNonNull(managementService));
    invalidateCache();
  }

  /**
   * Forces the next {@link #evaluate} call to re-read config from the DB. Called by
   * {@code ScriptSecurityPolicyRefresher} after writing new config on {@code /actuator/refresh}.
   */
  public void invalidateCache() {
    cachedPolicyRef.set(null);
  }

  @Override
  public ScriptSecurityDecision evaluate(ScriptSecurityContext context) {
    return effectivePolicy().evaluate(context);
  }

  private ScriptSecurityPolicy effectivePolicy() {
    CachedPolicy current = cachedPolicyRef.get();
    if (current != null && current.isFresh()) {
      return current.policy();
    }
    return refreshCache();
  }

  private synchronized ScriptSecurityPolicy refreshCache() {
    CachedPolicy current = cachedPolicyRef.get();
    if (current != null && current.isFresh()) {
      return current.policy();
    }

    Config config = loadConfig();
    DefaultScriptSecurityPolicy policy = new DefaultScriptSecurityPolicy(
        config.allowlistedKeys(), config.auditMode(), violationStore, listeners);
    cachedPolicyRef.set(new CachedPolicy(policy, System.currentTimeMillis() + ttlMs));
    return policy;
  }

  private Config loadConfig() {
    ManagementService mgmt = managementServiceRef.get();
    if (mgmt == null) {
      return initialConfig;
    }
    try {
      Map<String, String> props = mgmt.getProperties();
      String mode = props.get(PROP_MODE);
      if (mode == null) {
        return initialConfig;
      }
      boolean auditMode = "AUDIT".equalsIgnoreCase(mode);
      String allowlistRaw = props.getOrDefault(PROP_ALLOWLIST, "");
      Set<String> keys = parseAllowlist(allowlistRaw);
      return new Config(auditMode, keys);
    } catch (Exception e) {
      log.warn("Failed to load script security config from DB, using cached/initial config: {}", e.getMessage());
      return initialConfig;
    }
  }

  private static Set<String> parseAllowlist(String raw) {
    if (raw == null || raw.isBlank()) {
      return Set.of();
    }
    return Arrays.stream(raw.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toUnmodifiableSet());
  }

  private record CachedPolicy(ScriptSecurityPolicy policy, long expiresAt) {
    boolean isFresh() {
      return System.currentTimeMillis() < expiresAt;
    }
  }

  public record Config(boolean auditMode, Set<String> allowlistedKeys) {
    public Config {
      allowlistedKeys = allowlistedKeys != null ? Set.copyOf(allowlistedKeys) : Set.of();
    }

    public static Config enforce(Set<String> allowlistedKeys) {
      return new Config(false, allowlistedKeys);
    }

    public static Config audit(Set<String> allowlistedKeys) {
      return new Config(true, allowlistedKeys);
    }
  }
}
