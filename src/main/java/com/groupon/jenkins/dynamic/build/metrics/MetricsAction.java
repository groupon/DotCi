package com.groupon.jenkins.dynamic.build.metrics;

import hudson.model.InvisibleAction;

public class MetricsAction  extends InvisibleAction{
  private String name;
  private int value;

  public  MetricsAction(String name, int value){
    this.name = name;
    this.value = value;
  }
}
