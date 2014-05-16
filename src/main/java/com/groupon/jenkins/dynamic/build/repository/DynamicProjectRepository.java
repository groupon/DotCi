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

import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.kohsuke.github.GHRepository;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicSubProject;
import com.groupon.jenkins.dynamic.build.IdentifableItemGroup;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainerRepository;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import com.groupon.jenkins.github.GithubRepoProperty;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import com.groupon.jenkins.mongo.MongoRepository;
import com.groupon.jenkins.notifications.DotCiNotifier;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import static java.lang.String.format;

public class DynamicProjectRepository extends MongoRepository {

	private final OrganizationContainerRepository organizationRepository;
	private final DynamicBuildRepository dynamicBuildRepository;

	public DynamicProjectRepository() {
		this(new OrganizationContainerRepository(), new DynamicBuildRepository());
	}

	protected DynamicProjectRepository(OrganizationContainerRepository organizationRepository, DynamicBuildRepository dynamicBuildRepository) {
		super("dotci_project");
		this.organizationRepository = organizationRepository;
		this.dynamicBuildRepository = dynamicBuildRepository;
	}

	public ObjectId saveOrUpdate(DbBackedProject project) {
		String buildXml = Items.XSTREAM.toXML(project);
		BasicDBObject doc = new BasicDBObject("xml", buildXml).append("name", project.getName()).append("parent", project.getIdentifableParent().getId());
		BasicDBObject query = new BasicDBObject("name", project.getName()).append("parent", project.getIdentifableParent().getId());
		return saveOrUpdate(query, doc);
	}

	public DynamicSubProject getChild(IdentifableItemGroup<DynamicSubProject> parent, String name) {
		BasicDBObject query = new BasicDBObject("name", name).append("parent", parent.getId());
		return findOne(query, DynamicProjectRepository.<DynamicSubProject> getTransformer(parent));
	}

	public Iterable<DynamicSubProject> getChildren(DynamicProject parent) {
		BasicDBObject query = new BasicDBObject("parent", parent.getId());
		return find(query, DynamicProjectRepository.<DynamicSubProject> getTransformer(parent));
	}

	private static <T extends DbBackedProject> Function<DBObject, T> getTransformer(final ItemGroup parent) {

		return new Function<DBObject, T>() {
			@Override
			public T apply(@Nonnull DBObject input) {
				String xml = (String) input.get("xml");
				T project = (T) hudson.model.Items.XSTREAM.fromXML(xml);
				project.setId((ObjectId) input.get("_id"));
				try {
					project.onLoad(parent, (String) input.get("name"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return project;
			}
		};
	}

	public void delete(DynamicProject project) {
		for (DbBackedProject subProject : getChildren(project)) {
			BasicDBObject subProjectDeleteQuery = new BasicDBObject("name", subProject.getName()).append("parent", project.getId());
			delete(subProjectDeleteQuery);
			dynamicBuildRepository.delete(subProject);

		}
		BasicDBObject mainProjectDeleteQuery = new BasicDBObject("name", project.getName()).append("parent", project.getParent().getId());
		delete(mainProjectDeleteQuery);
		dynamicBuildRepository.delete(project);

	}

	public Iterable<DynamicProject> getJobsFor(final String url) {
		return Iterables.filter(getAllLoadedDynamicProjects(), new Predicate<DynamicProject>() {
			@Override
			public boolean apply(@Nonnull DynamicProject input) {
				return url.equals(input.getGithubRepoUrl());
			}
		});
	}

	protected List<DynamicProject> getAllLoadedDynamicProjects() {
		return Jenkins.getInstance().getAllItems(DynamicProject.class);
	}

	public DynamicProject createNewProject(GHRepository githubRepository) {
		try {
			new GithubRepositoryService(githubRepository).addHook();
			OrganizationContainer folder = this.organizationRepository.getOrCreateContainer(githubRepository.getOwner().getLogin());
			String projectName = githubRepository.getName();
			DynamicProject project = folder.createProject(DynamicProject.class, projectName);

			project.setDescription(format("<a href=\"%s\">%s</a>", githubRepository.getUrl(), githubRepository.getUrl()));
			project.setConcurrentBuild(true);
			if (StringUtils.isNotEmpty(SetupConfig.get().getLabel())) {
				project.setAssignedLabel(Jenkins.getInstance().getLabel(SetupConfig.get().getLabel()));
			}
			project.addProperty(new ParametersDefinitionProperty(new StringParameterDefinition("BRANCH", "master")));
			project.addProperty(new GithubRepoProperty(githubRepository.getUrl()));

			project.getPublishersList().add(new DotCiNotifier());
			project.save();
			folder.addItem(project);
			folder.save();
			return project;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean projectExists(GHRepository repository) throws IOException {
		OrganizationContainer folder = this.organizationRepository.getOrganizationContainer(repository.getOwner().getLogin());
		return folder != null && folder.getItem(repository.getName()) != null;
	}

	public Iterable<DynamicProject> getProjectsForOrg(final OrganizationContainer organizationContainer) {
		return find(new BasicDBObject("parent", organizationContainer.getId()), castToDynamicProject(getTransformer(organizationContainer)));
	}

	private Function<DBObject, DynamicProject> castToDynamicProject(final Function<DBObject, DbBackedProject> transformer) {
		return new Function<DBObject, DynamicProject>() {
			@Override
			public DynamicProject apply(DBObject input) {
				return (DynamicProject) transformer.apply(input);
			}
		};
	}
}
