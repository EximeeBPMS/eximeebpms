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
package org.eximeebpms.bpm.engine.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.http.ContentType;
import javax.ws.rs.core.Response.Status;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eximeebpms.bpm.engine.AuthorizationService;
import org.eximeebpms.bpm.engine.IdentityService;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.identity.Authentication;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbAwareScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptOrigin;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSourceType;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationEvent;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptViolationStore;
import org.eximeebpms.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ScriptSecurityRestServiceTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String VIOLATIONS_URL = TEST_RESOURCE_ROOT_PATH + ScriptSecurityRestService.PATH + "/violations";
  protected static final String VIOLATIONS_COUNT_URL = VIOLATIONS_URL + "/count";
  protected static final String CONFIG_URL = TEST_RESOURCE_ROOT_PATH + ScriptSecurityRestService.PATH + "/config";

  protected ProcessEngineConfigurationImpl configMock;
  protected ScriptViolationStore storeMock;

  @Before
  public void setupMocks() {
    configMock = mock(ProcessEngineConfigurationImpl.class);
    storeMock = mock(ScriptViolationStore.class);

    when(processEngine.getProcessEngineConfiguration()).thenReturn(configMock);
    when(configMock.isAuthorizationEnabled()).thenReturn(false);
    when(configMock.getScriptViolationStore()).thenReturn(storeMock);
  }

  @Test
  public void shouldReturnEmptyViolationsList() {
    when(storeMock.getRecent(50)).thenReturn(Collections.emptyList());

    given()
        .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
        .body("$", hasSize(0))
        .when()
        .get(VIOLATIONS_URL);
  }

  @Test
  public void shouldReturnViolationsWithAllFields() {
    Instant timestamp = Instant.parse("2026-06-25T10:00:00Z");
    ScriptViolationEvent event = new ScriptViolationEvent(
        timestamp,
        "myProcess",
        null,
        "serviceTask1",
        "javascript",
        ScriptSourceType.INLINE_SOURCE,
        ScriptOrigin.USER,
        "SCRIPT_SECURITY_SYSTEM_GETENV",
        "Access to environment variables is forbidden"
    );
    when(storeMock.getRecent(50)).thenReturn(List.of(event));

    given()
        .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
        .body("[0].timestamp", equalTo(timestamp.toString()))
        .body("[0].processDefinitionKey", equalTo("myProcess"))
        .body("[0].activityId", equalTo("serviceTask1"))
        .body("[0].language", equalTo("javascript"))
        .body("[0].sourceType", equalTo("INLINE_SOURCE"))
        .body("[0].origin", equalTo("USER"))
        .body("[0].ruleCode", equalTo("SCRIPT_SECURITY_SYSTEM_GETENV"))
        .body("[0].reason", equalTo("Access to environment variables is forbidden"))
        .when()
        .get(VIOLATIONS_URL);
  }

  @Test
  public void shouldApplyDefaultPaginationLimit() {
    when(storeMock.getRecent(50)).thenReturn(Collections.emptyList());

    given()
        .when()
        .get(VIOLATIONS_URL);

    verify(storeMock).getRecent(50);
  }

  @Test
  public void shouldApplyPagination() {
    ScriptViolationEvent e1 = sampleEvent("proc1");
    ScriptViolationEvent e2 = sampleEvent("proc2");
    ScriptViolationEvent e3 = sampleEvent("proc3");
    when(storeMock.getRecent(5)).thenReturn(Arrays.asList(e1, e2, e3));

    given()
        .queryParam("firstResult", 2)
        .queryParam("maxResults", 3)
        .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("$", hasSize(1))
        .body("[0].processDefinitionKey", equalTo("proc3"))
        .when()
        .get(VIOLATIONS_URL);

    verify(storeMock).getRecent(5);
  }

  @Test
  public void shouldReturnViolationsCount() {
    when(storeMock.getTotalCount()).thenReturn(42L);

    given()
        .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
        .body("count", equalTo(42))
        .when()
        .get(VIOLATIONS_COUNT_URL);
  }

  @Test
  public void shouldReturn403ForViolationsWhenNotAuthorized() {
    setupUnauthorizedUser();

    given()
        .then()
        .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .when()
        .get(VIOLATIONS_URL);
  }

  @Test
  public void shouldReturn403ForCountWhenNotAuthorized() {
    setupUnauthorizedUser();

    given()
        .then()
        .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .when()
        .get(VIOLATIONS_COUNT_URL);
  }

  // --- GET /config ---

  @Test
  public void shouldReturnDefaultConfigWhenPropertiesAreEmpty() {
    // given
    when(processEngine.getManagementService().getProperties()).thenReturn(Collections.emptyMap());

    // when / then
    given()
        .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
        .body("mode", equalTo("ENFORCE"))
        .body("allowlistedKeys", hasSize(0))
        .when()
        .get(CONFIG_URL);
  }

  @Test
  public void shouldReturnConfigFromDbProperties() {
    // given
    Map<String, String> props = new HashMap<>();
    props.put(DbAwareScriptSecurityPolicy.PROP_MODE, "AUDIT");
    props.put(DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, "invoiceProcess,legacyMigration");
    when(processEngine.getManagementService().getProperties()).thenReturn(props);

    // when / then
    given()
        .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
        .body("mode", equalTo("AUDIT"))
        .body("allowlistedKeys", hasSize(2))
        .when()
        .get(CONFIG_URL);
  }

  @Test
  public void shouldReturn403ForGetConfigWhenNotAuthorized() {
    // given
    setupUnauthorizedUser();

    // when / then
    given()
        .then()
        .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .when()
        .get(CONFIG_URL);
  }

  // --- PUT /config ---

  @Test
  public void shouldUpdateConfigAndWritePropertiesToDb() {
    // given
    when(configMock.getScriptSecurityPolicy()).thenReturn(mock(DbAwareScriptSecurityPolicy.class));
    Map<String, Object> body = new HashMap<>();
    body.put("mode", "AUDIT");
    body.put("allowlistedKeys", Collections.emptyList());

    // when / then
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("mode", equalTo("AUDIT"))
        .body("allowlistedKeys", hasSize(0))
        .when()
        .put(CONFIG_URL);

    verify(processEngine.getManagementService()).setProperty(DbAwareScriptSecurityPolicy.PROP_MODE, "AUDIT");
    verify(processEngine.getManagementService()).setProperty(DbAwareScriptSecurityPolicy.PROP_ALLOWLIST, "");
  }

  @Test
  public void shouldInvalidatePolicyCacheOnConfigUpdate() {
    // given
    DbAwareScriptSecurityPolicy policyMock = mock(DbAwareScriptSecurityPolicy.class);
    when(configMock.getScriptSecurityPolicy()).thenReturn(policyMock);
    Map<String, Object> body = new HashMap<>();
    body.put("mode", "ENFORCE");

    // when
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .put(CONFIG_URL);

    // then
    verify(policyMock).invalidateCache();
  }

  @Test
  public void shouldReturn400WhenModeIsNullInUpdateConfig() {
    // given — body with no "mode" field
    Map<String, Object> body = new HashMap<>();

    // when / then
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .then()
        .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .when()
        .put(CONFIG_URL);
  }

  @Test
  public void shouldReturn400WhenModeIsInvalidInUpdateConfig() {
    // given
    Map<String, Object> body = new HashMap<>();
    body.put("mode", "UNKNOWN_MODE");

    // when / then
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .then()
        .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .when()
        .put(CONFIG_URL);
  }

  @Test
  public void shouldReturn403ForUpdateConfigWhenNotAuthorized() {
    // given
    setupUnauthorizedUser();
    Map<String, Object> body = new HashMap<>();
    body.put("mode", "AUDIT");

    // when / then
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .then()
        .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .when()
        .put(CONFIG_URL);
  }

  private void setupUnauthorizedUser() {
    IdentityService identityServiceMock = mock(IdentityService.class);
    AuthorizationService authorizationServiceMock = mock(AuthorizationService.class);
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);
    when(processEngine.getAuthorizationService()).thenReturn(authorizationServiceMock);

    when(configMock.isAuthorizationEnabled()).thenReturn(true);

    Authentication auth = mock(Authentication.class);
    when(auth.getUserId()).thenReturn("unauthorized-user");
    when(auth.getGroupIds()).thenReturn(null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(auth);
    when(authorizationServiceMock.isUserAuthorized(
        org.mockito.ArgumentMatchers.anyString(),
        org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(false);
  }

  private ScriptViolationEvent sampleEvent(String processDefinitionKey) {
    return new ScriptViolationEvent(
        Instant.now(),
        processDefinitionKey,
        null,
        "task1",
        "javascript",
        ScriptSourceType.INLINE_SOURCE,
        ScriptOrigin.USER,
        "RULE_CODE",
        "some reason"
    );
  }
}
