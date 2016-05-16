/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.groupon.jenkins.dynamic.build;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.buildtype.InvalidBuildConfigurationException;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.dynamic.build.execution.BuildEnvironment;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.dynamic.buildtype.BuildType;
import com.groupon.jenkins.github.Payload;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import hudson.EnvVars;
import hudson.Functions;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Descriptor;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.tasks.BuildStep;
import hudson.util.HttpResponses;
import hudson.util.VersionNumber;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.kohsuke.github.GHRepository;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.mongodb.morphia.annotations.Property;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DynamicBuild extends DbBackedBuild<DynamicProject, DynamicBuild> {

    private transient DynamicBuildModel model;

    @Property(concreteClass = AxisList.class)
    private AxisList axisList;

    public DynamicBuild(DynamicProject project) throws IOException {
        super(project);
        this.model = new DynamicBuildModel(this);
    }

    public DynamicBuild(DynamicProject project, File buildDir) throws IOException {
        super(project, buildDir);
        this.model = new DynamicBuildModel(this);
    }

    public void postMorphiaLoad() {
        super.postMorphiaLoad();
        this.model = new DynamicBuildModel(this);
        this.description = getDescription();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            this.model.run();
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
        execute(new DynamicRunExecution());
    }

    public boolean isNewJenkins() {
        VersionNumber matrixBreakOutVersion = new VersionNumber("1.560");
        return Jenkins.getVersion().isNewerThan(matrixBreakOutVersion);
    }

    public DynamicBuildLayouter getLayouter() {

        return new DynamicBuildLayouter(axisList, this);
    }

    // This needs to be overriden here to override @RequirePOST annotation,
    // which seems like a bug in the version were are using.
    @Override
    public synchronized HttpResponse doStop() throws IOException, ServletException {
        return super.doStop();
    }

    @Override
    public void restoreFromDb(AbstractProject project, Map<String, Object> input) {
        super.restoreFromDb(project, input);
        this.model = new DynamicBuildModel(this);
    }

    @Override
    @RequirePOST
    public void doDoDelete(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        checkPermission(DELETE);
        model.deleteBuild();
        rsp.sendRedirect2(req.getContextPath() + '/' + getParent().getUrl());
    }

    @Override
    public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
        try {
            Build item = getRun(Combination.fromString(token));
            if (item != null) {
                if (item.getNumber() == this.getNumber()) {
                    return item;
                } else {
                    // redirect the user to the correct URL
                    String url = Functions.joinPath(item.getUrl(), req.getRestOfPath());
                    String qs = req.getQueryString();
                    if (qs != null) {
                        url += '?' + qs;
                    }
                    throw HttpResponses.redirectViaContextPath(url);
                }
            }
        } catch (IllegalArgumentException e) {
            // failed to parse the token as Combination. Must be something else
        }
        return super.getDynamic(token, req, rsp);
    }

    @Override
    public Map<String, String> getDotCiEnvVars(EnvVars jenkinsEnvVars) {
        Map<String, String> vars = super.getDotCiEnvVars(jenkinsEnvVars);
        Map<String, String> dotCiEnvVars = model.getDotCiEnvVars();
        vars.putAll(dotCiEnvVars);
        return vars;
    }

    public Iterable<DynamicSubProject> getAllSubProjects() {
        return getConductor().getItems();
    }

    public DynamicSubProject getSubProject(Combination subBuildCombination) {
        return Iterables.getOnlyElement(getSubProjects(Arrays.asList(subBuildCombination)));
    }



    public void setAxisList(AxisList axisList) {
        this.axisList = axisList;
        try {
            save();
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }


    public Map<String,Object> getEnvironmentWithChangeSet(TaskListener listener) throws IOException, InterruptedException {
        return model.getEnvironmentWithChangeSet(listener);
    }


    public boolean isPullRequest() {
        return StringUtils.isNotEmpty( getCause().getPullRequestNumber());
    }

    protected class DynamicRunExecution extends Build.BuildExecution implements BuildExecutionContext {
        @Override
        public boolean performStep(BuildStep execution, BuildListener listener) throws IOException, InterruptedException {
            return perform(execution, listener);
        }

        @Override
        public void setResult(Result r) {
            DynamicBuild.this.setResult(r);
        }

        @Override
        public Map<String, Object> getBuildEnvironmentVariables() {
            try {
                return DynamicBuild.this.getEnvironmentWithChangeSet(getListener());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected Result doRun(BuildListener listener) throws Exception{

            BuildEnvironment buildEnvironment = new BuildEnvironment(DynamicBuild.this, launcher, listener);
            try {
                if (!buildEnvironment.initialize()) {
                    return Result.FAILURE;
                }
                exportDeployKeysIfPrivateRepo(listener, launcher);
                BuildType buildType = BuildType.newBuildType(getParent());
                Result buildRunResult =   buildType.runBuild(DynamicBuild.this, this, launcher, listener);
                setResult(buildRunResult);
                return buildRunResult;
            } catch (InvalidBuildConfigurationException invalidBuildConfigurationException) {
                for (String error : invalidBuildConfigurationException.getValidationErrors()) {
                    listener.error(error);
                }
                return Result.FAILURE;
            }catch (InterruptedException e) {
                Executor x = Executor.currentExecutor();
                x.recordCauseOfInterruption(DynamicBuild.this, listener);
                return x.abortResult();
            }catch (Exception e) {
                PrintStream logger = listener.getLogger();
                logger.println(e.getMessage());
                logger.println(ExceptionUtils.getStackTrace(e));
                Executor x = Executor.currentExecutor();
                x.recordCauseOfInterruption(DynamicBuild.this, listener);
                x.doStop();
                return Result.FAILURE;
            } finally {
                if (buildEnvironment.tearDownBuildEnvironments(listener)) {
                    return Result.FAILURE;
                }
                deleteDeployKeys(listener, launcher);
            }

        }
    }


    @Override
    @Exported
    public Executor getExecutor() {
        final Executor executor = super.getExecutor();
        return executor == null ? getOneOffExecutor() : executor;
    }

    private DynamicProject getConductor() {
        return this.getParent();
    }

    public Iterable<DynamicSubProject> getSubProjects(Iterable<Combination> mainRunCombinations) {
        return getConductor().getSubProjects(mainRunCombinations);
    }

    public Build getRun(Combination combination) {
        for (DynamicSubProject subProject : getAllSubProjects()) {
            if (subProject.getCombination().equals(combination)) {
                return getRunForConfiguration(subProject);
            }

        }
        return null;
    }

    @Override
    public DynamicProject getParent() {
        return super.getParent();
    }

    private DynamicSubBuild getRunForConfiguration(DynamicSubProject c) {
        DynamicSubBuild r = c.getBuildByNumber(getNumber());
        return r != null ? r : null;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DynamicBuild) {
            DbBackedBuild<DynamicProject, DynamicBuild> otherBuild = (DbBackedBuild<DynamicProject, DynamicBuild>) other;
            if (otherBuild.getName().equals(this.getName()) && otherBuild.getNumber() == this.getNumber()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), getNumber());
    }

    @Override
    public String getSha() {
        return this.getCause() == null ? "" : getCause().getSha();
    }

    @Override
    public BuildCause getCause() {
        return model.getBuildCause();
    }

    public String getGithubRepoUrl() {
        return getProject().getGithubRepoUrl();
    }

    public void addCause(Cause manualCause) {
        List<Cause> exisitingCauses = this.getAction(CauseAction.class).getCauses();
        ArrayList<Cause> causes = new ArrayList<Cause>();
        causes.add(manualCause);
        causes.addAll(exisitingCauses);
        this.replaceAction(new CauseAction(causes));
    }

    /*
     * Jenkins method is final cannot be mocked. Work around to make this
     * mockable without powermock
     */

    public String getFullUrl() {
        return this.getAbsoluteUrl();
    }

    public Map<String, String> getDotCiEnvVars() {
        return model.getDotCiEnvVars();
    }


    public void skip() {
        addAction(new SkippedBuildAction());
    }

    @Override
    public String getDescription() {
        String description = super.getDescription();
        return description == null? getCurrentBranch().toString() : description;
    }

    @Override
    public DynamicBuild getPreviousBuild() {
        String parentSha = getCause().getParentSha();
        DynamicBuildRepository buildRepository = SetupConfig.get().getDynamicBuildRepository();
        return StringUtils.isEmpty(parentSha) ?  null: (DynamicBuild) buildRepository.getBuildBySha(this.getProject(), parentSha, Result.SUCCESS);
    }
}
