module lang::java::jdt::Project

@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.m3.internal.EclipseProject}
java set[loc] sourceRootsForProject(loc project);

@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.m3.internal.EclipseProject}
java set[loc] classPathForProject(loc project);

@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.m3.internal.EclipseProject}
java map[str,str] getProjectOptions(loc project);

public set[loc] sourceFilesForProject(loc project) 
  = ( { } | it + find(root, "java") | root <- sourceRootsForProject(project) );
