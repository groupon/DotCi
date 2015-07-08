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
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.mongodb.gridfs.GridFSInputFile;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.Util;
import hudson.console.*;
import hudson.model.*;
import hudson.model.Queue;
import hudson.model.Queue.Executable;
import hudson.model.listeners.RunListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.security.ACL;
import hudson.tasks.BuildWrapper;
import hudson.util.FlushProofOutputStream;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.INFO;

@Entity("run")
public abstract class DbBackedBuild<P extends DbBackedProject<P, B>, B extends DbBackedBuild<P, B>> extends Build<P, B> {
    @Id
    private ObjectId id = new ObjectId();

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
        dbObj.put("scheduledDate", getTime());
    }

    @PostLoad
    private void restoreTimestamp(final DBObject dbObj) {
        Date scheduledDate = (Date) dbObj.get("scheduledDate");
         this.timestamp = scheduledDate.getTime();
    }

    @PrePersist
    private void saveNumber(final DBObject dbObj) {
        dbObj.put("number", getNumber());
    }

    @PostLoad
    private void restoreNumber(final DBObject dbObj) {
        if(this.number == 0 && dbObj.get("number")!=null ){
            this.number = (Integer)dbObj.get("number");
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    // Keeping this so we can more easily migrate from existing systems
    public void restoreFromDb(AbstractProject project, Map<String, Object> input) {
        id = (ObjectId) input.get("_id");

        String state = ((String) input.get("state"));
        setField(getState(state), "state");

        Date date = ((Date) input.get("last_updated"));
        setField(date.getTime(), "timestamp");

        setField(project, "project");

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
        String branch = jenkinsEnvVars.get("BRANCH");
        envVars.put("DOTCI_BRANCH", branch);
        if(branch.startsWith("tags/")){
            envVars.put("DOTCI_TAG", branch.replace("tags/",""));
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
        return getDynamicBuildRepository()
                .getPreviousFinishedBuildOfSameBranch(this, getCurrentBranch().toString());
    }

    public boolean isPrivateRepo() {
        return SetupConfig.get().getGithubDeployKeyRepository().hasDeployKey(getGithubRepoUrl());
    }

    public abstract String getGithubRepoUrl();

    protected void exportDeployKeysIfPrivateRepo(BuildListener listener, Launcher launcher) throws IOException, InterruptedException {
        if(isPrivateRepo()){
            DeployKeyPair deployKeyPair = SetupConfig.get().getGithubDeployKeyRepository().get(getGithubRepoUrl());
            WorkspaceFileExporter.WorkspaceFile privateKeyFile = new WorkspaceFileExporter.WorkspaceFile("deploykey_rsa", deployKeyPair.privateKey, "rw-------");
            WorkspaceFileExporter.WorkspaceFile publicKeyFile = new WorkspaceFileExporter.WorkspaceFile("deploykey_rsa.pub", deployKeyPair.privateKey, "rw-r--r--");
            new WorkspaceFileExporter(publicKeyFile, WorkspaceFileExporter.Operation.CREATE).perform((AbstractBuild)this,launcher,listener);
            new WorkspaceFileExporter(privateKeyFile, WorkspaceFileExporter.Operation.CREATE).perform((AbstractBuild)this,launcher,listener);
        }

    }

    protected void deleteDeployKeys(BuildListener listener, Launcher launcher) throws IOException, InterruptedException {
        if(isPrivateRepo()){
            new WorkspaceFileExporter(new WorkspaceFileExporter.WorkspaceFile("deploykey_rsa"), WorkspaceFileExporter.Operation.DELETE).perform((AbstractBuild)this,launcher,listener);
            new WorkspaceFileExporter(new WorkspaceFileExporter.WorkspaceFile("deploykey_rsa.pub"), WorkspaceFileExporter.Operation.DELETE).perform((AbstractBuild) this, launcher, listener);
        }
    }

    public String getPusher() {
        return getCause() == null ? null : getCause().getPusher();
    }

    public abstract String getSha();

    public void doLogTail(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        rsp.setContentType("text/plain;charset=UTF-8");
        Joiner joiner = Joiner.on("\n");
        PlainTextConsoleOutputStream out = new PlainTextConsoleOutputStream( new FlushProofOutputStream(rsp.getCompressedOutputStream(req)));
        try{
            out.write(joiner.join(getLog(5000)).getBytes());
        } catch (IOException e) {
            // see comment in writeLogTo() method
            InputStream input = getLogInputStream();
            try {
                IOUtils.copy(input, out);
            } finally {
                IOUtils.closeQuietly(input);
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public long getEstimatedDurationForDefaultBranch() {
        return isBuilding()? getDynamicBuildRepository().getEstimatedDuration(this):-1;
    }

    @Nonnull
    @Override
    public File getLogFile() {
        throw new UnsupportedOperationException("Logs are not stored on FS in DotCi");
    }
    public List<String> getLog(int maxLines) throws IOException {
        int lineCount = 0;
        List<String> logLines = new LinkedList<String>();
        if (maxLines == 0) {
            return logLines;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(getLogInputStream(),getCharset()));
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                logLines.add(line);
                ++lineCount;
                // If we have too many lines, remove the oldest line.  This way we
                // never have to hold the full contents of a huge log file in memory.
                // Adding to and removing from the ends of a linked list are cheap
                // operations.
                if (lineCount > maxLines)
                    logLines.remove(0);
            }
        } finally {
            reader.close();
        }

        // If the log has been truncated, include that information.
        // Use set (replaces the first element) rather than add so that
        // the list doesn't grow beyond the specified maximum number of lines.
        if (lineCount > maxLines)
            logLines.set(0, "[...truncated " + (lineCount - (maxLines - 1)) + " lines...]");

        return ConsoleNote.removeNotes(logLines);
    }


    protected void executeBuild(@Nonnull RunExecution job) {
        if(result!=null)
            return;     // already built.

        StreamBuildListener listener=null;

        onStartBuilding();
        try {
            // to set the state to COMPLETE in the end, even if the thread dies abnormally.
            // otherwise the queue state becomes inconsistent

            long start = System.currentTimeMillis();

            try {
                try {
                    Computer computer = Computer.currentComputer();
                    Charset charset = null;
                    if (computer != null) {
                        charset = computer.getDefaultCharset();
                        this.charset = charset.name();
                    }

                    // don't do buffering so that what's written to the listener
                    // gets reflected to the file immediately, which can then be
                    // served to the browser immediately
                    OutputStream logger = getLogOutputStream();
                    DbBackedBuild build = job.getBuild();

                    // Global log filters
                    for (ConsoleLogFilter filter : ConsoleLogFilter.all()) {
                        logger = filter.decorateLogger((AbstractBuild) build, logger);
                    }

                    // Project specific log filters
                    if (project instanceof BuildableItemWithBuildWrappers && build instanceof AbstractBuild) {
                        BuildableItemWithBuildWrappers biwbw = (BuildableItemWithBuildWrappers) project;
                        for (BuildWrapper bw : biwbw.getBuildWrappersList()) {
                            logger = bw.decorateLogger((AbstractBuild) build, logger);
                        }
                    }

                    listener = new StreamBuildListener(logger,charset);

                    listener.started(getCauses());

                    Authentication auth = Jenkins.getAuthentication();
                    if (!auth.equals(ACL.SYSTEM)) {
                        String name = auth.getName();
                        if (!auth.equals(Jenkins.ANONYMOUS)) {
                            name = ModelHyperlinkNote.encodeTo(User.get(name));
                        }
                        listener.getLogger().println(Messages.Run_running_as_(name));
                    }

                    RunListener.fireStarted(this, listener);

                    updateSymlinks(listener);

                    setResult(job.run(listener));

                    LOGGER.log(INFO, "{0} main build action completed: {1}", new Object[] {this, result});
                    CheckPoint.MAIN_COMPLETED.report();
                } catch (ThreadDeath t) {
                    throw t;
                } catch( AbortException e ) {// orderly abortion.
                    result = Result.FAILURE;
                    listener.error(e.getMessage());
                    LOGGER.log(FINE, "Build "+this+" aborted",e);
                } catch( RunnerAbortedException e ) {// orderly abortion.
                    result = Result.FAILURE;
                    LOGGER.log(FINE, "Build "+this+" aborted",e);
                } catch( InterruptedException e) {
                    // aborted
                    result = Executor.currentExecutor().abortResult();
                    listener.getLogger().println(Messages.Run_BuildAborted());
                    Executor.currentExecutor().recordCauseOfInterruption(DbBackedBuild.this,listener);
                    LOGGER.log(Level.INFO, this + " aborted", e);
                } catch( Throwable e ) {
                    handleFatalBuildProblem(listener,e);
                    result = Result.FAILURE;
                }

                // even if the main build fails fatally, try to run post build processing
                job.post(listener);

            } catch (ThreadDeath t) {
                throw t;
            } catch( Throwable e ) {
                handleFatalBuildProblem(listener,e);
                result = Result.FAILURE;
            } finally {
                long end = System.currentTimeMillis();
                duration = Math.max(end - start, 0);  // @see HUDSON-5844

                // advance the state.
                // the significance of doing this is that Jenkins
                // will now see this build as completed.
                // things like triggering other builds requires this as pre-condition.
                // see issue #980.
                LOGGER.log(FINER, "moving into POST_PRODUCTION on {0}", this);
                Object state = getState("POST_PRODUCTION");
                setField(state, "state");

                if (listener != null) {
                    RunListener.fireCompleted(this,listener);
                    try {
                        job.cleanUp(listener);
                    } catch (Exception e) {
                        handleFatalBuildProblem(listener,e);
                        // too late to update the result now
                    }
                    listener.finished(result);
                    listener.closeQuietly();
                }

                try {
                    save();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to save build record",e);
                }
            }

            try {
                getParent().logRotate();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to rotate log",e);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Failed to rotate log",e);
            }
        } finally {
            onEndBuilding();
        }
    }

    private OutputStream getLogOutputStream() {

        BuildLog buildLog = getDynamicBuildRepository().getDatastore().createQuery(BuildLog.class).disableValidation().field("buildId").equal(id).get();
        if(buildLog == null){
            buildLog = new BuildLog(id);
            getDynamicBuildRepository().getDatastore().save(buildLog);
            buildLog = getDynamicBuildRepository().getDatastore().createQuery(BuildLog.class).disableValidation().field("buildId").equal(id).get();
        }
        return  new BuildLogOutputStream(buildLog,getDynamicBuildRepository().getDatastore());
    }
    public AnnotatedLargeText getLogText() {
        return new AnnotatedLargeText(getLogFile(),getCharset(),!isLogUpdated(),this);
    }

    @Override
    public InputStream getLogInputStream() throws IOException {
        BasicDBObject query = new BasicDBObject();
        query.append("buildId",id);
        DBObject buildLog = getDynamicBuildRepository().getDatastore().getDB().getCollection("build_log").findOne(query);
       byte[] log = (byte[]) buildLog.get("log");
        return new  ByteArrayInputStream(log);
    }
    public void handleFatalBuildProblem(StreamBuildListener listener, Throwable e){
        e.printStackTrace();
        listener.getLogger().print(e);

    }

}
