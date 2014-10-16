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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.dynamic.build.CurrentBuildState;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DbBackedRunList;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.github.services.GithubCurrentUserService;
import com.groupon.jenkins.mongo.BuildInfo;
import com.groupon.jenkins.mongo.MongoRepository;
import com.groupon.jenkins.mongo.MongoRunMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.ReadPreference;
import hudson.model.Result;
import hudson.model.Run;
import hudson.util.RunList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DynamicBuildRepository extends MongoRepository {
	private static final Logger LOGGER = Logger.getLogger(DynamicBuildRepository.class.getName());

	public DynamicBuildRepository() {
		super("dotci_build");
	}

	public void save(DbBackedProject project, int buildNumber, Map<String, Object> attributes) {
		BasicDBObject doc = new BasicDBObject(attributes);

		saveOrUpdate(getQuery(project).append("number", buildNumber), doc);

	}

	public <T extends DbBackedBuild> RunList<T> getBuilds(DbBackedProject project) {
		return new DbBackedRunList(project);
	}

	public <P extends DbBackedProject<P, B>, B extends DbBackedBuild<P, B>> MongoRunMap<P, B> getBuildsAsMap(DbBackedProject project) {
		return new MongoRunMap<P, B>(project);
	}

	public <T extends DbBackedBuild> T getFirstBuild(DbBackedProject project) {
		BasicDBObject query = getQuery(project);
		return findOne(query, new BasicDBObject("number", 1), DynamicBuildRepository.<T> getTransformer(project), ReadPreference.secondary());
	}

	private BasicDBObject getQuery(DbBackedProject project) {
		return new BasicDBObject("parent", project.getId());
	}

	public <T extends DbBackedBuild> T getLastBuild(DbBackedProject project) {
		BasicDBObject query = getQuery(project);
		return findOne(query, new BasicDBObject("number", -1), DynamicBuildRepository.<T> getTransformer(project), ReadPreference.secondary());
	}

	public <T extends DbBackedBuild> T getLastFailedBuild(DbBackedProject project) {
		BasicDBObject query = getQuery(project).append("result", Result.FAILURE.toString());
		return findOne(query, new BasicDBObject("number", -1), DynamicBuildRepository.<T> getTransformer(project), ReadPreference.secondary());
	}

	public <T extends DbBackedBuild> T getLastSuccessfulBuild(DbBackedProject project) {
		BasicDBObject query = getQuery(project).append("result", Result.SUCCESS.toString());
		return findOne(query, new BasicDBObject("number", -1), DynamicBuildRepository.<T> getTransformer(project), ReadPreference.secondary());
	}

	public <T extends DbBackedBuild> T getLastSuccessfulBuild(DbBackedProject project, String branch) {
		BasicDBObject query = getQuery(project).append("result", Result.SUCCESS.toString()).append("branch", branch);
		return findOne(query, new BasicDBObject("number", -1), DynamicBuildRepository.<T> getTransformer(project), ReadPreference.secondary());
	}

	private static <T extends DbBackedBuild> Function<DBObject, T> getTransformer(final DbBackedProject project) {

		return new Function<DBObject, T>() {
			@Override
			public T apply(@Nonnull DBObject input) {
				String xml = (String) input.get("xml");
				Object build = Run.XSTREAM.fromXML(xml);
				T dynamicBuild = (T) build;
				dynamicBuild.restoreFromDb(project, input.toMap());
				return dynamicBuild;
			}
		};
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
		BasicDBObject query = getQuery(project).append("number", number);
		return findOne(query, DynamicBuildRepository.<T> getTransformer(project));
	}

	public <T extends DbBackedBuild> T getBuildBySha(DbBackedProject<?, ?> project, String sha) {
		BasicDBObject query = getQuery(project).append("sha", sha);
		return findOne(query, DynamicBuildRepository.<T> getTransformer(project));
	}

	public boolean hasBuilds(DbBackedProject<?, ?> project) {
		return getBuildCount(project) > 0;
	}

	public int getBuildCount(DbBackedProject<?, ?> project) {
		BasicDBObject query = getQuery(project);
		return size(query);
	}

	public <T extends DbBackedBuild> Iterable<T> getBuildGreaterThan(DbBackedProject project, String n, String branch) {
		BasicDBObject query = getQuery(project);
		if (branch != null) {
			query.append("branch", branch);
		}
		return getBuildsGreaterThan(project, n, query);
	}

	private <T extends DbBackedBuild> Iterable<T> getBuildsGreaterThan(DbBackedProject project, String n, BasicDBObject query) {
		int number = Integer.parseInt(n) - 1;
		query.put("number", new BasicDBObject("$gt", number));
		return find(query, new BasicDBObject("number", -1), null, DynamicBuildRepository.<T> getTransformer(project));
	}

	public <T extends DbBackedBuild> Iterable<T> getCurrentUserBuildsGreaterThan(DbBackedProject project, String n) {
		return getBuildsGreaterThan(project, n, getCurrentUserQuery(project));
	}

	public <T extends DbBackedBuild> Iterable<T> getLast(DbBackedProject project, int i, String branch) {
		BasicDBObject query = getQuery(project);
		if (branch != null) {
			query.append("branch", branch);
		}
		return find(query, new BasicDBObject("number", -1), i, DynamicBuildRepository.<T> getTransformer(project));
	}

	public <T extends DbBackedBuild> Iterable<T> getCurrentUserBuilds(DbBackedProject project, int i) {
		BasicDBObject query = getCurrentUserQuery(project);
		return find(query, new BasicDBObject("number", -1), i, DynamicBuildRepository.<T> getTransformer(project));
	}

	private BasicDBObject getCurrentUserQuery(DbBackedProject project) {
		return getQuery(project).append("pusher", GithubCurrentUserService.current().getCurrentLogin());
	}

	public void delete(DbBackedProject project) {
		new DynamicBuildRepository().delete(getQuery(project));
	}

	public void deleteBuild(DbBackedBuild build) {
		BasicDBObject query = getQuery((DbBackedProject) build.getProject());
		query.append("number", build.getNumber());
		delete(query);
	}

	public DbBackedBuild getPreviousFinishedBuildOfSameBranch(DbBackedBuild build, String branch) {
		BasicDBObject query = getQuery((DbBackedProject) build.getProject());
		query.append("branch", branch);
		query.append("state", "COMPLETED");
		query.putAll(QueryBuilder.start("number").lessThan(build.getNumber()).get());
		return findOne(query, null, new BasicDBObject("number", -1), getTransformer((DbBackedProject) build.getProject()));
	}

	public CurrentBuildState getCurrentStateByNumber(DbBackedProject project, int number) {
		BasicDBObject query = getQuery(project).append("number", number);
		BasicDBObject fields = new BasicDBObject();
		fields.put("state", 1);
		fields.put("result", 1);
		return new DynamicBuildRepository().findOne(query, fields, null, new Function<DBObject, CurrentBuildState>() {
			@Override
			public CurrentBuildState apply(@Nonnull DBObject input) {
				return new CurrentBuildState(input.get("state"), input.get("result"));
			}
		});
	}

	public Iterable<BuildInfo> getBuildsForNode(String nodeName) {
		Iterable<DbBackedBuild> buildsForNode = fetchBuildInfo(new BasicDBObject("built_on", nodeName), 50);
		return Iterables.transform(buildsForNode, new Function<DbBackedBuild, BuildInfo>() {
			@Override
			public BuildInfo apply(@Nullable DbBackedBuild build) {
				return new BuildInfo(build);
			}

		});
	}

	private Iterable<DbBackedBuild> fetchBuildInfo(BasicDBObject query, int limit) {
		final List<DynamicProject> dynamicProjects = new DynamicProjectRepository().getAllLoadedDynamicProjects();
		return find(query, new BasicDBObject("last_updated", -1), limit, new Function<DBObject, DbBackedBuild>() {
			@Override
			public DbBackedBuild apply(@Nonnull DBObject input) {
				return getTransformer(findParent(dynamicProjects, input)).apply(input);
			}
		});
	}

	protected DbBackedProject findParent(List<DynamicProject> dynamicProjects, DBObject input) {
		for (DynamicProject dynamicProject : dynamicProjects) {
			if (dynamicProject.getId().equals(input.get("parent")))
				return dynamicProject;
		}
		return null;
	}

	public Iterable<DbBackedBuild> getLastBuildsForUser(String pusher, int numberOfBuilds) {
		return fetchBuildInfo(new BasicDBObject("pusher", pusher).append("main_build", true), numberOfBuilds);
	}

	public <T extends DbBackedBuild> T getLastBuild(DbBackedProject project, String branch) {
		BasicDBObject query = getQuery(project).append("branch", branch);
		return findOne(query, new BasicDBObject("number", -1), DynamicBuildRepository.<T> getTransformer(project), ReadPreference.secondary());
	}
}
