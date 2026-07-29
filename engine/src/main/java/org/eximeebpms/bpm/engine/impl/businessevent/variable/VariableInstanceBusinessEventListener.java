package org.eximeebpms.bpm.engine.impl.businessevent.variable;

import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEvent;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProcessor;
import org.eximeebpms.bpm.engine.impl.businessevent.BusinessEventProducer;
import org.eximeebpms.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.eximeebpms.bpm.engine.impl.core.variable.scope.VariableInstanceLifecycleListener;
import org.eximeebpms.bpm.engine.impl.persistence.entity.VariableInstanceEntity;

public class VariableInstanceBusinessEventListener implements VariableInstanceLifecycleListener<VariableInstanceEntity> {

    public static final VariableInstanceBusinessEventListener INSTANCE = new VariableInstanceBusinessEventListener();

    @Override
    public void onCreate(final VariableInstanceEntity variableInstance, final AbstractVariableScope sourceScope) {
        if (!variableInstance.isTransient()) {
            BusinessEventProcessor.processBusinessEvents(new BusinessEventProcessor.BusinessEventCreator() {
                @Override
                public BusinessEvent createBusinessEvent(BusinessEventProducer producer) {
                    return producer.createVariableCreateEvt(variableInstance, sourceScope);
                }
            });
        }
    }

    @Override
    public void onDelete(final VariableInstanceEntity variableInstance, final AbstractVariableScope sourceScope) {
        if (!variableInstance.isTransient()) {
            BusinessEventProcessor.processBusinessEvents(new BusinessEventProcessor.BusinessEventCreator() {
                @Override
                public BusinessEvent createBusinessEvent(BusinessEventProducer producer) {
                    return producer.createVariableDeleteEvt(variableInstance, sourceScope);
                }
            });
        }
    }

    @Override
    public void onUpdate(final VariableInstanceEntity variableInstance, final AbstractVariableScope sourceScope) {
        if (!variableInstance.isTransient()) {
            BusinessEventProcessor.processBusinessEvents(new BusinessEventProcessor.BusinessEventCreator() {
                @Override
                public BusinessEvent createBusinessEvent(BusinessEventProducer producer) {
                    return producer.createVariableUpdateEvt(variableInstance, sourceScope);
                }
            });
        }
    }
}
