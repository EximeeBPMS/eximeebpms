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
package org.eximeebpms.bpm.engine.impl.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.eximeebpms.bpm.engine.impl.db.entitymanager.cache.CachedDbEntity;
import org.eximeebpms.bpm.engine.impl.db.entitymanager.cache.DbEntityState;
import org.junit.jupiter.api.Test;

/**
 * A property whose value is null must still report a persistent state, because the dirty
 * check dereferences whatever {@code getPersistentState()} returns.
 *
 * <p>This is not hypothetical and not reachable only through a null: Oracle stores the
 * empty string as NULL, so a property legitimately written as "" - an empty
 * script-security allowlist is one the engine writes at startup - reads back null. The
 * engine then failed every command context close after that with a
 * NullPointerException, which took 18 of the 187 integration tests down on Oracle and
 * nowhere else (BPMS-755).
 */
class PropertyEntityPersistentStateTest {

  @Test
  void shouldReportPersistentStateWhenValueIsNull() {
    PropertyEntity property = new PropertyEntity("script.security.allowlist", null);

    assertThat(property.getPersistentState()).isNotNull();
  }

  @Test
  void shouldNotBeDirtyWhenValueStaysNull() {
    CachedDbEntity cached = cache(new PropertyEntity("script.security.allowlist", null));

    assertThat(cached.isDirty()).isFalse();
  }

  @Test
  void shouldBeDirtyWhenValueIsSetFromNull() {
    PropertyEntity property = new PropertyEntity("script.security.allowlist", null);
    CachedDbEntity cached = cache(property);

    property.setValue("beans");

    assertThat(cached.isDirty()).isTrue();
  }

  @Test
  void shouldBeDirtyWhenValueIsClearedToNull() {
    PropertyEntity property = new PropertyEntity("script.security.allowlist", "beans");
    CachedDbEntity cached = cache(property);

    property.setValue(null);

    assertThat(cached.isDirty()).isTrue();
  }

  private static CachedDbEntity cache(PropertyEntity property) {
    CachedDbEntity cached = new CachedDbEntity();
    cached.setEntity(property);
    cached.setEntityState(DbEntityState.PERSISTENT);
    cached.makeCopy();
    return cached;
  }
}
