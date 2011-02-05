package org.rascalmpl.eclipse.library.vis;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.library.vis.FigurePApplet;

public class FigureLibrary {
	
	public FigureLibrary(IValueFactory values){
		super();
	}
	
	public void render(IConstructor fig, IEvaluatorContext ctx){
		FigurePApplet vlp = new FigurePApplet(fig, ctx);
		FigureViewer.open(vlp);
	}
	
	public void render(IString name, IConstructor fig, IEvaluatorContext ctx){
		FigurePApplet vlp = new FigurePApplet(name, fig, ctx);
		FigureViewer.open(vlp);
	}
}
