@doc{
Synopsis: provide access to m3 ASTs for Java using Eclipse project meta-data
}
module lang::java::jdt::m3::AST

extend lang::java::m3::AST;
extend lang::java::jdt::Project;

@doc{Creates ASTs from a project}
public Declaration createAstsFromEclipseFile(loc file, bool collectBindings) {
  assert file.scheme == "project";
  project = file[path=""];
  setEnvironmentOptions(classPathForProject(project), sourceRootsForProject(project));
  compliance = getProjectOptions(project)["org.eclipse.jdt.core.compiler.compliance"];
  return createAstFromFile(file, collectBindings, javaVersion = compliance);
}

@doc{Creates ASTs from a project}
public set[Declaration] createAstsFromEclipseProject(loc project, bool collectBindings) {
  assert project.scheme == "project";
  setEnvironmentOptions(classPathForProject(project), sourceRootsForProject(project));
  compliance = getProjectOptions(project)["org.eclipse.jdt.core.compiler.compliance"];
  return { createAstFromFile(f, collectBindings, javaVersion = compliance) | loc f <- sourceFilesForProject(project) };
}