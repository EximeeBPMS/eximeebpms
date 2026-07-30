package org.eximeebpms.bpm.engine.impl.businessevent.variable;

import org.eximeebpms.bpm.engine.impl.persistence.entity.ExecutionEntity;

record VariableSourceContext(ExecutionEntity sourceExecution, String sourceActivityInstanceId) {

}
