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

import org.eximeebpms.commons.logging.BaseLogger;

public class BootstrapLogger extends BaseLogger {

  public static final String PROJECT_CODE = "ENGINE-REST";

  public static final BootstrapLogger BOOTSTRAP_LOGGER = BaseLogger.createLogger(
      BootstrapLogger.class,
      PROJECT_CODE,
      "org.eximeebpms.bpm.engine.rest.impl.web.bootstrap",
      "BOOT"
  );

  public void deprecatedApplicationServerDetected(String serverInfo) {
    logWarn(
        "001",
        "Detected application server '{}'. Support for this version is DEPRECATED and will be REMOVED in EximeeBPMS 1.4.0. "
            + "Migrate to a supported version (Tomcat 10+ or WildFly 37+).",
        serverInfo
    );
  }

}
