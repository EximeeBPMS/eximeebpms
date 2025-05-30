/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.eximeebpms.bpm.engine.test.api.authorization.externaltask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eximeebpms.bpm.engine.authorization.Authorization.ANY;
import static org.eximeebpms.bpm.engine.authorization.Permissions.ALL;
import static org.eximeebpms.bpm.engine.authorization.Permissions.READ;
import static org.eximeebpms.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.eximeebpms.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.eximeebpms.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.junit.Assert.assertEquals;

import java.util.List;
import org.eximeebpms.bpm.engine.externaltask.ExternalTaskQuery;
import org.eximeebpms.bpm.engine.externaltask.LockedExternalTask;
import org.eximeebpms.bpm.engine.test.api.authorization.AuthorizationTest;
import org.eximeebpms.commons.testing.ProcessEngineLoggingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskQueryAuthorizationTest extends AuthorizationTest {

  protected static final String WORKER_ID = "aWorkerId";
  protected static final String EXTERNAL_TASK_TOPIC = "externalTaskTopic";
  protected String deploymentId;
  protected String instance1Id;
  protected String instance2Id;

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  @Override
  @Before
  public void setUp() throws Exception {
    deploymentId = testRule.deploy(
        "org/eximeebpms/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
        "org/eximeebpms/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml").getId();

    instance1Id = startProcessInstanceByKey("oneExternalTaskProcess").getId();
    instance2Id = startProcessInstanceByKey("twoExternalTaskProcess").getId();
    super.setUp();
  }

  @Test
  public void testQueryWithoutAuthorization() {
    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryWithReadOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);

    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 1);
    assertEquals(instance1Id, query.list().get(0).getProcessInstanceId());
  }

  @Test
  public void testQueryWithReadOnAnyProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 2);
  }

  @Test
  public void testQueryWithReadInstanceOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, READ_INSTANCE);

    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 1);
    assertEquals(instance1Id, query.list().get(0).getProcessInstanceId());
  }

  @Test
  public void testQueryWithReadInstanceOnAnyProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 2);
  }

  @Test
  public void testQueryWithReadInstanceWithMultiple() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, READ_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);

    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 2);
  }

  @Test
  public void shouldNotFindTaskWithRevokedReadOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);
    createRevokeAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);

    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void shouldFetchAndLockWhenUserAuthorized() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, ALL);

    // when
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(EXTERNAL_TASK_TOPIC, 10)
        .execute();

    // then
    assertThat(externalTasks).hasSize(1);
    assertThat(externalTasks).extracting(LockedExternalTask::getTopicName).first().isEqualTo(EXTERNAL_TASK_TOPIC);
  }

  @Test
  public void shouldFetchAndLockWhenGroupAuthorized() {
    // given
    createGrantAuthorizationGroup(PROCESS_DEFINITION, ANY, groupId, ALL);

    // when
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(EXTERNAL_TASK_TOPIC, 10)
        .execute();

    // then
    assertThat(externalTasks).hasSize(1);
    assertThat(externalTasks).extracting(LockedExternalTask::getTopicName).first().isEqualTo(EXTERNAL_TASK_TOPIC);
  }

  @Test
  public void shouldNotFetchAndLockWhenUnauthorized() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);
    createGrantAuthorizationGroup(PROCESS_DEFINITION, ANY, groupId, READ_INSTANCE);

    // when
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(EXTERNAL_TASK_TOPIC, 10)
        .execute();

    // then
    assertThat(externalTasks).isEmpty();
  }
}
