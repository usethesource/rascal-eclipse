module vis::Render

import vis::Figure;

@doc{Render a figure}
@reflect{Needs calling context when calling argument function}
@javaClass{org.rascalmpl.eclipse.library.vis.FigureLibrary}
public void java render(Figure elem);

@doc{Render a figure and write it to file}
@reflect{Needs calling context when calling argument function}
@javaClass{org.rascalmpl.library.vis.FigureLibrary}
public void java renderSave(Figure fig, loc file);