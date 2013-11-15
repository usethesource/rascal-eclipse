@doc{
Synopsis: create M3 models for Eclipse projects

Description: this is the preferred API for generate M3 models, since it reuses the Eclipse meta-data for projects to set up classpaths etc. before calling the Java compiler.
}
module lang::java::jdt::m3::Core

extend lang::java::m3::Core;
extend lang::java::jdt::Project;
import lang::java::jdt::m3::AST;
import analysis::m3::Registry;
import IO;
import Set;
import List;
import Node;

@doc{
Synopsis: Extract a full m3 model from an Eclipse project

Examples:

<screen>
import lang::java::jdt::m3::Core;
myModel = createM3FromEclipseProject(|project://HelloWorld|);
</screen>
}
public M3 createM3FromEclipseProject(loc project) {
  setEnvironmentOptions(classPathForProject(project), sourceRootsForProject(project));
  compliance = getProjectOptions(project)["org.eclipse.jdt.core.compiler.compliance"];
  result = composeJavaM3(project, { createM3FromFile(f, javaVersion=compliance) | loc f <- sourceFilesForProject(project)});
  registerProject(project, result);
  return result;
}

@doc{
Synopsis: Create an AST for the given logical method location
}
public Declaration getMethodASTEclipse(loc methodLoc, M3 model = m3(|unknown:///|)) {
  if (isEmpty(model)) {
    model = getModelContaining(methodLoc);
  }
  if (model.id notin methodASTs) {
    methodASTs[model.id] = ( d@decl : d |/Declaration d := createAstsFromEclipseProject(model.id, true), d is method || d is constructor);
  }
  try return methodASTs[model.id][methodLoc];
  catch: throw "Method <methodLoc> not found in any model";
}

@doc{
Synopsis: Extract an M3 model for a file that is located in an eclipse project
}
public M3 createM3FromEclipseFile(loc file) {
   project = file[path=""];
   setEnvironmentOptions(classPathForProject(file[path=""]), sourceRootsForProject(project));
   compliance = getProjectOptions(project)["org.eclipse.jdt.core.compiler.compliance"];
   return createM3FromFile(file, javaVersion=compliance);
}

@doc{Experimental functionality to create M3 models from jar files}
public set[M3] createM3FromProjectJars(loc project) {
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
