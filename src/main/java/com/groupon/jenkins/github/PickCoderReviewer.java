package com.groupon.jenkins.github;

import com.google.common.collect.*;
import com.groupon.jenkins.dynamic.build.*;
import com.groupon.jenkins.github.services.*;
import com.groupon.jenkins.notifications.*;
import hudson.*;
import hudson.model.*;
import org.kohsuke.github.*;

import java.io.*;
import java.util.*;

@Extension
public class PickCoderReviewer extends PostBuildNotifier {
    public PickCoderReviewer() {
        super("pick_code_reviewer");
    }

    @Override
    protected Type getType() {
        return Type.ALL;
    }

    @Override
    protected boolean notify(DynamicBuild build, BuildListener listener) {
        listener.getLogger().println("Picking code Reviewer");
        String repoUrl = build.getParent().getGithubRepoUrl();
        GHRepository repo = new GithubRepositoryService(repoUrl).getGithubRepository();
        Map<String,Integer> count = new HashMap<String, Integer>();
        try {
            repo.getIssue(1).comment("meow");
            List<GHPullRequestFileDetail> files = repo.getPullRequest(1).listFiles().asList();
            for(GHPullRequestFileDetail file: files){
//                String fileName = file.
                GHCommitQueryBuilder commits = repo.queryCommits().path(file.getFilename());
                List<GHCommit> commitList = commits.list().asList();
                for(GHCommit commit : commitList){
                    String commiter = commit.getCommitter().getLogin();
                    if(count.containsKey(commiter)){
                        count.put(commiter, count.get(commiter)+1);
                    }else{
                        count.put(commiter,1);
                    }
                }
            }
            ArrayList values = new ArrayList(count.values());
          Collections.sort(values);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
