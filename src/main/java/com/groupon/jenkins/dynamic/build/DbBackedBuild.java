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

import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import com.groupon.jenkins.util.GReflectionUtils;
import com.mongodb.DBObject;
import hudson.EnvVars;
import hudson.Util;
import hudson.model.*;
import hudson.model.Queue.Executable;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import jenkins.model.Jenkins;

import org.bson.types.ObjectId;
import org.kohsuke.stapler.export.Exported;
import org.mongodb.morphia.annotations.*;
import org.springframework.util.ReflectionUtils;

import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.github.GitBranch;

@Entity("run")
public abstract class DbBackedBuild<P extends DbBackedProject<P, B>, B extends DbBackedBuild<P, B>> extends Build<P, B> {
    @Id
    private ObjectId id;

    private ObjectId projectId; //TODO replace with Reference

    public ObjectId getProjectId() {
        return projectId;
    }

    @PrePersist
    void saveState(final DBObject dbObj) {
        dbObj.put("state", getState().toString());
        hudson.model.Items.XSTREAM.ignoreUnknownElements();
    }

    @PostLoad
    void restoreState(final DBObject dbObj) {
        Object state = getState((String) dbObj.get("state"));
        setField(state, "state");
    }

    @PrePersist
    private void saveTimestamp(final DBObject dbObj) {
        dbObj.put("timestamp", getTime());
    }

    @PostLoad
    private void restoreTimestamp(final DBObject dbObj) {
        Date time = (Date) dbObj.get("timestamp");
        if(time != null) {
            setField(time.getTime(), "timestamp");
        }
    }

    @PrePersist
    void saveProjectId() {
        projectId =  project.getId();
    }


    public void postMorphiaLoad() {
        super.onLoad();
    }


    private static final Logger LOGGER = Logger.getLogger(DbBackedBuild.class.getName());

    protected DbBackedBuild(P project) throws IOException {
        super(project);
    }

    public DbBackedBuild(P project, File buildDir) throws IOException {
        super(project, buildDir);
    }

    public DbBackedBuild(P job, Calendar timestamp) {
        super(job, timestamp);
    }

    @Override
    public synchronized void save() throws IOException {
        LOGGER.info("saving build:" + getName() + ": " + getNumber());
        new DynamicBuildRepository().save(this);
    }

    @Override
    protected void onEndBuilding() {
        super.onEndBuilding();
        try {
            this.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    // Keeping this so we can more easily migrate from existing systems
    public void restoreFromDb(AbstractProject project, Map<String, Object> input) {
        String state = ((String) input.get("state"));
        Date date = ((Date) input.get("last_updated"));
        setField(project, "project");
        setField(date.getTime(), "timestamp");
        setField(getState(state), "state");
        super.onLoad();
    }

    public String getState() {
        String stateName = null;
        try {
            Field field = Run.class.getDeclaredField("state");
            field.setAccessible(true);
            stateName = ReflectionUtils.getField(field, this).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return stateName;
    }
    public Object getState(String state) {
        try {
            return Enum.valueOf((Class<Enum>) Class.forName("hudson.model.Run$State"), state);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasParticipant(User user) {
        return false;
    }

    @Override
    public B getNextBuild() {
        return getParent().getBuildByNumber(getNumber() + 1);
    }

    @Override
    public B getPreviousBuild() {
        return null;
    }

    private void setField(Object project, String fieldStr) {
        Field field;
        try {
            field = Run.class.getDeclaredField(fieldStr);
            field.setAccessible(true);
            field.set(this, project);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Exported
    public Executor getExecutor() {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            return null;
        }
        for (Computer computer : jenkins.getComputers()) {
            for (Executor executor : computer.getExecutors()) {
                if (isCurrent(executor)) {
                    return executor;
                }
            }
        }
        return null;
    }

    private boolean isCurrent(Executor executor) {
        Executable currentExecutable = executor.getCurrentExecutable();
        return currentExecutable != null && currentExecutable instanceof DbBackedBuild && this.equals(currentExecutable);
    }

    @Override
    public Executor getOneOffExecutor() {
        for (Computer c : Jenkins.getInstance().getComputers()) {
            for (Executor e : c.getOneOffExecutors()) {
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
        return new HashSet<User>();
    }

    public GitBranch getCurrentBranch() {
        return getCause().getBranch();
    }

    public String getDisplayTime() {
        return Util.getPastTimeString(System.currentTimeMillis() - getTimestamp().getTimeInMillis()) + " ago";
    }

    public abstract BuildCause getCause();

    @Override
    public EnvVars getEnvironment(TaskListener log) throws IOException, InterruptedException {
        EnvVars envVars = getJenkinsEnvVariables(log);
        envVars.putAll(getDotCiEnvVars(envVars));
        return envVars;
    }

    public Map<String, String> getDotCiEnvVars(EnvVars jenkinsEnvVars) {
        Map<String, String> envVars = new HashMap<String, String>();
        envVars.put("DOTCI_BRANCH", jenkinsEnvVars.get("BRANCH"));
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

    public EnvVars getJenkinsEnvVariables(TaskListener log) {
        try {
            return super.getEnvironment(log);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isSkipped() {
        return getAction(SkippedBuildAction.class) != null;
    }

    @Override
    public String getWhyKeepLog() {
        return null;
    }

    public Run getPreviousFinishedBuildOfSameBranch(BuildListener listener) {
        return new DynamicBuildRepository().getPreviousFinishedBuildOfSameBranch(this, getCurrentBranch().toString());
    }

    public String getPusher() {
        return getCause() == null ? null : getCause().getPusher();
    }

    public abstract String getSha();

}
