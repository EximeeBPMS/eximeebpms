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
package org.eximeebpms.bpm.integrationtest.deployment.callbacks;

import java.util.List;

import org.junit.Assert;

import org.eximeebpms.bpm.engine.ProcessEngine;
import org.eximeebpms.bpm.integrationtest.deployment.callbacks.apps.PostDeployInjectApp;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class PostDeployInjectDefaultEngineTest {
  
  @Deployment
  public static WebArchive createDeployment() {
    
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
        .addClass(PostDeployInjectApp.class);

    return archive;
    
  }
  
  @Test
  public void test() {
    Assert.assertNotNull("processEngine must be injected", PostDeployInjectApp.processEngine);
    Assert.assertNotNull("processApplicationInfo must be injected", PostDeployInjectApp.processApplicationInfo);
    
    List<ProcessEngine> processEngines = PostDeployInjectApp.processEngines;
    Assert.assertNotNull("processEngines must be injected", processEngines);
    
    // the app did no do a deployment so no engines are in the list
    Assert.assertEquals(0, processEngines.size());
    
  }
  
}
