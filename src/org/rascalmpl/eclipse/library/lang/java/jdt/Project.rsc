@doc{
Synopsis: API to extract classpath information from Eclipse project meta-data
}
module lang::java::jdt::Project

import util::FileSystem;

@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.internal.EclipseProject}
java set[loc] sourceRootsForProject(loc project);

@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.internal.EclipseProject}
java set[loc] classPathForProject(loc project);

@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.internal.EclipseProject}
java map[str,str] getProjectOptions(loc project);

set[loc] sourceFilesForProject(loc project) = {*find(root, "java") | root <- sourceRootsForProject(project)};
