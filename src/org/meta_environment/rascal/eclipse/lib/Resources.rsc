module Resources

data Resource = root(set[Resource] projects) 
              | project(loc id, set[Resource] contents)
              | folder(loc id, set[Resource] contents)
              | file(loc id);

@javaClass{org.meta_environment.rascal.eclipse.lib.Resources}
public Resource java root();

@javaClass{org.meta_environment.rascal.eclipse.lib.Resources}
public set[loc] java projects();

@javaClass{org.meta_environment.rascal.eclipse.lib.Resources}
public set[loc] java references(loc project);

@javaClass{org.meta_environment.rascal.eclipse.lib.Resources}
public loc java location(loc project);

@javaClass{org.meta_environment.rascal.eclipse.lib.Resources}
public set[loc] java files(loc project);

@javaClass{org.meta_environment.rascal.eclipse.lib.Resources}
public Resource java getProject(loc project);