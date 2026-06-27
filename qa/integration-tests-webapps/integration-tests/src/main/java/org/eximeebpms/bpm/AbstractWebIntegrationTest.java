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
package org.eximeebpms.bpm;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import kong.unirest.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.chrome.ChromeDriverService;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public abstract class AbstractWebIntegrationTest {

  private final static Logger LOGGER = Logger.getLogger(AbstractWebIntegrationTest.class.getName());
  
  protected static final String TASKLIST_PATH = "app/tasklist/default/";
  
  protected static final String COOKIE_HEADER = "Cookie";
  protected static final String X_XSRF_TOKEN_HEADER = "X-XSRF-TOKEN";

  protected static final String JSESSIONID_IDENTIFIER = "JSESSIONID=";
  protected static final String XSRF_TOKEN_IDENTIFIER = "XSRF-TOKEN=";
  
  protected static final String HOST_NAME = "localhost";

  protected String appBasePath;
  protected String appUrl;
  protected TestProperties testProperties;

  protected static ChromeDriverService service;

  protected String httpPort;
  
  protected String csrfToken;
  protected String sessionId;

  @BeforeClass
  public static void setUpClass() {
    Unirest.config().reset().enableCookieManagement(false).setObjectMapper(new ObjectMapper() {
      final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

      public String writeValue(Object value) {
        try {
          return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }

      public <T> T readValue(String value, Class<T> valueType) {
        try {
          return mapper.readValue(value, valueType);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  @Before
  public void before() throws Exception {
    testProperties = new TestProperties(48080);
  }

  public void createClient(String ctxPath) throws Exception {
    testProperties = new TestProperties();

    appBasePath = testProperties.getApplicationPath("/" + ctxPath);
    LOGGER.info("Connecting to application " + appBasePath);
  }

  protected void getTokens() {
    // first request, first set of cookies
    HttpResponse<String> response = Unirest.get(appBasePath + TASKLIST_PATH).asString();
    List<String> cookieValues = response.getHeaders().get("Set-Cookie");

    String startCsrfCookie = getCookie(cookieValues, XSRF_TOKEN_IDENTIFIER);
    String startSessionCookie = getCookie(cookieValues, JSESSIONID_IDENTIFIER);

    // login with user, update session cookie
    response = Unirest.post(appBasePath + "api/admin/auth/user/default/login/cockpit")
        .body("username=demo&password=demo")
        .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
        .header(COOKIE_HEADER, createCookieHeader(startCsrfCookie, startSessionCookie))
        .header(X_XSRF_TOKEN_HEADER, startCsrfCookie)
        .header("Accept", MediaType.APPLICATION_JSON)
        .asString();
    cookieValues = response.getHeaders().get("Set-Cookie");

    sessionId = getCookie(cookieValues, JSESSIONID_IDENTIFIER);

    // update CSRF cookie
    response = Unirest.get(appBasePath + "api/engine/engine")
        .header(COOKIE_HEADER, createCookieHeader(startCsrfCookie, sessionId))
        .header(X_XSRF_TOKEN_HEADER, startCsrfCookie)
        .asString();

    cookieValues = response.getHeaders().get("Set-Cookie");

    csrfToken = getCookie(cookieValues, XSRF_TOKEN_IDENTIFIER);
  }

  protected List<String> getCookieHeaders(HttpResponse<?> response) {
    return response.getHeaders().get("Set-Cookie");
  }

  protected String getCookie(List<String> cookieValues, String cookieName) {
    String cookieValue = getCookieValue(cookieValues, cookieName);
    if (cookieValue == null || cookieValue.isEmpty() || cookieValue.length() <= cookieName.length()) {
      return "";
    }
    int valueEnd = cookieValue.contains(";") ? cookieValue.indexOf(';') : cookieValue.length();
    return cookieValue.substring(cookieName.length(), valueEnd);
  }
  
  protected String createCookieHeader() {
    return createCookieHeader(csrfToken, sessionId);
  }
  
  protected String createCookieHeader(String csrf, String session) {
    return XSRF_TOKEN_IDENTIFIER + csrf + "; " + JSESSIONID_IDENTIFIER + session;
  }

  protected String getXsrfTokenHeader(HttpResponse<?> response) {
    return response.getHeaders().getFirst(X_XSRF_TOKEN_HEADER);
  }

  protected String getXsrfCookieValue(HttpResponse<?> response) {
    return getCookieValue(response, XSRF_TOKEN_IDENTIFIER);
  }
  
  protected String getCookieValue(HttpResponse<?> response, String cookieName) {
    return getCookieValue(getCookieHeaders(response), cookieName);
  }

  protected String getCookieValue(List<String> cookieValues, String cookieName) {
    if (cookieValues != null) {
      for (String cookieValue : cookieValues) {
        if (cookieValue != null && cookieValue.contains(cookieName)) {
          return cookieValue;
        }
      }
    }
    return "";
  }

  // Helper methods for common test operations
  protected String getWebappCtxPath() {
    return testProperties.getWebappCtxPath();
  }

  protected String getRestCtxPath() {
    return testProperties.getRestCtxPath();
  }

  /**
   * Waits until the application is fully initialized and ready to serve test requests.
   *
   * <p><b>Why this is needed — the race condition:</b><br>
   * Cargo fires the {@code pingURL} as soon as the first deployed WAR responds (in practice: when
   * {@code /engine-rest/engine/default/user/demo/profile} returns 200, which happens after
   * DemoDataGenerator step 1 creates the demo user). At that point the remaining DemoDataGenerator
   * steps are still running asynchronously: group creation, authorization grants, and group
   * membership. Tests that start immediately therefore find an empty group list and fail with
   * authorization errors or missing data.
   *
   * <p><b>The signal we wait for:</b><br>
   * We poll {@code GET /engine-rest/engine/default/group?memberId=demo} until the response body
   * contains {@code "eximeebpms-admin"}. That group appears only after DemoDataGenerator has
   * completed all four steps (user → group → authorizations → membership), so its presence is a
   * reliable "fully ready" indicator.
   *
   * <p><b>Why HTTP Basic Auth on the REST endpoint (not a webapp session):</b><br>
   * When {@code authorizationEnabled=true} is set in {@code bpm-platform.xml}, anonymous REST
   * calls return an empty list {@code []} for group queries — authorization checks apply.
   * The webapp session approach (CSRF token → form login → session cookie) requires the webapp
   * to be up and correctly routing the proxy path, which introduces its own timing dependency.
   * HTTP Basic Auth on the REST endpoint is simpler, stateless, and works as soon as the REST
   * layer is up. The {@code getRestCtxPath()} value ({@code engine-rest/} by default) is read
   * from test properties, keeping the path configurable.
   *
   * <p><b>Polling strategy:</b><br>
   * Awaitility polls every 1 second for up to 120 seconds. Any exception during a poll attempt
   * (server not yet responding, connection refused) is silently ignored and treated as "not ready"
   * so the poll retries. A timeout causes the test to fail with a clear Awaitility message.
   */
  protected void preventRaceConditions() {
    String groupUrl = testProperties.getApplicationPath("/" + getRestCtxPath())
        + "engine/default/group?memberId=demo";

    Awaitility.await("demo user in eximeebpms-admin group")
        .atMost(300, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .ignoreExceptions()
        .until(() -> {
          HttpResponse<String> response = Unirest.get(groupUrl)
              .basicAuth("demo", "demo")
              .asString();
          return response.getStatus() == 200
              && response.getBody().contains("eximeebpms-admin");
        });
  }

}
