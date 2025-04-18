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

import org.eximeebpms.bpm.model.bpmn.instance.BaseElement;
import org.eximeebpms.bpm.model.bpmn.instance.Participant;
import org.eximeebpms.bpm.model.bpmn.instance.ParticipantAssociation;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.child.SequenceBuilder;
import org.eximeebpms.bpm.model.xml.type.reference.ElementReference;

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_PARTICIPANT_ASSOCIATION;
import static org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN participantAssociation element
 *
 * @author Sebastian Menski
 */
public class ParticipantAssociationImpl extends BaseElementImpl implements ParticipantAssociation {

  protected static ElementReference<Participant, InnerParticipantRef> innerParticipantRefChild;
  protected static ElementReference<Participant, OuterParticipantRef> outerParticipantRefChild;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ParticipantAssociation.class, BPMN_ELEMENT_PARTICIPANT_ASSOCIATION)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<ParticipantAssociation>() {
        public ParticipantAssociation newInstance(ModelTypeInstanceContext instanceContext) {
          return new ParticipantAssociationImpl(instanceContext);
        }
      });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    innerParticipantRefChild = sequenceBuilder.element(InnerParticipantRef.class)
      .required()
      .qNameElementReference(Participant.class)
      .build();

    outerParticipantRefChild = sequenceBuilder.element(OuterParticipantRef.class)
      .required()
      .qNameElementReference(Participant.class)
      .build();

    typeBuilder.build();
  }

  public ParticipantAssociationImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Participant getInnerParticipant() {
    return innerParticipantRefChild.getReferenceTargetElement(this);
  }

  public void setInnerParticipant(Participant innerParticipant) {
   innerParticipantRefChild.setReferenceTargetElement(this, innerParticipant);
  }

  public Participant getOuterParticipant() {
    return outerParticipantRefChild.getReferenceTargetElement(this);
  }

  public void setOuterParticipant(Participant outerParticipant) {
     outerParticipantRefChild.setReferenceTargetElement(this, outerParticipant);
  }
}
