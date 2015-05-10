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
package com.groupon.jenkins.dynamic.build.cause;

import com.groupon.jenkins.git.GitBranch;
import com.groupon.jenkins.github.Payload;
import com.jcraft.jsch.jce.MD5;
import hudson.model.Cause;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;


public abstract class BuildCause extends Cause {

    public static final BuildCause NULL_BUILD_CAUSE = new NullBuildCause();

    public abstract String getSha();

    public abstract String getPusher();

    public abstract String getPullRequestNumber();

    public abstract CommitInfo getCommitInfo();

    public Map<String, String> getEnvVars() {
        Map<String, String> vars = new HashMap<String, String>();
        putIfNotNull(vars, "DOTCI_SHA", getSha());
        putIfNotNull(vars, "DOTCI_PUSHER", getPusher());
        putIfNotNull(vars, "DOTCI_PULL_REQUEST", getPullRequestNumber());
        return vars;
    }

    private static void putIfNotNull(Map<String, String> vars, String key, String value) {
        if (value != null) {
            vars.put(key, value);
        }
    }

    public abstract GitBranch getBranch();

    public abstract Iterable<GithubLogEntry> getChangeLogEntries();


    @ExportedBean(defaultVisibility = 99)
    public static class CommitInfo{
        public static CommitInfo NULL_INFO =  new CommitInfo();
        private final String avatarUrl;
        private  String committerEmail;
        private String message;
        private String committerName;
        private String branch;
        private final String sha;
        private String commitUrl;
        //for backward compat
        private CommitInfo(){
            this.committerEmail = "unknown@unknown.com";
            this.message = "unknown";
            this.committerName="unknown";
            this.commitUrl="http://unknown.com";
            this.branch="unknown.com";
            this.avatarUrl = null;
            this.sha = "unknown";
        }
        public CommitInfo(GHCommit commit, GitBranch branch){
            this.sha = commit.getSHA1();
            this.message = commit.getCommitShortInfo().getMessage();
            this.committerName = commit.getCommitShortInfo().getCommitter().getName();
            this.committerEmail= commit.getCommitShortInfo().getCommitter().getEmail();
            this.commitUrl = commit.getOwner().getUrl()+"/commit/"+sha;
            this.branch = branch.toString();
            this.avatarUrl = null;
        }

        public CommitInfo( String message, String committerName, String branch) {
            this.message = message;
            this.committerName = committerName;
            this.avatarUrl = null;
            this.branch = branch;
            this.sha = branch;
        }

        public CommitInfo(Payload payload) {
            this.sha = payload.getSha();
            this.message = payload.getCommitMessage();
            this.committerName = payload.getCommitterName();
            this.committerEmail = payload.getCommitterEmail();
            this.avatarUrl = payload.getAvatarUrl();
            this.commitUrl = payload.getDiffUrl();
            this.branch = payload.getBranch();
        }

        @Exported
        public String getCommitUrl() {
            return commitUrl;
        }

        @Exported
        public String getShortSha(){
            return sha.length()<9?sha:sha.substring(0,7);
        }

        @Exported
        public String getMessage(){
            return StringUtils.abbreviate(message, 50);
        }

        @Exported
        public  String getCommitterName(){
            return committerName;
        }

        @Exported
        public String getBranch() {
            return branch;
        }
        @Exported
        public String getAvatarUrl(){
            if(avatarUrl == null){
                if(committerEmail ==null) return null;
                byte[] md5 = DigestUtils.md5(committerEmail);
                String emailDigest = new BigInteger(1, md5).toString(16);
                return "https://secure.gravatar.com/avatar/"+emailDigest+".png?";
            }
            return avatarUrl;
        }
    }

}
