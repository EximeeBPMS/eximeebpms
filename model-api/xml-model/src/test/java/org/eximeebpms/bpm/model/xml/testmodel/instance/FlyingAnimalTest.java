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
package org.eximeebpms.bpm.model.xml.testmodel.instance;

import org.eximeebpms.bpm.model.xml.ModelInstance;
import org.eximeebpms.bpm.model.xml.impl.parser.AbstractModelParser;
import org.eximeebpms.bpm.model.xml.testmodel.Gender;
import org.eximeebpms.bpm.model.xml.testmodel.TestModelParser;
import org.eximeebpms.bpm.model.xml.testmodel.TestModelTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eximeebpms.bpm.model.xml.testmodel.TestModelConstants.MODEL_NAMESPACE;
import static org.junit.runners.Parameterized.Parameters;

/**
 * @author Sebastian Menski
 */
public class FlyingAnimalTest extends TestModelTest {

  private FlyingAnimal tweety;
  private FlyingAnimal hedwig;
  private FlyingAnimal birdo;
  private FlyingAnimal plucky;
  private FlyingAnimal fiffy;
  private FlyingAnimal timmy;
  private FlyingAnimal daisy;

  public FlyingAnimalTest(String testName, ModelInstance testModelInstance, AbstractModelParser modelParser) {
    super(testName, testModelInstance, modelParser);
  }

  @Parameters(name="Model {0}")
  public static Collection<Object[]> models() {
    Object[][] models = {createModel(), parseModel(FlyingAnimalTest.class)};
    return Arrays.asList(models);
  }

  public static Object[] createModel() {
    TestModelParser modelParser = new TestModelParser();
    ModelInstance modelInstance = modelParser.getEmptyModel();

    Animals animals = modelInstance.newInstance(Animals.class);
    modelInstance.setDocumentElement(animals);

    // add a tns namespace prefix for QName testing
    animals.getDomElement().registerNamespace("tns", MODEL_NAMESPACE);

    FlyingAnimal tweety = createBird(modelInstance, "tweety", Gender.Female);
    FlyingAnimal hedwig = createBird(modelInstance, "hedwig", Gender.Male);
    FlyingAnimal birdo = createBird(modelInstance, "birdo", Gender.Female);
    FlyingAnimal plucky = createBird(modelInstance, "plucky", Gender.Unknown);
    FlyingAnimal fiffy = createBird(modelInstance, "fiffy", Gender.Female);
    createBird(modelInstance, "timmy", Gender.Male);
    createBird(modelInstance, "daisy", Gender.Female);

    tweety.setFlightInstructor(hedwig);

    tweety.getFlightPartnerRefs().add(hedwig);
    tweety.getFlightPartnerRefs().add(birdo);
    tweety.getFlightPartnerRefs().add(plucky);
    tweety.getFlightPartnerRefs().add(fiffy);


    return new Object[]{"created", modelInstance, modelParser};
  }

  @Before
  public void copyModelInstance() {
    modelInstance = cloneModelInstance();
    tweety = modelInstance.getModelElementById("tweety");
    hedwig = modelInstance.getModelElementById("hedwig");
    birdo = modelInstance.getModelElementById("birdo");
    plucky = modelInstance.getModelElementById("plucky");
    fiffy = modelInstance.getModelElementById("fiffy");
    timmy = modelInstance.getModelElementById("timmy");
    daisy = modelInstance.getModelElementById("daisy");
  }

  @Test
  public void testSetWingspanAttributeByHelper() {
    double wingspan = 2.123;
    tweety.setWingspan(wingspan);
    assertThat(tweety.getWingspan()).isEqualTo(wingspan);
  }

  @Test
  public void testSetWingspanAttributeByAttributeName() {
    Double wingspan = 2.123;
    tweety.setAttributeValue("wingspan", wingspan.toString(), false);
    assertThat(tweety.getWingspan()).isEqualTo(wingspan);
  }

  @Test
  public void testRemoveWingspanAttribute() {
    double wingspan = 2.123;
    tweety.setWingspan(wingspan);
    assertThat(tweety.getWingspan()).isEqualTo(wingspan);

    tweety.removeAttribute("wingspan");

    assertThat(tweety.getWingspan()).isNull();
  }

  @Test
  public void testSetFlightInstructorByHelper() {
    tweety.setFlightInstructor(timmy);
    assertThat(tweety.getFlightInstructor()).isEqualTo(timmy);
  }

  @Test
  public void testUpdateFlightInstructorByIdHelper() {
    hedwig.setId("new-" + hedwig.getId());
    assertThat(tweety.getFlightInstructor()).isEqualTo(hedwig);
  }

  @Test
  public void testUpdateFlightInstructorByIdAttributeName() {
    hedwig.setAttributeValue("id", "new-" + hedwig.getId(), true);
    assertThat(tweety.getFlightInstructor()).isEqualTo(hedwig);
  }

  @Test
  public void testUpdateFlightInstructorByReplaceElement() {
    hedwig.replaceWithElement(timmy);
    assertThat(tweety.getFlightInstructor()).isEqualTo(timmy);
  }

  @Test
  public void testUpdateFlightInstructorByRemoveElement() {
    Animals animals = (Animals) modelInstance.getDocumentElement();
    animals.getAnimals().remove(hedwig);
    assertThat(tweety.getFlightInstructor()).isNull();
  }

