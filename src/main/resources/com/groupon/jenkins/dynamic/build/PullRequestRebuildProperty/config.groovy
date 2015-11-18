package com.groupon.jenkins.dynamic.build;
def f=namespace(lib.FormTagLib)

f.entry(title:_("Build Pull Requests from the same Repository"), field:"buildPullRequestsFromSameRepo") {
    f.checkbox(name:"buildPullRequestsFromSameRepo", checked:instance.shouldRebuildPullRequestsFromSameRepo() )
}
