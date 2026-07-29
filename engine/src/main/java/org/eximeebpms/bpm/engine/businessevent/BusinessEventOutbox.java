package org.eximeebpms.bpm.engine.businessevent;

import java.util.Date;

public interface BusinessEventOutbox {
    String getId();
    Long getIdAsLong();
    boolean isProcessed();
    Date getProcessedDate();
    String getProcessInstanceId();
}
