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
package org.eximeebpms.bpm.engine.test.cmmn.handler;

import static org.eximeebpms.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_ACTIVITY_DESCRIPTION;
import static org.eximeebpms.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_ACTIVITY_TYPE;
import static org.eximeebpms.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_DISCRETIONARY;
import static org.eximeebpms.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_IS_BLOCKING;
import static org.eximeebpms.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE;
import static org.eximeebpms.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_REQUIRED_RULE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eximeebpms.bpm.engine.impl.cmmn.CaseControlRule;
import org.eximeebpms.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.eximeebpms.bpm.engine.impl.cmmn.behavior.HumanTaskActivityBehavior;
import org.eximeebpms.bpm.engine.impl.cmmn.handler.HumanTaskItemHandler;
import org.eximeebpms.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.eximeebpms.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.eximeebpms.bpm.model.cmmn.Cmmn;
import org.eximeebpms.bpm.model.cmmn.instance.ConditionExpression;
import org.eximeebpms.bpm.model.cmmn.instance.DefaultControl;
import org.eximeebpms.bpm.model.cmmn.instance.DiscretionaryItem;
import org.eximeebpms.bpm.model.cmmn.instance.HumanTask;
import org.eximeebpms.bpm.model.cmmn.instance.ItemControl;
import org.eximeebpms.bpm.model.cmmn.instance.ManualActivationRule;
import org.eximeebpms.bpm.model.cmmn.instance.PlanItemControl;
import org.eximeebpms.bpm.model.cmmn.instance.PlanningTable;
import org.eximeebpms.bpm.model.cmmn.instance.RequiredRule;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class HumanTaskDicretionaryItemHandlerTest extends CmmnElementHandlerTest {

  protected HumanTask humanTask;
  protected PlanningTable planningTable;
  protected DiscretionaryItem discretionaryItem;
  protected HumanTaskItemHandler handler = new HumanTaskItemHandler();

  @Before
  public void setUp() {
    humanTask = createElement(casePlanModel, "aHumanTask", HumanTask.class);

    planningTable = createElement(casePlanModel, "aPlanningTable", PlanningTable.class);

    discretionaryItem = createElement(planningTable, "DI_aHumanTask", DiscretionaryItem.class);
    discretionaryItem.setDefinition(humanTask);

  }

  @Test
  public void testHumanTaskActivityName() {
    // given:
    // the humanTask has a name "A HumanTask"
    String name = "A HumanTask";
    humanTask.setName(name);

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    assertEquals(name, activity.getName());
  }

  @Test
  public void testHumanTaskActivityType() {
    // given

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    String activityType = (String) activity.getProperty(PROPERTY_ACTIVITY_TYPE);
    assertEquals("humanTask", activityType);
  }

  @Test
  public void testHumanTaskDescription() {
    // given
    String description = "This is a humanTask";
    humanTask.setDescription(description);

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    assertEquals(description, activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testDiscretionaryItemDescription() {
    // given
    String description = "This is a discretionaryItem";
    discretionaryItem.setDescription(description);

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    assertEquals(description, activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testActivityBehavior() {
    // given: a discretionaryItem

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    assertTrue(behavior instanceof HumanTaskActivityBehavior);
  }

  @Test
  public void testIsBlockingEqualsTrueProperty() {
    // given: a humanTask with isBlocking = true (defaultValue)

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    Boolean isBlocking = (Boolean) activity.getProperty(PROPERTY_IS_BLOCKING);
    assertTrue(isBlocking);
  }

  @Test
  public void testIsBlockingEqualsFalseProperty() {
    // given:
    // a humanTask with isBlocking = false
    humanTask.setIsBlocking(false);

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    // According to the specification:
    // When a HumanTask is not 'blocking'
    // (isBlocking is 'false'), it can be
    // considered a 'manual' Task, i.e.,
    // the Case management system is not
    // tracking the lifecycle of the HumanTask (instance).
    assertNull(activity);
  }

  @Test
  public void testIsDiscretionaryProperty() {
    // given:
    // a discretionary item to handle

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    Boolean discretionary = (Boolean) activity.getProperty(PROPERTY_DISCRETIONARY);
    assertTrue(discretionary);
  }

  @Test
  public void testWithoutParent() {
    // given: a discretionaryItem

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    assertNull(activity.getParent());
  }

  @Test
  public void testWithParent() {
    // given:
    // a new activity as parent
    CmmnCaseDefinition parent = new CmmnCaseDefinition("aParentActivity");
    context.setParent(parent);

    // when
    CmmnActivity activity = handler.handleElement(discretionaryItem, context);

    // then
    assertEquals(parent, activity.getParent());
    assertTrue(parent.getActivities().contains(activity));
  }

  @Test
  public void testManualActivationRule() {
    // given
    ItemControl itemControl = createElement(discretionaryItem, "ItemControl_1", ItemControl.class);
    ManualActivationRule manualActivationRule = createElement(itemControl, "ManualActivationRule_1", ManualActivationRule.class);
    ConditionExpression expression = createElement(manualActivationRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(discretionaryItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_MANUAL_ACTIVATION_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testManualActivationRuleByDefaultPlanItemControl() {
    // given
    PlanItemControl defaultControl = createElement(humanTask, "ItemControl_1", DefaultControl.class);
    ManualActivationRule manualActivationRule = createElement(defaultControl, "ManualActivationRule_1", ManualActivationRule.class);
    ConditionExpression expression = createElement(manualActivationRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(discretionaryItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_MANUAL_ACTIVATION_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testRequiredRule() {
    // given
    ItemControl itemControl = createElement(discretionaryItem, "ItemControl_1", ItemControl.class);
    RequiredRule requiredRule = createElement(itemControl, "RequiredRule_1", RequiredRule.class);
    ConditionExpression expression = createElement(requiredRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(discretionaryItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_REQUIRED_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testRequiredRuleByDefaultPlanItemControl() {
    // given
    PlanItemControl defaultControl = createElement(humanTask, "ItemControl_1", DefaultControl.class);
    RequiredRule requiredRule = createElement(defaultControl, "RequiredRule_1", RequiredRule.class);
    ConditionExpression expression = createElement(requiredRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(discretionaryItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_REQUIRED_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

}
