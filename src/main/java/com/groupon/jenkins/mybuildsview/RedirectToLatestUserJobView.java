package com.groupon.jenkins.mybuildsview;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.RecentProjectsRsp;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.util.JsonResponse;
import com.groupon.jenkins.views.AuthenticatedView;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.ViewDescriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

public class RedirectToLatestUserJobView extends AuthenticatedView{
    @DataBoundConstructor
    public RedirectToLatestUserJobView(String name) {
        super(name);
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {
//noop
    }

    private DynamicProject getProject() {
        Iterable<DynamicProject> jobs = Iterables.filter(Jenkins.getInstance().getAllItems(DynamicProject.class), new Predicate<DynamicProject>() {
            @Override
            public boolean apply(DynamicProject input) {
                return input.getParent().getName().equalsIgnoreCase("suryagaddipati") && input.getName().equals("DotCi");
            }
        });
        return Iterables.getOnlyElement(jobs);
    }


    public  void redirectToLatestUserBuild() throws IOException {
        Iterable<DynamicBuild> builds = makeDynamicBuildRepository().getLastBuildsForUser(getCurrentUser(), 1);
        if(builds != null && Iterables.size(builds) == 1){
            Stapler.getCurrentResponse().sendRedirect2(Iterables.getOnlyElement(builds).getAbsoluteUrl());
        }
    }
    private String getCurrentUser() {
        return Jenkins.getAuthentication().getName();
    }
    protected DynamicBuildRepository makeDynamicBuildRepository() {
        return SetupConfig.get().getDynamicBuildRepository();
    }
    @Extension
    public static final class DescriptorImpl extends ViewDescriptor {

        @Override
        public String getDisplayName() {
            return "Latest Project";
        }
    }

}
