package com.groupon.jenkins.dynamic.build.api.metrics;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.extensions.DotCiExtension;
import com.mongodb.DB;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.Nullable;
import java.util.List;

@ExportedBean
public abstract class JobMetric implements ExtensionPoint {

    private DynamicProject project;
    private String branch;
    private int buildCount;

    @Exported(inline = true)
    public abstract Chart getChart();

    protected DB getDB() {
        return SetupConfig.get().getDynamicBuildRepository().getDatastore().getDB();
    }

    @Exported(inline = true)
    public abstract String getName();

    public abstract boolean isApplicable();

    public static List<JobMetric> getJobMetrics(DynamicProject project, String branch, int buildCount) {
        List<JobMetric> jobMetrics = Jenkins.getInstance().getExtensionList(JobMetric.class);
        for (JobMetric metric : jobMetrics) {
            metric.setProject(project);
            metric.setBranch(branch);
            metric.setBuildCount(buildCount);
        }
        return jobMetrics;
    }

    private void setProject(DynamicProject project) {
        this.project = project;
    }

    public DynamicProject getProject() {
        return project;
    }

    public static List<JobMetric> getApplicableJobMetrics(DynamicProject dynamicProject, String branch, int buildCount) {
        return Lists.newArrayList( Iterables.filter(getJobMetrics(dynamicProject,branch,buildCount), new Predicate<JobMetric>() {
            @Override
            public boolean apply(JobMetric metric) {
                return metric.isApplicable();
            }
        }));
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setBuildCount(int buildCount) {
        this.buildCount = buildCount;
    }
    public int getBuildCount() {
        return buildCount;
    }

    public String getBranch() {
        return branch;
    }
}
