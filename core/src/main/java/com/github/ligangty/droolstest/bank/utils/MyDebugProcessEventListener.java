package com.github.ligangty.droolstest.bank.utils;

import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;

public class MyDebugProcessEventListener extends
    DefaultProcessEventListener {

  @Override
  public void beforeNodeLeft(ProcessNodeLeftEvent event) {
    System.err.println(event + " " + event.getNodeInstance().getNodeName());
  }
  
  @Override
  public void beforeNodeTriggered(
      ProcessNodeTriggeredEvent event) {
    System.err.println(event + " " + event.getNodeInstance().getNodeName());
  }
  
  @Override
  public void beforeProcessCompleted(
      ProcessCompletedEvent event) {
    System.err.println(event + " " + event.getProcessInstance().getProcessId());
  }
  
  @Override
  public void beforeProcessStarted(ProcessStartedEvent event) {
    System.err.println(event + " " + event.getProcessInstance().getProcessId());
  }
  
  @Override
  public void afterNodeLeft(ProcessNodeLeftEvent event) {
    System.err.println(event + " " + event.getNodeInstance().getNodeName());
  }
  
  @Override
  public void afterNodeTriggered(
      ProcessNodeTriggeredEvent event) {
    System.err.println(event + " " + event.getNodeInstance().getNodeName());
  }
  
  @Override
  public void afterProcessCompleted(
      ProcessCompletedEvent event) {
    System.err.println(event + " " + event.getProcessInstance().getProcessId());
  }
  
  @Override
  public void afterProcessStarted(ProcessStartedEvent event) {
    System.err.println(event + " " + event.getProcessInstance().getProcessId());
  }
  
  
}
