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
package org.eximeebpms.bpm.run;

import jakarta.servlet.Filter;
import org.apache.catalina.filters.CorsFilter;
import org.eximeebpms.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.eximeebpms.bpm.run.property.EximeeBpmsBpmRunAuthenticationProperties;
import org.eximeebpms.bpm.run.property.EximeeBpmsBpmRunCorsProperty;
import org.eximeebpms.bpm.run.property.EximeeBpmsBpmRunProperties;
import org.eximeebpms.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.eximeebpms.bpm.spring.boot.starter.rest.CamundaBpmRestInitializer;
import org.eximeebpms.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.JerseyApplicationPath;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@EnableConfigurationProperties(EximeeBpmsBpmRunProperties.class)
@Configuration
@AutoConfigureAfter({ CamundaBpmAutoConfiguration.class })
@ConditionalOnClass(CamundaBpmRestInitializer.class)
public class EximeeBpmsBpmRunRestConfiguration {

  @Autowired
  EximeeBpmsBpmRunProperties eximeeBpmsBpmRunProperties;

  /*
   * The CORS and Authentication filters need to run before other camunda
   * filters because they potentially block the request and this should be done
   * as early as possible.
   * 
   * The default order parameter for spring-boot managed filters is
   * org.springframework.core.Ordered.LOWEST_PRECEDENCE = Integer.MAX_VALUE.
   * Order can range from -Integer.MAX_VALUE to Integer.MAX_VALUE.
   * 
   * The CORS filter must run before the Authentication filter because CORS
   * preflight requests must not contain authentication. The CORS filter will
   * not invoke the next filter in the chain for preflight requests.
   */
  private static int CORS_FILTER_PRECEDENCE = 0;
  private static int AUTH_FILTER_PRECEDENCE = 1;

  @Bean
  @ConditionalOnProperty(name = "enabled", havingValue = "true", prefix = EximeeBpmsBpmRunAuthenticationProperties.PREFIX)
  public FilterRegistrationBean<Filter> processEngineAuthenticationFilter(JerseyApplicationPath applicationPath) {
    FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
    registration.setName("eximeebpms-auth");
    registration.setFilter(new ProcessEngineAuthenticationFilter());
    registration.setOrder(AUTH_FILTER_PRECEDENCE);

    String restApiPathPattern = applicationPath.getUrlMapping();
    registration.addUrlPatterns(restApiPathPattern);

    // if nothing is set, use Http Basic authentication
    EximeeBpmsBpmRunAuthenticationProperties properties = eximeeBpmsBpmRunProperties.getAuth();
    if (properties.getAuthentication() == null || EximeeBpmsBpmRunAuthenticationProperties.DEFAULT_AUTH.equals(properties.getAuthentication())) {
      registration.addInitParameter("authentication-provider", "org.eximeebpms.bpm.engine.rest.security.auth.impl.HttpBasicAuthenticationProvider");
    }
    return registration;
  }

  @Bean
  @ConditionalOnProperty(name = "enabled", havingValue = "true", prefix = EximeeBpmsBpmRunCorsProperty.PREFIX)
  public FilterRegistrationBean<Filter> corsFilter(JerseyApplicationPath applicationPath) {
    FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
    registration.setName("eximeebpms-cors");
    CorsFilter corsFilter = new CorsFilter();
    registration.setFilter(corsFilter);
    registration.setOrder(CORS_FILTER_PRECEDENCE);

    String restApiPathPattern = applicationPath.getUrlMapping();
    registration.addUrlPatterns(restApiPathPattern);

    registration.addInitParameter(CorsFilter.PARAM_CORS_ALLOWED_ORIGINS,
                                  eximeeBpmsBpmRunProperties.getCors().getAllowedOrigins());
    registration.addInitParameter(CorsFilter.PARAM_CORS_ALLOWED_METHODS,
                                  EximeeBpmsBpmRunCorsProperty.DEFAULT_HTTP_METHODS);
    registration.addInitParameter(CorsFilter.PARAM_CORS_ALLOWED_HEADERS,
                                  eximeeBpmsBpmRunProperties.getCors().getAllowedHeaders());
    registration.addInitParameter(CorsFilter.PARAM_CORS_EXPOSED_HEADERS,
                                  eximeeBpmsBpmRunProperties.getCors().getExposedHeaders());
    registration.addInitParameter(CorsFilter.PARAM_CORS_SUPPORT_CREDENTIALS,
                                  String.valueOf(eximeeBpmsBpmRunProperties.getCors().getAllowCredentials()));
    registration.addInitParameter(CorsFilter.PARAM_CORS_PREFLIGHT_MAXAGE,
                                  eximeeBpmsBpmRunProperties.getCors().getPreflightMaxAge());

    return registration;
  }

  @Bean
  public CamundaJerseyResourceConfig camundaRunJerseyResourceConfig() {
    CamundaJerseyResourceConfig camundaJerseyResourceConfig = new CamundaJerseyResourceConfig();
    camundaJerseyResourceConfig.setProperties(Collections.singletonMap("jersey.config.server.wadl.disableWadl", eximeeBpmsBpmRunProperties.getRest().isDisableWadl()));
    return camundaJerseyResourceConfig;
  }

}
