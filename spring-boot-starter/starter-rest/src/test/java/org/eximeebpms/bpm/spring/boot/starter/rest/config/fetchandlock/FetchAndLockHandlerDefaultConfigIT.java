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
package org.eximeebpms.bpm.spring.boot.starter.rest.config.fetchandlock;

import jakarta.servlet.ServletContext;
import org.eximeebpms.bpm.spring.boot.starter.rest.test.TestRestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestRestApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FetchAndLockHandlerDefaultConfigIT {

    @Autowired
    protected ServletContext servletContext;

    @Test
    public void shouldReturnDefaultValues() {
        // when
        String queueCapacity = servletContext.getInitParameter("fetch-and-lock-queue-capacity");
        String uniqueWorkerRequest = servletContext.getInitParameter("fetch-and-lock-unique-worker-request");

        // then
        assertThat(queueCapacity).isNull();
        assertThat(uniqueWorkerRequest).isNull();
    }

}
