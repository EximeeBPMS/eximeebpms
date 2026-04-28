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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EximeeBPMSSpringSecurityOAuth2AutoConfigurationTest {

  // --- sanitizePath: null / blank / empty ---

  @Test
  public void sanitizePath_null_returnsEmpty() {
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath(null)).isEqualTo("");
  }

  @Test
  public void sanitizePath_empty_returnsEmpty() {
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath("")).isEqualTo("");
  }

  @Test
  public void sanitizePath_blank_returnsEmpty() {
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath("   ")).isEqualTo("");
  }

  // --- sanitizePath: leading slash ---

  @Test
  public void sanitizePath_withLeadingSlash_unchanged() {
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath("/oauth2/authorization"))
        .isEqualTo("/oauth2/authorization");
  }

  @Test
  public void sanitizePath_noLeadingSlash_addsSlash() {
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath("oauth2/authorization"))
        .isEqualTo("/oauth2/authorization");
  }

  // --- sanitizePath: trailing slash ---

  @Test
  public void sanitizePath_trailingSlash_stripped() {
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath("/oauth2/authorization/"))
        .isEqualTo("/oauth2/authorization");
  }

  @Test
  public void sanitizePath_multipleTrailingSlashes_stripped() {
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath("/oauth2/authorization///"))
        .isEqualTo("/oauth2/authorization");
  }

  // --- sanitizePath: duplicate slashes ---

  @Test
  public void sanitizePath_duplicateLeadingSlashes_collapsed() {
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath("//login"))
        .isEqualTo("/login");
  }

  @Test
  public void sanitizePath_duplicateInternalSlashes_collapsed() {
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath("/login//oauth2//code"))
        .isEqualTo("/login/oauth2/code");
  }

  // --- sanitizePath: root path ---

  @Test
  public void sanitizePath_rootSlash_returnsEmpty() {
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath("/")).isEqualTo("");
  }

  // --- sanitizePath: wildcard preserved ---

  @Test
  public void sanitizePath_wildcard_preserved() {
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath("/login/oauth2/code/*"))
        .isEqualTo("/login/oauth2/code/*");
  }

  @Test
  public void sanitizePath_wildcardWithTrailingSlash_stripped() {
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath("/login/oauth2/code/*/"))
        .isEqualTo("/login/oauth2/code/*");
  }

  // --- default endpoint URI semantics ---

  @Test
  public void defaultAuthorizationBaseUri_sanitizesToStandardPath() {
    String defaultValue = new OAuth2Properties.OAuth2EndpointProperties().getAuthorizationBaseUri();
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath(defaultValue))
        .isEqualTo("/oauth2/authorization");
  }

  @Test
  public void defaultRedirectionBaseUri_sanitizesToStandardPath() {
    String defaultValue = new OAuth2Properties.OAuth2EndpointProperties().getRedirectionBaseUri();
    assertThat(EximeeBPMSSpringSecurityOAuth2AutoConfiguration.sanitizePath(defaultValue))
        .isEqualTo("/login/oauth2/code/*");
  }

}
