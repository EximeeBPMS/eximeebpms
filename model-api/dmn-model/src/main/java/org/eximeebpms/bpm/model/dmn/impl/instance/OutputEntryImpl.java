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
import static org.eximeebpms.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT_OUTPUT_ENTRY;

import org.eximeebpms.bpm.model.dmn.instance.LiteralExpression;
import org.eximeebpms.bpm.model.dmn.instance.OutputEntry;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

public class OutputEntryImpl extends LiteralExpressionImpl implements OutputEntry {

  public OutputEntryImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(OutputEntry.class, DMN_ELEMENT_OUTPUT_ENTRY)
      .namespaceUri(LATEST_DMN_NS)
      .extendsType(LiteralExpression.class)
      .instanceProvider(new ModelTypeInstanceProvider<OutputEntry>() {
        public OutputEntry newInstance(ModelTypeInstanceContext instanceContext) {
          return new OutputEntryImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

}
