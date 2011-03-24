module vis::Render

import vis::Figure;

@doc{Render a figure in a tab named "Figure"}
@reflect{Needs calling context when calling argument function}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public void java render(Figure fig);

@doc{Render a figure in an explicitly named tab}
@reflect{Needs calling context when calling argument function}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public void java render(str name, Figure fig);

@doc{Render a figure and write it to file}
@reflect{Needs calling context when calling argument function}
@javaClass{org.rascalmpl.library.vis.FigureLibrary}
public void java renderSave(Figure fig, loc file);

@doc{Set custom colors for errors}
@javaClass{org.rascalmpl.library.vis.FigureColorUtils}
public void java setErrorColors(list[Color] colors);

@doc{Set custom colors for line highlights}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public void java setHighlightColors(list[Color] colors);

@doc{Open a source editor}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public void java edit(loc file, list[LineDecoration] lineInfo);

