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
package org.eximeebpms.bpm.run.property;

import org.eximeebpms.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(EximeeBpmsBpmRunProperties.PREFIX)
public class EximeeBpmsBpmRunProperties {

  public static final String PREFIX = CamundaBpmProperties.PREFIX + ".run";

  @NestedConfigurationProperty
  protected EximeeBpmsBpmRunAuthenticationProperties auth = new EximeeBpmsBpmRunAuthenticationProperties();

  @NestedConfigurationProperty
  protected EximeeBpmsBpmRunCorsProperty cors = new EximeeBpmsBpmRunCorsProperty();

  @NestedConfigurationProperty
  protected EximeeBpmsBpmRunLdapProperties ldap = new EximeeBpmsBpmRunLdapProperties();

  @NestedConfigurationProperty
  protected List<EximeeBpmsBpmRunProcessEnginePluginProperty> processEnginePlugins = new ArrayList<>();

  @NestedConfigurationProperty
  protected EximeeBpmsBpmRunRestProperties rest = new EximeeBpmsBpmRunRestProperties();

  @NestedConfigurationProperty
  protected EximeeBpmsBpmRunDeploymentProperties deployment = new EximeeBpmsBpmRunDeploymentProperties();

  protected EximeeBpmsBpmRunAdministratorAuthorizationProperties adminAuth
      = new EximeeBpmsBpmRunAdministratorAuthorizationProperties();

  public EximeeBpmsBpmRunAuthenticationProperties getAuth() {
    return auth;
  }

  public void setAuth(EximeeBpmsBpmRunAuthenticationProperties auth) {
    this.auth = auth;
  }

  public EximeeBpmsBpmRunCorsProperty getCors() {
    return cors;
  }

  public void setCors(EximeeBpmsBpmRunCorsProperty cors) {
    this.cors = cors;
  }

  public EximeeBpmsBpmRunLdapProperties getLdap() {
    return ldap;
  }

  public void setLdap(EximeeBpmsBpmRunLdapProperties ldap) {
    this.ldap = ldap;
  }

  public EximeeBpmsBpmRunAdministratorAuthorizationProperties getAdminAuth() {
    return adminAuth;
  }

  public void setAdminAuth(EximeeBpmsBpmRunAdministratorAuthorizationProperties adminAuth) {
    this.adminAuth = adminAuth;
  }

  public List<EximeeBpmsBpmRunProcessEnginePluginProperty> getProcessEnginePlugins() {
    return processEnginePlugins;
  }

  public void setProcessEnginePlugins(List<EximeeBpmsBpmRunProcessEnginePluginProperty> processEnginePlugins) {
    this.processEnginePlugins = processEnginePlugins;
  }

  public EximeeBpmsBpmRunRestProperties getRest() {
    return rest;
  }

  public void setRest(EximeeBpmsBpmRunRestProperties rest) {
    this.rest = rest;
  }

  public EximeeBpmsBpmRunDeploymentProperties getDeployment() {
    return deployment;
  }

  public void setDeployment(EximeeBpmsBpmRunDeploymentProperties deployment) {
    this.deployment = deployment;
  }


  @Override
  public String toString() {
    return "CamundaBpmRunProperties [" +
        "auth=" + auth +
        ", cors=" + cors +
        ", ldap=" + ldap +
        ", adminAuth=" + adminAuth +
        ", plugins=" + processEnginePlugins +
        ", rest=" + rest +
        ", deployment=" + deployment +
        "]";
  }
}
