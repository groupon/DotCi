package com.groupon.jenkins.dynamic.build.api.metrics;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class JobMetric {
 @Exported(inline =true)
 public abstract Chart getChart();

 @Exported(inline =true)
 public abstract String getName();
}
