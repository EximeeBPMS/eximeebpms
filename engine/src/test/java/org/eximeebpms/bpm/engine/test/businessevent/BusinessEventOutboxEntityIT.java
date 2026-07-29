package org.eximeebpms.bpm.engine.test.businessevent;

import org.eximeebpms.bpm.engine.impl.persistence.entity.BusinessEventOutboxEntity;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link BusinessEventOutboxEntity} persistence against the H2 database.
 *
 * <p>Covers the three fundamental operations:
 * <ul>
 *   <li><b>Save</b> — {@code insertWithoutId} triggers the identity-column INSERT,
 *       the DB assigns the primary key, and all fields round-trip correctly.</li>
 *   <li><b>Select by ID</b> — {@code selectById} returns the same entity that was saved,
 *       with every column value intact.</li>
 *   <li><b>Delete by ID</b> — {@code delete(BusinessEventOutboxEntity.class, …)} removes
 *       the row; a subsequent select returns {@code null}.</li>
 * </ul>
 */
public class BusinessEventOutboxEntityIT extends AbstractBusinessEventIT {

    // -------------------------------------------------------------------------
    // Save
    // -------------------------------------------------------------------------

    /**
     * GIVEN a saved entity with all fields populated
     * WHEN  it is saved and then re-read via {@code selectById}
     * THEN  the retrieved entity has all fields equal to the original
     */
    @Test
    public void shouldSave_andPersistAllFieldsCorrectly() {
        Date createdDate = new Date();
        String payload = "{\"taskId\":\"t-2\",\"assignee\":\"alice\"}";

        // Save (ID not returned — identity column assigned by DB)
        commandExecutor.execute(ctx -> {
            BusinessEventOutboxEntity entity = BusinessEventOutboxEntity.builder()
                    .createdDate(createdDate)
                    .eventType("TASK_ASSIGNED")
                    .businessEvent(payload)
                    .processInstanceId("pi-2")
                    .rootProcessInstanceId("root-pi-2")
                    .processDefinitionKey("onboarding-process")
                    .taskId("t-2")
                    .processed(false)
                    .build();
            ctx.getDbEntityManager().insertWithoutId(entity);
            return null;
        });

        // Query back in a separate transaction to get the DB-assigned ID and all persisted values
        BusinessEventOutboxEntity reloaded = findByProcessInstanceId("pi-2");

        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getIdAsLong()).as("DB must assign a positive identity key").isPositive();
        assertThat(reloaded.getEventType()).isEqualTo("TASK_ASSIGNED");
        assertThat(reloaded.getBusinessEvent()).isEqualTo(payload);
        assertThat(reloaded.getProcessInstanceId()).isEqualTo("pi-2");
        assertThat(reloaded.getRootProcessInstanceId()).isEqualTo("root-pi-2");
        assertThat(reloaded.getProcessDefinitionKey()).isEqualTo("onboarding-process");
        assertThat(reloaded.getTaskId()).isEqualTo("t-2");
        assertThat(reloaded.isProcessed()).isFalse();
        assertThat(reloaded.getProcessedDate()).isNull();
        assertThat(reloaded.getCreatedDate()).isEqualTo(createdDate);
    }

    // -------------------------------------------------------------------------
    // Select by ID
    // -------------------------------------------------------------------------

    /**
     * GIVEN a saved entity
     * WHEN  {@code selectById} is called with its DB-assigned ID
     * THEN  the correct entity is returned
     */
    @Test
    public void shouldSelectById_returningCorrectEntity() {
        BusinessEventOutboxEntity saved = saveMinimalEntity("PROCESS_STARTED", "pi-3");

        BusinessEventOutboxEntity found = selectById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getEventType()).isEqualTo("PROCESS_STARTED");
        assertThat(found.getProcessInstanceId()).isEqualTo("pi-3");
    }

    /**
     * GIVEN two saved entities
     * WHEN  {@code selectById} is called for each
     * THEN  only the matching entity is returned for each ID
     */
    @Test
    public void shouldSelectById_returningOnlyTheRequestedEntity() {
        BusinessEventOutboxEntity first = saveMinimalEntity("EVENT_A", "pi-a");
        BusinessEventOutboxEntity second = saveMinimalEntity("EVENT_B", "pi-b");

        BusinessEventOutboxEntity foundFirst = selectById(first.getId());
        BusinessEventOutboxEntity foundSecond = selectById(second.getId());

        assertThat(foundFirst.getId()).isEqualTo(first.getId());
        assertThat(foundFirst.getEventType()).isEqualTo("EVENT_A");

        assertThat(foundSecond.getId()).isEqualTo(second.getId());
        assertThat(foundSecond.getEventType()).isEqualTo("EVENT_B");
    }

    /**
     * GIVEN no entity stored with a given ID
     * WHEN  {@code selectById} is called with that ID
     * THEN  {@code null} is returned
     */
    @Test
    public void shouldSelectById_returningNull_whenEntityDoesNotExist() {
        BusinessEventOutboxEntity result = selectById("999999999");

        assertThat(result).isNull();
    }

    // -------------------------------------------------------------------------
    // Delete by ID
    // -------------------------------------------------------------------------

    /**
     * GIVEN a saved entity
     * WHEN  it is deleted by ID
     * THEN  a subsequent {@code selectById} returns {@code null}
     */
    @Test
    public void shouldDeleteById_makingEntityUnreachableBySelectById() {
        BusinessEventOutboxEntity saved = saveMinimalEntity("TASK_COMPLETED", "pi-4");
        String id = saved.getId();

        // Delete
        commandExecutor.execute(ctx -> {
            ctx.getDbEntityManager().delete(
                    BusinessEventOutboxEntity.class,
                    "deleteBusinessEventOutbox",
                    saved.getIdAsLong());
            return null;
        });

        assertThat(selectById(id))
                .as("entity must not be found after deletion")
                .isNull();
    }

    /**
     * GIVEN two saved entities
     * WHEN  only one is deleted by ID
     * THEN  the deleted one is gone and the other is still retrievable
     */
    @Test
    public void shouldDeleteById_notAffectingOtherEntities() {
        BusinessEventOutboxEntity toDelete = saveMinimalEntity("EVENT_DELETE", "pi-5");
        BusinessEventOutboxEntity toKeep = saveMinimalEntity("EVENT_KEEP", "pi-6");

        commandExecutor.execute(ctx -> {
            ctx.getDbEntityManager().delete(
                    BusinessEventOutboxEntity.class,
                    "deleteBusinessEventOutbox",
                    toDelete.getIdAsLong());
            return null;
        });

        assertThat(selectById(toDelete.getId()))
                .as("deleted entity must not be found")
                .isNull();
        assertThat(selectById(toKeep.getId()))
                .as("unrelated entity must still be present")
                .isNotNull()
                .extracting(BusinessEventOutboxEntity::getEventType)
                .isEqualTo("EVENT_KEEP");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Inserts a minimal entity and queries it back in a second transaction to obtain
     * the DB-assigned identity key. Uses a unique {@code processInstanceId} as the
     * lookup key because {@code insertWithoutId} does not populate the entity's {@code id}.
     */
    private BusinessEventOutboxEntity saveMinimalEntity(String eventType, String processInstanceId) {
        commandExecutor.execute(ctx -> {
            ctx.getDbEntityManager().insertWithoutId(
                    BusinessEventOutboxEntity.builder()
                            .createdDate(new Date())
                            .eventType(eventType)
                            .businessEvent("{\"eventType\":\"" + eventType + "\"}")
                            .processInstanceId(processInstanceId)
                            .processed(false)
                            .build());
            return null;
        });
        // Separate transaction — queries the DB directly, not the entity cache
        return findByProcessInstanceId(processInstanceId);
    }

    /**
     * Returns the first outbox record whose {@code PROC_INST_ID_} matches, or {@code null}.
     * Used to retrieve the DB-assigned ID after an identity-column insert.
     */
    @SuppressWarnings("unchecked")
    private BusinessEventOutboxEntity findByProcessInstanceId(String processInstanceId) {
        return commandExecutor.execute(ctx -> {
            List<BusinessEventOutboxEntity> results = ctx.getDbEntityManager()
                    .selectList("selectBusinessEventOutboxByProcInstId", processInstanceId);
            return results.isEmpty() ? null : results.get(0);
        });
    }

    /**
     * Loads an entity by its string ID in a fresh transaction, bypassing any entity cache.
     */
    private BusinessEventOutboxEntity selectById(String id) {
        return commandExecutor.execute(ctx ->
                ctx.getDbEntityManager().selectById(BusinessEventOutboxEntity.class, id));
    }
}
