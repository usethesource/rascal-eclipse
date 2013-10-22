module lang::java::jdt::m3::Core

extend lang::java::m3::Core;
extend lang::java::jdt::Project;
import IO;
import demo::common::Crawl;
import Set;
import List;

M3 createM3FromEclipseProject(loc project) {
  setEnvironmentOptions(classPathForProject(project), sourceRootsForProject(project));
  compliance = getProjectOptions(project)["org.eclipse.jdt.core.compiler.compliance"];
  result = composeJavaM3(project, { createM3FromFile(f, javaVersion=compliance) | loc f <- sourceFilesForProject(project)});
  registerProject(project.authority, result);
  return result;
}

M3 createM3FromEclipseFile(loc file) {
   project = file[path=""];
   setEnvironmentOptions(classPathForProject(file[path=""]), sourceRootsForProject(project));
   compliance = getProjectOptions(project)["org.eclipse.jdt.core.compiler.compliance"];
   return createM3FromFile(file, javaVersion=compliance);
}

set[M3] createM3FromProjectJars(loc project) {
  set[M3] jarResults = {};
  classPaths = classPathForProject(project);
  for (class <- classPaths) {
    if (class.extension == "jar" || class.extension == "zip") {
      loc jarLoc = class;
      jarLoc.scheme = "jar";
      jarLoc.path += "!/";
      jarResults += createM3FromJar(jarLoc);
    }
  }
  return jarResults; 
}
