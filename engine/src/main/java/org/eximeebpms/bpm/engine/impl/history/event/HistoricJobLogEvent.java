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
package org.eximeebpms.bpm.engine.impl.history.event;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.eximeebpms.bpm.engine.job.JobState;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.eximeebpms.bpm.engine.impl.util.ExceptionUtil;
import org.eximeebpms.bpm.engine.impl.util.StringUtil;

/**
 * @author Roman Smirnov
 *
 */
@Getter
public class HistoricJobLogEvent extends HistoryEvent {

  private static final long serialVersionUID = 1L;

  @Setter
  protected Date timestamp;

  @Setter
  protected String jobId;

  @Setter
  protected Date jobDueDate;

  @Setter
  protected int jobRetries;

  @Setter
  protected long jobPriority;

  protected String jobExceptionMessage;

  @Setter
  protected String exceptionByteArrayId;

  /**
   * Not persisted. Holds the stacktrace until the component that persists this
   * event creates the byte array for it (BPMS-662).
   */
  @Setter
  protected byte[] exceptionStacktraceBytes;

  @Setter
  protected String jobDefinitionId;

  @Setter
  protected String jobDefinitionType;

  @Setter
  protected String jobDefinitionConfiguration;

  @Setter
  protected String activityId;

  @Setter
  protected String failedActivityId;

  @Setter
  protected String deploymentId;

  @Setter
  protected int state;

  @Setter
  protected String tenantId;

  @Setter
  protected String hostname;

  @Setter
  protected String batchId;

  public void setJobExceptionMessage(String jobExceptionMessage) {
    // note: it is not a clean way to truncate where the history event is produced, since truncation is only
    //   relevant for relational history databases that follow our schema restrictions;
    //   a similar problem exists in JobEntity#setExceptionMessage where truncation may not be required for custom
    //   persistence implementations
    this.jobExceptionMessage = StringUtil.trimToMaximumLengthAllowed(jobExceptionMessage);
  }

  public String getExceptionStacktrace() {
    ByteArrayEntity byteArray = getExceptionByteArray();
    return ExceptionUtil.getExceptionStacktrace(byteArray);
  }

  protected ByteArrayEntity getExceptionByteArray() {
    if (exceptionByteArrayId != null) {
      return Context
        .getCommandContext()
        .getDbEntityManager()
        .selectById(ByteArrayEntity.class, exceptionByteArrayId);
    }

    return null;
  }
  public boolean isCreationLog() {
    return state == JobState.CREATED.getStateCode();
  }

  public boolean isFailureLog() {
    return state == JobState.FAILED.getStateCode();
  }

  public boolean isSuccessLog() {
    return state == JobState.SUCCESSFUL.getStateCode();
  }

  public boolean isDeletionLog() {
    return state == JobState.DELETED.getStateCode();
  }

}
