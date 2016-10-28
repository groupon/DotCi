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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.BuildNumberCounter;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicProjectBranchTabsProperty;
import com.groupon.jenkins.dynamic.build.DynamicSubProject;
import com.groupon.jenkins.dynamic.build.GithubBranchParameterDefinition;
import com.groupon.jenkins.dynamic.build.IdentifableItemGroup;
import com.groupon.jenkins.dynamic.build.JobUiProperty;
import com.groupon.jenkins.dynamic.buildtype.BuildTypeProperty;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainerRepository;
import com.groupon.jenkins.git.GitUrl;
import com.groupon.jenkins.github.GithubRepoProperty;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import com.groupon.jenkins.mongo.MongoRepository;
import com.sonyericsson.rebuild.RebuildSettings;
import hudson.model.ParametersDefinitionProperty;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.kohsuke.github.GHRepository;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

public class DynamicProjectRepository extends MongoRepository {

    private final OrganizationContainerRepository organizationRepository;
    private final DynamicBuildRepository dynamicBuildRepository;

    @Inject
    public DynamicProjectRepository(final Datastore datastore, final DynamicBuildRepository buildRepository) {
        this(datastore, new OrganizationContainerRepository(), buildRepository);
    }

    protected DynamicProjectRepository(final Datastore datastore, final OrganizationContainerRepository organizationRepository, final DynamicBuildRepository dynamicBuildRepository) {
        super(datastore);
        this.organizationRepository = organizationRepository;
        this.dynamicBuildRepository = dynamicBuildRepository;
    }

    public ObjectId saveOrUpdate(final DbBackedProject project) {
        getDatastore().save(project);
        return project.getId();
    }

    public DynamicSubProject getChild(final IdentifableItemGroup<DynamicSubProject> parent, final String name) {
        final DynamicSubProject subProject = getDatastore().createQuery(DynamicSubProject.class).
            disableValidation().
            field("name").equal(name).
            field("parentId").exists().
            field("parentId").equal(parent.getId()).
            get();

        if (subProject != null) {
            try {
                subProject.onLoad(parent, name);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }


        return subProject;
    }

    public Iterable<DynamicSubProject> getChildren(final DynamicProject parent) {
        final List<DynamicSubProject> children = getDatastore().createQuery(DynamicSubProject.class).
            disableValidation().
            field("parentId").exists().
            field("parentId").equal(parent.getId()).
            asList();

        for (final DynamicSubProject subProject : children) {
            try {
                subProject.onLoad(parent, subProject.getName());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        return children;
    }

    public void delete(final DynamicProject project) {
        // TODO do this in a query not iteratively in memory
        for (final DynamicSubProject subProject : getChildren(project)) {
            this.dynamicBuildRepository.delete(subProject);
            getDatastore().delete(subProject);
        }
        this.dynamicBuildRepository.delete(project);
        getDatastore().delete(project);
        final BuildNumberCounter seq = getDatastore().createQuery(BuildNumberCounter.class).disableValidation().field("key").equal(project.getFullName()).get();
        if (seq != null) {
            getDatastore().delete(seq);
        }
    }

    public Iterable<DynamicProject> getJobsFor(final String url) {
        return Iterables.filter(Jenkins.getInstance().getAllItems(DynamicProject.class), new Predicate<DynamicProject>() {
            @Override
            public boolean apply(final DynamicProject input) {
                final GitUrl gitUrl = new GitUrl(url);
                final String[] orgRepo = gitUrl.getFullRepoName().split("/");
                return input.getParent().getName().equalsIgnoreCase(orgRepo[0]) && input.getName().equals(orgRepo[1]);
            }
        });
    }

    protected List<DynamicProject> getAllLoadedDynamicProjects() {
        return Jenkins.getInstance().getAllItems(DynamicProject.class);
    }

    public DynamicProject createNewProject(final GHRepository githubRepository, final String accessToken, final String user) {
        try {
            new GithubRepositoryService(githubRepository).linkProjectToCi(accessToken, user);

            final OrganizationContainer folder = this.organizationRepository.getOrCreateContainer(githubRepository.getOwner().getLogin());
            final String projectName = githubRepository.getName();
            final DynamicProject project = folder.createProject(DynamicProject.class, projectName);

            project.setDescription(format("<a href=\"%s\">%s</a>", githubRepository.getUrl(), githubRepository.getUrl()));
            project.setConcurrentBuild(true);
            if (StringUtils.isNotEmpty(SetupConfig.get().getLabel())) {
                project.setAssignedLabel(Jenkins.getInstance().getLabel(SetupConfig.get().getLabel()));
            }
            project.addProperty(new ParametersDefinitionProperty(new GithubBranchParameterDefinition("BRANCH", "master", githubRepository.getHtmlUrl().toString())));
            project.addProperty(new GithubRepoProperty(githubRepository.getHtmlUrl().toExternalForm()));
            project.addProperty(new BuildTypeProperty(SetupConfig.get().getDefaultBuildType()));
            project.addProperty(new DynamicProjectBranchTabsProperty("master"));
            project.addProperty(new RebuildSettings(true, false));
            project.addProperty(new JobUiProperty(SetupConfig.get().isDefaultToNewUi()));
            project.save();
            folder.addItem(project);
            folder.save();
            return project;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean projectExists(final GHRepository repository) throws IOException {
        final OrganizationContainer folder = this.organizationRepository.getOrganizationContainer(repository.getOwner().getLogin());
        return folder != null && folder.getItem(repository.getName()) != null;
    }

    public Iterable<DynamicProject> getProjectsForOrg(final OrganizationContainer organizationContainer) {
        return getDatastore().createQuery(DynamicProject.class).disableValidation()
            .field("containerName").equal(organizationContainer.getName())
            .asList();
    }

    public DynamicProject getProjectById(final ObjectId id) {
        return getDatastore()
            .createQuery(DynamicProject.class)
            .field("id").equal(id)
            .get();
    }


    public int assignNextBuildNumber(final DynamicProject project) {
        final Datastore datastore = getDatastore();
        BuildNumberCounter seq = datastore.findAndModify(
            datastore.find(BuildNumberCounter.class, "key = ", project.getFullName()), // query
            datastore.createUpdateOperations(BuildNumberCounter.class).inc("counter") // update
        );
        if (seq == null) {
            seq = new BuildNumberCounter(project.getFullName(), 1);
            datastore.save(seq);
        }

        return seq.getCounter();
    }

    public int getNextBuildNumber(final DynamicProject project) {
        final BuildNumberCounter seq = getDatastore().createQuery(BuildNumberCounter.class).field("key").equal(project.getFullName()).get();
        if (seq == null) {
            return 1;
        }

        return seq.getCounter() + 1;
    }
}
