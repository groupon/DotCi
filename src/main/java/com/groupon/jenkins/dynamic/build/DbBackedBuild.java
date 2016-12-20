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

import com.google.common.base.Joiner;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.dynamic.build.execution.WorkspaceFileExporter;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.git.GitBranch;
import com.groupon.jenkins.github.DeployKeyPair;
import com.mongodb.DBObject;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.Util;
import hudson.console.PlainTextConsoleOutputStream;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BallColor;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.Queue.Executable;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.util.FlushProofOutputStream;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.springframework.util.ReflectionUtils;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@Entity("run")
public abstract class DbBackedBuild<P extends DbBackedProject<P, B>, B extends DbBackedBuild<P, B>> extends Build<P, B> {
    private static final Logger LOGGER = Logger.getLogger(DbBackedBuild.class.getName());
    @Id
    private ObjectId id = new ObjectId();
    private ObjectId projectId; //TODO replace with Reference

    protected DbBackedBuild(final P project) throws IOException {
        super(project);
    }

    public DbBackedBuild(final P project, final File buildDir) throws IOException {
        super(project, buildDir);
    }

    public DbBackedBuild(final P job, final Calendar timestamp) {
        super(job, timestamp);
    }

    public ObjectId getProjectId() {
        return this.projectId;
    }

    @PrePersist
    void saveState(final DBObject dbObj) {
        dbObj.put("state", getState().toString());
        hudson.model.Items.XSTREAM.ignoreUnknownElements();
    }

    @PostLoad
    void restoreState(final DBObject dbObj) {
        final Object state = getState((String) dbObj.get("state"));
        setField(state, "state");
    }

    @PrePersist
    private void saveTimestamp(final DBObject dbObj) {
        dbObj.put("scheduledDate", getTime());
    }

    @PostLoad
    private void restoreTimestamp(final DBObject dbObj) {
        final Date scheduledDate = (Date) dbObj.get("scheduledDate");
        this.timestamp = scheduledDate.getTime();
    }

    @PrePersist
    private void saveNumber(final DBObject dbObj) {
        dbObj.put("number", getNumber());
    }

    @PostLoad
    private void restoreNumber(final DBObject dbObj) {
        if (this.number == 0 && dbObj.get("number") != null) {
            this.number = (Integer) dbObj.get("number");
        }
    }

    @PrePersist
    void saveProjectId() {
        this.projectId = this.project.getId();
    }

    public void postMorphiaLoad() {
        super.onLoad();
    }

    @Override
    public synchronized void save() throws IOException {
        getDynamicBuildRepository().save(this);
    }

    private DynamicBuildRepository getDynamicBuildRepository() {
        return SetupConfig.get().getDynamicBuildRepository();
    }

