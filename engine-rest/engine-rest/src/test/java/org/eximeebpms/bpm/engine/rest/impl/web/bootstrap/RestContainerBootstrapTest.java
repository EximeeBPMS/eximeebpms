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
package org.eximeebpms.bpm.engine.rest.impl.web.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import org.eximeebpms.commons.testing.ProcessEngineLoggingRule;
import org.eximeebpms.commons.testing.WatchLogger;
import org.junit.Rule;
import org.junit.Test;

public class RestContainerBootstrapTest {

  protected static final String LOGGER_NAME = "org.eximeebpms.bpm.engine.rest.impl.web.bootstrap";

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch(LOGGER_NAME)
      .level(Level.WARN);

  protected final RestContainerBootstrap bootstrap = new RestContainerBootstrap();

  @Test
  @WatchLogger(loggerNames = {LOGGER_NAME}, level = "WARN")
  public void shouldWarnOnTomcat9() {
    // given
    String serverInfo = "Apache Tomcat/9.0.113";

    // when
    bootstrap.warnIfDeprecatedApplicationServer(serverInfo);

    // then
    assertThat(loggingRule.getFilteredLog("DEPRECATED")).hasSize(1);
  }

  @Test
  @WatchLogger(loggerNames = {LOGGER_NAME}, level = "WARN")
  public void shouldWarnOnWildFly26() {
    // given
    String serverInfo = "WildFly Full 26.0.1.Final (WildFly Core 18.0.4.Final)";

    // when
    bootstrap.warnIfDeprecatedApplicationServer(serverInfo);

    // then
    assertThat(loggingRule.getFilteredLog("DEPRECATED")).hasSize(1);
  }

  @Test
  @WatchLogger(loggerNames = {LOGGER_NAME}, level = "WARN")
  public void shouldNotWarnOnSupportedTomcat() {
    // given
    String serverInfo = "Apache Tomcat/10.1.50";

    // when
    bootstrap.warnIfDeprecatedApplicationServer(serverInfo);

    // then
    assertThat(loggingRule.getFilteredLog("DEPRECATED")).isEmpty();
  }

  @Test
  @WatchLogger(loggerNames = {LOGGER_NAME}, level = "WARN")
  public void shouldNotWarnOnSupportedWildFly() {
    // given
    String serverInfo = "WildFly Full 37.0.0.Final (WildFly Core 29.0.0.Final)";

    // when
    bootstrap.warnIfDeprecatedApplicationServer(serverInfo);

    // then
    assertThat(loggingRule.getFilteredLog("DEPRECATED")).isEmpty();
  }

  @Test
  @WatchLogger(loggerNames = {LOGGER_NAME}, level = "WARN")
  public void shouldNotWarnOnNullServerInfo() {
    // given
    String serverInfo = null;

    // when
    bootstrap.warnIfDeprecatedApplicationServer(serverInfo);

    // then
    assertThat(loggingRule.getFilteredLog("DEPRECATED")).isEmpty();
  }

}
