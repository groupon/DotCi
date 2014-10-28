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

      def build = xml_processor.fromXML(doc)

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

      def project = xml_processor.fromXML(doc)
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
