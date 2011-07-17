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
module vis::Render

import vis::Figure;


public void render(Figure fig){
	render("Rascal Figure",fig);
}

public void render(str name,Figure fig){
	renderActual(name,normalize(fig));
}

public void renderSave(Figure fig,loc file){
	renderSaveActual(normalize(fig),file);
}


@doc{Render a figure in an explicitly named tab}
@reflect{Needs calling context when calling argument function}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public java void renderActual(str name, Figure fig);

@doc{Render a figure and write it to file}
@reflect{Needs calling context when calling argument function}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public java void renderSaveActual(Figure fig, loc file);


@doc{Set custom colors for errors}
@javaClass{org.rascalmpl.library.vis.FigureColorUtils}
public java void setErrorColors(list[Color] colors);

@doc{Set custom colors for line highlights}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public java void setHighlightColors(list[Color] colors);

@doc{Open a source editor (using annotations from location)}
public void edit(loc file){
	edit(file,info,"Here");
}

@doc{Open a source editor (using annotations from location)}
public void edit(loc file,str msg){
	edit(file,info,msg);
}

@doc{Open a source editor (using annotations from location)}
public void edit(loc file,LineDecoration (int,str) decorator,str msg){
	edit(file,[decorator(i,msg) | i <- [file.begin[0]..file.end[0]]]);
}
	
@doc{Open a source editor}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public java void edit(loc file, list[LineDecoration] lineInfo);

alias ComputedLineDecorations = list[LineDecoration] ();
@doc{Open a source editor, but with computed line dectorations}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public java void edit(loc file,  ComputedLineDecorations lineInfo);

@doc{Provide a closure to add line decorations for file not opened using the edit method, but of a certain extensions (such as .java)}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public java void provideDefaultLineDecorations(str extension,  ComputedLineDecorations (loc newFile) handleNewFile);
