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
package org.eximeebpms.bpm.spring.boot.starter.rest;

import org.eximeebpms.bpm.engine.rest.filter.CacheControlFilter;
import org.eximeebpms.bpm.engine.rest.filter.EmptyBodyFilter;
import org.eximeebpms.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.JerseyApplicationPath;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;

import java.util.EnumSet;
import java.util.Map;

/**
 * Inspired by:
 * https://groups.google.com/forum/#!msg/eximeebpms-bpm-users/BQHdcLIivzs
 * /iNVix8GkhYAJ (Christoph Berg)
 */
public class CamundaBpmRestInitializer implements ServletContextInitializer {

  private static final Logger log = LoggerFactory.getLogger(CamundaBpmRestInitializer.class);

  private static final EnumSet<DispatcherType> DISPATCHER_TYPES = EnumSet.of(DispatcherType.REQUEST);

  private ServletContext servletContext;

  private JerseyApplicationPath applicationPath;

  private final CamundaBpmProperties properties;

  public CamundaBpmRestInitializer(JerseyApplicationPath applicationPath, CamundaBpmProperties properties) {
    this.applicationPath = applicationPath;
    this.properties = properties;
  }

  @Override
  public void onStartup(ServletContext servletContext) {
    this.servletContext = servletContext;

    properties.getRestApi().getFetchAndLock().getInitParams().forEach(servletContext::setInitParameter);

    String restApiPathPattern = applicationPath.getUrlMapping();

    registerFilter("EmptyBodyFilter", EmptyBodyFilter.class, restApiPathPattern);
    registerFilter("CacheControlFilter", CacheControlFilter.class, restApiPathPattern);
  }

  private FilterRegistration registerFilter(final String filterName, final Class<? extends Filter> filterClass, final String... urlPatterns) {
    return registerFilter(filterName, filterClass, null, urlPatterns);
  }

  private FilterRegistration registerFilter(final String filterName, final Class<? extends Filter> filterClass, final Map<String, String> initParameters,
                                            final String... urlPatterns) {
    FilterRegistration filterRegistration = servletContext.getFilterRegistration(filterName);

    if (filterRegistration == null) {
      filterRegistration = servletContext.addFilter(filterName, filterClass);
      filterRegistration.addMappingForUrlPatterns(DISPATCHER_TYPES, true, urlPatterns);

      if (initParameters != null) {
        filterRegistration.setInitParameters(initParameters);
      }

      log.debug("Filter {} for URL {} registered.", filterName, urlPatterns);
    }

    return filterRegistration;
  }

}
