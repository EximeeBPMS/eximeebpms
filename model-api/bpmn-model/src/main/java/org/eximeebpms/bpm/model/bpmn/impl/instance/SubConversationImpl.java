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

import org.eximeebpms.bpm.model.bpmn.instance.ConversationNode;
import org.eximeebpms.bpm.model.bpmn.instance.SubConversation;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.child.ChildElementCollection;
import org.eximeebpms.bpm.model.xml.type.child.SequenceBuilder;

import java.util.Collection;

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_SUB_CONVERSATION;
import static org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN subConversation element
 *
 * @author Sebastian Menski
 */
public class SubConversationImpl extends ConversationNodeImpl implements SubConversation {

  protected static ChildElementCollection<ConversationNode> conversationNodeCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(SubConversation.class, BPMN_ELEMENT_SUB_CONVERSATION)
      .namespaceUri(BPMN20_NS)
      .extendsType(ConversationNode.class)
      .instanceProvider(new ModelTypeInstanceProvider<SubConversation>() {
        public SubConversation newInstance(ModelTypeInstanceContext instanceContext) {
          return new SubConversationImpl(instanceContext);
        }
      });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    conversationNodeCollection = sequenceBuilder.elementCollection(ConversationNode.class)
      .build();

    typeBuilder.build();
  }

  public SubConversationImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Collection<ConversationNode> getConversationNodes() {
    return conversationNodeCollection.get(this);
  }
}
