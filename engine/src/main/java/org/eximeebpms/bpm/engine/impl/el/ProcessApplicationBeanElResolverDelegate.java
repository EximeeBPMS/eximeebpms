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
package org.eximeebpms.bpm.engine.impl.el;

import org.eximeebpms.bpm.application.ProcessApplicationInterface;
import org.eximeebpms.bpm.application.ProcessApplicationReference;
import org.eximeebpms.bpm.application.ProcessApplicationUnavailableException;
import org.eximeebpms.bpm.engine.ProcessEngineException;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.impl.juel.jakarta.el.BeanELResolver;
import org.eximeebpms.bpm.impl.juel.jakarta.el.ELResolver;

/**
 * <p>Resolves a {@link BeanELResolver} from the current process application.
 * This allows to cache resolvers on the process application level. Such a resolver
 * cannot be cached globally as {@link BeanELResolver} keeps a cache of classes
 * involved in expressions.</p>
 *
 * <p>If resolution is attempted outside the context of a process application,
 * then always a new resolver instance is returned (i.e. no caching in these cases).</p>
 *
 * @author Thorben Lindhauer
 */
public class ProcessApplicationBeanElResolverDelegate extends AbstractElResolverDelegate {

  protected ELResolver getElResolverDelegate() {

    ProcessApplicationReference processApplicationReference = Context.getCurrentProcessApplication();
    if(processApplicationReference != null) {

      try {
        ProcessApplicationInterface processApplication = processApplicationReference.getProcessApplication();
        return processApplication.getBeanElResolver();

      } catch (ProcessApplicationUnavailableException e) {
        throw new ProcessEngineException("Cannot access process application '"+processApplicationReference.getName()+"'", e);
      }

    } else {
      return new BeanELResolver();
    }

  }
}
