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


/**
 * Copy and paste this into the Jenkins script console (Manage Jekins -> Script Console) after updating to DotCi 2.2 from any earlier version.
 * You may need to restart Jenkins again to reload root containers
 */
import com.groupon.jenkins.dynamic.build.repository.*;
import com.groupon.jenkins.mongo.MongoRepository;
import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import hudson.util.XStream2;
import com.groupon.jenkins.SetupConfig;
import java.util.logging.Level;
import java.util.logging.Logger;
import groovy.xml.*;
import com.groupon.jenkins.dynamic.build.*;
import hudson.model.Run;
import hudson.model.Items

class Migrator {
  def LOGGER = Logger.getLogger(MongoRepository.class.getName())
  def xml_processor = new XStream2()

  def migrate_build(project, db_build_obj) {
    try{
      xml_processor.ignoreUnknownElements()

      def root = new XmlSlurper().parseText(db_build_obj.get("xml"))
      root.dynamicBuildLayouter.replaceNode{}
      root.actions."org.jenkinsci.plugins.envinject.EnvInjectPluginAction".replaceNode{}
      root.actions."htmlpublisher.HtmlPublisherTarget_-HTMLBuildAction".replaceNode{ node ->
        "htmlpublisher.HTMLBuildAction" (plugin: node.@plugin) {
          actualHtmlPublisherTarget(node.actualHtmlPublisherTarget.'*')
        }
      }
      def doc = XmlUtil.serialize(root)

      def build = Run.XSTREAM.fromXML(doc)

      build.restoreFromDb(project, db_build_obj.toMap())
      return build
    } catch(e) {
      LOGGER.log(Level.SEVERE, "Unable to migrate build: ${db_build_obj.get("_id")}", e)
      throw(e)
    }
  }

  def migrate_project(parent, db_project_obj) {
    try {
      xml_processor.ignoreUnknownElements()

      def root = new XmlSlurper().parseText(db_project_obj.get("xml"))
      root.dynamicProjectRepository.replaceNode{}
      root.dynamicBuildRepository.replaceNode{}
      root.properties."EnvInjectJobProperty".replaceNode{}
      root.buildWrappers.EnvInjectBuildWrapper.replaceNode{}

      def doc = XmlUtil.serialize(root)

      def project = Items.XSTREAM.fromXML(doc)
      project.setId(db_project_obj.get("_id"))
      project.onLoad(parent, db_project_obj.get("name"))

      def build_coll = SetupConfig.get().getDynamicBuildRepository().getDatastore().getDB().getCollection("dotci_build")
      def builds = build_coll.find(new BasicDBObject("parent", project.getId()))
      for(build_obj in builds) {
        LOGGER.log(Level.INFO, "Migrating Build: ${project.getName()} [Num: ${build_obj.get("number")} ID:${build_obj.get("_id")}]")

        def build = migrate_build(project, build_obj)
        if(build != null) {
          build.save()
        }
      }
      return project
    } catch(e) {
      LOGGER.log(Level.SEVERE, "Unable to migrate project: ${db_project_obj.get("_id")}", e)
      throw(e)
    }
  }

  def migrate_container(container) {
    container.save()
    LOGGER.log(Level.INFO, "Migrating Container: " + container.getName())

    def job_coll = SetupConfig.get().getDynamicBuildRepository().getDatastore().getDB().getCollection("dotci_project")
    def child_projects = job_coll.find(new BasicDBObject("parent", container.getName()) )
    for(db_project_obj in child_projects) {
      LOGGER.log(Level.INFO,"Migrating Project: ${db_project_obj.get("name")} [ID: ${db_project_obj.get("_id")}]")

      def project = migrate_project(container, db_project_obj)

      if(project != null) {
        project.save()

        def subprojects = job_coll.find(new BasicDBObject("parent", project.getId()))
        for(subproject_obj in subprojects) {
          LOGGER.log(Level.INFO,"Migrating SubProject: ${db_project_obj.get("name")} [ID: ${db_project_obj.get("_id")}]")
          def subproject = migrate_project(project, subproject_obj)
          if(subproject != null) {
            subproject.save()
          }
        }
      }
    }
    container.doReload()
  }

  void run() {
    LOGGER.log(Level.INFO,"Starting DotCi Migration")

    def org_containers = Jenkins.getInstance().getItems(OrganizationContainer.class)
    for(container in org_containers) {
      migrate_container(container)      
    }
  }
}

def migrator = new Migrator()
migrator.run()
