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
package org.eximeebpms.impl.test.utils.testcontainers.arquillian;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.After;
import org.junit.Test;

/**
 * A MySQL URL carries two query parameters, so the {@code &} joining them reaches
 * Arquillian's descriptor. Arquillian exports the descriptor to XML text, substitutes
 * placeholders in that text and re-parses it, which turns a raw ampersand into the start
 * of an entity reference and fails the whole suite. These tests pin both halves of the
 * fix: the resolver substitutes on the model, and the model survives the round-trip.
 */
public class DatabaseContainerPlaceholderResolverTest {

  private static final String MYSQL_URL =
      "jdbc:mysql://localhost:32789/test?sendFractionalSeconds=false"
          + "&sessionVariables=transaction_isolation='READ-COMMITTED'";

  @After
  public void clearProperties() {
    System.clearProperty(DatabaseContainerPlaceholderResolver.URL_PROPERTY);
  }

  @Test
  public void substitutesThePlaceholderOnTheModel() {
    System.setProperty(DatabaseContainerPlaceholderResolver.URL_PROPERTY, MYSQL_URL);

    ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class);
    descriptor.container("tomcat")
        .property("javaVmArguments", "-Xmx512m -Deximeebpms.ds.url=${eximeebpms.ds.url}");

    ArquillianDescriptor resolved = new DatabaseContainerPlaceholderResolver().resolve(descriptor);

    assertThat(resolved.getContainers().get(0).getContainerProperty("javaVmArguments"))
        .isEqualTo("-Xmx512m -Deximeebpms.ds.url=" + MYSQL_URL);
  }

  @Test
  public void survivesTheExportAndReimportArquillianPerformsAfterwards() {
    System.setProperty(DatabaseContainerPlaceholderResolver.URL_PROPERTY, MYSQL_URL);

    ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class);
    descriptor.container("tomcat")
        .property("javaVmArguments", "-Deximeebpms.ds.url=${eximeebpms.ds.url}");

    String exported = new DatabaseContainerPlaceholderResolver().resolve(descriptor).exportAsString();
    ArquillianDescriptor reimported =
        Descriptors.importAs(ArquillianDescriptor.class).fromString(exported);

    assertThat(reimported.getContainers().get(0).getContainerProperty("javaVmArguments"))
        .isEqualTo("-Deximeebpms.ds.url=" + MYSQL_URL);
  }

  @Test
  public void leavesTheDescriptorAloneWhenNoDatabaseIsConfigured() {
    ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class);
    descriptor.container("tomcat").property("javaVmArguments", "-Xmx512m");

    ArquillianDescriptor resolved = new DatabaseContainerPlaceholderResolver().resolve(descriptor);

    assertThat(resolved.getContainers().get(0).getContainerProperty("javaVmArguments"))
        .isEqualTo("-Xmx512m");
  }
}
