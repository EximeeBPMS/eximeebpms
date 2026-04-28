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
package org.eximeebpms.bpm.spring.boot.starter.security.oauth2;

import lombok.Getter;
import lombok.Setter;
import org.eximeebpms.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.eximeebpms.bpm.spring.boot.starter.security.oauth2.impl.OAuth2IdentityProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(OAuth2Properties.PREFIX)
public class OAuth2Properties {

  public static final String PREFIX = CamundaBpmProperties.PREFIX + ".oauth2";

  /**
   * OAuth2 SSO logout properties.
   */
  @NestedConfigurationProperty
  private OAuth2SSOLogoutProperties ssoLogout = new OAuth2SSOLogoutProperties();

  /**
   * OAuth2 identity provider properties.
   */
  @NestedConfigurationProperty
  private OAuth2IdentityProviderProperties identityProvider = new OAuth2IdentityProviderProperties();

  /**
   * OAuth2 Endpoint properties.
   */
  @NestedConfigurationProperty
  private OAuth2EndpointProperties endpoints = new OAuth2EndpointProperties();

  @Getter
  @Setter
  public static class OAuth2SSOLogoutProperties {
    /**
     * Enable SSO Logout. Default {@code false}.
     */
    private boolean enabled = false;

    /**
     * URI the user is redirected after SSO logout from the provider. Default {@code {baseUrl}}.
     */
    private String postLogoutRedirectUri = "{baseUrl}";
  }

  @Getter
  @Setter
  public static class OAuth2IdentityProviderProperties {
    /**
     * Enable {@link OAuth2IdentityProvider}. Default {@code true}.
     */
    private boolean enabled = true;

    /**
     * Name of the attribute (claim) that holds the groups.
     */
    private String groupNameAttribute;

    /**
     * Group name attribute delimiter. Only used if the {@link #groupNameAttribute} is a {@link String}.
     */
    private String groupNameDelimiter = ",";
  }

  @Getter
  @Setter
  public static class OAuth2EndpointProperties {
    /**
     * Base URI used to initiate the OAuth2 authorization flow.
     *
     * <p>This is an absolute path that is passed directly to Spring Security as the authorization
     * endpoint base URI. It will be sanitized at runtime: a leading {@code /} is ensured, any
     * trailing {@code /} is stripped, and consecutive {@code /} characters are collapsed.
     *
     * <p>Default: {@code /oauth2/authorization} (the Spring Security standard default).
     */
    private String authorizationBaseUri = "/oauth2/authorization";

    /**
     * Base URI used for the OAuth2 authorization response (redirection/callback).
     *
     * <p>This is an absolute path that is passed directly to Spring Security as the redirection
     * endpoint base URI. It will be sanitized at runtime: a leading {@code /} is ensured, any
     * trailing {@code /} is stripped, and consecutive {@code /} characters are collapsed.
     * Wildcards ({@code *}) are preserved.
     *
     * <p>Default: {@code /login/oauth2/code/*} (the Spring Security standard default).
     *
     * <p>Must be aligned with {@code spring.security.oauth2.client.registration.*.redirect-uri}.
     */
    private String redirectionBaseUri = "/login/oauth2/code/*";
  }
}
