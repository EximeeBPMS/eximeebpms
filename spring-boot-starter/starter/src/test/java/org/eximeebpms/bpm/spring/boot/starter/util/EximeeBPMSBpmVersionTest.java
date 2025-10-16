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
package org.eximeebpms.bpm.spring.boot.starter.util;

import org.eximeebpms.bpm.engine.ProcessEngine;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.env.PropertiesPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eximeebpms.bpm.spring.boot.starter.util.EximeeBPMSBpmVersion.key;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

public class EximeeBPMSBpmVersionTest {

  protected static String currentVersion;

  public static EximeeBPMSBpmVersion camundaBpmVersion(final String version) {
    final Package pkg = mock(Package.class);
    when(pkg.getImplementationVersion()).thenReturn(version);
    return new EximeeBPMSBpmVersion(pkg);
  }

  @BeforeClass
  public static void setUp() throws IOException {
    currentVersion = ProcessEngine.class.getPackage().getImplementationVersion();
  }

  @Test
  public void currentVersion() {
    final EximeeBPMSBpmVersion version =  new EximeeBPMSBpmVersion();
    assertThat(version.isEnterprise()).isFalse();
    assertThat(version.get()).startsWith(currentVersion);

    final PropertiesPropertySource source = version.getPropertiesPropertySource();
    assertThat(source.getName()).isEqualTo("EximeeBPMSBpmVersion");
    final String versionFromPropertiesSource = (String) source.getProperty(key(EximeeBPMSBpmVersion.VERSION));
    assertThat(versionFromPropertiesSource).startsWith(currentVersion);
    assertThat(source.getProperty(key(EximeeBPMSBpmVersion.FORMATTED_VERSION))).isEqualTo("(v" + versionFromPropertiesSource + ")");
    assertThat(source.getProperty(key(EximeeBPMSBpmVersion.IS_ENTERPRISE))).isEqualTo(Boolean.FALSE);
  }

  @Test
  public void isEnterprise_true() throws Exception {
    assertThat(camundaBpmVersion("7.6.0-alpha3-ee").isEnterprise()).isTrue();
  }

  @Test
  public void isEnterprise_false() throws Exception {
    assertThat(camundaBpmVersion("7.6.0-alpha3").isEnterprise()).isFalse();
  }
}
