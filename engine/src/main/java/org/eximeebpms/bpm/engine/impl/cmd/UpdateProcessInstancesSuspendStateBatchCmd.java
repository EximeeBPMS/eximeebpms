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
package org.eximeebpms.bpm.engine.impl.cmd;

import org.eximeebpms.bpm.engine.BadUserRequestException;
import org.eximeebpms.bpm.engine.authorization.BatchPermissions;
import org.eximeebpms.bpm.engine.batch.Batch;
import org.eximeebpms.bpm.engine.impl.UpdateProcessInstancesSuspensionStateBuilderImpl;
import org.eximeebpms.bpm.engine.impl.batch.builder.BatchBuilder;
import org.eximeebpms.bpm.engine.impl.batch.BatchConfiguration;
import org.eximeebpms.bpm.engine.impl.batch.BatchElementConfiguration;
import org.eximeebpms.bpm.engine.impl.batch.update.UpdateProcessInstancesSuspendStateBatchConfiguration;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandExecutor;
import org.eximeebpms.bpm.engine.impl.util.EnsureUtil;

public class UpdateProcessInstancesSuspendStateBatchCmd extends AbstractUpdateProcessInstancesSuspendStateCmd<Batch> {

  public UpdateProcessInstancesSuspendStateBatchCmd(CommandExecutor commandExecutor,
                                                    UpdateProcessInstancesSuspensionStateBuilderImpl builder,
                                                    boolean suspending) {
    super(commandExecutor, builder, suspending);
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    BatchElementConfiguration elementConfiguration = collectProcessInstanceIds(commandContext);

    EnsureUtil.ensureNotEmpty(BadUserRequestException.class,
        "No process instance ids given", "process Instance Ids", elementConfiguration.getIds());
    EnsureUtil.ensureNotContainsNull(BadUserRequestException.class,
        "Cannot be null.", "Process Instance ids", elementConfiguration.getIds());

    return new BatchBuilder(commandContext)
        .type(Batch.TYPE_PROCESS_INSTANCE_UPDATE_SUSPENSION_STATE)
        .config(getConfiguration(elementConfiguration))
        .permission(BatchPermissions.CREATE_BATCH_UPDATE_PROCESS_INSTANCES_SUSPEND)
        .operationLogHandler(this::writeUserOperationLogAsync)
        .build();
  }

  public BatchConfiguration getConfiguration(BatchElementConfiguration elementConfiguration) {
    return new UpdateProcessInstancesSuspendStateBatchConfiguration(elementConfiguration.getIds(),
        elementConfiguration.getMappings(), suspending);
  }

}
