package org.rascalmpl.eclipse.library.viz;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.library.experiments.VL.FigurePApplet;

public class VL {
	
	public VL(IValueFactory values){
		super();
	}
	
	public void render(IConstructor velem, IEvaluatorContext ctx){
		FigurePApplet vlp = new FigurePApplet(velem, ctx);
		VLViewer.open(vlp);
	}
}
