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
package org.eximeebpms.bpm.model.bpmn.impl.instance.di;

import org.eximeebpms.bpm.model.bpmn.impl.instance.dc.PointImpl;
import org.eximeebpms.bpm.model.bpmn.instance.dc.Point;
import org.eximeebpms.bpm.model.bpmn.instance.di.Waypoint;
import org.eximeebpms.bpm.model.xml.ModelBuilder;
import org.eximeebpms.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder;

import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.DI_ELEMENT_WAYPOINT;
import static org.eximeebpms.bpm.model.bpmn.impl.BpmnModelConstants.DI_NS;
import static org.eximeebpms.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The DI waypoint element of the DI Edge type
 *
 * @author Sebastian Menski
 */
public class WaypointImpl extends PointImpl implements Waypoint {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Waypoint.class, DI_ELEMENT_WAYPOINT)
      .namespaceUri(DI_NS)
      .extendsType(Point.class)
      .instanceProvider(new ModelTypeInstanceProvider<Waypoint>() {
        public Waypoint newInstance(ModelTypeInstanceContext instanceContext) {
          return new WaypointImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

  public WaypointImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }
}
