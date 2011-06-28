@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
@contributor{Paul Klint - Paul.Klint@cwi.nl - CWI}
@contributor{Davy Landman - Davy.Landman@cwi.nl - CWI}
module vis::RenderSWT

import vis::FigureSWTApplet;

@doc{Render a figure in a tab named "Figure"}
@reflect{Needs calling context when calling argument function}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public void java render(list[DrawCmd] fig);

@doc{Render a figure in an explicitly named tab}
@reflect{Needs calling context when calling argument function}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public void java render(str name, list[DrawCmd] fig);