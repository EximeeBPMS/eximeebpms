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
package org.eximeebpms.bpm.model.dmn.impl.instance;

import static org.eximeebpms.bpm.model.dmn.impl.DmnModelConstants.LATEST_DMN_NS;
import static org.eximeebpms.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT_PERFORMANCE_INDICATOR;

import java.util.Collection;

import org.eximeebpms.bpm.model.dmn.instance.BusinessContextElement;
import org.eximeebpms.bpm.model.dmn.instance.Decision;
import org.eximeebpms.bpm.model.dmn.instance.ImpactingDecisionReference;
import org.eximeebpms.bpm.model.dmn.instance.PerformanceIndicator;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.eximeebpms.bpm.model.xml.type.child.SequenceBuilder;
import org.eximeebpms.bpm.model.xml.type.reference.ElementReferenceCollection;

public class PerformanceIndicatorImpl extends BusinessContextElementImpl implements PerformanceIndicator {

  protected static ElementReferenceCollection<Decision, ImpactingDecisionReference> impactingDecisionRefCollection;

  public PerformanceIndicatorImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Collection<Decision> getImpactingDecisions() {
    return impactingDecisionRefCollection.getReferenceTargetElements(this);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(PerformanceIndicator.class, DMN_ELEMENT_PERFORMANCE_INDICATOR)
      .namespaceUri(LATEST_DMN_NS)
      .extendsType(BusinessContextElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<PerformanceIndicator>() {
        public PerformanceIndicator newInstance(ModelTypeInstanceContext instanceContext) {
          return new PerformanceIndicatorImpl(instanceContext);
        }
      });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    impactingDecisionRefCollection = sequenceBuilder.elementCollection(ImpactingDecisionReference.class)
      .uriElementReferenceCollection(Decision.class)
      .build();

    typeBuilder.build();
  }

}
