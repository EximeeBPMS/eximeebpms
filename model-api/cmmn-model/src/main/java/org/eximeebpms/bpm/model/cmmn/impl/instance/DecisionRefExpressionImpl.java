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
package org.eximeebpms.bpm.model.cmmn.impl.instance;

import static org.eximeebpms.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.eximeebpms.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_DECISION_REF_EXPRESSION;

import org.eximeebpms.bpm.model.cmmn.instance.DecisionRefExpression;
import org.eximeebpms.bpm.model.cmmn.instance.Expression;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class DecisionRefExpressionImpl extends ExpressionImpl implements DecisionRefExpression {

  public DecisionRefExpressionImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(DecisionRefExpression.class, CMMN_ELEMENT_DECISION_REF_EXPRESSION)
      .namespaceUri(CMMN11_NS)
      .extendsType(Expression.class)
      .instanceProvider(new ModelElementTypeBuilder.ModelTypeInstanceProvider<DecisionRefExpression>() {
        public DecisionRefExpression newInstance(ModelTypeInstanceContext instanceContext) {
          return new DecisionRefExpressionImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

}
