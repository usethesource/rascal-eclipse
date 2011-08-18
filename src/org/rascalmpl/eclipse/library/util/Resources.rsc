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
public java Resource root();

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public java set[loc] projects();

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public java set[loc] references(loc project);

public set[loc] dependencies(loc project) {
  set[loc] closure = references(project);
  
  solve (closure) {
    closure += { dep | loc project <- closure, loc dep <- references(project) };
  }
  
  return closure;
}

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public java void closeProject(loc project);

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public java void openProject(loc project);

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public java loc location(loc project);

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public java set[loc] files(loc project);

@javaClass{org.rascalmpl.eclipse.library.util.Resources}
public java Resource getProject(loc project);