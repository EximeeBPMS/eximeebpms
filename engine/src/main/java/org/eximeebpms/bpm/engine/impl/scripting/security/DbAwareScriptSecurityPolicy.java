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
   * Wires the {@link ManagementService} and, if {@code ACT_GE_PROPERTY} has no
   * {@link #PROP_MODE} row yet, seeds it from this policy's {@code initialConfig} — first-start
   * bootstrap shared by every deployment model that installs this policy (Spring Boot's
   * {@code ScriptSecurityAutoConfiguration}, the plain container/Tomcat bootstrap via
   * {@code StartProcessEngineStep}). After this call, {@code ACT_GE_PROPERTY} is authoritative:
   * a later change to the deployment's own configuration (YAML property, {@code bpm-platform.xml})
   * has no further effect — only {@code PUT /script-security/config} (or a direct
   * {@code ManagementService.setProperty} call) can change the mode from then on.
   */
  public void wireAndSeed(ManagementService managementService) {
    setManagementService(managementService);

    Map<String, String> existingProps = managementService.getProperties();
    if (!existingProps.containsKey(PROP_MODE)) {
      managementService.setProperty(PROP_MODE, initialConfig.mode().name());
      managementService.setProperty(PROP_ALLOWLIST, String.join(",", initialConfig.allowlistedKeys()));
    }
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
    ScriptSecurityPolicy policy = config.disabled()
        ? AlwaysAllowScriptSecurityPolicy.INSTANCE
        : new DefaultScriptSecurityPolicy(config.allowlistedKeys(), config.auditMode(), violationStore, listeners);
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
      String allowlistRaw = props.getOrDefault(PROP_ALLOWLIST, "");
      Set<String> keys = parseAllowlist(allowlistRaw);
      return new Config(resolveMode(mode), keys);
    } catch (Exception e) {
      log.warn("Failed to load script security config from DB, using cached/initial config: {}", e.getMessage());
      return initialConfig;
    }
  }

  /**
   * Case-insensitive {@link ScriptSecurityMode#valueOf}, falling back to {@code ENFORCE} for a
   * value that doesn't match any mode (e.g. a legacy row predating stricter validation, or a
   * direct {@code ManagementService.setProperty} bypassing the REST API's own check).
   */
  private static ScriptSecurityMode resolveMode(String mode) {
    try {
      return ScriptSecurityMode.valueOf(mode.toUpperCase());
    } catch (IllegalArgumentException e) {
      return ScriptSecurityMode.ENFORCE;
    }
  }

  /**
   * The runtime counterpart of a startup-time {@code DISABLED}
   * (see {@code ProcessEngineConfigurationImpl.initScriptSecurityPolicy()}, which skips
   * installing any policy at all). Reached only via {@code PUT /script-security/config} with
   * {@code mode: DISABLED} switching an already-running {@link DbAwareScriptSecurityPolicy} —
   * there is no other way to reach {@code DISABLED} after engine startup.
   */
  private static final class AlwaysAllowScriptSecurityPolicy implements ScriptSecurityPolicy {
    static final AlwaysAllowScriptSecurityPolicy INSTANCE = new AlwaysAllowScriptSecurityPolicy();

    @Override
    public ScriptSecurityDecision evaluate(ScriptSecurityContext context) {
      return ScriptSecurityDecision.allow();
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

  public record Config(ScriptSecurityMode mode, Set<String> allowlistedKeys) {
    public Config {
      allowlistedKeys = allowlistedKeys != null ? Set.copyOf(allowlistedKeys) : Set.of();
      mode = mode != null ? mode : ScriptSecurityMode.ENFORCE;
    }

    public boolean auditMode() {
      return mode == ScriptSecurityMode.AUDIT;
    }

    public boolean disabled() {
      return mode == ScriptSecurityMode.DISABLED;
    }

    public static Config enforce(Set<String> allowlistedKeys) {
      return new Config(ScriptSecurityMode.ENFORCE, allowlistedKeys);
    }

    public static Config audit(Set<String> allowlistedKeys) {
      return new Config(ScriptSecurityMode.AUDIT, allowlistedKeys);
    }

    public static Config disabled(Set<String> allowlistedKeys) {
      return new Config(ScriptSecurityMode.DISABLED, allowlistedKeys);
    }
  }
}
