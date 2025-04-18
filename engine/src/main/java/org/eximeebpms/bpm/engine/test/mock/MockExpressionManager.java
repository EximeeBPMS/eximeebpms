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
package org.eximeebpms.bpm.engine.test.mock;

import org.eximeebpms.bpm.engine.delegate.VariableScope;
import org.eximeebpms.bpm.engine.impl.el.ExpressionManager;
import org.eximeebpms.bpm.engine.impl.el.JuelExpressionManager;
import org.eximeebpms.bpm.engine.impl.el.VariableContextElResolver;
import org.eximeebpms.bpm.engine.impl.el.VariableScopeElResolver;
import org.eximeebpms.bpm.impl.juel.jakarta.el.ArrayELResolver;
import org.eximeebpms.bpm.impl.juel.jakarta.el.BeanELResolver;
import org.eximeebpms.bpm.impl.juel.jakarta.el.CompositeELResolver;
import org.eximeebpms.bpm.impl.juel.jakarta.el.ELResolver;
import org.eximeebpms.bpm.impl.juel.jakarta.el.ListELResolver;
import org.eximeebpms.bpm.impl.juel.jakarta.el.MapELResolver;

public class MockExpressionManager extends JuelExpressionManager {

  protected ELResolver createElResolver(VariableScope scope) {
    return createElResolver();
  }

  @Override
  protected ELResolver createElResolver() {
    CompositeELResolver compositeElResolver = new CompositeELResolver();
    compositeElResolver.add(new VariableScopeElResolver());
    compositeElResolver.add(new VariableContextElResolver());
    compositeElResolver.add(new MockElResolver());
    compositeElResolver.add(new ArrayELResolver());
    compositeElResolver.add(new ListELResolver());
    compositeElResolver.add(new MapELResolver());
    compositeElResolver.add(new BeanELResolver());
    return compositeElResolver;
  }

}
