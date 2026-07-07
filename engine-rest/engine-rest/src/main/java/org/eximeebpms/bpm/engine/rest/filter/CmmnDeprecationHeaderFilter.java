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
package org.eximeebpms.bpm.engine.rest.filter;

import java.io.IOException;
import java.util.regex.Pattern;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Adds RFC 8594 {@code Deprecation}/{@code Sunset} response headers to requests hitting
 * CMMN REST endpoints ({@code case-definition}, {@code case-instance}, {@code case-execution},
 * {@code case-activity-instance}), which are deprecated and will be removed in EximeeBPMS 1.4.0.
 */
public class CmmnDeprecationHeaderFilter implements Filter {

  protected static final Pattern CMMN_PATH_PATTERN =
      Pattern.compile(".*/(case-definition|case-instance|case-execution|case-activity-instance)(/.*)?$");

  // Sunset is optional; set this to a valid HTTP-date (RFC 7231 IMF-fixdate) once the 1.4.0 date is known
  protected static final String SUNSET_DATE = null;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // no initialization necessary
  }

  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) resp;

    if (CMMN_PATH_PATTERN.matcher(request.getRequestURI()).matches()) {
      response.setHeader("Deprecation", "true");
      if (SUNSET_DATE != null) {
        response.setHeader("Sunset", SUNSET_DATE);
      }
    }

    chain.doFilter(req, resp);
  }

  @Override
  public void destroy() {
    // no cleanup necessary
  }

}
