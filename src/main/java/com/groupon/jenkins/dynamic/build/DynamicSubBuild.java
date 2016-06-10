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
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import com.groupon.jenkins.dynamic.build.execution.SubBuildExecutionAction;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.matrix.Combination;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.Node;
import hudson.model.Result;
import hudson.slaves.WorkspaceList;
import hudson.slaves.WorkspaceList.Lease;
import hudson.tasks.BuildStep;
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

public class DynamicSubBuild extends DbBackedBuild<DynamicSubProject, DynamicSubBuild> {

    private final BuildCause cause;

    public DynamicSubBuild(DynamicSubProject project, Cause cause, int number) throws IOException {
        super(project);
        this.cause = (BuildCause) cause;
        this.number = number;
        getRootDir().mkdirs();
    }


    @Override
    public DynamicSubProject getParent() {
        return super.getParent();
    }

    @Override
    protected void onStartBuilding() {
        super.onStartBuilding();
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public String getUpUrl() {
        StaplerRequest req = Stapler.getCurrentRequest();
        if (req != null) {
            List<Ancestor> ancs = req.getAncestors();
            for (int i = 1; i < ancs.size(); i++) {
                if (ancs.get(i).getObject() == this) {
                    Object parentObj = ancs.get(i - 1).getObject();
                    if (parentObj instanceof DynamicBuild || parentObj instanceof DynamicSubProject) {
                        return ancs.get(i - 1).getUrl() + '/';
                    }
                }
            }
        }
        return super.getDisplayName();
    }

    public DynamicBuild getParentBuild() {
        return getParent().getParent().getBuildByNumber(getNumber());
    }

    @Override
    public AbstractBuild<?, ?> getRootBuild() {
        return getParentBuild();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        execute(new DynamicSubBuildExecution());
    }

    protected class DynamicSubBuildExecution extends BuildExecution implements BuildExecutionContext {
        protected Lease getParentWorkspaceLease(Node n, WorkspaceList wsl) throws InterruptedException, IOException {
            DynamicProject mp = getParent().getParent();

            String customWorkspace = mp.getCustomWorkspace();
            if (customWorkspace != null) {
                // we allow custom workspaces to be concurrently used between
                // jobs.
                return Lease.createDummyLease(n.getRootPath().child(getEnvironment(listener).expand(customWorkspace)));
            }
            return wsl.allocate(n.getWorkspaceFor(mp), getParentBuild());
        }

        @Override
        protected Lease decideWorkspace(Node n, WorkspaceList wsl) throws InterruptedException, IOException {
            Lease baseLease = getParentWorkspaceLease(n, wsl);
            FilePath baseDir = baseLease.path;
            EnvVars env = getEnvironment(listener);
            env.putAll(getBuildVariables());
            String childWs = getParent().getName();
            return Lease.createLinkedDummyLease(baseDir.child(env.expand(childWs)), baseLease);
        }

        @Override
        protected Result doRun(BuildListener listener) throws Exception {
            try{
                exportDeployKeysIfPrivateRepo(listener, launcher);
                SubBuildExecutionAction subBuildExecutionAction = getAction(SubBuildExecutionAction.class);
                Result runResult = subBuildExecutionAction.run(DynamicSubBuild.this.getCombination(), this, listener);;
                subBuildExecutionAction.getSubBuildFinishListener().runFinished(DynamicSubBuild.this);
                return  runResult;
            }finally {
                deleteDeployKeys(listener, launcher);
            }

        }


        @Override
        public boolean performStep(BuildStep buildStep, BuildListener listener) throws InterruptedException, IOException {
            return perform(buildStep, listener);
        }

        @Override
        public void setResult(Result r) {
            DynamicSubBuild.this.setResult(r);
        }

        @Override
        public Map<String, Object> getBuildEnvironmentVariables() {
            try{
                EnvVars envVars = DynamicSubBuild.this.getJenkinsEnvVariables(getListener());
                Map<String, Object> buildVariables = DynamicSubBuild.this.getParentBuild().getEnvironmentWithChangeSet(getListener());
                buildVariables.putAll(envVars);
                return buildVariables;
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DynamicSubBuild) {
            return Objects.equal(getBuildId(), ((DynamicSubBuild) other).getBuildId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getBuildId());
    }

    public Combination getCombination() {
        return getParent().getCombination();
    }

    @Override
    public BuildCause getCause() {
        return cause;
    }

    @Override
    public String getGithubRepoUrl() {
        return getParentBuild().getParent().getGithubRepoUrl();
    }

    @Override
    public String getSha() {
        return getParentBuild().getSha();
    }

}
