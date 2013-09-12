module lang::java::jdt::m3::Core

extend lang::java::m3::Core;
extend lang::java::jdt::Project;

M3 createM3FromEclipseProject(loc project) {
  setEnvironmentOptions(classPathForProject(project), sourceRootsForProject(project));
  compliance = getProjectOptions(project)["org.eclipse.jdt.core.compiler.compliance"];
  result = (m3(project.authority) | composeJavaM3(it, createM3FromFile(f, compliance)) | loc f <- find(project, "java"));
  registerProject(project.authority, result);
  return result;
}

M3 createM3FromEclipseFile(loc file) {
   setEnvironmentOptions(classPathForProject(file[path=""]), sourceRootsForProject(file[path=""]));
   return createM3FromFile(file);
} 

