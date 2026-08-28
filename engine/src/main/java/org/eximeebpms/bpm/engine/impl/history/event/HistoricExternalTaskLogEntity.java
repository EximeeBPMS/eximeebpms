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

import lombok.Getter;
import lombok.Setter;
import org.eximeebpms.bpm.engine.history.ExternalTaskState;
import org.eximeebpms.bpm.engine.history.HistoricExternalTaskLog;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.eximeebpms.bpm.engine.impl.util.EnsureUtil;
import org.eximeebpms.bpm.engine.impl.util.ExceptionUtil;

import java.io.Serial;
import java.util.Date;

import static org.eximeebpms.bpm.engine.impl.util.StringUtil.toByteArray;

@Getter
public class HistoricExternalTaskLogEntity extends HistoryEvent implements HistoricExternalTaskLog {

  @Serial
  private static final long serialVersionUID = 1L;
  /**
   * Name carried by every external-task error-details byte array. Public because
   * DbHistoryEventHandler creates the row and operational queries identify it by
   * this name, so it lives in one place.
   */
  public static final String EXCEPTION_NAME = "historicExternalTaskLog.exceptionByteArray";

  @Setter
  protected Date timestamp;

  @Setter
  protected String externalTaskId;

  @Setter
  protected String topicName;
  @Setter
  protected String workerId;
  @Setter
  protected long priority;
  @Setter
  protected Integer retries;

  protected String errorMessage;

  @Setter
  protected String errorDetailsByteArrayId;

  /**
   * Not persisted. Holds the error details until the component that persists this
   * event creates the byte array for it (BPMS-662).
   */
  @Setter
  protected byte[] errorDetailsBytes;
  @Setter
  protected String activityId;

  @Setter
  protected String activityInstanceId;
  @Setter
  protected String tenantId;

  @Setter
  protected int state;

    public void setErrorMessage(String errorMessage) {
    // note: it is not a clean way to truncate where the history event is produced, since truncation is only
    //   relevant for relational history databases that follow our schema restrictions;
    //   a similar problem exists in ExternalTaskEntity#setErrorMessage where truncation may not be required for custom
    //   persistence implementations
    if(errorMessage != null && errorMessage.length() > ExternalTaskEntity.MAX_EXCEPTION_MESSAGE_LENGTH) {
      this.errorMessage = errorMessage.substring(0, ExternalTaskEntity.MAX_EXCEPTION_MESSAGE_LENGTH);
    } else {
      this.errorMessage = errorMessage;
    }
  }

    public String getErrorDetails() {
    ByteArrayEntity byteArray = getErrorByteArray();
    return ExceptionUtil.getExceptionStacktrace(byteArray);
  }

  public void setErrorDetails(String exception) {
    EnsureUtil.ensureNotNull("exception", exception);

    // carried on the event; the byte array is created by whichever component persists it
    // (DbHistoryEventHandler). Inserting here would leave an unreferenced row behind
    // whenever a HistoryEventHandler declines to persist the event (BPMS-662).
    errorDetailsBytes = toByteArray(exception);
  }

  protected ByteArrayEntity getErrorByteArray() {
    if (errorDetailsByteArrayId != null) {
      return Context
          .getCommandContext()
          .getDbEntityManager()
          .selectById(ByteArrayEntity.class, errorDetailsByteArrayId);
    }
    return null;
  }

  @Override
  public boolean isCreationLog() {
    return state == ExternalTaskState.CREATED.getStateCode();
  }

  @Override
  public boolean isFailureLog() {
    return state == ExternalTaskState.FAILED.getStateCode();
  }

  @Override
  public boolean isSuccessLog() {
    return state == ExternalTaskState.SUCCESSFUL.getStateCode();
  }

  @Override
  public boolean isDeletionLog() {
    return state == ExternalTaskState.DELETED.getStateCode();
  }

  @Override
  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

}
