module lang::java::jdt::m3::Core

extend lang::java::m3::Core;
extend lang::java::jdt::Project;
import lang::java::jdt::m3::AST;
import analysis::m3::Registry;
import IO;
import Set;
import List;
import Node;

M3 createM3FromEclipseProject(loc project) {
  setEnvironmentOptions(classPathForProject(project), sourceRootsForProject(project));
  compliance = getProjectOptions(project)["org.eclipse.jdt.core.compiler.compliance"];
  result = composeJavaM3(project, { createM3FromFile(f, javaVersion=compliance) | loc f <- sourceFilesForProject(project)});
  registerProject(project.authority, result);
  return result;
}

Declaration getMethodASTEclipse(loc methodLoc, M3 model = m3(|unknown:///|)) {
  if (isMethod(methodLoc)) {
    if (isEmpty(model)) {
      model = getModelContaining(methodLoc);
      if (isEmpty(model))
        throw "Declaration for <methodLoc> not found in any models";
    }
    loc file = getFileContaining(methodLoc, model);
    Declaration fileAST = createAstsFromEclipseFile(file, true);
    visit(fileAST) {
      case Declaration d: {
        if ("decl" in getAnnotations(d) && d@decl == methodLoc)
          return d;
      }
    }
    throw "No declaration matching <methodLoc> found";
  }
  throw "Only methods are supported at the moment";
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
