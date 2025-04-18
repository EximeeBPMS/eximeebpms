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
package org.eximeebpms.bpm.dmn.engine.el;

import org.eximeebpms.bpm.dmn.engine.test.DecisionResource;
import org.eximeebpms.bpm.dmn.engine.test.DmnEngineTest;
import org.junit.Test;

public class FeelTest extends DmnEngineTest {

  public static final String FEEL_TEST_DMN = "FeelTest.dmn";

  @Test
  @DecisionResource(resource = FEEL_TEST_DMN)
  public void testStringVariable() {
    variables.putValue("stringInput", "eximeebpms");
    variables.putValue("numberInput", 13.37);
    variables.putValue("booleanInput", true);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);
  }

}
