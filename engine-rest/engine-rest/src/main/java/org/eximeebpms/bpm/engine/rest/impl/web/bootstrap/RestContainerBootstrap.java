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
package org.eximeebpms.bpm.engine.rest.impl.web.bootstrap;

import java.util.regex.Pattern;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.eximeebpms.bpm.engine.rest.util.WebApplicationUtil;

public class RestContainerBootstrap implements ServletContextListener {

  private static final BootstrapLogger LOG = BootstrapLogger.BOOTSTRAP_LOGGER;

  // TODO before 1.4.0 release: remove this detection together with dropping Tomcat 9 / WildFly 26 support
  protected static final Pattern DEPRECATED_TOMCAT_9_PATTERN = Pattern.compile("Tomcat/9\\.");
  protected static final Pattern DEPRECATED_WILDFLY_26_PATTERN = Pattern.compile("WildFly.*\\b26\\.\\d");

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    String serverInfo = sce.getServletContext().getServerInfo();
    WebApplicationUtil.setApplicationServer(serverInfo);
    warnIfDeprecatedApplicationServer(serverInfo);
  }

  protected void warnIfDeprecatedApplicationServer(String serverInfo) {
    if (serverInfo == null) {
      return;
    }

    if (DEPRECATED_TOMCAT_9_PATTERN.matcher(serverInfo).find()
        || DEPRECATED_WILDFLY_26_PATTERN.matcher(serverInfo).find()) {
      LOG.deprecatedApplicationServerDetected(serverInfo);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // do nothing
  }

}
