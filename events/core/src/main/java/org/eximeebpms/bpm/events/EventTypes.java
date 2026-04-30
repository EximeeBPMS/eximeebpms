package org.eximeebpms.bpm.events;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * EximeeBPMS event type names.
 *
 * <p>These values preserve the existing external event contract.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventTypes {

  public static final String PREFIX = "camunda7:";
  public static final String PROCESS_INSTANCE_STARTED = PREFIX + "process-instance:started";
}
