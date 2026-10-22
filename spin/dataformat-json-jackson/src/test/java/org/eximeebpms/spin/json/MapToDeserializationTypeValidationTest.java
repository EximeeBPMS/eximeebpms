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
package org.eximeebpms.spin.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eximeebpms.spin.Spin.JSON;

import org.eximeebpms.spin.DataFormats;
import org.eximeebpms.spin.DeserializationTypeValidator;
import org.eximeebpms.spin.SpinRuntimeException;
import org.eximeebpms.spin.json.mapping.Order;
import org.eximeebpms.spin.json.mapping.ProbeState;
import org.junit.After;
import org.junit.Test;

/**
 * Verifies that Spin's {@code mapTo} consults the validator resolved through
 * {@link DataFormats#setDeserializationTypeValidatorProvider}, and — crucially — that a
 * rejected class is not initialized (its static initializer must not run) on the
 * {@code mapTo(String)} fully-qualified-class path.
 */
public class MapToDeserializationTypeValidationTest {

  protected static final String PROBE_CLASS = "org.eximeebpms.spin.json.mapping.StaticInitializerProbe";

  @After
  public void resetProvider() {
    DataFormats.setDeserializationTypeValidatorProvider(null);
  }

  @Test
  public void shouldMapWhenNoProviderRegistered() {
    // default: no provider -> no validation -> unchanged behaviour
    Order order = JSON(JsonTestConstants.EXAMPLE_JSON).mapTo(Order.class);
    assertThat(order).isNotNull();
  }

  @Test
  public void shouldMapWhenValidatorAllows() {
    DeserializationTypeValidator validator = type -> true;
    DataFormats.setDeserializationTypeValidatorProvider(() -> validator);

    Order order = JSON(JsonTestConstants.EXAMPLE_JSON).mapTo(Order.class);
    assertThat(order).isNotNull();
  }

  @Test
  public void shouldRejectWhenValidatorDenies() {
    DeserializationTypeValidator validator = type -> false;
    DataFormats.setDeserializationTypeValidatorProvider(() -> validator);
    SpinJsonNode node = JSON(JsonTestConstants.EXAMPLE_JSON);

    assertThatThrownBy(() -> node.mapTo(Order.class))
        .isInstanceOf(SpinRuntimeException.class);
  }

  @Test
  public void shouldNotInitializeRejectedClassOnStringPath() {
    // guards the ordering fix: Class.forName must load without initializing, so a class the
    // validator rejects never runs its static initializer.
    assertThat(ProbeState.probeInitialized)
        .as("probe must be pristine before the test")
        .isFalse();

    DeserializationTypeValidator validator = type -> false;
    DataFormats.setDeserializationTypeValidatorProvider(() -> validator);
    SpinJsonNode node = JSON("{\"name\":\"probe\"}");

    assertThatThrownBy(() -> node.mapTo(PROBE_CLASS))
        .isInstanceOf(SpinRuntimeException.class);

    assertThat(ProbeState.probeInitialized)
        .as("rejected class must not have been initialized")
        .isFalse();
  }

}