    @Override
    protected void onEndBuilding() {
        super.onEndBuilding();
        try {
            this.save();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    // Keeping this so we can more easily migrate from existing systems
    public void restoreFromDb(final AbstractProject project, final Map<String, Object> input) {
        this.id = (ObjectId) input.get("_id");

        final String state = ((String) input.get("state"));
        setField(getState(state), "state");

        final Date date = ((Date) input.get("last_updated"));
        setField(date.getTime(), "timestamp");

        setField(project, "project");

        super.onLoad();
    }

    public String getState() {
        String stateName = null;
        try {
            final Field field = Run.class.getDeclaredField("state");
            field.setAccessible(true);
            stateName = ReflectionUtils.getField(field, this).toString();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return stateName;
    }

    public Object getState(final String state) {
        try {
            return Enum.valueOf((Class<Enum>) Class.forName("hudson.model.Run$State"), state);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasParticipant(final User user) {
        return false;
    }

    @Override
    public B getNextBuild() {
        return getParent().getNextBuild(getNumber());
    }

    @Override
    public B getPreviousBuild() {
        return null;
    }

    private void setField(final Object project, final String fieldStr) {
        final Field field;
        try {
            field = Run.class.getDeclaredField(fieldStr);
            field.setAccessible(true);
            field.set(this, project);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Exported
    public Executor getExecutor() {
        final Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            return null;
        }
        for (final Computer computer : jenkins.getComputers()) {
            for (final Executor executor : computer.getExecutors()) {
                if (isCurrent(executor)) {
                    return executor;
                }
            }
        }
        return null;
    }

    private boolean isCurrent(final Executor executor) {
        final Executable currentExecutable = executor.getCurrentExecutable();
        return currentExecutable != null && currentExecutable instanceof DbBackedBuild && this.equals(currentExecutable);
    }

    @Override
    public Executor getOneOffExecutor() {
        for (final Computer c : Jenkins.getInstance().getComputers()) {
            for (final Executor e : c.getOneOffExecutors()) {
                if (isCurrent(e))
                    return e;
            }
        }
        return null;
    }

    @Override
    @Exported
    public ChangeLogSet<? extends Entry> getChangeSet() {
        return new GithubChangeLogSet(this, getCause().getChangeLogEntries());
    }

    public String getName() {
        return getParent().getName();
    }

    @Override
    @Exported
    public Set<User> getCulprits() {
        return new HashSet<>();
    }

    public GitBranch getCurrentBranch() {
        return getCause().getBranch();
    }

    public String getDisplayTime() {
        return Util.getPastTimeString(System.currentTimeMillis() - getTimestamp().getTimeInMillis()) + " ago";
    }

    public abstract BuildCause getCause();

    @Override
    public EnvVars getEnvironment(final TaskListener log) throws IOException, InterruptedException {
        final EnvVars envVars = getJenkinsEnvVariables(log);
        envVars.putAll(getDotCiEnvVars(envVars));
        return envVars;
    }


    public Map<String, String> getDotCiEnvVars(final EnvVars jenkinsEnvVars) {
        final Map<String, String> envVars = new HashMap<>();
        final String branch = jenkinsEnvVars.get("BRANCH");
        envVars.put("DOTCI_BRANCH", branch);
        if (branch.startsWith("tags/")) {
            envVars.put("DOTCI_TAG", branch.replace("tags/", ""));
        }
        envVars.put("DOTCI", "true");
        envVars.put("CI", "true");
        if (getCause() != null) {
            envVars.putAll(getCause().getEnvVars());
        }
        return envVars;
    }

    public String getBuildId() {
        return getProject().getId() + getId();
    }

    public EnvVars getJenkinsEnvVariables(final TaskListener log) {
        try {
            return super.getEnvironment(log);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isSkipped() {
        return getAction(SkippedBuildAction.class) != null;
    }

    public boolean isPhantom() {
        final String state = getState();
        return ("NOT_STARTED".equals(state) || "BUILDING".equals(state)) && getExecutor() == null;
    }

    @Override
    public String getWhyKeepLog() {
        return null;
    }

    public Run getPreviousFinishedBuildOfSameBranch(final BuildListener listener) {
        return getDynamicBuildRepository()
            .getPreviousFinishedBuildOfSameBranch(this, getCurrentBranch().toString());
    }

    public boolean isPrivateRepo() {
        return SetupConfig.get().getGithubDeployKeyRepository().hasDeployKey(getGithubRepoUrl());
    }

    public abstract String getGithubRepoUrl();

    protected void exportDeployKeysIfPrivateRepo(final BuildListener listener, final Launcher launcher) throws IOException, InterruptedException {
        if (isPrivateRepo()) {
            final DeployKeyPair deployKeyPair = SetupConfig.get().getGithubDeployKeyRepository().get(getGithubRepoUrl());
            final WorkspaceFileExporter.WorkspaceFile privateKeyFile = new WorkspaceFileExporter.WorkspaceFile("deploykey_rsa", deployKeyPair.privateKey, "rw-------");
            final WorkspaceFileExporter.WorkspaceFile publicKeyFile = new WorkspaceFileExporter.WorkspaceFile("deploykey_rsa.pub", deployKeyPair.privateKey, "rw-r--r--");
            new WorkspaceFileExporter(publicKeyFile, WorkspaceFileExporter.Operation.CREATE).perform((AbstractBuild) this, launcher, listener);
            new WorkspaceFileExporter(privateKeyFile, WorkspaceFileExporter.Operation.CREATE).perform((AbstractBuild) this, launcher, listener);
        }

    }

    protected void deleteDeployKeys(final BuildListener listener, final Launcher launcher) throws IOException, InterruptedException {
        if (isPrivateRepo()) {
            new WorkspaceFileExporter(new WorkspaceFileExporter.WorkspaceFile("deploykey_rsa"), WorkspaceFileExporter.Operation.DELETE).perform((AbstractBuild) this, launcher, listener);
            new WorkspaceFileExporter(new WorkspaceFileExporter.WorkspaceFile("deploykey_rsa.pub"), WorkspaceFileExporter.Operation.DELETE).perform((AbstractBuild) this, launcher, listener);
        }
    }

    public String getPusher() {
        return getCause() == null ? null : getCause().getPusher();
    }

    public abstract String getSha();

    public void doLogTail(final StaplerRequest req, final StaplerResponse rsp) throws IOException, ServletException {
        rsp.setContentType("text/plain;charset=UTF-8");
        final Joiner joiner = Joiner.on("\n");
        final PlainTextConsoleOutputStream out = new PlainTextConsoleOutputStream(new FlushProofOutputStream(rsp.getCompressedOutputStream(req)));
        try {
            out.write(joiner.join(getLog(5000)).getBytes());
        } catch (final IOException e) {
            // see comment in writeLogTo() method
            final InputStream input = getLogInputStream();
            try {
                IOUtils.copy(input, out);
            } finally {
                IOUtils.closeQuietly(input);
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    @Override
    public BallColor getIconColor() {
        return !isBuilding() ? getResult().color : BallColor.YELLOW_ANIME;
    }

}
