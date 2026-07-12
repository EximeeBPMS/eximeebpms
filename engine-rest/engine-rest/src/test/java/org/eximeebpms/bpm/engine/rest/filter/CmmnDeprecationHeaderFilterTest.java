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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;

public class CmmnDeprecationHeaderFilterTest {

  protected final CmmnDeprecationHeaderFilter filter = new CmmnDeprecationHeaderFilter();

  protected static final String[] CMMN_PATHS = {
      "/engine-rest/case-definition",
      "/engine-rest/case-definition/aCaseDefinitionId",
      "/engine-rest/case-instance",
      "/engine-rest/case-instance/aCaseInstanceId/variables",
      "/engine-rest/case-execution/aCaseExecutionId",
      "/engine-rest/history/case-activity-instance",
      "/engine-rest/engine/default/case-definition/key/aKey"
  };

  protected static final String[] NON_CMMN_PATHS = {
      "/engine-rest/process-definition",
      "/engine-rest/task",
      "/engine-rest/decision-definition",
      "/engine-rest/history/process-instance"
  };

  @Test
  public void shouldSetDeprecationHeadersForCmmnPaths() throws Exception {
    for (String requestUri : CMMN_PATHS) {
      // given
      HttpServletRequest request = mock(HttpServletRequest.class);
      HttpServletResponse response = mock(HttpServletResponse.class);
      FilterChain chain = mock(FilterChain.class);
      when(request.getRequestURI()).thenReturn(requestUri);

      // when
      filter.doFilter(request, response, chain);

      // then
      verify(response).setHeader("Deprecation", "true");
      verify(response).setHeader("Sunset", CmmnDeprecationHeaderFilter.SUNSET_DATE);
      verify(chain).doFilter(request, response);
    }
  }

  @Test
  public void shouldNotSetDeprecationHeadersForNonCmmnPaths() throws Exception {
    for (String requestUri : NON_CMMN_PATHS) {
      // given
      HttpServletRequest request = mock(HttpServletRequest.class);
      HttpServletResponse response = mock(HttpServletResponse.class);
      FilterChain chain = mock(FilterChain.class);
      when(request.getRequestURI()).thenReturn(requestUri);

      // when
      filter.doFilter(request, response, chain);

      // then
      verifyNoInteractions(response);
      verify(chain).doFilter(request, response);
    }
  }

}
