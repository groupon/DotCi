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
import com.groupon.jenkins.mongo.BuildInfo;
import com.groupon.jenkins.mongo.MongoRepository;
import com.groupon.jenkins.mongo.MongoRunMap;
import com.groupon.jenkins.util.GReflectionUtils;
import hudson.model.Result;
import hudson.model.Run;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DynamicBuildRepository extends MongoRepository {

    @Inject
    public DynamicBuildRepository(final Datastore datastore) {
        super(datastore);
    }

    public void save(final DbBackedBuild build) {
        getDatastore().save(build);
    }

    public <T extends DbBackedBuild> RunList<T> getBuilds(final DbBackedProject project) {
        return new DbBackedRunList(project);
    }

    public <P extends DbBackedProject<P, B>, B extends DbBackedBuild<P, B>> MongoRunMap<P, B> getBuildsAsMap(final DbBackedProject project) {
        return new MongoRunMap<>(project);
    }

    public <T extends DbBackedBuild> T getFirstBuild(final DbBackedProject project) {
        final DbBackedBuild build = getQuery(project).disableValidation().
            limit(1).order("number").
            get();

        associateProject(project, build);

        return (T) build;
    }

    private Query<DbBackedBuild> getQuery(final DbBackedProject project) {
        return getDatastore().createQuery(DbBackedBuild.class).disableValidation().field("projectId").equal(project.getId());
    }

    public <T extends DbBackedBuild> T getLastBuild(final DbBackedProject project) {
        final DbBackedBuild build = getQuery(project).limit(1).order("-number").disableValidation().get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getLastFailedBuild(final DbBackedProject project) {
        final DbBackedBuild build = getQuery(project).limit(1).order("-number").
            field("result").equal(Result.FAILURE.toString()).
            get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getLastSuccessfulBuild(final DbBackedProject project) {
        final DbBackedBuild build = getQuery(project).order("-number").
            field("result").equal(Result.SUCCESS.toString()).
            get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getLastSuccessfulBuild(final DbBackedProject project, final String branch) {
        final DbBackedBuild build = getQuery(project).order("-number").
            field("result").equal(Result.SUCCESS.toString()).
            field("actions.causes.branch.branch").equal(branch).
            get();

        associateProject(project, build);

        return (T) build;
    }


    public <T extends DbBackedBuild> Iterable<T> latestBuilds(final DbBackedProject project, final int count) {
        return getLast(project, count, null, null);
    }

    public boolean hasBuild(final DbBackedProject project, final Integer number) {
        return getBuild(project, number) != null;
    }

    public <T extends DbBackedBuild> boolean hasBuild(final DbBackedBuild build) {
        return this.<T>getBuild((DbBackedProject) build.getProject(), build.getNumber()) != null;
    }

    public <T extends DbBackedBuild> T getBuild(final DbBackedProject<?, ?> project, final Integer number) {
        final DbBackedBuild build = getQuery(project).
            field("number").equal(number).
            get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getBuildBySha(final DbBackedProject<?, ?> project, final String sha, final Result result) {

        Query<DbBackedBuild> query = getQuery(project).
            field("actions.causes.sha").equal(sha);

        if (result != null) {
            query = query.filter("result", result.toString());
        }
        final DbBackedBuild build = query.get();

        associateProject(project, build);

        return (T) build;
    }

    public <T extends DbBackedBuild> T getBuildBySha(final DbBackedProject<?, ?> project, final String sha) {
        return getBuildBySha(project, sha, null);
    }


    public boolean hasBuilds(final DbBackedProject<?, ?> project) {
        return getBuildCount(project) > 0;
    }

    public int getBuildCount(final DbBackedProject<?, ?> project) {
        return (int) getQuery(project).countAll();
    }

    public <T extends DbBackedBuild> Iterable<T> getBuildGreaterThan(final DbBackedProject project, final int number, final String branch) {
        Query<DbBackedBuild> query = getQuery(project).order("number")
            .field("number").greaterThan(number)
            .order("-number");

        if (branch != null) {
            query = query.field("actions.causes.branch.branch").equal(branch);
        }

        final List<DbBackedBuild> builds = query.asList();

        for (final DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) query.asList();
    }

    public <T extends DbBackedBuild> Iterable<T> getCurrentUserBuildsGreaterThan(final DbBackedProject project, final int number) {
        final List<DbBackedBuild> builds = getQuery(project)
            .order("-number")
            .field("pusher").equal(Jenkins.getAuthentication().getName())
            .field("number").greaterThan(number)
            .asList();

        for (final DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) builds;
    }

    public <T extends DbBackedBuild> Iterable<T> getLast(final DbBackedProject project, final int i, final String branch, final Result result) {
        Query<DbBackedBuild> query = getQuery(project).limit(i).order("-number");

        if (branch != null) {
            query = filterExpression(branch, query);
        }
        if (result != null) {
            query = query.filter("result", result.toString());
        }

        final List<DbBackedBuild> builds = query.asList();

        for (final DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) builds;
    }

    public <T extends DbBackedBuild> Query<T> filterExpression(final String filterExpression, Query<T> query) {
        if (filterExpression.contains("=")) {
            final String[] paramExpression = filterExpression.split("=");
            final String paramName = paramExpression[0];
            final String paramValue = paramExpression[1];
            query = query.filter("actions.parameters.name", paramName);
            query = query.filter("actions.parameters.value", Pattern.compile(paramValue));
        } else {

            query = query.filter("actions.causes.branch.branch", Pattern.compile(filterExpression));
        }
        return query;
    }

    public <T extends DbBackedBuild> Iterable<T> getCurrentUserBuilds(final DbBackedProject project, final int i, final Result result) {
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
        final List<DbBackedBuild> builds = query.asList();

        for (final DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) builds;
    }

    public void delete(final DbBackedProject project) {
        getDatastore().delete(getQuery(project));
    }

    public void deleteBuild(final DbBackedBuild build) {
        getDatastore().delete(build);
    }

    public DbBackedBuild getPreviousFinishedBuildOfSameBranch(final DbBackedBuild build, final String branch) {
        final DbBackedProject project = (DbBackedProject) build.getProject();

        final Query<DbBackedBuild> query = getQuery(project);
        if (branch != null) filterExpression(branch, query);
        final DbBackedBuild previousBuild = query.
            limit(1).
            order("-number").
            field("state").equal("COMPLETED").field("number").lessThan(build.getNumber()).
            get();

        associateProject(project, previousBuild);

        return previousBuild;
    }

    public CurrentBuildState getCurrentStateByNumber(final DbBackedProject project, final int number) {
        final DbBackedBuild build = getQuery(project).field("number").equal(number).get();
        if (build == null) {
            return null;
        } else {
            return new CurrentBuildState(build.getState(), build.getResult());
        }
    }

    public Iterable<DynamicBuild> getLastBuildsForUser(final String user, final int numberOfBuilds) {

        final Query<DynamicBuild> query = getDynamicBuildsForUser(user, numberOfBuilds);

        final List<DynamicBuild> builds = query.asList();

        final DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();
        for (final DbBackedBuild build : builds) {
            final DbBackedProject project = repo.getProjectById(build.getProjectId());
            associateProject(project, build);
        }

        return builds;
    }

    private Query<DynamicBuild> getDynamicBuildsForUser(final String user, final int numberOfBuilds) {
        final Query<DynamicBuild> query = getDatastore().createQuery(DynamicBuild.class)
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


    public <T extends DbBackedBuild> T getLastBuild(final DbBackedProject project, final String branch) {
        final Query<DbBackedBuild> query = getQuery(project);
        filterExpression(branch, query);
        final DbBackedBuild build = query
            .order("-number").get();
        associateProject(project, build);
        return (T) build;
    }

    public List<BuildInfo> getBuildHistory(final String nodeName) {
        final List<DbBackedBuild> builds = getDatastore().createQuery(DbBackedBuild.class)
            .field("builtOn").equal(nodeName)
            .asList();

        final List<BuildInfo> buildInfos = new ArrayList<>();
        for (final DbBackedBuild build : builds) {
            buildInfos.add(new BuildInfo(build));
        }

        return buildInfos;
    }

    private void associateProject(final DbBackedProject project, final DbBackedBuild build) {
        if (build != null) {
            GReflectionUtils.setField(Run.class, "project", build, project);
            build.postMorphiaLoad();
        }
    }

    public <T extends DbBackedBuild> Iterable<T> getBuilds(final DbBackedProject project, final int offset) {
        final Query<DbBackedBuild> query = getQuery(project).order("-number")
            .offset(offset);


        final List<DbBackedBuild> builds = query.asList();

        for (final DbBackedBuild build : builds) {
            associateProject(project, build);
        }

        return (Iterable<T>) query.asList();
    }

    public <B extends DbBackedBuild<P, B>, P extends DbBackedProject<P, B>> B getNextBuild(final DbBackedProject<P, B> project, final int number) {
        final DbBackedBuild build = getQuery(project).
            field("number").greaterThan(number).order("number").
            get();
        if (build != null) {
            associateProject(project, build);
        }
        return (B) build;
    }
}

