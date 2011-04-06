@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
@contributor{Bas Basten - Bas.Basten@cwi.nl (CWI)}
@contributor{Arnold Lankamp - Arnold.Lankamp@cwi.nl}
module util::Resources

data Resource = root(set[Resource] projects) 
              | project(loc id, set[Resource] contents)
              | folder(loc id, set[Resource] contents)
              | file(loc id);

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public Resource java root();

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public set[loc] java projects();

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public set[loc] java references(loc project);

public set[loc] dependencies(loc project) {
  set[loc] closure = references(project);
  
  solve (closure) {
    closure += { dep | loc project <- closure, loc dep <- references(project) };
  }
  
  return closure;
}

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public void java closeProject(loc project);

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public void java openProject(loc project);

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public loc java location(loc project);

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public set[loc] java files(loc project);

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public Resource java getProject(loc project);