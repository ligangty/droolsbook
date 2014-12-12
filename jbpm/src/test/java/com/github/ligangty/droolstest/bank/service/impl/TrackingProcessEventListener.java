package com.github.ligangty.droolstest.bank.service.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.runtime.process.NodeInstance;

public class TrackingProcessEventListener extends
    DefaultProcessEventListener {

  Collection<NodeInstance> nodesTriggeredCollection = new ArrayList<NodeInstance>();

  @Override
  public void beforeNodeTriggered(
      ProcessNodeTriggeredEvent event) {
    System.out.println(event.getNodeInstance().getNodeName() + " id=" + event.getNodeInstance().getNodeId());
    nodesTriggeredCollection.add(event.getNodeInstance());
  }

  
  public boolean isNodeTriggered(String processId, long nodeId) {
    for (NodeInstance triggeredNodeInstance : nodesTriggeredCollection) {
      if (triggeredNodeInstance.getProcessInstance()
          .getProcessId().equals(processId)
          && triggeredNodeInstance.getNodeId() == nodeId) {
        return true;
      }
    }
    return false;
  }
  
  public void reset() {
    nodesTriggeredCollection.clear();
  }

}
