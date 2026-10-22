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
package org.eximeebpms.bpm.engine.impl.telemetry.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import org.eximeebpms.bpm.engine.telemetry.Command;
import org.eximeebpms.bpm.engine.telemetry.Internals;
import org.eximeebpms.bpm.engine.telemetry.Metric;

import com.google.gson.annotations.SerializedName;

public class InternalsImpl implements Internals {

  public static final String SERIALIZED_APPLICATION_SERVER = "application-server";
  public static final String SERIALIZED_EXIMEEBPMS_INTEGRATION = "eximeebpms-integration";
  public static final String SERIALIZED_LICENSE_KEY = "license-key";
  public static final String SERIALIZED_DATA_COLLECTION_START_DATE = "data-collection-start-date";

  @Setter
  protected DatabaseImpl database;
  @Setter
  @SerializedName(value = SERIALIZED_APPLICATION_SERVER)
  protected ApplicationServerImpl applicationServer;
  @Setter
  @SerializedName(value = SERIALIZED_LICENSE_KEY)
  protected LicenseKeyDataImpl licenseKey;
  @SerializedName(value = SERIALIZED_EXIMEEBPMS_INTEGRATION)
  protected Set<String> eximeebpmsIntegration;
  @Setter
  @SerializedName(value = SERIALIZED_DATA_COLLECTION_START_DATE)
  protected Date dataCollectionStartDate;
  @Setter
  protected Map<String, Command> commands;
  @Setter
  protected Map<String, Metric> metrics;
  @Setter
  protected Set<String> webapps;

  @Setter
  protected JdkImpl jdk;

  public InternalsImpl() {
    this(null, null, null, null);
  }

  public InternalsImpl(DatabaseImpl database, ApplicationServerImpl server, LicenseKeyDataImpl licenseKey, JdkImpl jdk) {
    this.database = database;
    this.applicationServer = server;
    this.licenseKey = licenseKey;
    this.commands = new HashMap<>();
    this.jdk = jdk;
    this.eximeebpmsIntegration = new HashSet<>();
  }

  public InternalsImpl(InternalsImpl internals) {
    this(internals.database, internals.applicationServer, internals.licenseKey, internals.jdk);
    this.eximeebpmsIntegration = internals.eximeebpmsIntegration == null ? null : new HashSet<>(internals.getEximeeBpmsIntegration());
    this.commands = new HashMap<>(internals.getCommands());
    this.metrics = internals.metrics == null ? null : new HashMap<>(internals.getMetrics());
    this.webapps = internals.webapps;
    this.dataCollectionStartDate = internals.dataCollectionStartDate;
  }

  @Override
  public DatabaseImpl getDatabase() {
    return database;
  }

  @Override
  public ApplicationServerImpl getApplicationServer() {
    return applicationServer;
  }

  @Override
  public Date getDataCollectionStartDate() {
    return dataCollectionStartDate;
  }

  @Override
  public Map<String, Command> getCommands() {
    return commands;
  }

  public void putCommand(String commandName, int count) {
    if (commands == null) {
      commands = new HashMap<>();
    }

    commands.put(commandName, new CommandImpl(count));
  }

  @Override
  public Map<String, Metric> getMetrics() {
    return metrics;
  }

  public void putMetric(String metricName, int count) {
    if (metrics == null) {
      metrics = new HashMap<>();
    }

    metrics.put(metricName, new MetricImpl(count));
  }

  public void mergeDynamicData(InternalsImpl other) {
    this.commands = other.commands;
    this.metrics = other.metrics;
  }

  @Override
  public JdkImpl getJdk() {
    return jdk;
  }

  @Override
  public Set<String> getEximeeBpmsIntegration() {
    return eximeebpmsIntegration;
  }

  public void setEximeeBpmsIntegration(Set<String> eximeebpmsIntegration) {
    this.eximeebpmsIntegration = eximeebpmsIntegration;
  }

  @Override
  public LicenseKeyDataImpl getLicenseKey() {
    return licenseKey;
  }

  @Override
  public Set<String> getWebapps() {
    return webapps;
  }
}
