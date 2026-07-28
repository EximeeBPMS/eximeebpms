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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.eximeebpms.bpm.util.SeleniumScreenshotRule;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedCondition;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class AbstractWebappUiIntegrationTest extends AbstractWebIntegrationTest {

  protected static WebDriver driver;

  // Pre-warm all webapp JAX-RS applications once per JVM run.
  // Without this, lazy deployments happen concurrently with the first Selenium auth POST,
  // and cold SQL Server query plans make the first auth call for each app take >120 s
  // (causing LoginIT to fail on wildfly+sqlserver before things are warm enough).
  private static volatile boolean appsWarmedUp = false;

  @Rule
  public SeleniumScreenshotRule screenshotRule = new SeleniumScreenshotRule(driver);

  @BeforeClass
  public static void createDriver() {
    String chromeDriverExecutable = "chromedriver";
    if (System.getProperty("os.name").toLowerCase(Locale.US).contains("windows")) {
      chromeDriverExecutable += ".exe";
    }

    File chromeDriver = new File("target/chromedriver/" + chromeDriverExecutable);
    if (!chromeDriver.exists()) {
      throw new RuntimeException("chromedriver could not be located!");
    }

    ChromeDriverService chromeDriverService = new ChromeDriverService.Builder()
        .withVerbose(true)
        .usingAnyFreePort()
        .usingDriverExecutable(chromeDriver)
        .build();

    LoggingPreferences logPrefs = new LoggingPreferences();
    logPrefs.enable(LogType.BROWSER, Level.ALL);

    ChromeOptions chromeOptions = new ChromeOptions()
        .addArguments("--headless=new")
        .addArguments("--window-size=1920,1200")
        .addArguments("--disable-gpu")
        .addArguments("--no-sandbox")
        .addArguments("--disable-dev-shm-usage")
        .addArguments("--remote-allow-origins=*")
        .addArguments("--disable-features=PasswordLeakDetection,AutofillServerCommunication");
    chromeOptions.setCapability("goog:loggingPrefs", logPrefs);

    // Chrome's built-in "Save password?" prompt / autofill UI can otherwise pop up after
    // the first successful password-field submission in a test run and steal focus on
    // every subsequent page load for the rest of the (shared, class-scoped) browser
    // session — causing later sendKeys() calls to silently miss the actual login form
    // fields (observed server-side as login attempts reaching doLogin with an empty
    // username, only ever starting from the second test method onward).
    Map<String, Object> prefs = new HashMap<>();
    prefs.put("credentials_enable_service", false);
    prefs.put("profile.password_manager_enabled", false);
    prefs.put("profile.password_manager_leak_detection", false);
    chromeOptions.setExperimentalOption("prefs", prefs);

    // Without an explicit binary, chromedriver falls back to its own browser
    // auto-discovery (checking hardcoded install paths such as /opt/google/chrome/chrome
    // ahead of the PATH-resolved /usr/bin/google-chrome), which can silently pick up a
    // different Chrome version than the one pinned to match this chromedriver build.
    String chromeBinary = System.getProperty("chrome.binary");
    if (chromeBinary != null && !chromeBinary.isEmpty()) {
      chromeOptions.setBinary(chromeBinary);
    }

    driver = new ChromeDriver(chromeDriverService, chromeOptions);
  }

  public static ExpectedCondition<Boolean> currentURIIs(final URI pageURI) {

    return new ExpectedCondition<Boolean>() {
      @Override
      public Boolean apply(WebDriver webDriver) {
        try {
          return new URI(webDriver.getCurrentUrl()).equals(pageURI);
        } catch (URISyntaxException e) {
          return false;
        }
      }
    };

  }

  public static ExpectedCondition<Boolean> containsCurrentUrl(final String url) {

    return new ExpectedCondition<Boolean>() {
      @Override
      public Boolean apply(WebDriver webDriver) {
        return webDriver.getCurrentUrl().contains(url);
      }
    };

  }

  @Before
  public void createClient() throws Exception {
    preventRaceConditions();
    ensureAppsWarmedUp();
    createClient(getWebappCtxPath());
    appUrl = testProperties.getApplicationPath("/" + getWebappCtxPath());
  }

  private void ensureAppsWarmedUp() {
    if (appsWarmedUp) return;
    synchronized (AbstractWebappUiIntegrationTest.class) {
      if (appsWarmedUp) return;
      try {
        warmupApps();
      } catch (Exception ignored) {
      } finally {
        appsWarmedUp = true;
      }
    }
  }

  /**
   * For each webapp (admin, cockpit, tasklist, welcome):
   * 1. GET the app page → triggers lazy JAX-RS deployment and retrieves the CSRF token.
   * 2. POST a demo login → executes the auth SQL query against the DB, warming up the
   *    SQL Server execution-plan cache so the first Selenium login does not time out.
   */
  private void warmupApps() throws Exception {
    String base = testProperties.getApplicationPath("/eximeebpms/");
    for (String app : new String[] {"admin", "cockpit", "tasklist", "welcome"}) {
      try {
        HttpResponse<String> pageResp = Unirest.get(base + "app/" + app + "/default/").asString();
        List<String> cookies = pageResp.getHeaders().get("Set-Cookie");
        String xsrfToken = getCookie(cookies, XSRF_TOKEN_IDENTIFIER);
        String sessionId = getCookie(cookies, JSESSIONID_IDENTIFIER);
        if (xsrfToken.isEmpty()) {
          continue;
        }
        Unirest.post(base + "api/" + app + "/auth/user/default/login/" + app)
            .body("username=demo&password=demo")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header(COOKIE_HEADER, createCookieHeader(xsrfToken, sessionId))
            .header(X_XSRF_TOKEN_HEADER, xsrfToken)
            .asString();
      } catch (Exception ignored) {
      }
    }
  }

  @AfterClass
  public static void quitDriver() {
    driver.quit();
  }

}
