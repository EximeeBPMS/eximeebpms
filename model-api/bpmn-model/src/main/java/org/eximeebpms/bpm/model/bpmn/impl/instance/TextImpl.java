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
package org.eximeebpms.bpm.model.bpmn.impl.instance;

import org.eximeebpms.bpm.model.bpmn.instance.Text;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_TEXT;
import static org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN 2.0 text element from the tTextAnnotation complex type
 *
 * @author Filip Hrisafov
 */
public class TextImpl extends BpmnModelElementInstanceImpl implements Text {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Text.class, BPMN_ELEMENT_TEXT)
      .namespaceUri(BPMN20_NS)
      .instanceProvider(new ModelTypeInstanceProvider<Text>() {
        public Text newInstance(ModelTypeInstanceContext instanceContext) {
          return new TextImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

  public TextImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }
}
