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
package org.eximeebpms.bpm.model.bpmn.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eximeebpms.bpm.model.bpmn.Bpmn;
import org.eximeebpms.bpm.model.bpmn.BpmnModelInstance;
import org.eximeebpms.bpm.model.xml.instance.ModelElementInstance;
import org.eximeebpms.bpm.model.xml.validation.ModelElementValidator;
import org.eximeebpms.bpm.model.xml.validation.ValidationResult;
import org.eximeebpms.bpm.model.xml.validation.ValidationResultType;
import org.eximeebpms.bpm.model.xml.validation.ValidationResults;
import org.junit.Test;
import org.eximeebpms.bpm.model.bpmn.instance.Process;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Daniel Meyer
 *
 */
public class ValidateProcessTest {

  @Test
  public void validationFailsIfNoStartEventFound() {

    List<ModelElementValidator<?>> validators = new ArrayList<ModelElementValidator<?>>();
    validators.add(new ProcessStartEventValidator());

    BpmnModelInstance bpmnModelInstance = Bpmn.createProcess().done();

    ValidationResults validationResults = bpmnModelInstance.validate(validators);

    assertThat(validationResults.hasErrors()).isTrue();

    Map<ModelElementInstance, List<ValidationResult>> results = validationResults.getResults();
    assertThat(results.size()).isEqualTo(1);

    Process process = bpmnModelInstance.getDefinitions().getChildElementsByType(Process.class).iterator().next();
    assertThat(results.containsKey(process)).isTrue();

    List<ValidationResult> resultsForProcess = results.get(process);
    assertThat(resultsForProcess.size()).isEqualTo(1);

    ValidationResult validationResult = resultsForProcess.get(0);
    assertThat(validationResult.getElement()).isEqualTo(process);
    assertThat(validationResult.getCode()).isEqualTo(10);
    assertThat(validationResult.getMessage()).isEqualTo("Process does not have exactly one start event. Got 0.");
    assertThat(validationResult.getType()).isEqualTo(ValidationResultType.ERROR);

  }

}
