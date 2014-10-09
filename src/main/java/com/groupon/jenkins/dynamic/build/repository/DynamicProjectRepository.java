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
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicSubProject;
import com.groupon.jenkins.dynamic.build.GithubBranchParameterDefinition;
import com.groupon.jenkins.dynamic.build.IdentifableItemGroup;
import com.groupon.jenkins.dynamic.buildtype.BuildTypeProperty;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainerRepository;
import com.groupon.jenkins.github.GithubRepoProperty;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import com.groupon.jenkins.mongo.MongoRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.ParametersDefinitionProperty;
import java.io.IOException;
import java.util.List;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.kohsuke.github.GHRepository;

import javax.annotation.Nonnull;

import static java.lang.String.format;

public class DynamicProjectRepository extends MongoRepository {

    private final OrganizationContainerRepository organizationRepository;
    private final DynamicBuildRepository dynamicBuildRepository;

    public DynamicProjectRepository() {
        this(new OrganizationContainerRepository(), new DynamicBuildRepository());
    }

    protected DynamicProjectRepository(OrganizationContainerRepository organizationRepository, DynamicBuildRepository dynamicBuildRepository) {
        this.organizationRepository = organizationRepository;
        this.dynamicBuildRepository = dynamicBuildRepository;
    }

    public ObjectId saveOrUpdate(DbBackedProject project) {
        getDatastore().save(project);
        return project.getId();
    }

    public DynamicSubProject getChild(IdentifableItemGroup<DynamicSubProject> parent, String name) {
        DynamicSubProject subProject = getDatastore().createQuery(DynamicSubProject.class).
                disableValidation().
                field("name").equal(name).
                field("parentId").exists().
                field("parentId").equal(parent.getId()).
                get();

        if(subProject != null) {
            try {
                subProject.onLoad(parent, name);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }


        return subProject;
    }

    public Iterable<DynamicSubProject> getChildren(DynamicProject parent) {
        List<DynamicSubProject> children = getDatastore().createQuery(DynamicSubProject.class).
                disableValidation().
                field("parentId").exists().
                field("parentId").equal(parent.getId()).
                asList();

        for(DynamicSubProject subProject : children) {
            try {
                subProject.onLoad(parent, subProject.getName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return children;
    }

    public void delete(DynamicProject project) {
        // TODO do this in a query not iteratively in memory
        for (DynamicSubProject subProject : getChildren(project)) {
            dynamicBuildRepository.delete(subProject);
            getDatastore().delete(subProject);
        }
        dynamicBuildRepository.delete(project);
        getDatastore().delete(project);
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
            new GithubRepositoryService(githubRepository).linkProjectToCi();

            OrganizationContainer folder = this.organizationRepository.getOrCreateContainer(githubRepository.getOwner().getLogin());
            String projectName = githubRepository.getName();
            DynamicProject project = folder.createProject(DynamicProject.class, projectName);

            project.setDescription(format("<a href=\"%s\">%s</a>", githubRepository.getUrl(), githubRepository.getUrl()));
            project.setConcurrentBuild(true);
            if (StringUtils.isNotEmpty(SetupConfig.get().getLabel())) {
                project.setAssignedLabel(Jenkins.getInstance().getLabel(SetupConfig.get().getLabel()));
            }
            project.addProperty(new ParametersDefinitionProperty(new GithubBranchParameterDefinition("BRANCH", "master",githubRepository.getUrl())));
            project.addProperty(new GithubRepoProperty(githubRepository.getUrl()));
            project.addProperty(new BuildTypeProperty(SetupConfig.get().getDefaultBuildType()));

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
        return getDatastore().createQuery(DynamicProject.class).disableValidation().field("containerName").equal(organizationContainer.getName()).asList();
    }

    public DynamicProject getProjectById(ObjectId id) {
        return getDatastore()
                .createQuery(DynamicProject.class)
                .field("id").equal(id).get();
    }

}
