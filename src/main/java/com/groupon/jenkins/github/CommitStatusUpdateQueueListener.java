package com.groupon.jenkins.github;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;
import jenkins.model.Jenkins;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class CommitStatusUpdateQueueListener extends QueueListener {
    private static final Logger LOGGER = Logger.getLogger(CommitStatusUpdateQueueListener.class.getName());

    @Override
    public void onEnterWaiting(final Queue.WaitingItem wi) {
        final List<Cause> causes = wi.getCauses();
        if (causes != null) {
            for (final Cause cause : causes) {
                if (cause instanceof BuildCause && wi.task instanceof DynamicProject) {
                    setCommitStatus((BuildCause) cause, (DynamicProject) wi.task);
                    return;
                }
            }
        }
    }

    private void setCommitStatus(final BuildCause cause, final DynamicProject project) {
        final BuildCause buildCause = cause;
        final String sha = buildCause.getSha();
        if (sha != null) {
            final GHRepository githubRepository = getGithubRepository(project);
            final String context = buildCause.getPullRequestNumber() != null ? "DotCi/PR" : "DotCi/push";
            try {
                githubRepository.createCommitStatus(sha, GHCommitState.PENDING, getJenkinsRootUrl(), "Build in queue.", context);
            } catch (final IOException e) {
                LOGGER.log(Level.WARNING, "Failed to Update commit status", e);
            }
        }
    }

    protected String getJenkinsRootUrl() {
        return Jenkins.getInstance().getRootUrl();
    }

    protected GHRepository getGithubRepository(final DynamicProject project) {
        return new GithubRepositoryService(project.getGithubRepoUrl()).getGithubRepository();
    }

}
