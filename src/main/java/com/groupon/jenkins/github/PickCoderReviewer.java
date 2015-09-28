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
        Map<GHUser,Integer> count = new HashMap<GHUser, Integer>();
        try {
            List<GHPullRequestFileDetail> files = repo.getPullRequest(1).listFiles().asList();
            for(GHPullRequestFileDetail file: files){
//                String fileName = file.
                GHCommitQueryBuilder commits = repo.queryCommits().path(file.getFilename());
                List<GHCommit> commitList = commits.list().asList();
                for(GHCommit commit : commitList){
                    GHUser commiter = commit.getCommitter();
                    if(count.containsKey(commiter)){
                        count.put(commiter, count.get(commiter)+1);
                    }else{
                        count.put(commiter,1);
                    }
                }
            }
            GHUser topCommiter = getTopCommiter(count);
            listener.getLogger().println("Assigning issue to " + topCommiter.getLogin());
            repo.getIssue(1).assignTo(topCommiter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private GHUser getTopCommiter(Map<GHUser, Integer> counts) {
        TreeMap<Integer,List<GHUser>> sorted =  new TreeMap<Integer, List<GHUser>>();
        for(GHUser commiter: counts.keySet()){
            Integer count = counts.get(commiter);
            if(sorted.containsKey(count)){
               sorted.get(count) .add(commiter);
            }else{
                ArrayList<GHUser> values = new ArrayList<GHUser>();
                values.add(commiter);
               sorted.put(count,values);
            }
        }
        return sorted.firstEntry().getValue().get(0);
    }
}
