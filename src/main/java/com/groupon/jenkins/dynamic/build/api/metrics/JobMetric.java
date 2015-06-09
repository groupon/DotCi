package com.groupon.jenkins.dynamic.build.api.metrics;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.util.GReflectionUtils;
import com.mongodb.DB;
import hudson.ExtensionPoint;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.mongodb.morphia.query.Query;

import java.util.List;
import java.util.regex.Pattern;

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

        return  jobMetrics;
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
    protected List<DynamicBuild> getBuilds(Query<DynamicBuild> query) {
        query = applyQueryValues(query);
        query = applyQueryValues(query);

        List<DynamicBuild> builds = query.asList();
        for(DynamicBuild build: builds){
            associateProject(getProject(),build);
        }
        return builds;
    }
    protected Query<DynamicBuild> getQuery(DbBackedProject project) {
        return SetupConfig.get().getDynamicBuildRepository().getDatastore().createQuery(DynamicBuild.class).disableValidation().field("projectId").equal(project.getId());
    }
    private Query<DynamicBuild> applyQueryValues(Query<DynamicBuild> query) {
        query = filterBranch(query);
        query = query.limit(getBuildCount());
        return query.order("-number");
    }
    private Query<DynamicBuild> filterBranch(Query<DynamicBuild> query) {
        String branch = getBranch();
        if("All".equals(branch)){
            return query;
        }
        if("Mine".equals(branch)){
            query.or(
                    query.criteria("actions.causes.user").equal(Jenkins.getAuthentication().getName()),
                    query.criteria("actions.causes.pusher").equal(Jenkins.getAuthentication().getName())
            );
            return query;
        }


        Pattern branchRegex = Pattern.compile(branch);
        query = query.filter("actions.causes.branch.branch", branchRegex);
        return query;
    }
    private void associateProject(DbBackedProject project, DbBackedBuild build) {
        if(build != null) {
            GReflectionUtils.setField(Run.class, "project", build, project);
            build.postMorphiaLoad();
        }
    }

}
