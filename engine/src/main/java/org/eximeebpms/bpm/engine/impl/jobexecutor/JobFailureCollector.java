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
package org.eximeebpms.bpm.engine.impl.jobexecutor;

import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContextListener;
import org.eximeebpms.bpm.engine.impl.persistence.entity.JobEntity;

import java.util.Date;

public class JobFailureCollector implements CommandContextListener {

  protected Throwable failure;
  protected JobEntity job;
  protected String jobId;
  protected String failedActivityId;
  // Immutable snapshot of the job's lock expiration time, taken before job.execute(...) runs.
  // Job handlers may legitimately mutate the JobEntity's lockExpirationTime as a side effect
  // (e.g. rescheduling an ever-living job resets the lock), and that in-memory mutation survives
  // even if the surrounding transaction is rolled back afterwards. Comparing against this snapshot,
  // instead of the live (mutable) entity field, keeps the "was this job re-acquired by another
  // thread?" check in FailedJobListener#isJobReacquired accurate.
  protected Date lockExpirationTime;

  public JobFailureCollector(String jobId) {
    this.jobId = jobId;
  }

  public void setFailure(Throwable failure) {
    // log failure if not already present
    if (this.failure == null) {
      this.failure = failure;
    }
  }

  public Throwable getFailure() {
    return failure;
  }

  @Override
  public void onCommandFailed(CommandContext commandContext, Throwable t) {
    setFailure(t);
  }

  @Override
  public void onCommandContextClose(CommandContext commandContext) {
    // ignore
  }

  public void setJob(JobEntity job) {
    this.job = job;
    this.lockExpirationTime = job != null ? job.getLockExpirationTime() : null;
  }

  public JobEntity getJob() {
    return job;
  }

  public String getJobId() {
    return jobId;
  }

  public String getFailedActivityId() {
    return failedActivityId;
  }

  public void setFailedActivityId(String activityId) {
    this.failedActivityId = activityId;
  }

  /**
   * @return the lock expiration time of the job as it was right before {@code job.execute(...)} was
   * invoked, i.e. before any handler-triggered side effects (like rescheduling) could mutate it.
   */
  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }

}