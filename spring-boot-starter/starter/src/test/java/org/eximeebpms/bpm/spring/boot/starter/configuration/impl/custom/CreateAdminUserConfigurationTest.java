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
package org.eximeebpms.bpm.spring.boot.starter.configuration.impl.custom;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.engine.identity.User;
import org.eximeebpms.bpm.engine.identity.UserQuery;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.eximeebpms.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.eximeebpms.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.eximeebpms.bpm.engine.impl.interceptor.Session;
import org.eximeebpms.bpm.engine.impl.interceptor.SessionFactory;
import org.eximeebpms.bpm.engine.test.ProcessEngineRule;
import org.eximeebpms.bpm.spring.boot.starter.property.EximeeBpmsBpmProperties;
import org.eximeebpms.bpm.spring.boot.starter.test.helper.StandaloneInMemoryTestConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.util.SpringBootProcessEngineLogger;
import org.eximeebpms.bpm.spring.boot.starter.util.SpringBootStarterException;
import org.eximeebpms.commons.testing.ProcessEngineLoggingRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateAdminUserConfigurationTest {

  private static final String OAUTH2_REGISTRATION_PROPERTY = "spring.security.oauth2.client.registration.entra.client-id";

  private final EximeeBpmsBpmProperties eximeeBpmsBpmProperties = new EximeeBpmsBpmProperties();
  {
    eximeeBpmsBpmProperties.getAdminUser().setId("admin");
    eximeeBpmsBpmProperties.getAdminUser().setPassword("password");
  }

  private final CreateAdminUserConfiguration createAdminUserConfiguration = new CreateAdminUserConfiguration(new MockEnvironment());
  {
    ReflectionTestUtils.setField(createAdminUserConfiguration, "eximeeBpmsBpmProperties", eximeeBpmsBpmProperties);
    createAdminUserConfiguration.init();
  }

  private final ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemoryTestConfiguration(createAdminUserConfiguration);

  @Rule
  public final ProcessEngineRule processEngineRule = new ProcessEngineRule(processEngineConfiguration.buildProcessEngine());

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch(SpringBootProcessEngineLogger.PACKAGE);

  @Test
  public void createAdminUser() {
    User user = processEngineRule.getIdentityService().createUserQuery().userId("admin").singleResult();
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("admin@localhost");
  }

  @Test
  public void shouldLogInitialAdminUserCreationOnDebug() {
    processEngineConfiguration.buildProcessEngine();
    verifyLogs(Level.DEBUG, "STARTER-SB010 Creating initial Admin User: AdminUserProperty[id=admin, firstName=Admin, lastName=Admin, email=admin@localhost, password=******]");
  }

  @Test
  public void shouldFailFastWhenOAuth2ConfiguredWithoutOptIn() {
    // given
    CreateAdminUserConfiguration oauth2AdminUserConfiguration = oauth2AdminUserConfiguration();
    StandaloneInMemoryTestConfiguration configuration = new StandaloneInMemoryTestConfiguration(oauth2AdminUserConfiguration);

    // when/then
    assertThatThrownBy(configuration::buildProcessEngine)
        .isInstanceOf(SpringBootStarterException.class)
        .hasMessageContaining("STARTER-SB060")
        .hasMessageContaining("allow-with-external-identity-provider");
  }

  @Test
  public void shouldCreateAdminUserWhenOAuth2ConfiguredWithOptIn() {
    // given
    eximeeBpmsBpmProperties.getAdminUser().setAllowWithExternalIdentityProvider(true);
    CreateAdminUserConfiguration oauth2AdminUserConfiguration = oauth2AdminUserConfiguration();
    StandaloneInMemoryTestConfiguration configuration = new StandaloneInMemoryTestConfiguration(oauth2AdminUserConfiguration);

    // when
    ProcessEngine engine = configuration.buildProcessEngine();

    // then
    User user = engine.getIdentityService().createUserQuery().userId("admin").singleResult();
    assertThat(user).isNotNull();
  }

  private CreateAdminUserConfiguration oauth2AdminUserConfiguration() {
    CreateAdminUserConfiguration configuration =
        new CreateAdminUserConfiguration(new MockEnvironment().withProperty(OAUTH2_REGISTRATION_PROPERTY, "xyz"));
    ReflectionTestUtils.setField(configuration, "eximeeBpmsBpmProperties", eximeeBpmsBpmProperties);
    configuration.init();
    return configuration;
  }

  @Test
  public void shouldFailFastWhenReadOnlyIdentityProviderWithoutOptIn() {
    // given
    StandaloneInMemoryTestConfiguration configuration =
        new StandaloneInMemoryTestConfiguration(readOnlyIdentityProviderPlugin(), createAdminUserConfiguration);

    // when/then
    assertThatThrownBy(configuration::buildProcessEngine)
        .isInstanceOf(SpringBootStarterException.class)
        .hasMessageContaining("STARTER-SB061")
        .hasMessageContaining("allow-with-external-identity-provider");
  }

  @Test
  public void shouldSkipAdminUserCreationWhenReadOnlyIdentityProviderWithOptIn() {
    // given
    eximeeBpmsBpmProperties.getAdminUser().setAllowWithExternalIdentityProvider(true);
    StandaloneInMemoryTestConfiguration configuration =
        new StandaloneInMemoryTestConfiguration(readOnlyIdentityProviderPlugin(), createAdminUserConfiguration);

    // when
    configuration.buildProcessEngine();

    // then (no exception, and the warning was logged)
    verifyLogs(Level.WARN, "STARTER-SB062");
  }

  /**
   * Registers a {@link ReadOnlyIdentityProvider}-only session factory (no
   * {@code WritableIdentityProvider}), the same mechanism the LDAP plugin
   * uses ({@code ProcessEngineConfigurationImpl#setIdentityProviderSessionFactory}),
   * without depending on the real LDAP plugin module.
   */
  private ProcessEnginePlugin readOnlyIdentityProviderPlugin() {
    ReadOnlyIdentityProvider readOnlyIdentityProvider = mock(ReadOnlyIdentityProvider.class);
    UserQuery userQuery = mock(UserQuery.class);
    when(readOnlyIdentityProvider.createUserQuery()).thenReturn(userQuery);
    when(userQuery.userId(anyString())).thenReturn(userQuery);
    when(userQuery.singleResult()).thenReturn(null);

    return new ProcessEnginePlugin() {
      @Override
      public void preInit(ProcessEngineConfigurationImpl configuration) {
        configuration.setIdentityProviderSessionFactory(new SessionFactory() {
          @Override
          public Class<?> getSessionType() {
            return ReadOnlyIdentityProvider.class;
          }

          @Override
          public Session openSession() {
            return readOnlyIdentityProvider;
          }
        });
      }

      @Override
      public void postInit(ProcessEngineConfigurationImpl configuration) {
        // nothing to do
      }

      @Override
      public void postProcessEngineBuild(ProcessEngine processEngine) {
        // nothing to do
      }
    };
  }

  protected void verifyLogs(Level logLevel, String message) {
    List<ILoggingEvent> logs = loggingRule.getLog();
    assertThat(logs).anySatisfy(log -> {
      assertThat(log.getLevel()).isEqualTo(logLevel);
      assertThat(log.getFormattedMessage()).containsIgnoringCase(message);
    });
  }

}
