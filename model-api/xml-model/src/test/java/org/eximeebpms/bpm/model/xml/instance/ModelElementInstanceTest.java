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
package org.eximeebpms.bpm.model.xml.instance;

import org.eximeebpms.bpm.model.xml.ModelInstance;
import org.eximeebpms.bpm.model.xml.impl.parser.AbstractModelParser;
import org.eximeebpms.bpm.model.xml.testmodel.Gender;
import org.eximeebpms.bpm.model.xml.testmodel.TestModelParser;
import org.eximeebpms.bpm.model.xml.testmodel.TestModelTest;
import org.eximeebpms.bpm.model.xml.testmodel.instance.Animals;
import org.eximeebpms.bpm.model.xml.testmodel.instance.Bird;
import org.eximeebpms.bpm.model.xml.type.ModelElementType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eximeebpms.bpm.model.xml.testmodel.TestModelConstants.MODEL_NAMESPACE;
import static org.junit.runners.Parameterized.Parameters;

/**
 * @author Daniel Meyer
 * @author Sebastian Menski
 */
public class ModelElementInstanceTest extends TestModelTest {

  private Animals animals;
  private Bird tweety;
  private Bird donald;
  private Bird daisy;
  private Bird hedwig;

  public ModelElementInstanceTest(String testName, ModelInstance testModelInstance, AbstractModelParser modelParser) {
    super(testName, testModelInstance, modelParser);
  }

  @Parameters(name="Model {0}")
  public static Collection<Object[]> models() {
    Object[][] models = {createModel(), parseModel(ModelElementInstanceTest.class)};
    return Arrays.asList(models);
  }

  private static Object[] createModel() {
    TestModelParser modelParser = new TestModelParser();
    ModelInstance modelInstance = modelParser.getEmptyModel();

    Animals animals = modelInstance.newInstance(Animals.class);
    modelInstance.setDocumentElement(animals);

    createBird(modelInstance, "tweety", Gender.Female);
    Bird donald = createBird(modelInstance, "donald", Gender.Male);
    Bird daisy = createBird(modelInstance, "daisy", Gender.Female);
    Bird hedwig = createBird(modelInstance, "hedwig", Gender.Male);

    donald.setTextContent("some text content");
    daisy.setTextContent("\n        some text content with outer line breaks\n    ");
    hedwig.setTextContent("\n        some text content with inner\n        line breaks\n    ");

    return new Object[]{"created", modelInstance, modelParser};
  }

  @Before
  public void copyModelInstance() {
    modelInstance = cloneModelInstance();

    animals = (Animals) modelInstance.getDocumentElement();
    tweety = modelInstance.getModelElementById("tweety");
    donald = modelInstance.getModelElementById("donald");
    daisy = modelInstance.getModelElementById("daisy");
    hedwig = modelInstance.getModelElementById("hedwig");
  }

  @Test
  public void testAttribute() {
    String tweetyName = tweety.getId() + "-name";
    tweety.setAttributeValue("name", tweetyName);
    assertThat(tweety.getAttributeValue("name")).isEqualTo(tweetyName);
    tweety.removeAttribute("name");
    assertThat(tweety.getAttributeValue("name")).isNull();
  }

  @Test
  public void testAttributeWithNamespace() {
    String tweetyName = tweety.getId() + "-name";
    tweety.setAttributeValueNs(MODEL_NAMESPACE, "name", tweetyName);
    assertThat(tweety.getAttributeValue("name")).isEqualTo(tweetyName);
    assertThat(tweety.getAttributeValueNs(MODEL_NAMESPACE, "name")).isEqualTo(tweetyName);
    tweety.removeAttributeNs(MODEL_NAMESPACE, "name");
    assertThat(tweety.getAttributeValue("name")).isNull();
    assertThat(tweety.getAttributeValueNs(MODEL_NAMESPACE, "name")).isNull();
  }

  @Test
  public void TestElementType() {
    ModelElementType birdType = modelInstance.getModel().getType(Bird.class);
    assertThat(tweety.getElementType()).isEqualTo(birdType);
    assertThat(donald.getElementType()).isEqualTo(birdType);
    assertThat(daisy.getElementType()).isEqualTo(birdType);
    assertThat(hedwig.getElementType()).isEqualTo(birdType);
  }

  @Test
  public void TestParentElement() {
    assertThat(tweety.getParentElement()).isEqualTo(animals);
    assertThat(donald.getParentElement()).isEqualTo(animals);
    assertThat(daisy.getParentElement()).isEqualTo(animals);
    assertThat(hedwig.getParentElement()).isEqualTo(animals);

    Bird timmy = modelInstance.newInstance(Bird.class);
    timmy.setId("timmy");
    timmy.setGender(Gender.Male);
    assertThat(timmy.getParentElement()).isNull();
  }

  @Test
  public void TestModelInstance() {
    assertThat(tweety.getModelInstance()).isEqualTo(modelInstance);
    assertThat(donald.getModelInstance()).isEqualTo(modelInstance);
    assertThat(daisy.getModelInstance()).isEqualTo(modelInstance);
    assertThat(hedwig.getModelInstance()).isEqualTo(modelInstance);
  }

  @Test
  public void testReplaceWithElement() {
    Bird timmy = modelInstance.newInstance(Bird.class);
    timmy.setId("timmy");
    timmy.setGender(Gender.Male);

    assertThat(animals.getAnimals())
      .contains(tweety)
      .doesNotContain(timmy);

    tweety.replaceWithElement(timmy);

    assertThat(animals.getAnimals())
      .contains(timmy)
      .doesNotContain(tweety);
  }

  @Test
  public void testReplaceRootElement() {
    assertThat(((Animals) modelInstance.getDocumentElement()).getAnimals()).isNotEmpty();
    Animals newAnimals = modelInstance.newInstance(Animals.class);
    modelInstance.setDocumentElement(newAnimals);
    assertThat(((Animals) modelInstance.getDocumentElement()).getAnimals()).isEmpty();
  }

  @Test
  public void testTextContent() {
    assertThat(tweety.getTextContent()).isEqualTo("");
    assertThat(donald.getTextContent()).isEqualTo("some text content");
    assertThat(daisy.getTextContent()).isEqualTo("some text content with outer line breaks");
    assertThat(hedwig.getTextContent()).isEqualTo("some text content with inner\n        line breaks");

    String testContent = "\n test content \n \n \t camunda.org \t    \n   ";
    tweety.setTextContent(testContent);
    assertThat(tweety.getTextContent()).isEqualTo(testContent.trim());
  }

  @Test
  public void testRawTextContent() {
    assertThat(tweety.getRawTextContent()).isEqualTo("");
    assertThat(donald.getRawTextContent()).isEqualTo("some text content");
    assertThat(daisy.getRawTextContent()).isEqualTo("\n        some text content with outer line breaks\n    ");
    assertThat(hedwig.getRawTextContent()).isEqualTo("\n        some text content with inner\n        line breaks\n    ");

    String testContent = "\n test content \n \n \t camunda.org \t    \n   ";
    tweety.setTextContent(testContent);
    assertThat(tweety.getRawTextContent()).isEqualTo(testContent);
  }

}
