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
package org.eximeebpms.spin.spi;

import org.eximeebpms.spin.DeserializationTypeValidator;

/**
 * Resolves the {@link DeserializationTypeValidator} to apply to Spin's
 * {@code mapTo(Class)}/{@code mapTo(String)} object mapping, at the moment a
 * mapping is performed.
 *
 * <p>Spin has no dependency on the process engine, and {@link org.eximeebpms.spin.DataFormats}
 * is a JVM-wide singleton that may be shared by several process engines. A validator
 * therefore cannot be stored on a data format at registration time — it must be resolved
 * per call, so that the currently executing engine's configuration is consulted. The
 * default provider returns {@code null}, leaving standalone Spin (and any engine that has
 * not opted in) unvalidated, exactly as before.
 */
public interface DeserializationTypeValidatorProvider {

  /**
   * @return the validator to apply to the current {@code mapTo} call, or {@code null}
   *         when no validation should be performed.
   */
  DeserializationTypeValidator getValidator();

}
