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
package org.eximeebpms.bpm.engine.impl.calendar;

import java.util.Date;

import org.eximeebpms.bpm.engine.impl.ProcessEngineLogger;
import org.eximeebpms.bpm.engine.impl.util.EngineUtilLogger;
import org.eximeebpms.bpm.engine.task.Task;
import org.joda.time.DateTime;
import org.joda.time.format.ISOPeriodFormat;


public class DueDateBusinessCalendar implements BusinessCalendar {

  private final static EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;

  public static final String NAME = "dueDate";

  public Date resolveDuedate(String duedate, Task task) {
    return resolveDuedate(duedate);
  }

  public Date resolveDuedate(String duedate) {
    return resolveDuedate(duedate, (Date)null);
  }

  public Date resolveDuedate(String duedate, Date startDate) {
    try {
      if (duedate.startsWith("P")){
        DateTime start = null;
        if (startDate == null) {
          start = DateTimeUtil.now();
        } else {
          start = new DateTime(startDate);
        }
        return start.plus(ISOPeriodFormat.standard().parsePeriod(duedate)).toDate();
      }

      return DateTimeUtil.parseDateTime(duedate).toDate();

    }
    catch (Exception e) {
      throw LOG.exceptionWhileResolvingDuedate(duedate, e);
    }
  }
}
