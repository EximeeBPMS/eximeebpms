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
package org.eximeebpms.bpm.engine;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import org.eximeebpms.bpm.engine.impl.errorcode.BuiltinExceptionCode;

/**
 * Runtime exception that is the superclass of all exceptions in the process engine.
 *
 * @author Tom Baeyens
 */
@Setter
@Getter
public class ProcessEngineException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * -- GETTER --
   *  <p>Accessor of the exception error code.
   *  <p>If not changed via
   * , default code is
   *  which is always overridden by a custom or built-in error code provider.
   *  <p>You can implement a custom
   *  and register it in the
   *  via the
   *  property.
   * -- SETTER --
   *  <p>The exception code can be set via delegation code.
   *  <p>Setting an error code on the exception in delegation code always overrides
   *  the exception code from a custom
   * .
   *  <p>Your business logic can react to the exception code exposed
   *  via
   *  when calling Camunda Java API and is
   *  even exposed to the REST API when an error occurs.

   */
  protected int code = BuiltinExceptionCode.FALLBACK.getCode();

  public ProcessEngineException() {
    super();
  }

  public ProcessEngineException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProcessEngineException(String message) {
    super(message);
  }

  public ProcessEngineException(String message, int code) {
    super(message);
    this.code = code;
  }

  public ProcessEngineException(Throwable cause) {
    super(cause);
  }

}
