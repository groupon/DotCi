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
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.dynamic.build.execution.BuildEnvironment;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import com.groupon.jenkins.dynamic.build.execution.BuildType;
import com.groupon.jenkins.dynamic.build.execution.DotCiPluginRunner;
import com.groupon.jenkins.dynamic.build.execution.DynamicBuildExection;
import com.groupon.jenkins.dynamic.buildconfiguration.BuildConfiguration;
import com.groupon.jenkins.dynamic.buildconfiguration.EffectiveBuildConfigurationCalculator;
import com.groupon.jenkins.dynamic.buildconfiguration.InvalidDotCiYmlException;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import hudson.EnvVars;
import hudson.Functions;
import hudson.matrix.Combination;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.tasks.BuildStep;
import hudson.util.HttpResponses;
import hudson.util.VersionNumber;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;

public class DynamicBuild extends DbBackedBuild<DynamicProject, DynamicBuild> {

	private transient BuildConfiguration buildConfiguration;
	private transient DynamicBuildModel model;

	public DynamicBuild(DynamicProject project) throws IOException {
		super(project);
		this.model = new DynamicBuildModel(this);
	}

	public DynamicBuild(DynamicProject project, File buildDir) throws IOException {
		super(project, buildDir);
		this.model = new DynamicBuildModel(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		this.model.run();
		execute(new DynamicRunExecution());
	}

	public boolean isNewJenkins() {
		VersionNumber matrixBreakOutVersion = new VersionNumber("1.560");
		return Jenkins.getVersion().isNewerThan(matrixBreakOutVersion);
	}

	public DynamicBuildLayouter getLayouter() {
		return DynamicBuildLayouter.get(this);
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
		if (input.containsKey("build_configuration")) {
			String buildConfigYml = (String) input.get("build_configuration");
			this.buildConfiguration = BuildConfiguration.restoreFromYaml(buildConfigYml);
		}
		this.model = new DynamicBuildModel(this);
	}

	@Override
	protected Map<String, Object> getBuildAttributesForDb() {
		Map<String, Object> buildAttributes = super.getBuildAttributesForDb();
		if (isConfigurationCalculated()) {
			buildAttributes.put("build_configuration", buildConfiguration.toYaml());
		}
		buildAttributes.put("main_build", true);
		return buildAttributes;
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

	protected class DynamicRunExecution extends BuildExecution implements BuildExecutionContext {
		@Override
		public boolean performStep(BuildStep execution, BuildListener listener) throws IOException, InterruptedException {
			return perform(execution, listener);
		}

		@Override
		public void setResult(Result r) {
			DynamicBuild.this.setResult(r);
		}

		@Override
		protected Result doRun(BuildListener listener) throws Exception, hudson.model.Run.RunnerAbortedException {
			try {
				DynamicBuild.this.setBuildConfiguration(calculateBuildConfiguration(listener));
				BuildEnvironment buildEnvironment = new BuildEnvironment(DynamicBuild.this, launcher, listener);
				DotCiPluginRunner dotCiPluginRunner = new DotCiPluginRunner(DynamicBuild.this, launcher, getBuildConfiguration());
				DynamicBuildExection dynamicBuildExecution = new DynamicBuildExection(DynamicBuild.this, buildEnvironment, this, dotCiPluginRunner, getBuildType());

				Result buildRunResult = dynamicBuildExecution.doRun(listener);
				setResult(buildRunResult);
				return buildRunResult;
			} catch (InvalidDotCiYmlException invalidDotCiYmlException) {
				for (String error : invalidDotCiYmlException.getValidationErrors()) {
					listener.error(error);
				}
				return Result.FAILURE;
			}

		}

		public BuildType getBuildType() throws IOException {
			if (getBuildConfiguration().isDocker()) {
				return BuildType.DockerImage;
			}
			if (new GithubRepositoryService(getGithubRepoUrl()).hasDockerFile(getSha())) {
				return BuildType.DockerLocal;
			}
			return BuildType.BareMetal;
		}

		private BuildConfiguration calculateBuildConfiguration(BuildListener listener) throws IOException, InterruptedException, InvalidDotCiYmlException {
			return new EffectiveBuildConfigurationCalculator().calculateBuildConfiguration(getGithubRepoUrl(), getSha(), getEnvironment(listener));
		}

		@Override
		public BuildConfiguration getBuildConfiguration() {
			return DynamicBuild.this.getBuildConfiguration();
		}

	}

	@Override
	@Exported
	public Executor getExecutor() {
		return super.getExecutor() == null ? getOneOffExecutor() : super.getExecutor();
	}

	private DynamicProject getConductor() {
		return this.getParent();
	}

	public Iterable<DynamicSubProject> getRunSubProjects() {
		return getConductor().getSubProjects(model.getMainRunCombinations(getAxisList()));
	}

	public Iterable<Combination> getAxisList() {
		return DynamicBuildLayouter.calculateAxisList(this).list();
	}

	public Iterable<DynamicSubProject> getAllSubProjects() {
		return Iterables.concat(getRunSubProjects(), getPostBuildSubProjects());
	}

	public Iterable<DynamicSubProject> getPostBuildSubProjects() {
		Combination postBuildCombination = model.getPostBuildCombination(getAxisList());
		return postBuildCombination == null ? new ArrayList<DynamicSubProject>() : getConductor().getSubProjects(Arrays.asList(postBuildCombination));
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

	public BuildConfiguration getBuildConfiguration() {
		return buildConfiguration;
	}

	public void setBuildConfiguration(BuildConfiguration buildConfiguration) throws IOException {
		this.buildConfiguration = buildConfiguration;
		this.save(); // make sure it get saved into the db
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

	public boolean isConfigurationCalculated() {
		return buildConfiguration != null;
	}

	@Override
	public BuildCause getCause() {
		return model.getBuildCause();
	}

	public String getGithubRepoUrl() {
		return getProject().getGithubRepoUrl();
	}

	public void addCause(Cause manualCause) {
		this.getAction(CauseAction.class).getCauses().add(manualCause);
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

}
