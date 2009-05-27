module Resources

data Resource = root(set[Resource] projects) 
              | project(str name, set[Resource] contents)
              | folder(str name, set[Resource] contents)
              | file(str name, str extension, loc location);

public Resource java root()
@javaClass{org.meta_environment.rascal.eclipse.lib.Resources}
;

public set[str] java projects()
@javaClass{org.meta_environment.rascal.eclipse.lib.Resources}
;

public set[str] java references(str project)
@javaClass{org.meta_environment.rascal.eclipse.lib.Resources}
;

public loc java location(str project)
@javaClass{org.meta_environment.rascal.eclipse.lib.Resources}
;

public set[loc] java files(str project)
@javaClass{org.meta_environment.rascal.eclipse.lib.Resources}
;

public Resource java getProject(str project)
@javaClass{org.meta_environment.rascal.eclipse.lib.Resources}
;