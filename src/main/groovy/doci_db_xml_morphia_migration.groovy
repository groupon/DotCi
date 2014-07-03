import com.groupon.jenkins.dynamic.build.repository.*;
import com.mongodb.BasicDBObject;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import hudson.util.XStream2;
import groovy.xml.*;

def migrate_build(project, db_build_obj) {
  try{
    def xml_processor = new XStream2() 
    xml_processor.ignoreUnknownElements()

    def root = new XmlSlurper().parseText(db_build_obj.get("xml"))
    root.dynamicBuildLayouter.replaceNode{}
    root.actions."com.groupon.jenkins.dynamic.build.execution.SubBuildExecutionAction".replaceNode{}
    root.actions."org.jenkinsci.plugins.envinject.EnvInjectPluginAction".replaceNode{}
    root.actions."jenkins.plugins.show__build__parameters.ShowParametersBuildAction".replaceNode{}
    def doc = XmlUtil.serialize(root)

    def build = xml_processor.fromXML(doc)

    build.restoreFromDb(project, db_build_obj.toMap())
    return build
  } catch(e) {
    println("Unable to migrate build: ${db_build_obj.get("_id")}")
    throw(e)
  }
}

def migrate_project(parent, db_project_obj) {
  try {
    def xml_processor = new XStream2() 
    xml_processor.ignoreUnknownElements()

    def root = new XmlSlurper().parseText(db_project_obj.get("xml"))
    root.dynamicProjectRepository.replaceNode{}
    root.dynamicBuildRepository.replaceNode{}
    def doc = XmlUtil.serialize(root)

    def project = xml_processor.fromXML(doc)
    project.setId(db_project_obj.get("_id"))
    project.onLoad(parent, db_project_obj.get("name"))

    def build_coll = new DynamicBuildRepository().getDatastore().getDB().getCollection("dotci_build")
    def builds = build_coll.find(new BasicDBObject("parent", project.getId()))
    for(build_obj in builds) {
      println("Migrating Build: ${project.getName()} [Num: ${build_obj.get("number")} ID:${build_obj.get("_id")}]")
      def build = migrate_build(project, build_obj)
      if(build != null) {
        build.save()
      }
    }
    return project
  } catch(e) {
    println("Unable to migrate project: ${db_project_obj.get("_id")}")
    throw(e)
  }
}

def org_containers = Jenkins.getInstance().getItems(OrganizationContainer.class)

for(container in org_containers) {
  container.save()
  println("Migrating Container: " + container.getName())
  def job_coll = new DynamicBuildRepository().getDatastore().getDB().getCollection("dotci_project")
  def child_projects = job_coll.find(new BasicDBObject("parent", container.getName()) )
  for(db_project_obj in child_projects) {
    println("Migrating Project: ${db_project_obj.get("name")} [ID: ${db_project_obj.get("_id")}]")

    def project = migrate_project(container, db_project_obj)
    project.save()

    if(project != null) {
      def subprojects = job_coll.find(new BasicDBObject("parent", project.getId()))
      for(subproject_obj in subprojects) {
        println("Migrating SubProject: ${db_project_obj.get("name")} [ID: ${db_project_obj.get("_id")}]")
        def subproject = migrate_project(project, subproject_obj)
        if(subproject != null) {
          subproject.save()
        }
      }
    }
  }
  container.doReload()
}

