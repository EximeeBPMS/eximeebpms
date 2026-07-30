package org.eximeebpms.bpm.engine.impl.businessevent.process;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessProcessInstanceState {

  ACTIVE("ACTIVE"),
  SUSPENDED("SUSPENDED"),
  COMPLETED("COMPLETED"),
  INTERNALLY_TERMINATED("INTERNALLY_TERMINATED"),
  EXTERNALLY_TERMINATED("EXTERNALLY_TERMINATED");

  private final String value;
}
