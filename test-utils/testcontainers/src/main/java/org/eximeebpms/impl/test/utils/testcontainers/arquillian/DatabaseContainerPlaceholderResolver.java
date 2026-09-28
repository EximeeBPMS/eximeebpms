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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.GroupDef;
import org.jboss.arquillian.config.spi.ConfigurationPlaceholderResolver;
import org.testcontainers.containers.JdbcContainerUrls;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.JdbcDatabaseContainerProvider;
import org.testcontainers.jdbc.ConnectionUrl;

/**
 * Turns a {@code jdbc:tc:} URL into a running container and a plain vendor URL, early
 * enough for Arquillian to hand that URL to a managed application server.
 *
 * <p>A managed server is a separate JVM that Arquillian forks, and its datasource is
 * configured before any test runs. Letting the server open the {@code jdbc:tc:} URL
 * itself would work, but only by putting Testcontainers and its whole transitive
 * dependency set on the server's shared classpath, next to the engine - which is both a
 * maintenance burden and a source of version conflicts. Starting the container here
 * instead keeps the server configured exactly as a customer would configure it, against
 * the vendor driver and a plain URL.
 *
 * <p>The timing works because Arquillian resolves its descriptor on {@code ManagerStarted},
 * the first event of its lifecycle, and runs every registered
 * {@link ConfigurationPlaceholderResolver} in {@link #precedence()} order. This one runs
 * ahead of Arquillian's own system-property resolver, so the values it publishes are
 * already in place when {@code ${eximeebpms.ds.*}} is substituted in {@code arquillian.xml}.
 *
 * <p>No-op unless {@code eximeebpms.ds.url} holds a {@code jdbc:tc:} URL, so a run against
 * an externally provisioned database is unaffected.
 */
public class DatabaseContainerPlaceholderResolver implements ConfigurationPlaceholderResolver {

  public static final String URL_PROPERTY = "eximeebpms.ds.url";
  public static final String USERNAME_PROPERTY = "eximeebpms.ds.username";
  public static final String PASSWORD_PROPERTY = "eximeebpms.ds.password";
  public static final String DRIVER_PROPERTY = "eximeebpms.ds.driver";

  /** An XA datasource is configured from the parts, not from a URL. */
  public static final String HOST_PROPERTY = "eximeebpms.ds.host";
  public static final String PORT_PROPERTY = "eximeebpms.ds.port";
  public static final String NAME_PROPERTY = "eximeebpms.ds.name";

  /**
   * Held for the lifetime of the JVM on purpose. Unlike the connection-counting wrapper
   * in Testcontainers' own driver, nothing here closes the container when a pool drains,
   * so it stays up for the whole suite and Ryuk reaps it when the JVM exits.
   */
  private static JdbcDatabaseContainer<?> container;

  private static final String[] PROPERTIES = {
      URL_PROPERTY, USERNAME_PROPERTY, PASSWORD_PROPERTY, DRIVER_PROPERTY,
      HOST_PROPERTY, PORT_PROPERTY, NAME_PROPERTY
  };

  @Override
  public ArquillianDescriptor resolve(ArquillianDescriptor descriptor) {
    String url = System.getProperty(URL_PROPERTY);
    if (url != null && ConnectionUrl.accepts(url)) {
      publish(ConnectionUrl.newInstance(url));
    }
    substitute(descriptor);
    return descriptor;
  }

  /**
   * Substitutes our placeholders in the descriptor's object model rather than leaving them
   * to Arquillian's own resolver, which works on the exported XML as text.
   *
   * <p>A JDBC URL routinely carries more than one query parameter, and the {@code &}
   * joining them is the start of an entity reference once it lands in XML text. Arquillian
   * re-parses the descriptor after substituting, so a raw ampersand fails the whole suite
   * with "The reference to entity ... must end with the ';' delimiter". Setting the value
   * on the model instead leaves the escaping to the serializer, which gets it right, and
   * keeps the system property itself a truthful URL for anything else that reads it.
   */
  private static void substitute(ArquillianDescriptor descriptor) {
    List<ContainerDef> containers = new ArrayList<>(descriptor.getContainers());
    for (GroupDef group : descriptor.getGroups()) {
      containers.addAll(group.getGroupContainers());
    }

    for (ContainerDef container : containers) {
      for (Map.Entry<String, String> property : container.getContainerProperties().entrySet()) {
        String value = property.getValue();
        if (value == null) {
          continue;
        }
        String resolved = replacePlaceholders(value);
        if (!value.equals(resolved)) {
          container.overrideProperty(property.getKey(), resolved);
        }
      }
    }
  }

  private static String replacePlaceholders(String value) {
    if (!value.contains("${")) {
      return value;
    }
    String result = value;
    for (String name : PROPERTIES) {
      String resolved = System.getProperty(name);
      if (resolved != null) {
        result = result.replace("${" + name + "}", resolved);
      }
    }
    return result;
  }

  @Override
  public int precedence() {
    // Arquillian's own resolvers are 0, and higher runs sooner.
    return 10;
  }

  private static synchronized void publish(ConnectionUrl connectionUrl) {
    if (container == null) {
      container = start(connectionUrl);
    }

    System.setProperty(URL_PROPERTY, urlFor(container, connectionUrl.getQueryString().orElse("")));
    System.setProperty(USERNAME_PROPERTY, container.getUsername());
    System.setProperty(PASSWORD_PROPERTY, container.getPassword());
    System.setProperty(DRIVER_PROPERTY, container.getDriverClassName());

    System.setProperty(HOST_PROPERTY, container.getHost());
    System.setProperty(PORT_PROPERTY, String.valueOf(container.getFirstMappedPort()));
    try {
      System.setProperty(NAME_PROPERTY, container.getDatabaseName());
    } catch (UnsupportedOperationException e) {
      // JdbcDatabaseContainer declares getDatabaseName() but leaves it unimplemented, and
      // the containers that do not name a database (Oracle, for one) keep it that way.
      // Only an XA datasource reads this, and none is configured for those databases.
    }
  }

  private static JdbcDatabaseContainer<?> start(ConnectionUrl connectionUrl) {
    String databaseType = connectionUrl.getDatabaseType();

    for (JdbcDatabaseContainerProvider provider : ServiceLoader.load(JdbcDatabaseContainerProvider.class)) {
      if (provider.supports(databaseType)) {
        JdbcDatabaseContainer<?> instance = provider.newInstance(connectionUrl);
        instance.withTmpFs(connectionUrl.getTmpfsOptions());
        instance.setParameters(connectionUrl.getContainerParameters());
        instance.start();
        return instance;
      }
    }

    throw new IllegalStateException("No Testcontainers provider on the classpath supports database type '"
        + databaseType + "'; add the matching org.testcontainers:testcontainers-" + databaseType + " module");
  }

  /**
   * Asks the container to compose the URL rather than concatenating one here. The
   * composition is per-vendor - MySQL appends {@code useSSL} and
   * {@code allowPublicKeyRetrieval} of its own accord, SQL Server sets {@code encrypt=false}
   * on the container before delegating - and a URL assembled by hand would silently diverge
   * from the one Testcontainers' driver would have produced.
   */
  private static String urlFor(JdbcDatabaseContainer<?> container, String queryString) {
    return JdbcContainerUrls.forConnection(container, queryString);
  }
}
