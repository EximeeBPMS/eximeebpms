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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.eximeebpms.bpm.engine.ProcessEngines;
import org.eximeebpms.bpm.engine.impl.cfg.IdGenerator;
import org.eximeebpms.bpm.engine.impl.scripting.security.DbAwareScriptSecurityPolicy;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSecurityContext;
import org.eximeebpms.bpm.engine.impl.scripting.security.ScriptSourceType;
import org.eximeebpms.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.eximeebpms.bpm.spring.boot.starter.property.ScriptSecurityProperty;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class DefaultProcessEngineConfigurationTest {

  private final DefaultProcessEngineConfiguration instance = new DefaultProcessEngineConfiguration();
  private final SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
  private final CamundaBpmProperties properties = new CamundaBpmProperties();

  @Before
  public void setUp() throws Exception {
    ReflectionTestUtils.setField(instance, "camundaBpmProperties", properties);
    initIdGenerator(null);
  }

  @Test
  public void setName_if_not_empty() throws Exception {
    properties.setProcessEngineName("foo");
    instance.preInit(configuration);
    assertThat(configuration.getProcessEngineName()).isEqualTo("foo");
  }

  @Test
  public void setName_ignore_empty() throws Exception {
    properties.setProcessEngineName(null);
    instance.preInit(configuration);
    assertThat(configuration.getProcessEngineName()).isEqualTo(ProcessEngines.NAME_DEFAULT);

    properties.setProcessEngineName(" ");
    instance.preInit(configuration);
    assertThat(configuration.getProcessEngineName()).isEqualTo(ProcessEngines.NAME_DEFAULT);
  }

  @Test
  public void setName_ignore_hyphen() throws Exception {
    properties.setProcessEngineName("foo-bar");
    instance.preInit(configuration);
    assertThat(configuration.getProcessEngineName()).isEqualTo(ProcessEngines.NAME_DEFAULT);
  }

  @Test
  public void setDefaultSerializationFormat() {
    final String defaultSerializationFormat = "testformat";
    properties.setDefaultSerializationFormat(defaultSerializationFormat);
    instance.preInit(configuration);
    assertThat(configuration.getDefaultSerializationFormat()).isSameAs(defaultSerializationFormat);
  }

  @Test
  public void setDefaultSerializationFormat_ignore_null() {
    final String defaultSerializationFormat = configuration.getDefaultSerializationFormat();
    properties.setDefaultSerializationFormat(null);
    instance.preInit(configuration);
    assertThat(configuration.getDefaultSerializationFormat()).isEqualTo(defaultSerializationFormat);
  }

  @Test
  public void setDefaultSerializationFormat_ignore_empty() {
    final String defaultSerializationFormat = configuration.getDefaultSerializationFormat();
    properties.setDefaultSerializationFormat(" ");
    instance.preInit(configuration);
    assertThat(configuration.getDefaultSerializationFormat()).isEqualTo(defaultSerializationFormat);
  }

  @Test
  public void setJobExecutorAcquireByPriority() {
    properties.setJobExecutorAcquireByPriority(null);
    instance.preInit(configuration);
    assertThat(configuration.isJobExecutorAcquireByPriority()).isEqualTo(false);

    properties.setJobExecutorAcquireByPriority(true);
    instance.preInit(configuration);
    assertThat(configuration.isJobExecutorAcquireByPriority()).isEqualTo(true);
  }

  @Test
  public void setDefaultNumberOfRetries() {
    properties.setDefaultNumberOfRetries(null);
    instance.preInit(configuration);
    assertThat(configuration.getDefaultNumberOfRetries()).isEqualTo(3);

    properties.setDefaultNumberOfRetries(1);
    instance.preInit(configuration);
    assertThat(configuration.getDefaultNumberOfRetries()).isEqualTo(1);
  }

  @Test
  public void setScriptSecurityEnabled_default_true() {
    instance.preInit(configuration);
    assertThat(configuration.isScriptSecurityEnabled()).isTrue();
  }

  @Test
  public void setScriptSecurityEnabled_false() {
    properties.getScriptSecurity().setMode(ScriptSecurityProperty.Mode.DISABLED);
    instance.preInit(configuration);
    assertThat(configuration.isScriptSecurityEnabled()).isFalse();
  }

  @Test
  public void shouldConfigureScriptSecurityPolicyWithAllowlistedProcessDefinitionKeys() {
    // given
    ScriptSecurityProperty scriptSecurity = new ScriptSecurityProperty();
    scriptSecurity.setAllowlistedProcessDefinitionKeys(Set.of("legacyInvoiceProcess"));

    properties.setScriptSecurity(scriptSecurity);

    // when
    instance.preInit(configuration);

    // then
    assertThat(configuration.isScriptSecurityEnabled()).isTrue();
    assertThat(configuration.getScriptSecurityPolicy()).isInstanceOf(DbAwareScriptSecurityPolicy.class);

    // Before ManagementService is wired, DbAwareScriptSecurityPolicy uses initial config from env.
    ScriptSecurityContext allowlistedContext = ScriptSecurityContext.builder("javascript")
        .source("System.getenv('HOME');")
        .sourceType(ScriptSourceType.INLINE_SOURCE)
        .processDefinitionKey("legacyInvoiceProcess")
        .build();

    assertThat(configuration.getScriptSecurityPolicy()
        .evaluate(allowlistedContext)
        .isAllowed()).isTrue();
  }

  @Test
  public void setCmmnEnabled_default_true() {
    instance.preInit(configuration);
    assertThat(configuration.isCmmnEnabled()).isTrue();
  }

  @Test
  public void setCmmnEnabled_false() {
    properties.setCmmnEnabled(false);
    instance.preInit(configuration);
    assertThat(configuration.isCmmnEnabled()).isFalse();
  }

  private void initIdGenerator(IdGenerator idGenerator) {
    ReflectionTestUtils.setField(instance, "idGenerator", idGenerator);
  }
}
