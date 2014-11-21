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
package com.groupon.jenkins.dynamic.build.repository;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.CurrentBuildState;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DbBackedRunList;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.github.services.GithubCurrentUserService;
import com.groupon.jenkins.mongo.BuildInfo;
import com.groupon.jenkins.mongo.MongoRepository;
import com.groupon.jenkins.mongo.MongoRunMap;
import com.groupon.jenkins.util.GReflectionUtils;
import hudson.model.Result;
import hudson.model.Run;
import hudson.util.RunList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;

public class DynamicBuildRepository extends MongoRepository {
    private static final Logger LOGGER = Logger.getLogger(DynamicBuildRepository.class.getName());

    @Inject
    public DynamicBuildRepository(Datastore datastore) {
        super(datastore);
    }

    public void save(DbBackedBuild build) {
        getDatastore().save(build);
    }

    public <T extends DbBackedBuild> RunList<T> getBuilds(DbBackedProject project) {
        return new DbBackedRunList(project);
    }

    public <P extends DbBackedProject<P, B>, B extends DbBackedBuild<P, B>> MongoRunMap<P, B> getBuildsAsMap(DbBackedProject project) {
        return new MongoRunMap<P, B>(project);
    }

    public <T extends DbBackedBuild> T getFirstBuild(DbBackedProject project) {
        DbBackedBuild build =  getDatastore().createQuery(DbBackedBuild.class).
                limit(1).order("number").
                get();

        associateProject(project, build);

        return (T) build;
    }

    private Query<DbBackedBuild> getQuery(DbBackedProject project) {
        return getDatastore().createQuery(DbBackedBuild.class).disableValidation().field("projectId").equal(project.getId());
    }

