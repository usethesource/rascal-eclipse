module lang::java::jdt::m3::Core

extend lang::java::m3::Core;
extend lang::java::jdt::Project;
import IO;

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

