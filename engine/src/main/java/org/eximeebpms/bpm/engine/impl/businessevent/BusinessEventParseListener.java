package org.eximeebpms.bpm.engine.impl.businessevent;

import java.util.List;
import org.eximeebpms.bpm.engine.delegate.TaskListener;
import org.eximeebpms.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.eximeebpms.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.eximeebpms.bpm.engine.impl.businessevent.process.BusinessEventProcessInstanceExecutionListener;
import org.eximeebpms.bpm.engine.impl.businessevent.task.BusinessEventTaskInstanceTaskListener;
import org.eximeebpms.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.eximeebpms.bpm.engine.impl.pvm.PvmEvent;
import org.eximeebpms.bpm.engine.impl.pvm.process.ActivityImpl;
import org.eximeebpms.bpm.engine.impl.pvm.process.ScopeImpl;
import org.eximeebpms.bpm.engine.impl.task.TaskDefinition;
import org.eximeebpms.bpm.engine.impl.util.xml.Element;

public class BusinessEventParseListener extends AbstractBpmnParseListener {

  protected final BusinessEventProcessInstanceExecutionListener processInstanceListener = new BusinessEventProcessInstanceExecutionListener();
  protected final BusinessEventTaskInstanceTaskListener taskInstanceListener = new BusinessEventTaskInstanceTaskListener();

  @Override
  public void parseUserTask(final Element userTaskElement, final ScopeImpl scope, final ActivityImpl activity) {
    addTaskInstanceListeners(getTaskDefinition(activity));
  }

  @Override
  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      addProcessInstanceListeners(processDefinition);
    }
  }

  protected TaskDefinition getTaskDefinition(final ActivityImpl activity) {
    return ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition();
  }

  protected void addProcessInstanceListeners(ProcessDefinitionEntity processDefinition) {
    processDefinition.addBuiltInListener(PvmEvent.EVENTNAME_START, processInstanceListener);
    processDefinition.addBuiltInListener(PvmEvent.EVENTNAME_END, processInstanceListener);
    processDefinition.addBuiltInListener("update", processInstanceListener);
  }

  private void addTaskInstanceListeners(final TaskDefinition taskDefinition) {
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_CREATE, taskInstanceListener);
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, taskInstanceListener);
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_UPDATE, taskInstanceListener);
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_COMPLETE, taskInstanceListener);
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_DELETE, taskInstanceListener);
  }
}