    public <T extends DbBackedBuild> T getLastBuild(DbBackedProject project) {
        DbBackedBuild build =  getQuery(project).limit(1).order("-number").get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getLastFailedBuild(DbBackedProject project) {
        DbBackedBuild build =  getQuery(project).limit(1).order("-number").
                field("result").equal(Result.FAILURE.toString()).
                get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getLastSuccessfulBuild(DbBackedProject project) {
        DbBackedBuild build =  getQuery(project).order("-number").
                field("result").equal(Result.SUCCESS.toString()).
                get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getLastSuccessfulBuild(DbBackedProject project, String branch) {
        DbBackedBuild build =  getQuery(project).order("-number").
                field("result").equal(Result.SUCCESS.toString()).
                field("actions.causes.branch.branch").equal(branch).
                get();

        associateProject(project, build);

        return (T) build;
    }


    public <T extends DbBackedBuild> Iterable<T> latestBuilds(DbBackedProject project, int count) {
        return getLast(project, count, null);
    }

    public boolean hasBuild(DbBackedProject project, Integer number) {
        return getBuild(project, number) != null;
    }

    public <T extends DbBackedBuild> boolean hasBuild(DbBackedBuild build) {
        return this.<T> getBuild((DbBackedProject) build.getProject(), build.getNumber()) != null;
    }

    public <T extends DbBackedBuild> T getBuild(DbBackedProject<?, ?> project, Integer number) {
        DbBackedBuild build =  getQuery(project).
                field("number").equal(number).
                get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getBuildBySha(DbBackedProject<?, ?> project, String sha) {
        DbBackedBuild build =  getQuery(project).
            field("actions.causes.sha").equal(sha).
            get();

        associateProject(project, build);

        return (T) build;
    }


    public boolean hasBuilds(DbBackedProject<?, ?> project) {
        return getBuildCount(project) > 0;
    }

    public int getBuildCount(DbBackedProject<?, ?> project) {
        return (int) getQuery(project).countAll();
    }

    public <T extends DbBackedBuild> Iterable<T> getBuildGreaterThan(DbBackedProject project, String n, String branch) {
        int number = Integer.parseInt(n) - 1;
        Query<DbBackedBuild> query = getQuery(project).order("number")
                .field("number").greaterThan(number);

        if (branch != null) {
            query = query.field("actions.causes.branch.branch").equal(branch);
        }

        List<DbBackedBuild> builds = query.asList();

        for(DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) query.asList();
    }

    public <T extends DbBackedBuild> Iterable<T> getCurrentUserBuildsGreaterThan(DbBackedProject project, String n) {
        int number = Integer.parseInt(n) - 1;

        List<DbBackedBuild> builds = getQuery(project)
                .order("-number")
                .field("pusher").equal(GithubCurrentUserService.current().getCurrentLogin())
                .field("number").greaterThan(number)
                .asList();

        for(DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) builds;
    }

    public <T extends DbBackedBuild> Iterable<T> getLast(DbBackedProject project, int i, String branch) {
        Query<DbBackedBuild> query = getQuery(project).limit(i).order("-number");

        if (branch != null) {
            query = filterBranch(branch, query);
        }

        List<DbBackedBuild> builds = query.asList();

        for(DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) builds;
    }

    private Query<DbBackedBuild> filterBranch(String branch, Query<DbBackedBuild> query) {
        Pattern branchRegex = Pattern.compile(branch);
        query = query.filter("actions.causes.branch.branch",branchRegex);
        return query;
    }

    public <T extends DbBackedBuild> Iterable<T> getCurrentUserBuilds(DbBackedProject project, int i) {
        Query<DbBackedBuild> query = getQuery(project)
            .limit(i)
            .order("-number");

        query.or(
            query.criteria("actions.causes.user").equal(GithubCurrentUserService.current().getCurrentLogin()),
            query.criteria("actions.causes.pusher").equal(GithubCurrentUserService.current().getCurrentLogin())
        );

        List<DbBackedBuild> builds = query.asList();

        for(DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) builds;
    }

    public void delete(DbBackedProject project) {
        getDatastore().delete(getQuery(project));
    }

    public void deleteBuild(DbBackedBuild build) {
        getDatastore().delete(build);
    }

    public DbBackedBuild getPreviousFinishedBuildOfSameBranch(DbBackedBuild build, String branch) {
        DbBackedProject project = (DbBackedProject) build.getProject();

        Query<DbBackedBuild> query = getQuery(project);
        filterBranch(branch,query);
        DbBackedBuild previousBuild = query.
                limit(1).
                order("-number").
                field("state").equal("COMPLETED").field("number").lessThan(build.getNumber()).
                get();

        associateProject(project, previousBuild);

        return previousBuild;
    }

    public CurrentBuildState getCurrentStateByNumber(DbBackedProject project, int number) {
        DbBackedBuild build = getQuery(project).field("number").equal(number).get();
        if(build == null) {
            return null;
        } else {
            return new CurrentBuildState(build.getState(), build.getResult());
        }
    }

    public Iterable<DynamicBuild> getLastBuildsForUser(String pusher, int numberOfBuilds) {

        Query<DynamicBuild> query = getDatastore().createQuery(DynamicBuild.class)
            .limit(numberOfBuilds)
            .disableValidation()
            .order("-timestamp")
            .field("className").equal("com.groupon.jenkins.dynamic.build.DynamicBuild");

        query.or(
            query.criteria("actions.causes.user").equal(GithubCurrentUserService.current().getCurrentLogin()),
            query.criteria("actions.causes.pusher").equal(GithubCurrentUserService.current().getCurrentLogin())
        );

        List<DynamicBuild> builds = query.asList();

        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();
        for(DbBackedBuild build : builds) {
            DbBackedProject project = repo.getProjectById(build.getProjectId());
            associateProject(project, build);
        }

        return builds;
    }

    public <T extends DbBackedBuild> T getLastBuild(DbBackedProject project, String branch) {
        Query<DbBackedBuild> query = getQuery(project);
        filterBranch(branch,query);
        DbBackedBuild build = query
                .order("-number").get();
        associateProject(project, build);
        return (T) build;
    }

    public List<BuildInfo> getBuildHistory(String nodeName) {
        List<DbBackedBuild> builds = getDatastore().createQuery(DbBackedBuild.class)
                .field("builtOn").equal(nodeName)
                .asList();

        List<BuildInfo> buildInfos = new ArrayList<BuildInfo>();
        for(DbBackedBuild build : builds) {
            buildInfos.add(new BuildInfo(build));
        }

        return buildInfos;
    }

    private void associateProject(DbBackedProject project, DbBackedBuild build) {
        if(build != null) {
            GReflectionUtils.setField(Run.class, "project", build, project);
            build.postMorphiaLoad();
        }
    }
}
