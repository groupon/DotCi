package com.groupon.jenkins.dynamic.build.api.metrics;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.extensions.DotCiExtension;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

@ExportedBean
public abstract class JobMetric implements ExtensionPoint{

 private DynamicProject project;

 @Exported(inline =true)
 public abstract Chart getChart();

 @Exported(inline =true)
 public abstract String getName();

 public static List<JobMetric> getJobMetrics(DynamicProject project) {
  List<JobMetric> jobMetrics = Jenkins.getInstance().getExtensionList(JobMetric.class);
  for(JobMetric metric: jobMetrics){
   metric.setProject(project);
  }
  return jobMetrics;
 }

 private void setProject(DynamicProject project) {
  this.project = project;
 }
 public DynamicProject getProject() {
  return project;
 }
}
