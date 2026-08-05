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
package org.eximeebpms.bpm.spring.boot.starter.configuration.impl;

import javax.sql.DataSource;

import org.eximeebpms.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.configuration.EximeeBpmsDatasourceConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.property.DatabaseProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

public class DefaultDatasourceConfiguration extends AbstractEximeeBpmsConfiguration implements EximeeBpmsDatasourceConfiguration {

  @Autowired
  protected PlatformTransactionManager transactionManager;

  @Autowired(required = false)
  @Qualifier("eximeeBpmsBpmTransactionManager")
  protected PlatformTransactionManager eximeeBpmsTransactionManager;

  @Deprecated
  @Autowired(required = false)
  @Qualifier("camundaBpmTransactionManager")
  protected PlatformTransactionManager camundaTransactionManager;

  @Autowired
  protected DataSource dataSource;

  @Autowired(required = false)
  @Qualifier("eximeeBpmsBpmDataSource")
  protected DataSource eximeeBpmsDataSource;

  @Deprecated
  @Autowired(required = false)
  @Qualifier("camundaBpmDataSource")
  protected DataSource camundaDataSource;

  @Override
  public void preInit(SpringProcessEngineConfiguration configuration) {
    final DatabaseProperty database = eximeeBpmsBpmProperties.getDatabase();

    PlatformTransactionManager resolvedTm = eximeeBpmsTransactionManager != null ? eximeeBpmsTransactionManager : camundaTransactionManager;
    if (resolvedTm == null) {
      configuration.setTransactionManager(transactionManager);
    } else {
      configuration.setTransactionManager(resolvedTm);
    }

    DataSource resolvedDs = eximeeBpmsDataSource != null ? eximeeBpmsDataSource : camundaDataSource;
    if (resolvedDs == null) {
      configuration.setDataSource(dataSource);
    } else {
      configuration.setDataSource(resolvedDs);
    }

    configuration.setDatabaseType(database.getType());
    configuration.setDatabaseSchemaUpdate(database.getSchemaUpdate());

    if (!StringUtils.isEmpty(database.getTablePrefix())) {
      configuration.setDatabaseTablePrefix(database.getTablePrefix());
    }

    if(!StringUtils.isEmpty(database.getSchemaName())) {
      configuration.setDatabaseSchema(database.getSchemaName());
    }

    configuration.setJdbcBatchProcessing(database.isJdbcBatchProcessing());
  }

  public PlatformTransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public PlatformTransactionManager getEximeeBpmsTransactionManager() {
    return eximeeBpmsTransactionManager;
  }

  public void setEximeeBpmsTransactionManager(PlatformTransactionManager eximeeBpmsTransactionManager) {
    this.eximeeBpmsTransactionManager = eximeeBpmsTransactionManager;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public DataSource getEximeeBpmsDataSource() {
    return eximeeBpmsDataSource;
  }

  public void setEximeeBpmsDataSource(DataSource eximeeBpmsDataSource) {
    this.eximeeBpmsDataSource = eximeeBpmsDataSource;
  }

}
