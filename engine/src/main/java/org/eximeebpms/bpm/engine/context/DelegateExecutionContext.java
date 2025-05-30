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
package org.eximeebpms.bpm.engine.context;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.impl.context.BpmnExecutionContext;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * Represents a delegation execution context which should return the current
 * delegation execution.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class DelegateExecutionContext {

  /**
   * Returns the current delegation execution or null if the
   * execution is not available.
   *
   * @return the current delegation execution or null if not available
   */
  public static DelegateExecution getCurrentDelegationExecution() {
    BpmnExecutionContext bpmnExecutionContext = Context.getBpmnExecutionContext();
    ExecutionEntity executionEntity = null;
    if (bpmnExecutionContext != null) {
      executionEntity = bpmnExecutionContext.getExecution();
    }
    return executionEntity;
  }
}
