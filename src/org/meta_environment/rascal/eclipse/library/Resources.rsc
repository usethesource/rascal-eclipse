module Resources

data Resource = root(set[Resource] projects) 
              | project(loc id, set[Resource] contents)
              | folder(loc id, set[Resource] contents)
              | file(loc id);

@javaClass{org.meta_environment.rascal.eclipse.library.Resources}
public Resource java root();

@javaClass{org.meta_environment.rascal.eclipse.library.Resources}
public set[loc] java projects();

@javaClass{org.meta_environment.rascal.eclipse.library.Resources}
public set[loc] java references(loc project);

public set[loc] dependencies(loc project) {
  set[loc] closure = references(project);
  
  solve (closure) {
    closure += { dep | loc project <- closure, loc dep <- references(project) };
  }
  
  return closure;
}

@javaClass{org.meta_environment.rascal.eclipse.library.Resources}
public void java closeProject(loc project);

@javaClass{org.meta_environment.rascal.eclipse.library.Resources}
public void java openProject(loc project);

@javaClass{org.meta_environment.rascal.eclipse.library.Resources}
public loc java location(loc project);

@javaClass{org.meta_environment.rascal.eclipse.library.Resources}
public set[loc] java files(loc project);

@javaClass{org.meta_environment.rascal.eclipse.library.Resources}
public Resource java getProject(loc project);