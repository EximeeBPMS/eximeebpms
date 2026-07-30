package org.eximeebpms.bpm.engine.impl.cmd;

import org.eximeebpms.bpm.engine.businessevent.BusinessEventOutbox;
import org.eximeebpms.bpm.engine.impl.interceptor.Command;
import org.eximeebpms.bpm.engine.impl.interceptor.CommandContext;

import java.io.Serializable;

public class DeleteBusinessEventCmd implements Command<Object>, Serializable {

    private final BusinessEventOutbox businessEventOutbox;

    public DeleteBusinessEventCmd(BusinessEventOutbox businessEventOutbox) {
        this.businessEventOutbox = businessEventOutbox;
    }

    public Object execute(CommandContext commandContext) {
        commandContext
                .getBusinessEventManager()
                .delete(businessEventOutbox);
        return null;
    }

}
