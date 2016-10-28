package com.groupon.jenkins.dynamic.build.api.metrics;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.api.metrics.charts.Chart;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.util.GReflectionUtils;
import com.mongodb.DB;
import hudson.ExtensionPoint;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.mongodb.morphia.query.Query;

import java.util.List;

@ExportedBean
public abstract class JobMetric implements ExtensionPoint {

    private DynamicProject project;
    private String branch;
    private int buildCount;

    public static List<JobMetric> getJobMetrics(final DynamicProject project, final String branch, final int buildCount) {
        final List<JobMetric> jobMetrics = Jenkins.getInstance().getExtensionList(JobMetric.class);
        for (final JobMetric metric : jobMetrics) {
            metric.setProject(project);
            metric.setBranch(branch);
            metric.setBuildCount(buildCount);
        }

        return jobMetrics;
    }

    public static List<JobMetric> getApplicableJobMetrics(final DynamicProject dynamicProject, final String branch, final int buildCount) {
        return Lists.newArrayList(Iterables.filter(getJobMetrics(dynamicProject, branch, buildCount), new Predicate<JobMetric>() {
            @Override
            public boolean apply(final JobMetric metric) {
                return metric.isApplicable();
            }
        }));
    }

    @Exported(inline = true)
    public abstract Chart getChart();

    protected DB getDB() {
        return getDynamicBuildRepository().getDatastore().getDB();
    }

    @Exported(inline = true)
    public abstract String getTitle();

    public abstract boolean isApplicable();

    public DynamicProject getProject() {
        return this.project;
    }

    private void setProject(final DynamicProject project) {
        this.project = project;
    }

    public int getBuildCount() {
        return this.buildCount;
    }

    public void setBuildCount(final int buildCount) {
        this.buildCount = buildCount;
    }

    public String getBranch() {
        return this.branch;
    }

    public void setBranch(final String branch) {
        this.branch = branch;
    }

    protected List<DynamicBuild> getBuilds(Query<DynamicBuild> query) {
        query = applyQueryFilters(query);

        final List<DynamicBuild> builds = query.asList();
        for (final DynamicBuild build : builds) {
            associateProject(getProject(), build);
        }
        return builds;
    }

    protected Query<DynamicBuild> getQuery() {
        return getDynamicBuildRepository().getDatastore().createQuery(DynamicBuild.class).disableValidation().field("projectId").equal(this.project.getId());
    }

    private DynamicBuildRepository getDynamicBuildRepository() {
        return SetupConfig.get().getDynamicBuildRepository();
    }

    private Query<DynamicBuild> applyQueryFilters(Query<DynamicBuild> query) {
        query = filterBranch(query);
        query = query.limit(getBuildCount());
        return query.order("number");
    }

    private Query<DynamicBuild> filterBranch(final Query<DynamicBuild> query) {
        final String branch = getBranch();
        if ("All".equals(branch)) {
            return query;
        }
        if ("Mine".equals(branch)) {
            query.or(
                query.criteria("actions.causes.user").equal(Jenkins.getAuthentication().getName()),
                query.criteria("actions.causes.pusher").equal(Jenkins.getAuthentication().getName())
            );
            return query;
        }

        return getDynamicBuildRepository().filterExpression(branch, query);
    }

    private void associateProject(final DbBackedProject project, final DbBackedBuild build) {
        if (build != null) {
            GReflectionUtils.setField(Run.class, "project", build, project);
            build.postMorphiaLoad();
        }
    }

}
