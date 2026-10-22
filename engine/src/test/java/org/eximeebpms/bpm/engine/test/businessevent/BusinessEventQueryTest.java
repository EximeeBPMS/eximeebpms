package org.eximeebpms.bpm.engine.test.businessevent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.eximeebpms.bpm.engine.BusinessEventService;
import org.eximeebpms.bpm.engine.businessevent.BusinessEventOutbox;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventConfiguration;
import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.junit.Test;

public class BusinessEventQueryTest extends AbstractBusinessEventIT {

  /**
   * Job events are disabled so the outbox cleanup job's own {@code job:create} does not
   * land in the outbox these tests query.
   */
  @Override
  protected BusinessEventConfiguration businessEventConfiguration() {
    return BusinessEventConfiguration.builder()
        .enabled(true)
        .disabledEventTypes(Set.of("job"))
        .build();
  }

  @Test
  public void shouldCountOnlyUnprocessedRecords() {
    insert("DELIVERED", true, new Date(1_000L));
    insert("PENDING_1", false, new Date(2_000L));
    insert("PENDING_2", false, new Date(3_000L));

    BusinessEventService service = engineRule.getProcessEngine().getBusinessEventService();

    assertThat(service.createBusinessEventOutboxQuery().count()).isEqualTo(3);
    assertThat(service.createBusinessEventOutboxQuery().unprocessed().count()).isEqualTo(2);
  }

  @Test
  public void shouldReturnOldestUnprocessedRecordFirst() {
    insert("DELIVERED", true, new Date(1_000L));
    insert("PENDING_OLDEST", false, new Date(2_000L));
    insert("PENDING_NEWER", false, new Date(3_000L));

    List<BusinessEventOutbox> oldest = engineRule.getProcessEngine().getBusinessEventService()
        .createBusinessEventOutboxQuery()
        .unprocessed()
        .listPage(0, 1);

    assertThat(oldest).hasSize(1);
    assertThat(oldest.get(0).isProcessed()).isFalse();
    assertThat(oldest.get(0).getCreatedDate().getTime()).isEqualTo(2_000L);
  }

  private void insert(String eventType, boolean processed, Date createdDate) {
    commandExecutor.execute(ctx -> {
      ctx.getDbEntityManager().insertWithoutId(BusinessEventOutboxEntity.builder()
          .createdDate(createdDate)
          .eventType(eventType)
          .businessEvent("{\"eventType\":\"" + eventType + "\"}")
          .processed(processed)
          .processedDate(processed ? createdDate : null)
          .build());
      return null;
    });
  }
}
