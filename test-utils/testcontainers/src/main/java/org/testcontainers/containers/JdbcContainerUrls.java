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
package org.testcontainers.containers;

/**
 * Reaches {@link JdbcDatabaseContainer#constructUrlForConnection(String)}, which is
 * {@code protected} and therefore visible to this package.
 *
 * <p><strong>Why this class sits in Testcontainers' own package.</strong> The composed URL
 * is per-vendor and cannot be assembled by hand without silently diverging: MySQL appends
 * {@code useSSL=false} and {@code allowPublicKeyRetrieval=true}, and SQL Server puts
 * {@code encrypt=false} into the container's own {@code urlParameters} before delegating,
 * so the correction is invisible in {@code getJdbcUrl()} until that method has run.
 *
 * <p>Two alternatives were measured and rejected. Reading the URL back from
 * {@code DatabaseMetaData.getURL()} works for PostgreSQL and MySQL but not for SQL Server,
 * whose driver answers with every default expanded — roughly two kilobytes of parameters
 * rather than the connection string. Reflection with {@code setAccessible(true)} works
 * everywhere, but defeats an access check for no reason once package access will do.
 *
 * <p>Legal because Testcontainers ships plain classpath jars: neither
 * {@code testcontainers} nor {@code testcontainers-jdbc} carries a {@code module-info} or
 * an {@code Automatic-Module-Name}, so this package is not sealed against additions. If
 * that ever changes, this class stops compiling — which is the failure mode to want, since
 * it is loud rather than silent.
 */
public final class JdbcContainerUrls {

  private JdbcContainerUrls() {
  }

  /**
   * @param queryString the query part to merge in, {@code ""} for none, otherwise starting
   *                    with {@code ?}
   * @return the JDBC URL this container's own driver would connect with
   */
  public static String forConnection(JdbcDatabaseContainer<?> container, String queryString) {
    return container.constructUrlForConnection(queryString);
  }
}
