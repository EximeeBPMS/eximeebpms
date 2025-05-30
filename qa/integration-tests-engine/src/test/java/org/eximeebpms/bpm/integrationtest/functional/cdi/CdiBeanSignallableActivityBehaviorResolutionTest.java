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
package org.eximeebpms.bpm.integrationtest.functional.cdi;

import org.eximeebpms.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.eximeebpms.bpm.engine.runtime.ProcessInstance;
import org.eximeebpms.bpm.integrationtest.functional.cdi.beans.ExampleSignallableActivityBehaviorBean;
import org.eximeebpms.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.eximeebpms.bpm.integrationtest.util.DeploymentHelper;
import org.eximeebpms.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 *
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class CdiBeanSignallableActivityBehaviorResolutionTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createProcessArchiveDeplyoment() {
    return initWebArchiveDeployment()
            .addClass(ExampleSignallableActivityBehaviorBean.class)
            .addAsResource("org/eximeebpms/bpm/integrationtest/functional/cdi/CdiBeanSignallableActivityBehaviorResolutionTest.testResolveBean.bpmn20.xml");
  }

  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {
    WebArchive deployment = ShrinkWrap.create(WebArchive.class, "client.war")
            .addAsWebInfResource("org/eximeebpms/bpm/integrationtest/beans.xml", "beans.xml")
            .addClass(AbstractFoxPlatformIntegrationTest.class)
            .addAsLibraries(DeploymentHelper.getEngineCdi());

    TestContainer.addContainerSpecificResourcesForNonPaEmbedCdiLib(deployment);

    return deployment;
  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveClass() {
    // assert that we cannot resolve the bean here:
    Assert.assertNull(ProgrammaticBeanLookup.lookup("exampleSignallableActivityBehaviorBean"));

    // but the process can since it performs context switch to the process archive for execution
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testResolveBean");

    runtimeService.signal(processInstance.getId());

  }

}
