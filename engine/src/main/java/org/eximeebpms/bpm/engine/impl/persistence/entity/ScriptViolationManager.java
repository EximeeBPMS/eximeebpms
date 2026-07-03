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

import java.util.Date;
import java.util.List;
import org.eximeebpms.bpm.engine.impl.Page;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.persistence.AbstractManager;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEntity;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;

public class ScriptViolationManager extends AbstractManager {

  public void insert(ScriptViolationEvent event) {
    final ScriptViolationEntity entity = ScriptViolationEntity.fromEvent(event);
    entity.setId(Context.getProcessEngineConfiguration().getIdGenerator().getNextId());
    getDbEntityManager().insert(entity);
  }

  @SuppressWarnings("unchecked")
  public List<ScriptViolationEvent> findRecent(int limit) {
    List<ScriptViolationEntity> entities = getDbEntityManager().selectList(
        "selectRecentScriptViolations", (Object) null, new Page(0, limit));
    return entities.stream().map(ScriptViolationEntity::toEvent).toList();
  }

  public long countAll() {
    return (Long) getDbEntityManager().selectOne("countScriptViolations", null);
  }

  public void deleteOlderThan(Date cutoffDate) {
    getDbEntityManager().delete(ScriptViolationEntity.class, "deleteScriptViolationsOlderThan", cutoffDate);
  }
}
