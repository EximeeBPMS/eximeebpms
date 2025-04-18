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
package org.eximeebpms.bpm.model.xml.type.attribute;

import org.eximeebpms.bpm.model.xml.ModelInstance;
import org.eximeebpms.bpm.model.xml.impl.parser.AbstractModelParser;
import org.eximeebpms.bpm.model.xml.impl.type.attribute.AttributeImpl;
import org.eximeebpms.bpm.model.xml.testmodel.Gender;
import org.eximeebpms.bpm.model.xml.testmodel.TestModelParser;
import org.eximeebpms.bpm.model.xml.testmodel.TestModelTest;
import org.eximeebpms.bpm.model.xml.testmodel.instance.Animal;
import org.eximeebpms.bpm.model.xml.testmodel.instance.AnimalTest;
import org.eximeebpms.bpm.model.xml.testmodel.instance.Animals;
import org.eximeebpms.bpm.model.xml.testmodel.instance.Bird;
import org.eximeebpms.bpm.model.xml.type.ModelElementType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.eximeebpms.bpm.model.xml.test.assertions.ModelAssertions.assertThat;
import static org.junit.runners.Parameterized.Parameters;

/**
 * @author Sebastian Menski
 */
public class AttributeTest extends TestModelTest {

  private Bird tweety;
  private Attribute<String> idAttribute;
  private Attribute<String> nameAttribute;
  private Attribute<String> fatherAttribute;

  public AttributeTest(String testName, ModelInstance testModelInstance, AbstractModelParser modelParser) {
    super(testName, testModelInstance, modelParser);
  }

  @Parameters(name="Model {0}")
  public static Collection<Object[]> models() {
    Object[][] models = {createModel(), parseModel(AnimalTest.class)};
    return Arrays.asList(models);
  }

  public static Object[] createModel() {
    TestModelParser modelParser = new TestModelParser();
    ModelInstance modelInstance = modelParser.getEmptyModel();

    Animals animals = modelInstance.newInstance(Animals.class);
    modelInstance.setDocumentElement(animals);

    createBird(modelInstance, "tweety", Gender.Female);

    return new Object[]{"created", modelInstance, modelParser};
  }

  @Before
  @SuppressWarnings("unchecked")
  public void copyModelInstance() {
    modelInstance = cloneModelInstance();

    tweety = modelInstance.getModelElementById("tweety");

    ModelElementType animalType = modelInstance.getModel().getType(Animal.class);
    idAttribute = (Attribute<String>) animalType.getAttribute("id");
    nameAttribute = (Attribute<String>) animalType.getAttribute("name");
    fatherAttribute = (Attribute<String>) animalType.getAttribute("father");
  }

  @Test
  public void testOwningElementType() {
    ModelElementType animalType = modelInstance.getModel().getType(Animal.class);

    assertThat(idAttribute).hasOwningElementType(animalType);
    assertThat(nameAttribute).hasOwningElementType(animalType);
    assertThat(fatherAttribute).hasOwningElementType(animalType);
  }

  @Test
  public void testSetAttributeValue() {
    String identifier = "new-" + tweety.getId();
    idAttribute.setValue(tweety, identifier);
    assertThat(idAttribute).hasValue(tweety, identifier);
  }

  @Test
  public void testSetAttributeValueWithoutUpdateReference() {
    String identifier = "new-" + tweety.getId();
    idAttribute.setValue(tweety, identifier, false);
    assertThat(idAttribute).hasValue(tweety, identifier);
  }

  @Test
  public void testSetDefaultValue() {
    String defaultName = "default-name";
    assertThat(tweety.getName()).isNull();
    assertThat(nameAttribute).hasNoDefaultValue();

    ((AttributeImpl<String>) nameAttribute).setDefaultValue(defaultName);
    assertThat(nameAttribute).hasDefaultValue(defaultName);
    assertThat(tweety.getName()).isEqualTo(defaultName);

    tweety.setName("not-" + defaultName);
    assertThat(tweety.getName()).isNotEqualTo(defaultName);

    tweety.removeAttribute("name");
    assertThat(tweety.getName()).isEqualTo(defaultName);
    ((AttributeImpl<String>) nameAttribute).setDefaultValue(null);
    assertThat(nameAttribute).hasNoDefaultValue();
  }

  @Test
  public void testRequired() {
    tweety.removeAttribute("name");
    assertThat(nameAttribute).isOptional();

    ((AttributeImpl<String>) nameAttribute).setRequired(true);
    assertThat(nameAttribute).isRequired();

    ((AttributeImpl<String>) nameAttribute).setRequired(false);
  }

  @Test
  public void testSetNamespaceUri() {
    String testNamespace = "http://camunda.org/test";

    ((AttributeImpl<String>) idAttribute).setNamespaceUri(testNamespace);
    assertThat(idAttribute).hasNamespaceUri(testNamespace);

    ((AttributeImpl<String>) idAttribute).setNamespaceUri(null);
    assertThat(idAttribute).hasNoNamespaceUri();
  }

  @Test
  public void testIdAttribute() {
    assertThat(idAttribute).isIdAttribute();
    assertThat(nameAttribute).isNotIdAttribute();
    assertThat(fatherAttribute).isNotIdAttribute();
  }

  @Test
  public void testAttributeName() {
    assertThat(idAttribute).hasAttributeName("id");
    assertThat(nameAttribute).hasAttributeName("name");
    assertThat(fatherAttribute).hasAttributeName("father");
  }

  @Test
  public void testRemoveAttribute() {
    tweety.setName("test");
    assertThat(tweety.getName()).isNotNull();
    assertThat(nameAttribute).hasValue(tweety);

    ((AttributeImpl<String>) nameAttribute).removeAttribute(tweety);
    assertThat(tweety.getName()).isNull();
    assertThat(nameAttribute).hasNoValue(tweety);
  }

  @Test
  public void testIncomingReferences() {
    assertThat(idAttribute).hasIncomingReferences();
    assertThat(nameAttribute).hasNoIncomingReferences();
    assertThat(fatherAttribute).hasNoIncomingReferences();
  }

  @Test
  public void testOutgoingReferences() {
    assertThat(idAttribute).hasNoOutgoingReferences();
    assertThat(nameAttribute).hasNoOutgoingReferences();
    assertThat(fatherAttribute).hasOutgoingReferences();
  }

}
