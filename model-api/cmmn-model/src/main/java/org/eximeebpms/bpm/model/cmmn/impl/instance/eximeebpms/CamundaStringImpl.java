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
package org.eximeebpms.bpm.model.cmmn.impl.instance.eximeebpms;

import static org.eximeebpms.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ELEMENT_STRING;
import static org.eximeebpms.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_NS;

import org.eximeebpms.bpm.model.cmmn.impl.instance.CmmnModelElementInstanceImpl;
import org.eximeebpms.bpm.model.cmmn.instance.eximeebpms.CamundaString;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * @author Roman Smirnov
 *
 */
public class CamundaStringImpl extends CmmnModelElementInstanceImpl implements CamundaString {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaString.class, CAMUNDA_ELEMENT_STRING)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaString>() {
        public CamundaString newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaStringImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

  public CamundaStringImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

}
