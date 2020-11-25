@doc{
Synopsis: create M3 models for Eclipse projects

Description: this is the preferred API for generate M3 models, since it reuses the Eclipse meta-data for projects to set up classpaths etc. before calling the Java compiler.
}
module lang::java::jdt::m3::Core

extend lang::java::m3::Core;
extend lang::java::jdt::Project;
import lang::java::jdt::m3::AST;
import analysis::m3::Registry;

@reflect
@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.m3.internal.EclipseJavaCompiler}
private java set[M3] createM3sFromEclipseProject(loc project, bool errorRecovery = false);

@doc{
Synopsis: Extract a full m3 model from an Eclipse project

Examples:

<screen>
import lang::java::jdt::m3::Core;
myModel = createM3FromEclipseProject(|project://example-project|);
</screen>
}
public M3 createM3FromEclipseProject(loc project, bool errorRecovery = false) {
  result = composeJavaM3(project, createM3sFromEclipseProject(project, errorRecovery = errorRecovery));
  registerProject(project, result);
  return result;
}

@doc{
Synopsis: Extract an M3 model for a file that is located in an eclipse project
}
@reflect
@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.m3.internal.EclipseJavaCompiler}
public java M3 createM3FromEclipseFile(loc file, bool errorRecovery = false);

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