  @Test
  public void testClearFlightInstructor() {
    tweety.removeFlightInstructor();
    assertThat(tweety.getFlightInstructor()).isNull();
  }

  @Test
  public void testAddFlightPartnerRefsByHelper() {
    assertThat(tweety.getFlightPartnerRefs())
      .isNotEmpty()
      .hasSize(4)
      .containsOnly(hedwig, birdo, plucky, fiffy);

    tweety.getFlightPartnerRefs().add(timmy);
    tweety.getFlightPartnerRefs().add(daisy);

    assertThat(tweety.getFlightPartnerRefs())
      .isNotEmpty()
      .hasSize(6)
      .containsOnly(hedwig, birdo, plucky, fiffy, timmy, daisy);
  }

  @Test
  public void testUpdateFlightPartnerRefsByIdByHelper() {
    hedwig.setId("new-" + hedwig.getId());
    plucky.setId("new-" + plucky.getId());
    assertThat(tweety.getFlightPartnerRefs())
      .hasSize(4)
      .containsOnly(hedwig, birdo, plucky, fiffy);
  }

  @Test
  public void testUpdateFlightPartnerRefsByIdByAttributeName() {
    birdo.setAttributeValue("id", "new-" + birdo.getId(), true);
    fiffy.setAttributeValue("id", "new-" + fiffy.getId(), true);
    assertThat(tweety.getFlightPartnerRefs())
      .hasSize(4)
      .containsOnly(hedwig, birdo, plucky, fiffy);
  }

  @Test
  public void testUpdateFlightPartnerRefsByReplaceElements() {
    hedwig.replaceWithElement(timmy);
    plucky.replaceWithElement(daisy);
    assertThat(tweety.getFlightPartnerRefs())
      .hasSize(4)
      .containsOnly(birdo, fiffy, timmy ,daisy);
  }

  @Test
  public void testUpdateFlightPartnerRefsByRemoveElements() {
    tweety.getFlightPartnerRefs().remove(birdo);
    tweety.getFlightPartnerRefs().remove(fiffy);
    assertThat(tweety.getFlightPartnerRefs())
      .hasSize(2)
      .containsOnly(hedwig, plucky);
  }

  @Test
  public void testClearFlightPartnerRefs() {
    tweety.getFlightPartnerRefs().clear();
    assertThat(tweety.getFlightPartnerRefs()).isEmpty();
  }

  @Test
  public void testAddFlightPartnerRefElementsByHelper() {
    assertThat(tweety.getFlightPartnerRefElements())
      .isNotEmpty()
      .hasSize(4);

    FlightPartnerRef timmyFlightPartnerRef = modelInstance.newInstance(FlightPartnerRef.class);
    timmyFlightPartnerRef.setTextContent(timmy.getId());
    tweety.getFlightPartnerRefElements().add(timmyFlightPartnerRef);

    FlightPartnerRef daisyFlightPartnerRef = modelInstance.newInstance(FlightPartnerRef.class);
    daisyFlightPartnerRef.setTextContent(daisy.getId());
    tweety.getFlightPartnerRefElements().add(daisyFlightPartnerRef);

    assertThat(tweety.getFlightPartnerRefElements())
      .isNotEmpty()
      .hasSize(6)
      .contains(timmyFlightPartnerRef, daisyFlightPartnerRef);
  }

  @Test
  public void testFlightPartnerRefElementsByTextContent() {
    Collection<FlightPartnerRef> flightPartnerRefElements = tweety.getFlightPartnerRefElements();
    Collection<String> textContents = new ArrayList<String>();
    for (FlightPartnerRef flightPartnerRefElement : flightPartnerRefElements) {
      String textContent = flightPartnerRefElement.getTextContent();
      assertThat(textContent).isNotEmpty();
      textContents.add(textContent);
    }
    assertThat(textContents)
      .isNotEmpty()
      .hasSize(4)
      .containsOnly(hedwig.getId(), birdo.getId(), plucky.getId(), fiffy.getId());
  }

  @Test
  public void testUpdateFlightPartnerRefElementsByTextContent() {
    List<FlightPartnerRef> flightPartnerRefs = new ArrayList<FlightPartnerRef>(tweety.getFlightPartnerRefElements());

    flightPartnerRefs.get(0).setTextContent(timmy.getId());
    flightPartnerRefs.get(2).setTextContent(daisy.getId());

    assertThat(tweety.getFlightPartnerRefs())
      .hasSize(4)
      .containsOnly(birdo, fiffy, timmy, daisy);
  }

  @Test
  public void testUpdateFlightPartnerRefElementsByRemoveElements() {
    List<FlightPartnerRef> flightPartnerRefs = new ArrayList<FlightPartnerRef>(tweety.getFlightPartnerRefElements());
    tweety.getFlightPartnerRefElements().remove(flightPartnerRefs.get(1));
    tweety.getFlightPartnerRefElements().remove(flightPartnerRefs.get(3));
    assertThat(tweety.getFlightPartnerRefs())
      .hasSize(2)
      .containsOnly(hedwig, plucky);
  }

  @Test
  public void testClearFlightPartnerRefElements() {
    tweety.getFlightPartnerRefElements().clear();
    assertThat(tweety.getFlightPartnerRefElements()).isEmpty();

    // should not affect animals collection
    Animals animals = (Animals) modelInstance.getDocumentElement();
    assertThat(animals.getAnimals())
      .isNotEmpty()
      .hasSize(7);
  }

}
