package org.eximeebpms.bpm.engine.impl.batch.history;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

public class HistoricBatchEntityTest {

  @Test
  public void shouldSerializeReflectivelyWithoutDuplicateIdField() {
    // given
    HistoricBatchEntity entity = new HistoricBatchEntity();
    entity.setId("a-batch-id");
    entity.setType("migration");

    // when
    String json = new Gson().toJson(entity);

    // then
    Assert.assertTrue(json.contains("\"id\":\"a-batch-id\""));
  }

  @Test
  public void shouldRoundTripIdThroughInheritedAccessors() {
    // given
    HistoricBatchEntity entity = new HistoricBatchEntity();

    // when
    entity.setId("another-batch-id");

    // then
    Assert.assertEquals("another-batch-id", entity.getId());
  }

}
