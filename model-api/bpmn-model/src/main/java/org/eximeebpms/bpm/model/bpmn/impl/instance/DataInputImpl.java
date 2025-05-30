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

import org.eximeebpms.bpm.model.bpmn.instance.DataInput;
import org.eximeebpms.bpm.model.bpmn.instance.ItemAwareElement;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.attribute.Attribute;

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN dataInput element
 *
 * @author Sebastian Menski
 */
public class DataInputImpl extends ItemAwareElementImpl implements DataInput {

  protected static Attribute<String> nameAttribute;
  protected static Attribute<Boolean> isCollectionAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(DataInput.class, BPMN_ELEMENT_DATA_INPUT)
      .namespaceUri(BPMN20_NS)
      .extendsType(ItemAwareElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<DataInput>() {
        public DataInput newInstance(ModelTypeInstanceContext instanceContext) {
          return new DataInputImpl(instanceContext);
        }
      });

    nameAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_NAME)
      .build();

    isCollectionAttribute = typeBuilder.booleanAttribute(BPMN_ATTRIBUTE_IS_COLLECTION)
      .defaultValue(false)
      .build();

    typeBuilder.build();
  }

  public DataInputImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public boolean isCollection() {
    return isCollectionAttribute.getValue(this);
  }

  public void setCollection(boolean isCollection) {
    isCollectionAttribute.setValue(this, isCollection);
  }
}
