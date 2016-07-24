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

import com.groupon.jenkins.*;
import com.groupon.jenkins.dynamic.build.*;
import com.groupon.jenkins.mongo.*;
import com.groupon.jenkins.util.*;
import hudson.model.*;
import hudson.util.*;
import jenkins.model.*;
import org.mongodb.morphia.*;
import org.mongodb.morphia.query.*;

import javax.inject.*;
import java.util.*;
import java.util.regex.*;

public class DynamicBuildRepository extends MongoRepository {

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
        DbBackedBuild build = getDatastore().createQuery(DbBackedBuild.class).disableValidation().
                limit(1).order("number").
                get();

        associateProject(project, build);

        return (T) build;
    }

    private Query<DbBackedBuild> getQuery(DbBackedProject project) {
        return getDatastore().createQuery(DbBackedBuild.class).disableValidation().field("projectId").equal(project.getId());
    }

    public <T extends DbBackedBuild> T getLastBuild(DbBackedProject project) {
        DbBackedBuild build = getQuery(project).limit(1).order("-number").disableValidation().get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getLastFailedBuild(DbBackedProject project) {
        DbBackedBuild build = getQuery(project).limit(1).order("-number").
                field("result").equal(Result.FAILURE.toString()).
                get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getLastSuccessfulBuild(DbBackedProject project) {
        DbBackedBuild build = getQuery(project).order("-number").
                field("result").equal(Result.SUCCESS.toString()).
                get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getLastSuccessfulBuild(DbBackedProject project, String branch) {
        DbBackedBuild build = getQuery(project).order("-number").
                field("result").equal(Result.SUCCESS.toString()).
                field("actions.causes.branch.branch").equal(branch).
                get();

        associateProject(project, build);

        return (T) build;
    }


    public <T extends DbBackedBuild> Iterable<T> latestBuilds(DbBackedProject project, int count) {
        return getLast(project, count, null, null);
    }

    public boolean hasBuild(DbBackedProject project, Integer number) {
        return getBuild(project, number) != null;
    }

    public <T extends DbBackedBuild> boolean hasBuild(DbBackedBuild build) {
        return this.<T>getBuild((DbBackedProject) build.getProject(), build.getNumber()) != null;
    }

    public <T extends DbBackedBuild> T getBuild(DbBackedProject<?, ?> project, Integer number) {
        DbBackedBuild build = getQuery(project).
                field("number").equal(number).
                get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getBuildBySha(DbBackedProject<?, ?>  project, String sha, Result result) {

        Query<DbBackedBuild> query = getQuery(project).
                field("actions.causes.sha").equal(sha);

        if (result != null) {
            query = query.filter("result", result.toString());
        }
        DbBackedBuild build = query.get();

        associateProject(project, build);

        return (T) build;
    }
    public <T extends DbBackedBuild> T getBuildBySha(DbBackedProject<?, ?> project, String sha) {
        return getBuildBySha(project,sha,null);
    }


    public boolean hasBuilds(DbBackedProject<?, ?> project) {
        return getBuildCount(project) > 0;
    }

    public int getBuildCount(DbBackedProject<?, ?> project) {
        return (int) getQuery(project).countAll();
    }

    public <T extends DbBackedBuild> Iterable<T> getBuildGreaterThan(DbBackedProject project, int number, String branch) {
        Query<DbBackedBuild> query = getQuery(project).order("number")
                .field("number").greaterThan(number)
                .order("-number");

        if (branch != null) {
            query = query.field("actions.causes.branch.branch").equal(branch);
        }

        List<DbBackedBuild> builds = query.asList();

        for (DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) query.asList();
    }

    public <T extends DbBackedBuild> Iterable<T> getCurrentUserBuildsGreaterThan(DbBackedProject project, int number) {
        List<DbBackedBuild> builds = getQuery(project)
                .order("-number")
                .field("pusher").equal(Jenkins.getAuthentication().getName())
                .field("number").greaterThan(number)
                .asList();

        for (DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) builds;
    }

    public <T extends DbBackedBuild> Iterable<T> getLast(DbBackedProject project, int i, String branch, Result result) {
        Query<DbBackedBuild> query = getQuery(project).limit(i).order("-number");

        if (branch != null) {
            query = filterExpression(branch, query);
        }
        if (result != null) {
            query = query.filter("result", result.toString());
        }

        List<DbBackedBuild> builds = query.asList();

        for (DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) builds;
    }

    public <T extends DbBackedBuild> Query<T> filterExpression(String filterExpression, Query<T> query) {
        if (filterExpression.contains("=")) {
            String[] paramExpression = filterExpression.split("=");
            String paramName = paramExpression[0];
            String paramValue = paramExpression[1];
            query = query.filter("actions.parameters.name", paramName);
            query = query.filter("actions.parameters.value", Pattern.compile(paramValue));
        } else {

            query = query.filter("actions.causes.branch.branch", Pattern.compile(filterExpression));
        }
        return query;
    }

    public <T extends DbBackedBuild> Iterable<T> getCurrentUserBuilds(DbBackedProject project, int i, Result result) {
        Query<DbBackedBuild> query = getQuery(project)
                .limit(i)
                .order("-number");

        query.or(
                query.criteria("actions.causes.user").equal(Jenkins.getAuthentication().getName()),
                query.criteria("actions.causes.pusher").equal(Jenkins.getAuthentication().getName())
        );

        if (result != null) {
            query = query.filter("result", result.toString());
        }
        List<DbBackedBuild> builds = query.asList();

        for (DbBackedBuild build : builds) {
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
        if (branch != null) filterExpression(branch, query);
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
        if (build == null) {
            return null;
        } else {
            return new CurrentBuildState(build.getState(), build.getResult());
        }
    }

    public Iterable<DynamicBuild> getLastBuildsForUser(String user, int numberOfBuilds) {

        Query<DynamicBuild> query = getDynamicBuildsForUser(user, numberOfBuilds);

        List<DynamicBuild> builds = query.asList();

        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();
        for (DbBackedBuild build : builds) {
            DbBackedProject project = repo.getProjectById(build.getProjectId());
            associateProject(project, build);
        }

        return builds;
    }

    private Query<DynamicBuild> getDynamicBuildsForUser(String user, int numberOfBuilds) {
        Query<DynamicBuild> query = getDatastore().createQuery(DynamicBuild.class)
                .limit(numberOfBuilds)
                .disableValidation()
                .order("-timestamp")
                .field("className").equal("com.groupon.jenkins.dynamic.build.DynamicBuild");

        query.or(
                query.criteria("actions.causes.user").equal(user),
                query.criteria("actions.causes.pusher").equal(user)
        );
        return query;
    }


    public <T extends DbBackedBuild> T getLastBuild(DbBackedProject project, String branch) {
        Query<DbBackedBuild> query = getQuery(project);
        filterExpression(branch, query);
        DbBackedBuild build = query
                .order("-$natural").get();
        associateProject(project, build);
        return (T) build;
    }

    public List<BuildInfo> getBuildHistory(String nodeName) {
        List<DbBackedBuild> builds = getDatastore().createQuery(DbBackedBuild.class)
                .field("builtOn").equal(nodeName)
                .asList();

        List<BuildInfo> buildInfos = new ArrayList<BuildInfo>();
        for (DbBackedBuild build : builds) {
            buildInfos.add(new BuildInfo(build));
        }

        return buildInfos;
    }

    private void associateProject(DbBackedProject project, DbBackedBuild build) {
        if (build != null) {
            GReflectionUtils.setField(Run.class, "project", build, project);
            build.postMorphiaLoad();
        }
    }

    public  <T extends DbBackedBuild> Iterable<T>  getBuilds(DbBackedProject project, int offset) {
        Query<DbBackedBuild> query = getQuery(project).order("-number")
            .offset(offset);


        List<DbBackedBuild> builds = query.asList();

        for (DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) query.asList();
    }
}

